enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
    }
}

dependencyResolutionManagement {
    val testContainersVersion: String by settings
    val liquibaseSlf4jVersion: String by settings
    val springFoxVersion: String by settings

    versionCatalogs {
        create("libs") {
            version("testcontainers", testContainersVersion)
            version("liquibaseSlf4j", liquibaseSlf4jVersion)
            version("springFox", springFoxVersion)

            alias("testcontainers-junit-jupiter")
                .to("org.testcontainers", "junit-jupiter")
                .versionRef("testcontainers")
            alias("testcontainers-postgresql")
                .to("org.testcontainers", "postgresql")
                .versionRef("testcontainers")
            alias("liquibase-slf4j")
                .to("com.mattbertolini", "liquibase-slf4j")
                .versionRef("liquibaseSlf4j")
            alias("springfox-boot-starter")
                .to("io.springfox", "springfox-boot-starter")
                .versionRef("springFox")
            alias("springfox-bean-validators")
                .to("io.springfox", "springfox-bean-validators")
                .versionRef("springFox")
            alias("springfox-data-rest")
                .to("io.springfox", "springfox-data-rest")
                .versionRef("springFox")
            alias("springfox-swagger-ui")
                .to("io.springfox", "springfox-swagger-ui")
                .versionRef("springFox")
        }
    }
}

rootProject.name = "online-shop"

include("core")