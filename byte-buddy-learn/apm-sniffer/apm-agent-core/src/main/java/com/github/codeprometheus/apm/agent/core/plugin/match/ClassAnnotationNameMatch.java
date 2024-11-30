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

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.ArrayList;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

// 某个类要同时含有某几个注解匹配器
@Slf4j
public class ClassAnnotationNameMatch implements IndirectMatch {

    // 要匹配的类名称
    private final List<String> annotations;

    public ClassAnnotationNameMatch(String[] annotations) {
        if (annotations == null || annotations.length == 0) {
            throw new IllegalArgumentException("needMatchClassNames can't be empty");
        }
        this.annotations = List.of(annotations);
    }
    
    public static IndirectMatch byMultiAnnotationMatch(String... annotations) {
        return new ClassAnnotationNameMatch(annotations);
    }

    /**
     * 多个注解用 and 链接
     */
    @Override
    public ElementMatcher.Junction<? super TypeDescription> buildJunction() {
        ElementMatcher.Junction<? super TypeDescription> junction = null;
        for (String annotation : annotations) {
            if (junction == null) {
                junction = buildEachAnnotation(annotation);
            } else {
                junction = junction.and(buildEachAnnotation(annotation));
            }
        }
        junction = junction.and(not(isInterface()));
        return junction;
    }

    private ElementMatcher.Junction buildEachAnnotation(String annotationName) {
        return isAnnotatedWith(named(annotationName));
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        // 需要判断annotations是typeDescription的真子集
        List<String> annotationList = new ArrayList<>(annotations);
        AnnotationList declaredAnnotations = typeDescription.getDeclaredAnnotations();
        for (AnnotationDescription declaredAnnotation : declaredAnnotations) {
            annotationList.remove(declaredAnnotation.getAnnotationType().getActualName());
        }
        log.info("ClassAnnotationNameMatch|isMatch annotationList:{}", annotationList);
        return annotationList.isEmpty();
    }
}
