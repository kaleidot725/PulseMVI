plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":library"))
                implementation(project(":navigation3"))
                implementation("org.jetbrains.compose.foundation:foundation:1.10.1")
                implementation("org.jetbrains.compose.material3:material3:1.9.0")
                implementation("org.jetbrains.compose.runtime:runtime:1.10.1")
                implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.13.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.compose.ui:ui-test-junit4:1.10.1")
            }
        }
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "PulseMVIDemo"
            isStatic = true
        }
    }
}

android {
    namespace = "jp.kaleidot725.pulse.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "jp.kaleidot725.pulse.demo"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

compose.desktop {
    application {
        mainClass = "jp.kaleidot725.pulse.demo.MainKt"

        nativeDistributions {
            packageName = "PulseMVIDemo"
            packageVersion = "1.0.0"

            macOS {
                bundleID = "jp.kaleidot725.pulse.demo"
            }
        }
    }
}
