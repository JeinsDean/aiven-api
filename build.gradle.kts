plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.jeinsdean"
version = "0.0.1-SNAPSHOT"
description = "Aiven"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // ── Spring Boot Core ──────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // ── Security ──────────────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-security")

    // ── Data ──────────────────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // ── Async / Reactive (AI API 호출용) ───────────────────────
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // ── JWT ───────────────────────────────────────────────────
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // ── API Docs (Swagger)─────────────────────────────────────
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")

    // ── Lombok ────────────────────────────────────────────────
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // ── Dev Tools ─────────────────────────────────────────────
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Redis (캐싱 - AI 응답 캐시)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // ── Database ──────────────────────────────────────────────
    runtimeOnly("com.h2database:h2")                       // local
    // runtimeOnly("com.mysql:mysql-connector-j")          // dev/prd 배포 시 주석 해제

    // ── Test ──────────────────────────────────────────────────
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.withType<Test> {
    useJUnitPlatform()
}
