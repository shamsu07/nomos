plugins {
    `java-library`
    id("io.spring.dependency-management")
    id("org.springframework.boot") version "3.2.5" apply false
}

// This tells Spring Boot what versions to use
dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    // This is how we include our core engine
    api(project(":nomos-core"))

    // Spring Boot dependencies
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.boot:spring-boot-configuration-processor") // For @ConfigurationProperties

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
