package jp.kaleidot725.pulse.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import jp.kaleidot725.pulse.demo.count.PulseCountHost
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseNavEntryDecorators

private data class PulseCountRoute(
    val depth: Int,
) : NavKey

private val PulseCountBackStackSaver: Saver<SnapshotStateList<PulseCountRoute>, Any> =
    listSaver(
        save = { backStack -> backStack.map { it.depth } },
        restore = { saved ->
            mutableStateListOf(*saved.map { PulseCountRoute(it) }.toTypedArray())
        },
    )

@Composable
fun DemoApp() {
    val backStack = rememberSaveable(saver = PulseCountBackStackSaver) { mutableStateListOf(PulseCountRoute(depth = 1)) }
    val popLast: () -> Unit = {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    MaterialTheme {
        NavDisplay(
            backStack = backStack,
            onBack = popLast,
            entryDecorators = rememberPulseNavEntryDecorators(),
            entryProvider =
                entryProvider {
                    entry<PulseCountRoute> { route ->
                        PulseCountHost(
                            depth = route.depth,
                            onNewArea = { backStack.add(PulseCountRoute(route.depth + 1)) },
                            onBack = popLast.takeIf { route.depth > 1 },
                        )
                    }
                },
        )
    }
}
