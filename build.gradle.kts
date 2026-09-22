import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    kotlin("multiplatform") version "2.3.10" apply false
    kotlin("plugin.compose") version "2.3.10" apply false
    id("org.jetbrains.compose") version "1.10.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.9"
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.3.1")
        android.set(false)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
        filter {
            exclude("**/build/**")
        }
    }
}

dependencies {
    kover(project(":library"))
    kover(project(":navigation3"))
}

kover {
    reports {
        total {
            html { onCheck = false }
            xml { onCheck = false }

            verify {
                onCheck = true

                rule {
                    minBound(95, CoverageUnit.LINE)
                    minBound(95, CoverageUnit.INSTRUCTION)
                }
            }
        }
    }
}
