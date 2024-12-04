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

// 所有非NameMatch都实现该接口
public interface IndirectMatch extends ClassMatch {

    // 构造 type() 参数, e.g. named(...).or(named(...))
    ElementMatcher.Junction<? super TypeDescription> buildJunction();

    /**
     * 用于判断 typeDescription 是否满足当前匹配器(IndirectMatch 的实现类) 的条件
     *
     * @param typeDescription 待判断的类
     * @return true or false
     */
    boolean isMatch(TypeDescription typeDescription);
}

