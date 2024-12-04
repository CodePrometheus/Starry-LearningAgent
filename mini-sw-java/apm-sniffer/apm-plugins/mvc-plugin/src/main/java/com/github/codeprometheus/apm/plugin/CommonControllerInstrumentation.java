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

package com.github.codeprometheus.apm.plugin;

import com.github.codeprometheus.apm.agent.core.plugin.interceptor.ConstructorMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.StaticMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.enhance.ClassEnhancePluginDefine;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.nameEndsWith;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.not;

public abstract class CommonControllerInstrumentation extends ClassEnhancePluginDefine {
    private static final String MAPPING_PKG_PREFIX = "org.springframework.web.bind.annotation";
    private static final String MAPPING_PREFIX = "Mapping";
    private static final String INTERCEPTOR = "com.github.codeprometheus.apm.plugin.interceptor.MvcInterceptor";

    @Override
    protected InstanceMethodsInterceptorPoints[] getInstanceMethodsInterceptorPoints() {
        return new InstanceMethodsInterceptorPoints[]{new InstanceMethodsInterceptorPoints() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return not(isStatic()).and(isAnnotatedWith(nameStartsWith(MAPPING_PKG_PREFIX)
                        .and(nameEndsWith(MAPPING_PREFIX))));
            }

            @Override
            public String getMethodsInterceptor() {
                return INTERCEPTOR;
            }
        }};
    }

    @Override
    protected ConstructorMethodsInterceptorPoints[] getConstructorMethodsInterceptorPoints() {
        return null;
    }

    @Override
    protected StaticMethodsInterceptorPoints[] getStaticMethodsInterceptorPoints() {
        return null;
    }
}
