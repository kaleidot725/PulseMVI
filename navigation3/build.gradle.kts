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
    // navigation3-ui does not publish iosX64, so this module skips the Intel simulator target.
    iosArm64()
    iosSimulatorArm64()
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
        iosTest.dependencies {
            implementation("org.jetbrains.compose.ui:ui-test:1.10.1")
        }
    }
}

android {
    namespace = "jp.kaleidot725.pulse.mvi.navigation3"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = artifactId.replace(project.name, "pulsemvi-navigation3")

        pom {
            name.set("PulseMVI Navigation 3")
            description.set("Navigation 3 and ViewModel owned Store and Container lifetimes for PulseMVI")
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
        artifactId = "pulsemvi-navigation3-android"
    }
}
