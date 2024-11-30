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

package com.github.codeprometheus.mvc;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@Slf4j
public class SpringMvcInterceptor {
    @RuntimeType
    public Object intercept(
            @This Object targetObj,
            @Origin Method targetMethod,
            @AllArguments Object[] targetMethodArgs,
            @SuperCall Callable<?> zuper
    ) {
        long start = System.currentTimeMillis();
        Object call = null;
        try {
            call = zuper.call();
            log.info("SpringMvcInterceptor intercept targetObj: {}, targetMethod: {}, targetMethodArgs: {}, call: {}, cost: {}ms",
                    targetObj, targetMethod, targetMethodArgs, call, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("SpringMvcInterceptor intercept error", e);
        } finally {
            long end = System.currentTimeMillis();
            log.info("SpringMvcInterceptor|Finally, cost: {}ms", end - start);
        }
        return call;
    }
}
