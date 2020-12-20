# Simple-Wallet

This is a Sping Boot application written in Java. In-memory database H2 is used to ease infra setup and API testing. The main API implementation is a http PUT method(/accounts/{id}) which can be used to deduct or add amount to a user account balance.

## Building
Open a terminal, go to project root directory and run `./mvnw clean install` or `./mvnw package`

## Running Application
Run `java -jar target/wallet-2020.1.0.jar` or `./mvnw spring-boot:run`

## Running Tests
All the tests are in WalletApiTest.java. It is API test/integration test. So you need to have the application running before executing tests.
- Start the application as described above
- Run `./mvnw -Dtest=WalletApiTest test`

## REST API Documentation
The OpenAPI/Swagger is used to generate API documentation. When running the application, open url at:  
https://localhost:8443/swagger-ui/  
or http://localhost:8080/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config  
where you can view documentation and try out the api manually.


