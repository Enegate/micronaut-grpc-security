package com.enegate.micronaut.grpc.security;

import io.grpc.Context;
import io.micronaut.security.authentication.Authentication;

public class GrpcSecurityContext {
    public static final Context.Key<Authentication> AUTHENTICATION_CTX_KEY = Context.key("micronaut.grpc.security.context.authentication");

    public static Authentication getAuthentication() {
        return AUTHENTICATION_CTX_KEY.get();
    }
}
