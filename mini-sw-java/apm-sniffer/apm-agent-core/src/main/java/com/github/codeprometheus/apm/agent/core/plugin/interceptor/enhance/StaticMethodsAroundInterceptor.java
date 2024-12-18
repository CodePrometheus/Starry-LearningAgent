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

import java.lang.reflect.Method;

// 静态方法的 intercept 必须实现该接口
public interface StaticMethodsAroundInterceptor {

    /**
     * called before target method invocation.
     */
    void beforeMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes);

    /**
     * called after target method invocation. Even method's invocation triggers an exception.
     *
     * @param ret the method's original return value.
     * @return the method's actual return value.
     */
    Object afterMethod(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes, Object ret);

    /**
     * called when occur exception.
     *
     * @param t the exception occur.
     */
    void handleMethodException(Class clazz, Method method, Object[] allArguments, Class<?>[] parameterTypes,
                               Throwable t);
}
