import org.springframework.boot.gradle.tasks.bundling.BootJar

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

plugins {
    id("org.springdoc.openapi-gradle-plugin") apply(true)
    id("com.github.johnrengelman.processes") apply(true)
}

dependencies {
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")

    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")
    implementation(libs.liquibase.slf4j)

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.springdoc.openapi)

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
}

tasks.getByName<BootJar>("bootJar") {
    mainClass.set("com.core.CoreApp")
    archiveBaseName.set("core")
    archiveVersion.set("0.3.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}