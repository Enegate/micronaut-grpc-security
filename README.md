# Annotation based security support for [Micronaut enabled gRPC services](https://github.com/Enegate/micronaut-grpc-server)

[![Build Status](https://travis-ci.org/Enegate/micronaut-grpc-security.svg?branch=master)](https://travis-ci.org/Enegate/micronaut-grpc-security)

This project is inspired by [Micronaut’s security capabilities](https://docs.micronaut.io/latest/guide/index.html#security).

## Features
- Basic authentication
- JSON Web Token (JWT)

## Usage

### Dependencies
Artifacts are published to Maven Central.

#### Maven
````xml
...
<dependency>
  <groupId>com.enegate</groupId>
  <artifactId>micronaut-grpc-security</artifactId>
  <version>0.0.1</version>
</dependency>
...
````

#### Gradle
````gradle
dependencies {
  ...
  compile 'com.enegate:micronaut-grpc-security:0.0.1'
  compile "io.micronaut:micronaut-security-jwt"
  ...
}
````

## Examples

- [micronaut-grpc-security-example](https://github.com/Enegate/micronaut-grpc-security-example) (Java)
