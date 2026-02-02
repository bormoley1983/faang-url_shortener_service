
# URL Shortener Service

## Quick start

Prerequisites:
- Java 21+ (JDK)
- Docker (for container runs)
- [faang-infra services](https://github.com/bormoley1983/faang-infra) running locally or accessible

Run locally:
```sh
./gradlew bootRun
```

Run tests:
```sh
./gradlew test --info
```

Build and run in Docker:
```sh
./gradlew build
docker build -t account-service .
docker run -p 8090:8090 account-service
```

## Configuration

Main config: [src/main/resources/application.yaml](src/main/resources/application.yaml)  
Test containers config: [src/test/java/faang/school/accountservice/util/BaseContextTest.java](src/test/java/faang/school/accountservice/util/BaseContextTest.java)



The service will be available at [http://localhost:18080](http://localhost:18080).

---
For more information, see the [faang-infra repository](https://github.com/bormoley1983/faang-infra).