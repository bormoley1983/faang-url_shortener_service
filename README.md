# Url shortener service

# Requires Infra services running from 
# https://github.com/bormoley1983/faang-infra

# Local runs:
./gradlew bootRun

# build and run in docker
./gradlew build
docker build -t url-shortener .
docker run -p 8080:8080 url-shortener