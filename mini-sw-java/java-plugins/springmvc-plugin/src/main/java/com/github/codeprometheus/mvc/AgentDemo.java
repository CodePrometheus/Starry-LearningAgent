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
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

@Slf4j
public class AgentDemo {
    private static final String CONTROLLER_NAME = "org.springframework.stereotype.Controller";
    private static final String REST_CONTROLLER_NAME = "org.springframework.web.bind.annotation.RestController";

    public static void premain(String args, Instrumentation inst) {
        log.info("mvc|AgentDemo premain start, args: {}", args);
        AgentBuilder builder = new AgentBuilder.Default()
                .type(isAnnotatedWith(named(CONTROLLER_NAME).or(named(REST_CONTROLLER_NAME))))
                .transform(new AgentTransform());
        builder.installOn(inst);
    }
}
