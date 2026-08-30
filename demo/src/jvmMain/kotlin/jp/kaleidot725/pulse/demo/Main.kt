package jp.kaleidot725.pulse.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "PulseMVI - Counter Demo",
        ) {
            DemoApp()
        }
    }
