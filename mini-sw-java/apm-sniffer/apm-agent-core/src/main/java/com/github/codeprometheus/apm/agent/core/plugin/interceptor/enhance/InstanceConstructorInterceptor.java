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

/**
 * The instance constructor's interceptor interface. Any plugin, which wants to intercept constructor, must implement
 * this interface.
 * <p>
 */
public interface InstanceConstructorInterceptor {

    /**
     * 在构造器执行后调用
     * Called after the origin constructor invocation.
     */
    void onConstruct(EnhancedInstance onjInst, Object[] allArguments) throws Throwable;
}