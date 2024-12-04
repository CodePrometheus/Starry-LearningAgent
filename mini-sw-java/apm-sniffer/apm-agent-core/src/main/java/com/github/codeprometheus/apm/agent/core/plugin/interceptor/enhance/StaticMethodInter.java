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

package com.github.codeprometheus.apm.agent.core.plugin.interceptor.enhance;

import com.github.codeprometheus.apm.agent.core.plugin.load.InterceptorInstanceLoader;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@Slf4j
public class StaticMethodInter {

    /**
     * A class full name, and instanceof {@link StaticMethodsAroundInterceptor} This name should only stay in {@link
     * String}, the real {@link Class} type will trigger classloader failure. If you want to know more, please check on
     * books about Classloader or Classloader appointment mechanism.
     */
    private final String staticMethodsAroundInterceptorClassName;

    public StaticMethodInter(String staticMethodsAroundInterceptorClassName) {
        this.staticMethodsAroundInterceptorClassName = staticMethodsAroundInterceptorClassName;
    }

    /**
     * Intercept the target static method.
     *
     * @param clazz        target class
     * @param allArguments all method arguments
     * @param method       method description.
     * @param zuper        the origin call ref.
     * @return the return value of target static method.
     * @throws Exception only throw exception because of zuper.call() or unexpected exception in sky-walking ( This is a
     *                   bug, if anything triggers this condition ).
     */
    @RuntimeType
    public Object intercept(
            @Origin Class<?> clazz,
            @AllArguments Object[] allArguments,
            @Origin Method method,
            @SuperCall Callable<?> zuper
    ) throws Exception {

        // 由于构造器里无classLoad, 在此用 clazz 类加载器加载 StaticMethodsAroundInterceptor
        StaticMethodsAroundInterceptor staticMethodsAroundInterceptor =
                InterceptorInstanceLoader.load(staticMethodsAroundInterceptorClassName, clazz.getClassLoader());

        try {
            staticMethodsAroundInterceptor.beforeMethod(clazz, method, allArguments, method.getParameterTypes());
        } catch (Throwable t) {
            log.error("class[{}] before static method[{}] intercept failure:{}", clazz, method.getName(), t.getMessage());
        }

        Object call = null;
        try {
            call = zuper.call();
        } catch (Throwable t2) {
            try {
                staticMethodsAroundInterceptor.handleMethodException(clazz, method, allArguments, method.getParameterTypes(), t2);
            } catch (Exception e) {
                log.error("class[{}] handle static method[{}] exception failure:{}", clazz, method.getName(), t2.getMessage());
                throw new RuntimeException(e);
            }
            throw t2;
        } finally {
            try {
                staticMethodsAroundInterceptor.afterMethod(clazz, method, allArguments, method.getParameterTypes(), call);
            } catch (Exception e) {
                log.error("class[{}] after static method[{}] intercept failure:{}", clazz, method.getName(), e.getMessage());
            }
        }
        return call;
    }
}
