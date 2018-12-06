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

import io.grpc.Metadata;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.simple.SimpleHttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * This implementation maps gRPC Metadata to Micronaut HttpHeaders
 *
 * @author Steve Schneider
 */
public class GrpcSecurityHttpHeaders {

    public static SimpleHttpHeaders convert(Metadata metadata) {
        Map<String, String> headers = new HashMap<>();

        for (String key : metadata.keys()) {
            switch (key) {
                case "authorization":
                    String value = metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
                    headers.put("Authorization", value);
                    break;
            }
        }

        return new SimpleHttpHeaders(headers, ConversionService.SHARED);
    }
}
