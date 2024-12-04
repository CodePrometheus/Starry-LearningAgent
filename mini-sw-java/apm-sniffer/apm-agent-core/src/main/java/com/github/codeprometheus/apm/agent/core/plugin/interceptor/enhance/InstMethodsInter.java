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
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@Slf4j
public class InstMethodsInter {
    private final InstanceMethodsAroundInterceptor instanceMethodsAroundInterceptor;

    public InstMethodsInter(String instanceMethodsAroundInterceptorClassName, ClassLoader classLoader) {
        try {
            instanceMethodsAroundInterceptor = InterceptorInstanceLoader.load(instanceMethodsAroundInterceptorClassName, classLoader);
        } catch (Throwable t) {
            throw new RuntimeException("Can't create InstanceMethodsAroundInterceptor", t);
        }
    }

    /**
     * Intercept the target instance method.
     *
     * @param obj          target class instance.
     * @param allArguments all method arguments
     * @param method       method description.
     * @param zuper        the origin call ref.
     * @return the return value of target instance method.
     * @throws Exception only throw exception because of zuper.call() or unexpected exception in sky-walking ( This is a
     *                   bug, if anything triggers this condition ).
     */
    @RuntimeType
    public Object intercept(
            @This Object obj,
            @AllArguments Object[] allArguments,
            @Origin Method method,
            @SuperCall Callable<?> zuper
    ) throws Exception {
        EnhancedInstance targetObject = (EnhancedInstance) obj;
        try {
            instanceMethodsAroundInterceptor.beforeMethod(targetObject, method, allArguments, method.getParameterTypes());
        } catch (Throwable t) {
            log.error("class[{}] before instance method[{}] intercept failure:{}", obj.getClass(), method.getName(), t.getMessage());
        }

        Object call = null;
        try {
            call = zuper.call();
        } catch (Throwable t2) {
            try {
                instanceMethodsAroundInterceptor.handleMethodException(targetObject, method, allArguments, method.getParameterTypes(), t2);
            } catch (Exception e) {
                log.error("class[{}] handle instance method[{}] exception failure:{}", obj.getClass(), method.getName(), t2.getMessage());
                throw new RuntimeException(e);
            }
            throw t2;
        } finally {
            try {
                instanceMethodsAroundInterceptor.afterMethod(targetObject, method, allArguments, method.getParameterTypes(), call);
            } catch (Throwable e) {
                log.error("class[{}] after instance method[{}] intercept failure:{}", obj.getClass(), method.getName(), e.getMessage());
            }
        }
        return call;
    }
}
