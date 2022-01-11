enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val springDocPlugin: String by settings
    val engelmanProcesses: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.springdoc.openapi-gradle-plugin") version springDocPlugin
        id("com.github.johnrengelman.processes") version engelmanProcesses
    }
}

dependencyResolutionManagement {
    val testContainersVersion: String by settings
    val liquibaseSlf4jVersion: String by settings
    val springDoc: String by settings

    versionCatalogs {
        create("libs") {
            version("testcontainers", testContainersVersion)
            version("liquibaseSlf4j", liquibaseSlf4jVersion)
            version("springDoc", springDoc)

            alias("testcontainers-junit-jupiter")
                .to("org.testcontainers", "junit-jupiter")
                .versionRef("testcontainers")
            alias("testcontainers-postgresql")
                .to("org.testcontainers", "postgresql")
                .versionRef("testcontainers")
            alias("liquibase-slf4j")
                .to("com.mattbertolini", "liquibase-slf4j")
                .versionRef("liquibaseSlf4j")
            alias("springdoc-openapi")
                .to("org.springdoc", "springdoc-openapi-ui")
                .versionRef("springDoc")
        }
    }
}

rootProject.name = "online-shop"

include("core")