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

import com.github.codeprometheus.apm.agent.core.plugin.AbstractClassEnhancePluginDefine;
import com.github.codeprometheus.apm.agent.core.plugin.EnhanceContext;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.ConstructorMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptorPoints;
import com.github.codeprometheus.apm.agent.core.plugin.interceptor.StaticMethodsInterceptorPoints;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * 所有的插件都必须直接或者间接的继承这个类. 此类完成了增强(transform里指定的 method 和 intercept)
 * 也就是要拼接朴素实现的如下结构
 * DynamicType.Builder<?> build =
 * builder
 * .method(xxx)
 * .intercept(MethodDelegation.to(xxx));
 */
@Slf4j
public abstract class ClassEnhancePluginDefine extends AbstractClassEnhancePluginDefine {

    /**
     * Enhance a class to intercept constructors and class instance methods.
     *
     * @param typeDescription target class description
     * @param newClassBuilder byte-buddy's builder to manipulate class bytecode.
     * @return new byte-buddy's builder for further manipulation.
     */
    @Override
    protected DynamicType.Builder<?> enhanceInstance(TypeDescription typeDescription, DynamicType.Builder<?> newClassBuilder,
                                                     ClassLoader classLoader, EnhanceContext enhanceContext) {
        ConstructorMethodsInterceptorPoints[] constructorMethodsInterceptorPoints = getConstructorMethodsInterceptorPoints();
        InstanceMethodsInterceptorPoints[] instanceMethodsInterceptPoints = getInstanceMethodsInterceptorPoints();

        // 构造拦截点是否存在
        boolean existedConstructorInterceptPoint = constructorMethodsInterceptorPoints != null && constructorMethodsInterceptorPoints.length > 0;
        // 实例拦截点是否存在
        boolean existedMethodsInterceptPoints = instanceMethodsInterceptPoints != null && instanceMethodsInterceptPoints.length > 0;

        log.info("ClassEnhancePluginDefine|enhanceInstance existedConstructorInterceptPoint:{}, existedMethodsInterceptPoints:{}",
                existedConstructorInterceptPoint, existedMethodsInterceptPoints);
        if (!existedConstructorInterceptPoint && !existedMethodsInterceptPoints) {
            return newClassBuilder;
        }

        // add field 对于同一个 typeDescription 只需要执行一次
        if (!typeDescription.isAssignableTo(EnhancedInstance.class)) { // typeDescription 不是 EnhancedInstance 的实现类
            if (!enhanceContext.isObjExtended()) { // 没被扩展过
                newClassBuilder = newClassBuilder.defineField(CONTEXT_ATTR_NAME,
                                Object.class,
                                Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                        .implement(EnhancedInstance.class) // 指定属性的 getter & setter
                        .intercept(FieldAccessor.ofField(CONTEXT_ATTR_NAME));

                enhanceContext.objExtendedCompleted();
            }
        }

        if (existedConstructorInterceptPoint) {
            for (ConstructorMethodsInterceptorPoints constructorMethodsInterceptorPoint : constructorMethodsInterceptorPoints) {
                String constructorInterceptor = constructorMethodsInterceptorPoint.getConstructorInterceptor();
                if (constructorInterceptor == null || constructorInterceptor.isEmpty()) {
                    throw new RuntimeException("no InstanceConstructorInterceptor define to enhance class " + typeDescription.getTypeName());
                }
                ElementMatcher<MethodDescription> constructorMatcher = constructorMethodsInterceptorPoint.getConstructorMatcher();
                newClassBuilder = newClassBuilder.constructor(constructorMatcher)
                        .intercept(SuperMethodCall.INSTANCE.andThen(
                                MethodDelegation.withDefaultConfiguration().to(new ConstructorInter(constructorInterceptor, classLoader))));
            }
        }

        if (existedMethodsInterceptPoints) {
            for (InstanceMethodsInterceptorPoints instanceMethodsInterceptPoint : instanceMethodsInterceptPoints) {
                String methodsInterceptor = instanceMethodsInterceptPoint.getMethodsInterceptor();
                log.info("ClassEnhancePluginDefine|enhanceInstance instanceMethodsInterceptPoint:{}, methodsInterceptor:{}",
                        instanceMethodsInterceptPoint, methodsInterceptor);

                if (methodsInterceptor == null || methodsInterceptor.isEmpty()) {
                    throw new RuntimeException("no InstanceMethodsAroundInterceptor define to enhance class " + typeDescription.getTypeName());
                }
                ElementMatcher<MethodDescription> methodsMatcher = instanceMethodsInterceptPoint.getMethodsMatcher();
                log.info("ClassEnhancePluginDefine|enhanceInstance methodsMatcher:{}", methodsMatcher.getClass().getName());

                newClassBuilder = newClassBuilder
                        .method(not(isStatic()).and(methodsMatcher))
                        .intercept(MethodDelegation.withDefaultConfiguration()
                                .to(new InstMethodsInter(methodsInterceptor, classLoader)));
            }
        }
        return newClassBuilder;
    }

    @Override
    protected DynamicType.Builder<?> enhanceClass(TypeDescription typeDescription, DynamicType.Builder<?> newClassBuilder, ClassLoader classLoader) {
        StaticMethodsInterceptorPoints[] staticMethodsInterceptorPoints = getStaticMethodsInterceptorPoints();
        if (staticMethodsInterceptorPoints == null || staticMethodsInterceptorPoints.length == 0) {
            return newClassBuilder;
        }
        // 所有增强的类
        String enhanceOriginClassName = typeDescription.getTypeName();
        for (StaticMethodsInterceptorPoints staticMethodsInterceptorPoint : staticMethodsInterceptorPoints) {
            String methodsInterceptor = staticMethodsInterceptorPoint.getMethodsInterceptor();
            if (methodsInterceptor == null || methodsInterceptor.isEmpty()) {
                throw new RuntimeException("no StaticMethodsAroundInterceptor define to enhance class " + enhanceOriginClassName);
            }
            ElementMatcher<MethodDescription> methodsMatcher = staticMethodsInterceptorPoint.getMethodsMatcher();
            newClassBuilder = newClassBuilder.method(isStatic()
                            .and(methodsMatcher))
                    .intercept(MethodDelegation.withDefaultConfiguration()
                            .to(new StaticMethodInter(methodsInterceptor)));
        }
        return newClassBuilder;
    }
}
