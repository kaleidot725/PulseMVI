package jp.kaleidot725.pulse.demo.count.content.area

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaAction
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaEvent
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaState
import jp.kaleidot725.pulse.mvi.PulseContent
import kotlinx.coroutines.launch

@Composable
fun PulseAreaContent(
    viewModel: PulseAreaViewModel,
    onCharged: (PulseAreaEvent.Charged) -> Unit,
    modifier: Modifier = Modifier,
) {
    val flash = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    PulseContent(
        viewModel = viewModel,
        onEvent = { event ->
            when (event) {
                is PulseAreaEvent.Pulsed -> {
                    val strength = if (event.origin == viewModel.currentState.position) 1f else 0.55f
                    coroutineScope.launch { flash.flash(strength) }
                }
                is PulseAreaEvent.Charged -> onCharged(event)
            }
        },
    ) { state, onAction ->
        PulseAreaCell(
            state = state,
            flash = flash.value,
            onPulse = { onAction(PulseAreaAction.Pulse) },
            modifier = modifier,
        )
    }
}

private suspend fun Animatable<Float, AnimationVector1D>.flash(strength: Float) {
    snapTo(strength)
    animateTo(targetValue = 0f, animationSpec = tween(FLASH_MILLIS, easing = LinearOutSlowInEasing))
}

@Composable
private fun PulseAreaCell(
    state: PulseAreaState,
    flash: Float,
    onPulse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hue = state.position.hue
    val charge = (state.count.toFloat() / CHARGE_FULL).coerceIn(0f, 1f)
    val resting = Color.hsl(hue, 0.30f + 0.45f * charge, 0.90f - 0.45f * charge)
    val fill = lerp(resting, Color.hsl(hue, 1f, 0.96f), flash)
    val ink = if (charge > 0.55f) Color.White else Color.hsl(hue, 0.85f, 0.18f)

    Column(
        modifier =
            modifier
                .padding(6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(fill)
                .border(
                    width = (1 + 5 * flash).dp,
                    color = Color.hsl(hue, 0.80f, 0.45f).copy(alpha = 0.25f + 0.75f * flash),
                    shape = RoundedCornerShape(20.dp),
                ).clickable(onClick = onPulse)
                .testTag("area-${state.position.name}")
                .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = state.position.label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = ink.copy(alpha = 0.75f),
        )
        Text(
            text = state.count.toString(),
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = ink,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("count-${state.position.name}"),
        )
        Text(
            text = "setup ${state.setupCount}",
            fontSize = 11.sp,
            color = ink.copy(alpha = 0.65f),
            modifier = Modifier.testTag("caption-${state.position.name}"),
        )
    }
}

private val PulseAreaPosition.hue: Float
    get() =
        when (this) {
            PulseAreaPosition.TopLeft -> 196f
            PulseAreaPosition.TopRight -> 274f
            PulseAreaPosition.BottomLeft -> 158f
            PulseAreaPosition.BottomRight -> 336f
        }

private const val FLASH_MILLIS = 520
private const val CHARGE_FULL = 16f
