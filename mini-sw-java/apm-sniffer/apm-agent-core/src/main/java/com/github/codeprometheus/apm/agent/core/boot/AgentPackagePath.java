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

package com.github.codeprometheus.apm.agent.core.boot;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Slf4j
public class AgentPackagePath {

    private static File AGENT_PACKAGE_PATH;

    public static void main(String[] args) {
        getPath();
    }

    public static File getPath() {
        if (AGENT_PACKAGE_PATH == null) {
            AGENT_PACKAGE_PATH = findPath();
        }
        return AGENT_PACKAGE_PATH;
    }

    private static File findPath() {
        // com/github/codeprometheus/apm/agent/core/boot/AgentPackagePath.class
        String classResourcePath = AgentPackagePath.class.getName().replaceAll("\\.", "/") + ".class";
        // 1) file:/Users/zhouzixin/learn/Starry-LearningAgent/mini-sw-java/apm-sniffer/apm-agent-core/target/classes/com/github/codeprometheus/apm/agent/core/boot/AgentPackagePath.class
        // 2) jar:file:/Users/zhouzixin/learn/Starry-LearningAgent/mini-sw-java/dist/apm-agent-1.0-SNAPSHOT-jar-with-dependencies.jar!/com/github/codeprometheus/apm/agent/core/boot/AgentPackagePath.class
        URL resource = AgentPackagePath.class.getClassLoader().getResource(classResourcePath);
        if (resource != null) {
            String urlString = resource.toString();
            urlString = "jar:file:/Users/zhouzixin/learn/Starry-LearningAgent/mini-sw-java/dist/apm-agent-1.0-SNAPSHOT-jar-with-dependencies.jar!/com/github/codeprometheus/apm/agent/core/boot/AgentPackagePath.class";
            log.info("AgentPackagePath|findPath urlString = {}", urlString);
            int insidePathIndex = urlString.indexOf("!");
            boolean isInJar = insidePathIndex > -1;

            if (isInJar) {
                // file:/Users/zhouzixin/learn/Starry-LearningAgent/mini-sw-java/dist/apm-agent-1.0-SNAPSHOT-jar-with-dependencies.jar
                urlString = urlString.substring(urlString.indexOf("file:"), insidePathIndex);
                log.info("AgentPackagePath|findPath1 urlString = {}", urlString);
                File agentJarFile = null;
                try {
                    agentJarFile = new File(new URL(urlString).toURI());
                } catch (MalformedURLException | URISyntaxException e) {
                    log.error("AgentPackagePath|findPath Can not locate agent jar file by url:{}", urlString);
                }

                if (agentJarFile.exists()) {
                    // /Users/zhouzixin/learn/Starry-LearningAgent/mini-sw-java/dist
                    log.info("AgentPackagePath|findPath resp = {}", agentJarFile.getParentFile());
                    return agentJarFile.getParentFile();
                }
            } else {
                int prefixLength = "file:".length(); // 5
                // /Users/zhouzixin/learn/Starry-LearningAgent/mini-sw-java/apm-sniffer/apm-agent-core/target/classes/
                String classLocation = urlString.substring(prefixLength, urlString.length() - classResourcePath.length());
                log.info("AgentPackagePath|findPath classLocation:{}", classLocation);
                return new File(classLocation);
            }
        }
        log.error("Can not locate agent jar file.");
        throw new RuntimeException("Can not locate agent jar file.");
    }
}
