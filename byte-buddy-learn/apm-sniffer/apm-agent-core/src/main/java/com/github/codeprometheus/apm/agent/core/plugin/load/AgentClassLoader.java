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

package com.github.codeprometheus.apm.agent.core.plugin.load;

import com.github.codeprometheus.apm.agent.core.boot.AgentPackagePath;
import com.github.codeprometheus.apm.agent.core.plugin.PluginBootstrap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 用于加载插件和插件的拦截器
 */
@Slf4j
public class AgentClassLoader extends ClassLoader {

    // 用于加载插件的定义相关的类(除了插件的 interceptor) 比如 CommonControllerInstrumentation
    private static AgentClassLoader DEFAULT_LOADER;

    // 自定义类加载器加载类的路径
    private List<File> classPath;
    private List<Jar> allJars;
    private ReentrantLock jarScanLock = new ReentrantLock();

    public AgentClassLoader(ClassLoader parent) {
        super(parent);

        // 获取 agent.jar 的目录
        File agentJarDir = AgentPackagePath.getPath();
        classPath = new LinkedList<>();
        classPath.add(new File(agentJarDir, "plugins"));
    }

    public static void initDefaultLoader() {
        if (DEFAULT_LOADER == null) {
            synchronized (AgentClassLoader.class) {
                if (DEFAULT_LOADER == null) {
                    DEFAULT_LOADER = new AgentClassLoader(PluginBootstrap.class.getClassLoader());
                }
            }
        }
    }

    public static AgentClassLoader getDefault() {
        return DEFAULT_LOADER;
    }

    private static byte[] getBytes(Jar jar, String path) throws IOException {
        URL classFileUrl = new URL("jar:file:" + jar.sourceFile.getAbsolutePath() + "!/" + path);
        byte[] data;
        try (final BufferedInputStream is = new BufferedInputStream(classFileUrl.openStream());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ) {
            int ch;
            while ((ch = is.read()) != -1) {
                baos.write(ch);
            }
            data = baos.toByteArray();
        }
        return data;
    }

    /**
     * loadClass -> 自动回调 findClass(自定义类加载逻辑) -> defineClass
     *
     * @param name 全限定名 com.github.codeprometheus.apm.plugin.CommonControllerInstrumentation
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        List<Jar> allJars = getAllJars();
        String path = name.replace(".", "/").concat(".class");
        for (Jar jar : allJars) {
            JarEntry entry = jar.jarFile.getJarEntry(path);
            if (entry == null) continue;
            try {
                byte[] data = getBytes(jar, path);
                return defineClass(name, data, 0, data.length);
            } catch (IOException e) {
                log.error("AgentClassLoader|findClass find class fail, err:{}", e.getMessage());
            }
        }
        throw new ClassNotFoundException("Cannot find " + name);
    }

    @Override
    protected URL findResource(String name) {
        List<Jar> allJars = getAllJars();
        for (Jar jar : allJars) {
            JarEntry entry = jar.jarFile.getJarEntry(name);
            if (entry != null) {
                try {
                    return new URL("jar:file:" + jar.sourceFile.getAbsolutePath() + "!/" + name);
                } catch (MalformedURLException ignored) {
                }
            }
        }
        return null;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        List<URL> allResources = new LinkedList<>();
        List<Jar> allJars = getAllJars();
        for (Jar jar : allJars) {
            JarEntry entry = jar.jarFile.getJarEntry(name);
            if (entry != null) {
                allResources.add(new URL("jar:file:" + jar.sourceFile.getAbsolutePath() + "!/" + name));
            }
        }

        final Iterator<URL> iterator = allResources.iterator();
        return new Enumeration<>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }

    private List<Jar> getAllJars() {
        if (allJars == null) {
            jarScanLock.lock();
            try {
                if (allJars == null) {
                    allJars = doGetJar();
                }
            } finally {
                jarScanLock.unlock();
            }
        }
        return allJars;
    }

    private List<Jar> doGetJar() {
        List<Jar> jars = new LinkedList<>();
        for (File path : classPath) {
            if (path.exists() && path.isDirectory()) {
                String[] jarFileNames = path.list(((dir, name) -> name.endsWith(".jar")));
                for (String filename : jarFileNames) {
                    try {
                        File file = new File(path, filename);
                        Jar jar = new Jar(new JarFile(file), file);
                        jars.add(jar);
                        log.info("AgentClassLoader|doGetJar file:{}", file);
                    } catch (IOException e) {
                        log.error("{} jar file can't be resolved", filename);
                    }
                }
            }
        }
        return jars;
    }

    // 定义加载到的 jar 信息
    @RequiredArgsConstructor
    private static class Jar {
        private final JarFile jarFile; // jar 文件对应的 jarFile 对象
        private final File sourceFile; // jar 文件对象
    }
}
