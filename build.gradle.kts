plugins {
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.diffplug.spotless")

    group = "io.github.shamsu07.nomos"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.18.1")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
