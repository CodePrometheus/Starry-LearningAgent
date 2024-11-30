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

package com.github.codeprometheus.apm.agent.core.plugin.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class MultiClassNameMatch implements IndirectMatch {

    // 要匹配的类名称
    private List<String> needMatchClassNames;

    public MultiClassNameMatch(String[] classNames) {
        if (classNames == null || classNames.length == 0) {
            throw new IllegalArgumentException("needMatchClassNames can't be empty");
        }
        this.needMatchClassNames = List.of(classNames);
    }

    public static IndirectMatch byMultiClassMatch(String... classNames) {
        return new MultiClassNameMatch(classNames);
    }

    /**
     * 多个类要求是 or 的关系
     */
    @Override
    public ElementMatcher.Junction<? super TypeDescription> buildJunction() {
        ElementMatcher.Junction<? super TypeDescription> junction = null;
        for (String needMatchClassName : needMatchClassNames) {
            if (junction == null) {
                junction = named(needMatchClassName);
            } else {
                junction = junction.or(named(needMatchClassName));
            }
        }
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        // such as needMatchClassNames=["A", "B"]
        // 而 typeDescription 是 C, return false
        return needMatchClassNames.contains(typeDescription.getTypeName());
    }
}
