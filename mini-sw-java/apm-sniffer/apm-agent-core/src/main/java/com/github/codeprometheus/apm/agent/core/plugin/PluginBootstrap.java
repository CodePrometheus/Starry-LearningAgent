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

import com.github.codeprometheus.apm.agent.core.plugin.load.AgentClassLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PluginBootstrap {

    /**
     * Load all plugins.
     * 因为是自定义的路径下的 jar
     * 1.先获取到 agent.jar 的路径
     * 2.使用自定义类加载器进行加载插件
     */
    public List<AbstractClassEnhancePluginDefine> loadPlugins() {
        AgentClassLoader.initDefaultLoader();
        PluginResourcesResolver resolver = new PluginResourcesResolver();
        List<URL> resources = resolver.getResources();
        if (resources == null || resources.isEmpty()) {
            log.info("PluginBootstrap|loadPlugins no plugin files (skywalking-plugin.def) found, continue to start application.");
            return new ArrayList<>();
        }

        for (URL pluginUrl : resources) {
            try {
                PluginCfg.INSTANCE.load(pluginUrl.openStream());
            } catch (IOException e) {
                log.error("PluginBootstrap|loadPlugins plugin file [{}] init failure.", pluginUrl);
            }
        }

        List<PluginDefine> pluginClassList = PluginCfg.INSTANCE.getPluginClassList();

        // 拿到全限定类名通过反射获取到对象,且所有对象都继承自 AbstractClassEnhancePluginDefine
        List<AbstractClassEnhancePluginDefine> plugins = new ArrayList<>();
        for (PluginDefine pluginDefine : pluginClassList) {
            try {
                // initialize=true，会对加载的类进行初始化，执行类中的静态代码块，以及对静态变量的赋值等操作
                AbstractClassEnhancePluginDefine abstractClassEnhancePluginDefine = (AbstractClassEnhancePluginDefine) Class.forName(pluginDefine.getDefineClass(), true,
                        AgentClassLoader.getDefault()).newInstance();
                plugins.add(abstractClassEnhancePluginDefine);
            } catch (Throwable t) {
                log.error("load plugin [{}] failure.", pluginDefine.getDefineClass());
            }
        }
        log.info("PluginBootstrap|loadPlugins plugins resp = {}", plugins);
        return plugins;
    }
}
