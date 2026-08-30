
# URL Shortener Service

## Quick start

Prerequisites:
- Java 25+ (JDK)
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
docker build -t url-shortener-service .
docker run -p 18080:18080 url-shortener-service
```

## Configuration

Main config: [src/main/resources/application.yaml](src/main/resources/application.yaml)  
Test config: [src/test/resources/application-test.yaml](src/test/resources/application-test.yaml)

The service will be available at [http://localhost:18080](http://localhost:18080).

---
For more information, see the [faang-infra repository](https://github.com/bormoley1983/faang-infra).

**Note:** Base code structure and architecture patterns are based on [FAANG School](https://github.com/faang-school) educational project.
