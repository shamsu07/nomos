plugins {
    `java-library` apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
}

// Configure all subprojects
subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish") // Good to add for later

    group = "com.yourcompany.nomos" // Change this to your group ID
    version = "0.0.1-SNAPSHOT"

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
    }

    // Configure testing
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}