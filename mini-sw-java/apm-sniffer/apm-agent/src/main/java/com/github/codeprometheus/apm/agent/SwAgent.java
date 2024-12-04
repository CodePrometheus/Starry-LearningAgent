/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.codeprometheus.apm.agent;

import com.github.codeprometheus.apm.agent.core.plugin.AbstractClassEnhancePluginDefine;
import com.github.codeprometheus.apm.agent.core.plugin.EnhanceContext;
import com.github.codeprometheus.apm.agent.core.plugin.PluginBootstrap;
import com.github.codeprometheus.apm.agent.core.plugin.PluginFinder;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;

@Slf4j
public class SwAgent {

    public static void premain(String args, Instrumentation inst) {
        log.info("SwAgent|premain start, args: {}, inst: {}", args, inst);

        PluginFinder pluginFinder = null;
        try {
            pluginFinder = new PluginFinder(new PluginBootstrap().loadPlugins());
        } catch (Exception e) {
            log.error("SwAgent|premain init failed", e);
        }

        ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(true));
        AgentBuilder builder = new AgentBuilder.Default(byteBuddy);
        builder.type(pluginFinder.buildMatch())
                .transform(new Transform(pluginFinder))
                .installOn(inst);
    }

    private static class Transform implements AgentBuilder.Transformer {
        private PluginFinder pluginFinder;

        Transform(PluginFinder pluginFinder) {
            this.pluginFinder = pluginFinder;
        }

        @Override
        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                                ClassLoader classLoader, // classLoader 是加载 typeDescription 这个类的类加载器, 被拦截到类加载被拦截到的类的加载器
                                                JavaModule javaModule, ProtectionDomain protectionDomain) {
            log.info("SwAgent|transform typeName: {}", typeDescription.getTypeName());
            List<AbstractClassEnhancePluginDefine> pluginDefines = pluginFinder.find(typeDescription);
            log.info("SwAgent|transform pluginDefines: {}", pluginDefines);
            if (!pluginDefines.isEmpty()) {
                DynamicType.Builder<?> newBuilder = builder;
                // enhanceContext 对应一个 typeDescription
                EnhanceContext enhanceContext = new EnhanceContext();

                for (AbstractClassEnhancePluginDefine pluginDefine : pluginDefines) {
                    DynamicType.Builder<?> possibleNewBuilder = pluginDefine.define(typeDescription, newBuilder, classLoader, enhanceContext);
                    if (possibleNewBuilder != null) {
                        newBuilder = possibleNewBuilder;
                    }
                }

                if (enhanceContext.isEnhanced()) {
                    log.info("Transform|enhanced class: {}", typeDescription.getActualName());
                }
                return newBuilder;
            }
            log.info("Transform|pluginDefines is empty");
            return builder;
        }
    }
}
