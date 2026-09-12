plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
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
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
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
