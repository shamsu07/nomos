pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.diffplug.spotless") version "6.25.0"
        id("io.spring.dependency-management") version "1.1.5"
        id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    }
}

rootProject.name = "nomos"

include(
    "nomos-core",
    "nomos-spring-boot-starter",
    "nomos-example"
)
