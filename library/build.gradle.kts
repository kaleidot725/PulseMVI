plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.library")
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
    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.compose.runtime:runtime:1.10.1")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }
    }
}

android {
    namespace = "jp.kaleidot725.pulse.mvi"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = artifactId.replace(project.name, "pulsemvi")

        pom {
            name.set("PulseMVI")
            description.set("A Kotlin MVI library for Compose Multiplatform")
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

afterEvaluate {
    publishing.publications.named<MavenPublication>("androidRelease") {
        artifactId = "pulsemvi-android"
    }
}
