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

package com.github.codeprometheus.apm.agent.core.plugin;

import com.github.codeprometheus.apm.agent.core.plugin.interceptor.ConstructorMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.StaticMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.match.ClassMatch;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

@Slf4j
public abstract class AbstractClassEnhancePluginDefine {

    // 为匹配到的字节码新增的新属性名称
    public static final String CONTEXT_ATTR_NAME = "_$EnhancedClassField_ws";

    // 获取当前插件要增强的目标类
    protected abstract ClassMatch enhanceClass();

    // 实例方法的拦截点
    protected abstract InstanceMethodsInterceptorPoints[] getInstanceMethodsInterceptorPoints();

    // 构造方法的拦截点
    protected abstract ConstructorMethodsInterceptorPoints[] getConstructorMethodsInterceptorPoints();

    // 静态方法的拦截点
    protected abstract StaticMethodsInterceptorPoints[] getStaticMethodsInterceptorPoints();

    // 增强类的主入口
    public DynamicType.Builder<?> define(
            TypeDescription typeDescription,
            DynamicType.Builder<?> builder,
            ClassLoader classLoader,
            EnhanceContext enhanceContext
    ) {
        // com.github.codeprometheus.apm.plugin.RestControllerInstrumentation
        String pluginDefineClassName = this.getClass().getName();
        // com.github.codeprometheus.rest.UserController
        String typeName = typeDescription.getTypeName();
        log.info("AbstractClassEnhancePluginDefine|define typeName={}|pluginDefineClassName={}", typeName, pluginDefineClassName);
        DynamicType.Builder<?> newBuilder = this.enhance(typeDescription, builder, classLoader, enhanceContext);
        enhanceContext.initStageCompleted();
        return newBuilder;
    }

    private DynamicType.Builder<?> enhance(
            TypeDescription typeDescription,
            DynamicType.Builder<?> builder,
            ClassLoader classLoader,
            EnhanceContext enhanceContext
    ) {
        builder = this.enhanceClass(typeDescription, builder, classLoader);
        builder = this.enhanceInstance(typeDescription, builder, classLoader, enhanceContext);
        return builder;
    }

    /**
     * 增强实例方法|构造方法
     */
    protected abstract DynamicType.Builder<?> enhanceInstance(TypeDescription typeDescription,
                                                              DynamicType.Builder<?> builder, ClassLoader classLoader, EnhanceContext enhanceContext);

    /**
     * 增强静态方法
     */
    protected abstract DynamicType.Builder<?> enhanceClass(TypeDescription typeDescription, DynamicType.Builder<?> builder, ClassLoader classLoader);
}
