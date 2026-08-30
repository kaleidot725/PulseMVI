package jp.kaleidot725.pulse.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import jp.kaleidot725.pulse.demo.counter.detail.screen.CounterDetailScreen
import jp.kaleidot725.pulse.demo.counter.screen.CounterScreen
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseNavEntryDecorators

private sealed interface DemoRoute : NavKey {
    data object Counter : DemoRoute

    data class CounterDetails(
        val count: Int,
    ) : DemoRoute
}

/**
 * Keeps the back stack across configuration changes. `DemoRoute` is small enough that a hand
 * written saver is cheaper than pulling in kotlinx-serialization for `rememberNavBackStack`.
 */
private val DemoRouteBackStackSaver: Saver<SnapshotStateList<DemoRoute>, Any> =
    listSaver(
        save = { backStack ->
            backStack.map { route ->
                when (route) {
                    DemoRoute.Counter -> "counter"
                    is DemoRoute.CounterDetails -> "counter-details:${route.count}"
                }
            }
        },
        restore = { saved ->
            val routes =
                saved.map { token ->
                    when {
                        token == "counter" -> DemoRoute.Counter
                        else -> DemoRoute.CounterDetails(token.substringAfter(':').toInt())
                    }
                }
            mutableStateListOf(*routes.toTypedArray())
        },
    )

/**
 * Hosts the back stack only. Each destination builds its own `PulseHost` with its own Container and
 * Stores, and [rememberPulseNavEntryDecorators] scopes them to the entry: a Store exists
 * while its route is on the back stack and is cancelled when the route is popped.
 */
@Composable
fun DemoApp() {
    val backStack = rememberSaveable(saver = DemoRouteBackStackSaver) { mutableStateListOf<DemoRoute>(DemoRoute.Counter) }
    val showCounterDetails: (Int) -> Unit = { count ->
        backStack.add(DemoRoute.CounterDetails(count))
    }
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
                    entry<DemoRoute.Counter> {
                        CounterScreen(onShowCounterDetails = showCounterDetails)
                    }
                    entry<DemoRoute.CounterDetails> { route ->
                        CounterDetailScreen(
                            count = route.count,
                            onBack = popLast,
                        )
                    }
                },
        )
    }
}

@Composable
internal fun DemoPage(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}
