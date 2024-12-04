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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 加载插件里面的拦截器
 */
public class InterceptorInstanceLoader {

    private static final ConcurrentHashMap<String, Object> INSTANCE_CACHE = new ConcurrentHashMap<>();

    private static final ReentrantLock INSTANCE_LOAD_LOCK = new ReentrantLock();

    private static final Map<ClassLoader, ClassLoader> EXTEND_PLUGIN_CLASSLOADERS = new HashMap<>();

    /**
     * 需要使 加载 interceptorName 的类加载器作为 targetClassLoader 的子类加载器 可以达到互相访问
     *
     * @param interceptorName   插件中拦截器的全类名
     * @param targetClassLoader 要想在插件拦截器中 能够访问到被拦截的类，需要是同一个类加载器 或 子类类加载器
     *                          被拦截的类: A - 拦截类: C1
     * @return InstanceConstructorInterceptor | InstanceMethodsAroundInterceptor | StaticMethodsAroundInterceptor 的实例
     */
    public static <T> T load(String interceptorName, ClassLoader targetClassLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (targetClassLoader == null) {
            targetClassLoader = InterceptorInstanceLoader.class.getClassLoader();
        }
        String instanceKey = interceptorName + "_OF_" + targetClassLoader.getClass().getName()
                + "@" + Integer.toHexString(targetClassLoader.hashCode());

        Object instance = INSTANCE_CACHE.get(instanceKey);
        if (instance == null) {
            INSTANCE_LOAD_LOCK.lock();
            ClassLoader pluginLoader;
            try {
                pluginLoader = EXTEND_PLUGIN_CLASSLOADERS.get(targetClassLoader);
                if (pluginLoader == null) {
                    pluginLoader = new AgentClassLoader(targetClassLoader);
                    EXTEND_PLUGIN_CLASSLOADERS.put(targetClassLoader, pluginLoader);
                }
            } finally {
                INSTANCE_LOAD_LOCK.unlock();
            }
            instance = Class.forName(interceptorName, true, pluginLoader).newInstance();
            if (instance != null) {
                INSTANCE_CACHE.put(instanceKey, instance);
            }
        }
        return (T) instance;
    }
}
