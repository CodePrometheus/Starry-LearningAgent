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

package com.github.codeprometheus.apm.plugins;

import com.github.codeprometheus.apm.agent.core.plugin.interceptor.ConstructorMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.StaticMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.enhance.ClassEnhancePluginDefine;
import com.github.codeprometheus.apm.agent.core.plugin.match.ClassMatch;
import com.github.codeprometheus.apm.agent.core.plugin.match.MultiClassNameMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

public class UserServiceInstrumentation extends ClassEnhancePluginDefine {

    private static final String USER_SERVICE_NAME = "com.github.codeprometheus.service.UserService";
    private static final String INTERCEPTOR = "com.github.codeprometheus.apm.plugins.interceptor.ServiceInterceptor";

    @Override
    protected ClassMatch enhanceClass() {
        return MultiClassNameMatch.byMultiClassMatch(USER_SERVICE_NAME);
    }

    @Override
    protected InstanceMethodsInterceptorPoints[] getInstanceMethodsInterceptorPoints() {
        return new InstanceMethodsInterceptorPoints[]{
                new InstanceMethodsInterceptorPoints() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return namedOneOf("selectUserList");
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return INTERCEPTOR;
                    }
                }
        };
    }

    @Override
    protected ConstructorMethodsInterceptorPoints[] getConstructorMethodsInterceptorPoints() {
        return new ConstructorMethodsInterceptorPoints[0];
    }

    @Override
    protected StaticMethodsInterceptorPoints[] getStaticMethodsInterceptorPoints() {
        return new StaticMethodsInterceptorPoints[0];
    }
}
