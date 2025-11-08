plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management")
    java
}

dependencies {
    implementation(project(":nomos-spring-boot-starter"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}