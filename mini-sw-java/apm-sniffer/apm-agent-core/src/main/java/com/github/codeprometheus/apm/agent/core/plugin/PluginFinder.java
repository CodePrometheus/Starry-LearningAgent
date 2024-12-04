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

package com.github.codeprometheus.apm.agent.core.plugin;

import com.github.codeprometheus.apm.agent.core.plugin.match.ClassMatch;
import com.github.codeprometheus.apm.agent.core.plugin.match.IndirectMatch;
import com.github.codeprometheus.apm.agent.core.plugin.match.NameMatch;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.not;

@Slf4j
public class PluginFinder {

    /**
     * 用于存储 ClassMatch 类型为 NameMatch 的插件
     * key: 全定限类名
     * value: 同一个类可以同时被多个插件进行增强
     */
    private final Map<String, LinkedList<AbstractClassEnhancePluginDefine>> nameMatchDefine = new HashMap<>();

    /**
     * 用于存储 ClassMatch 类型为 IndirectMatch 的插件
     */
    private final List<AbstractClassEnhancePluginDefine> signatureMatchDefine = new ArrayList<>();

    /**
     * 对插件进行分类
     *
     * @param plugins 加载到的所有插件
     */
    public PluginFinder(List<AbstractClassEnhancePluginDefine> plugins) {
        for (AbstractClassEnhancePluginDefine plugin : plugins) {
            ClassMatch classMatch = plugin.enhanceClass();
            if (classMatch == null) {
                log.info("PluginFinder|Constructor classMatch is null, continue");
                continue;
            }

            if (classMatch instanceof NameMatch nameMatch) {
                LinkedList<AbstractClassEnhancePluginDefine> pluginDefines = nameMatchDefine.get(nameMatch.getClassName());
                if (pluginDefines == null) {
                    pluginDefines = new LinkedList<>();
                    nameMatchDefine.put(nameMatch.getClassName(), pluginDefines);
                }
            } else {
                signatureMatchDefine.add(plugin);
            }
        }
        log.info("PluginFinder|Constructor nameMatchDefine:{}, signatureMatchDefine:{}", nameMatchDefine, signatureMatchDefine);
    }

    /**
     * 返回已加载的所有插件最终拼接后的条件
     *
     * @return plugin1_junction.or(plugin2_junction).or(plugin3_junction)...
     */
    public ElementMatcher<? super TypeDescription> buildMatch() {
        ElementMatcher.Junction<? super TypeDescription> junction = new ElementMatcher.Junction.AbstractBase<NamedElement>() {
            @Override
            public boolean matches(NamedElement target) {
                // 当某个类第一次被加载时都会回调此函数
                return nameMatchDefine.containsKey(target.getActualName());
            }
        };
        junction = junction.and(not(isInterface())); // 只增强类
        for (AbstractClassEnhancePluginDefine pluginDefine : signatureMatchDefine) {
            ClassMatch classMatch = pluginDefine.enhanceClass();
            if (classMatch instanceof IndirectMatch indirectMatch) {
                junction = junction.or(indirectMatch.buildJunction());
            }
        }
        log.info("PluginFinder|buildMatch junction: {}", junction);
        return junction;
    }

    /**
     * @param typeDescription 当前匹配到的类
     * @return 对应的插件集合
     */
    public List<AbstractClassEnhancePluginDefine> find(TypeDescription typeDescription) {
        LinkedList<AbstractClassEnhancePluginDefine> matchedPlugins = new LinkedList<>();
        // 获取到全类名
        String typeName = typeDescription.getTypeName();
        if (nameMatchDefine.containsKey(typeName)) {
            matchedPlugins.addAll(nameMatchDefine.get(typeName));
        }
        for (AbstractClassEnhancePluginDefine pluginDefine : signatureMatchDefine) {
            IndirectMatch indirectMatch = (IndirectMatch) pluginDefine.enhanceClass();
            if (indirectMatch.isMatch(typeDescription)) {
                matchedPlugins.add(pluginDefine);
            }
        }
        log.info("PluginFinder|find matchedPlugins = {}, typeName = {}", matchedPlugins, typeName);
        return matchedPlugins;
    }
}
