# Simple-Wallet

This is a Sping Boot application written in Java. In-memory database H2 is used to ease infra setup and API testing. The main API implementation is a http PUT method(/accounts/{id}) which can be used to deduct or add amount to a user account balance.

## Requirements
Implement a simple integration between the game engine and the game account (Wallet) that allows you to buy games and pay out winnings.
In this context, a game account refers to a server that provides an HTTP API to game engines and manages customers’ gaming funds. The game engine does not need to be implemented for this project.
 
1. Design and document the HTTP interface between the game engine and the game account.

When you charge a game, the game engine transmits to the game account the unique identifier of the purchase transaction, the unique identifier of the player and the amount. In response, the game account forwards to the game engine the remaining balance of the player’s game account.

If the game round is a winning game, the game engine transmits to the game account the unique identifier of the winning event, the unique identifier of the player and the winning amount. In response, the game account forwards the new balance of the player’s game account to the game engine.

These HTTP APIs are idempotent.
 
2. Design and implement a database for the game account

Information to be saved about players:
* The unique identifier of the player
* Name
* Game account balance
* Game event information:
* Timestamp
* The unique identifier of the player
* Unique identifier of the event
* Type of transaction (purchase or profit)Amount

3. Implement a game account

When processing a game purchase, the game engine debits the purchase amount from the player’s game account. If the player wins, the game engine pays them into the game account.

If the game account balance is not sufficient for the purchase, the system will return an error to the game engine.

Traffic between the game engine and the game account must be encrypted.
 
4. Write tests for the system

5. Write instructions for running the game engine and tests
 
## Building
Open a terminal, go to project root directory and run `./mvnw clean install` or `./mvnw package`

## Running Application
Run `java -jar target/wallet-2020.1.0.jar` or `./mvnw spring-boot:run`

## Running Tests
All the tests are in WalletApiTest.java. It is API test/integration test. So you need to have the application running before executing tests.
- Start the application as described above
- Run `./mvnw -Dtest=WalletApiTest test`

## REST API Documentation
The OpenAPI/Swagger is used to generate API documentation. After the application is started, open url at:  
https://localhost:8443/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config  
or   
http://localhost:8080/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config  
where you can view documentation and try out the api.


