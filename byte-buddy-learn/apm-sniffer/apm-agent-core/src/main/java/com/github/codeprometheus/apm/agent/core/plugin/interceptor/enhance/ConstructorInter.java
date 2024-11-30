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
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

@Slf4j
public class ConstructorInter {

    private final InstanceConstructorInterceptor constructorInterceptor;

    public ConstructorInter(String constructorInterceptorClassName, ClassLoader classLoader) {
        try {
            constructorInterceptor = InterceptorInstanceLoader.load(constructorInterceptorClassName, classLoader);
        } catch (Throwable t) {
            throw new RuntimeException("Can't create InstanceConstructorInterceptor", t);
        }
    }


    /**
     * Intercept the target constructor.
     *
     * @param obj          target class instance.
     * @param allArguments all constructor arguments
     */
    @RuntimeType
    public void intercept(
            @This Object obj,
            @AllArguments Object[] allArguments
    ) throws Exception {
        try {
            EnhancedInstance targetObject = (EnhancedInstance) obj;
            constructorInterceptor.onConstruct(targetObject, allArguments);
        } catch (Throwable t) {
            log.error("class[{}] constructor intercept failure:{}", obj.getClass(), t.getMessage());
        }
    }
}
