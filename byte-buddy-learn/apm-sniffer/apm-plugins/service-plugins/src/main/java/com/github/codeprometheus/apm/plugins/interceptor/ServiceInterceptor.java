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

package com.github.codeprometheus.apm.plugins.interceptor;

import com.github.codeprometheus.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class ServiceInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes) throws Throwable {
        objInst.setSwDynamicField("select * from user_info = ?");
        log.info("SrvInterceptor|beforeMethod method:{}, args:{}", method.getName(), allArguments);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
        Object swDynamicField = objInst.getSwDynamicField();
        log.info("SrvInterceptor|afterMethod method:{}, args:{}, ret:{}, swDynamicField:{}", method.getName(), allArguments, ret, swDynamicField);
        return null;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        log.error("SrvInterceptor|handleMethodException method:{}, args:{}, exception:{}", method.getName(), allArguments, t.getMessage());
    }
}
