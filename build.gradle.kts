import org.gradle.api.file.FileTree
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    jacoco
    checkstyle
}

group = "faang.school"
version = "1.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

val mapstructVersion = "1.5.5.Final"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.0.2")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    testImplementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.redis.testcontainers:testcontainers-redis-junit-jupiter:1.4.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.13"
}

val jacocoExcludes = listOf(
    "**/generated/**",
    "**/build/**",
    "**/dto/**",
    "**/entity/**",
    "**/config/**",
    "**/configuration/**",
    "**/exceptions/**",
    "**/exception/**",
    "**/constants/**",
    "**/vo/**",
    "**/pojo/**",
    "**/model/**",
    "**/mapper/**",
    "**/*Application*",
    "com/json/student/**"
)

fun FileTree.filteredForCoverage(): FileTree = matching { exclude(jacocoExcludes) }

fun filteredClassDirectories() = files(
    sourceSets.main.get().output.classesDirs.files.map { dir ->
        fileTree(dir).filteredForCoverage()
    }
)

fun serviceOnlyClassDirectories() = files(
    sourceSets.main.get().output.classesDirs.files.map { dir ->
        fileTree(dir).matching {
            include("**/service/**")
            exclude(jacocoExcludes)
        }
    }
)

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacoco.xml"))
    }

    classDirectories.setFrom(filteredClassDirectories())
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.test)
    classDirectories.setFrom(filteredClassDirectories())

    violationRules {
        // Global rule
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }

        // Service-only rule (higher bar)
        rule {
            element = "BUNDLE"
            includes = listOf("*")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.85".toBigDecimal()
            }

            // Apply this rule only to service classes
            // (Jacoco API applies rules to classDirectories; we set a separate filtered set for service below)
        }
    }

    // Apply service-only class set to the second rule by running a second verification task
    // (Gradle/Jacoco does not support per-rule classDirectories in one task).
}

val jacocoServiceCoverageVerification by tasks.registering(JacocoCoverageVerification::class) {
    group = "verification"
    description = "Verifies JaCoCo coverage for service classes with a higher threshold."
    dependsOn(tasks.test)

    classDirectories.setFrom(serviceOnlyClassDirectories())

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
    dependsOn(jacocoServiceCoverageVerification)
}

// Enforce coverage on build as well
tasks.build {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
    dependsOn(jacocoServiceCoverageVerification)
}

tasks.bootJar {
    archiveFileName.set("service.jar")
}

checkstyle {
    toolVersion = "10.17.0"
    configFile = file("${project.rootDir}/config/checkstyle/checkstyle.xml")
    checkstyle.enableExternalDtdLoad.set(true)
}

tasks.checkstyleMain {
    source = fileTree("${project.rootDir}/src/main/java")
    include("**/*.java")
    exclude("**/resources/**")
    classpath = files()
}

tasks.checkstyleTest {
    source = fileTree("${project.rootDir}/src/test")
    include("**/*.java")
    classpath = files()
}
