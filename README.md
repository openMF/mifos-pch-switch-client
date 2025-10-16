# Mifos PCH Connector (gRPC version)

This project uses Spring Boot 3.X

## Packaging and running the application

The application can be packaged using Java 21:

```shell script
./mvnw clean package -Dmaven.test.skip=true
```

It produces the `pch.connector-0.0.1-SNAPSHOT.jar` file in the `target/` directory.

The application is now runnable using `java -jar target/pch.connector-0.0.1-SNAPSHOT.jar`.

## Provided Code

### REST

Easily start your REST Web Services
