plugins {
    id("org.springframework.boot") apply(false)
    id("io.spring.dependency-management") apply(false)
    id("org.springdoc.openapi-gradle-plugin") apply(false)
    id("com.github.johnrengelman.processes") apply(false)
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://jcenter.bintray.com/")
    }

    apply(plugin = "java")
    apply(plugin = "version-catalog")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    configurations.all {
        resolutionStrategy.failOnVersionConflict()

        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}