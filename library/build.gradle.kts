plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    `maven-publish`
}

group = "com.github.kaleidot725"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    explicitApi()
    jvm()
    jvmToolchain(17)

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])

            groupId = "com.github.kaleidot725"
            artifactId = "doma"

            pom {
                name.set("Doma")
                description.set("A Kotlin MVI library for Compose Multiplatform")
                url.set("https://github.com/kaleidot725/Doma")

                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("kaleidot725")
                        name.set("kaleidot725")
                    }
                }
            }
        }
    }
}
