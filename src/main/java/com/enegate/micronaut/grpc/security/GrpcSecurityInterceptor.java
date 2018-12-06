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

import com.enegate.micronaut.grpc.security.fakehttp.GrpcSecurityHttpHeaders;
import com.enegate.micronaut.grpc.security.fakehttp.GrpcSecurityHttpRequest;
import com.enegate.micronaut.grpc.security.fakehttp.GrpcSecurityServerFilterChain;
import com.enegate.micronaut.grpc.server.annotation.GrpcInterceptor;
import io.grpc.*;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.security.filters.SecurityFilter;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Steve Schneider
 */

@GrpcInterceptor(global = true)
public class GrpcSecurityInterceptor implements ServerInterceptor, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcSecurityInterceptor.class);

    private static final ServerCall.Listener EMPTY_LISTENER = new ServerCall.Listener() {
    };

    private ApplicationContext applicationContext;

    @Inject
    public GrpcSecurityInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Security check for gRPC service: {}", call.getMethodDescriptor().getFullMethodName());
        }

        GrpcSecuritySecuredMethodCollector collector = applicationContext.getBean(GrpcSecuritySecuredMethodCollector.class);
        MutableHttpHeaders headers = GrpcSecurityHttpHeaders.convert(metadata);
        HttpRequest request = new GrpcSecurityHttpRequest(call, headers);
        collector.getAnotationValues(call.getMethodDescriptor().getFullMethodName())
                .ifPresent(values -> request.setAttribute(GrpcSecurityHttpRequest.ANNOTATION_VALUES, values));

        SecurityFilter securityFilter = applicationContext.getBean(SecurityFilter.class);
        Publisher<MutableHttpResponse<?>> responsePublisher = securityFilter.doFilter(request, new GrpcSecurityServerFilterChain());
        MutableHttpResponse<?> response = Flowable.fromPublisher(responsePublisher).blockingSingle();
        Status status;
        switch (response.getStatus()) {
            case UNAUTHORIZED:
                status = Status.UNAUTHENTICATED;
                call.close(status, metadata);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Security check failed for gRPC service {} with status code: {}", call.getMethodDescriptor().getFullMethodName(), status);
                }
                return EMPTY_LISTENER;
            case FORBIDDEN:
                status = Status.PERMISSION_DENIED;
                call.close(status, metadata);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Security check failed for gRPC service {} with status code: {}", call.getMethodDescriptor().getFullMethodName(), status);
                }
                return EMPTY_LISTENER;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Security check successful for gRPC service {} with status code: {}", call.getMethodDescriptor().getFullMethodName(), Status.OK);
        }

        return next.startCall(call, metadata);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
