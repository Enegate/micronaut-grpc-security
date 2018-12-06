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

package com.enegate.micronaut.grpc.security.fakehttp;

import io.grpc.ServerCall;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.simple.SimpleHttpRequest;

/**
 * This implementation maps gRPC Requests to Micronaut HttpRequest
 *
 * @author Steve Schneider
 */

public class GrpcSecurityHttpRequest extends SimpleHttpRequest {

    public static final String ANNOTATION_VALUES = "micronaut.grpc.security.annotation.values";

    private ServerCall call;
    private MutableHttpHeaders headers;

    public GrpcSecurityHttpRequest(ServerCall call, MutableHttpHeaders headers) {
        super(HttpMethod.POST, "/" + call.getMethodDescriptor().getFullMethodName(), null);

        this.call = call;
        this.headers = headers;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return headers;
    }
}
