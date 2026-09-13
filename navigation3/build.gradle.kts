plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    `maven-publish`
}

// JitPack publishes multi-module builds under com.github.<user>.<repo> and versions them by tag.
// Matching both here keeps the inter-module dependency in the navigation3 POM resolvable.
group = "com.github.kaleidot725.PulseMVI"
version = System.getenv("VERSION") ?: "1.0.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    explicitApi()
    jvm()
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":library"))
                api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
                api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0")
                api("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = artifactId.replace(project.name, "pulsemvi-navigation3")

        pom {
            name.set("PulseMVI Navigation 3")
            description.set("Navigation 3 owned PulseViewModel and PulseContainer lifetimes for PulseMVI")
            url.set("https://github.com/kaleidot725/PulseMVI")

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
