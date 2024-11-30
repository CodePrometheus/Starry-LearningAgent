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

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public enum PluginCfg {
    INSTANCE;

    // 存放所有插件 .def 文件构造出来的 PluginDefine 实例
    private List<PluginDefine> pluginClassList = new ArrayList<>();

    /**
     * all .def -> PluginDefine
     */
    public void load(InputStream inputStream) throws IOException {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String pluginDefine;
            while ((pluginDefine = bufferedReader.readLine()) != null) {
                try {
                    if (pluginDefine.trim().isEmpty() || pluginDefine.startsWith("#")) continue;
                    PluginDefine define = PluginDefine.build(pluginDefine);
                    pluginClassList.add(define);
                } catch (Exception e) {
                    log.error("Failed to format plugin({}) define.", pluginDefine);
                }
            }
        } finally {
            inputStream.close();
        }
    }

    public List<PluginDefine> getPluginClassList() {
        return pluginClassList;
    }
}
