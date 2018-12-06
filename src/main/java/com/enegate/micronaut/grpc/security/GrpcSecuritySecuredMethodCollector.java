/*
 * Copyright 2018 Enegate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enegate.micronaut.grpc.security;

import com.enegate.micronaut.grpc.server.event.GrpcServerBeanServiceDefinition;
import com.enegate.micronaut.grpc.server.event.GrpcServerServicesAddedEvent;
import io.grpc.ServerServiceDefinition;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.security.annotation.Secured;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Steve Schneider
 */

@Singleton
public class GrpcSecuritySecuredMethodCollector {

    private final AtomicBoolean collected = new AtomicBoolean();
    private Map<String, List<String>> securedMethods = new HashMap<>();

    @EventListener
    public synchronized void onServicesAdded(GrpcServerServicesAddedEvent event) throws IOException {
        if (collected.get()) return;

        if (!(event.getSource() instanceof List))
            return;

        List<GrpcServerBeanServiceDefinition> beanServiceDefs = (List<GrpcServerBeanServiceDefinition>) event.getSource();
        beanServiceDefs.forEach(beanServiceDef -> {
            ServerServiceDefinition serviceDef = beanServiceDef.getServiceDef();
            serviceDef.getMethods().forEach(serverMethodDefinition -> {
                String fullMethodName = serverMethodDefinition.getMethodDescriptor().getFullMethodName();
                char[] methodNameChars = fullMethodName.substring(fullMethodName.lastIndexOf("/") + 1).toCharArray();
                methodNameChars[0] = Character.toLowerCase(methodNameChars[0]);
                String methodName = new String(methodNameChars);
                BeanDefinition<?> beanDef = beanServiceDef.getBeanDef();
                Optional<? extends ExecutableMethod> optionalMethod = beanDef.getExecutableMethods().stream().filter(executableMethod -> methodName.equals(executableMethod.getMethodName())).findFirst();
                if (optionalMethod.isPresent()) {
                    ExecutableMethod method = optionalMethod.get();
                    if (method.hasAnnotation(Secured.class)) {
                        Optional<String[]> value = method.getValue(Secured.class, String[].class);
                        if (value.isPresent()) {
                            List<String> securedValues = Arrays.asList(value.get());
                            securedMethods.put(fullMethodName, securedValues);
                        }
                    }
                }
            });
        });

        collected.set(true);
    }

    public Optional<List<String>> getAnotationValues(String fullMethodName) {
        return Optional.ofNullable(securedMethods.get(fullMethodName));
    }
}
