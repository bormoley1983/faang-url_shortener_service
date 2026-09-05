plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.github.ben-manes.versions") version "0.61.0"
}

group = "faang.school"
version = "1.0"

// Temporary CVE mitigation; remove after Spring Boot manages Tomcat 11.0.25+.
extra["tomcat.version"] = "11.0.25"

val javaVersion = 25
val springCloudVersion = "2025.1.3"
val testcontainersVersion = "2.0.5"
val mapstructVersion = "1.6.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")    
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")

    // Database and Redis
    runtimeOnly("org.postgresql:postgresql")

    // Utilities & logging
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("com.redis:testcontainers-redis:2.2.4")
    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("-Xshare:off", "-javaagent:${mockitoAgent.asPath}")
    testLogging.showStandardStreams = true
}

jacoco {
    toolVersion = "0.8.15"
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }

    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}

// Coverage gate for hand-written application logic.
// Excluded: bootstrap, config property holders/bean wiring (config.* except context), DTOs/entities without custom behavior,
// Spring Data repository & Feign client interfaces, exception classes with no logic.
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "CLASS"
            includes = listOf(
                "faang.school.urlshortenerservice.service.*",
                "faang.school.urlshortenerservice.controller.*",
                "faang.school.urlshortenerservice.util.*",
                "faang.school.urlshortenerservice.config.context.*",
                "faang.school.urlshortenerservice.config.UrlRateLimitInterceptor",
                "faang.school.urlshortenerservice.exception.UrlExceptionHandler"
            )
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                // Baseline gate per DEVPLAN_UNITSTESTS-RULES.md §3: starts at measured baseline, rises non-decreasingly.
                // Measured 2026-08-31: lowest in-scope class is Base62Encoder at 84% instruction coverage.
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("service.jar")
}
