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
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class PluginResourcesResolver {

    public List<URL> getResources() {
        List<URL> cfgUrlPaths = new ArrayList<>();
        Enumeration<URL> urls;
        try {
            urls = AgentClassLoader.getDefault().getResources("skywalking-plugin.def");
            while (urls.hasMoreElements()) {
                URL pluginUrl = urls.nextElement();
                cfgUrlPaths.add(pluginUrl);
                log.info("find skywalking plugin define in {}", pluginUrl);
            }
            return cfgUrlPaths;
        } catch (IOException e) {
            log.error("read resources failure.", e);
        }
        return null;
    }
}
