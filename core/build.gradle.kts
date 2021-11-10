import org.springframework.boot.gradle.tasks.bundling.BootJar

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
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
    implementation(libs.springfox.boot.starter)
    implementation(libs.springfox.bean.validators)
    implementation(libs.springfox.data.rest)
    implementation(libs.springfox.swagger.ui)

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
}

tasks.getByName<BootJar>("bootJar") {
    mainClass.set("com.core.CoreApp")
    archiveBaseName.set("core")
    archiveVersion.set("0.1.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}