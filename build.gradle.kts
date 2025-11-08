plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
}
group = "io.github.shamsu07"
version = "0.0.1"

allprojects {
    group = rootProject.group
    version = rootProject.version
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
        withSourcesJar()
        withJavadocJar()
    }
    tasks.withType<Test> { useJUnitPlatform() }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.18.1")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    tasks.withType<Javadoc> {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    val isPublishable = name != "nomos-example"

    if (isPublishable) {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    pom {
                        name.set(project.name)
                        description.set("Lightweight, high-performance rule engine for Java")
                        url.set("https://github.com/shamsu07/nomos")

                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("shamsu07")
                                name.set("Shamsuddeen K S")
                                email.set("shamsuddeenks@gmail.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:https://github.com/shamsu07/nomos.git")
                            developerConnection.set("scm:git:ssh://github.com/shamsu07/nomos.git")
                            url.set("https://github.com/shamsu07/nomos")
                        }
                    }
                }
            }
        }

        extensions.configure<SigningExtension> {
            val publishing = extensions.getByType<PublishingExtension>()
            sign(publishing.publications["mavenJava"])
            setRequired { !version.toString().endsWith("SNAPSHOT") }
        }
    } else {
        tasks.matching { it.name.startsWith("publish") }.configureEach { enabled = false }
    }
}

// ---- NEW CENTRAL PORTAL CONFIG ----
nexusPublishing {
    repositories {
        sonatype {
            // Portal OSSRH Staging API endpoints
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            
            username.set(findProperty("sonatypeUsername") as String? ?: System.getenv("SONATYPE_USERNAME"))
            password.set(findProperty("sonatypePassword") as String? ?: System.getenv("SONATYPE_PASSWORD"))
        }
    }
}