package jp.kaleidot725.pulse.demo.count.content.area

import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaPosition
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaState
import jp.kaleidot725.pulse.mvi.PulseContent

@Composable
fun PulseAreaContent(
    viewModel: PulseAreaViewModel,
    onCharged: (PulseAreaEvent.Charged) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFlashing by remember { mutableStateOf(false) }

    PulseContent(
        viewModel = viewModel,
        onEvent = { event ->
            when (event) {
                PulseAreaEvent.Pulsed -> isFlashing = true
                is PulseAreaEvent.Charged -> onCharged(event)
            }
        },
    ) { state, onAction ->
        PulseAreaCell(
            state = state,
            isFlashing = isFlashing,
            onFlashFinished = { isFlashing = false },
            onPulse = { onAction(PulseAreaAction.Pulse) },
            modifier = modifier,
        )
    }
}

@Composable
private fun PulseAreaCell(
    state: PulseAreaState,
    isFlashing: Boolean,
    onFlashFinished: () -> Unit,
    onPulse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val flash = remember { Animatable(0f) }
    LaunchedEffect(isFlashing) {
        if (!isFlashing) return@LaunchedEffect
        flash.snapTo(1f)
        flash.animateTo(targetValue = 0f, animationSpec = tween(FLASH_MILLIS, easing = LinearOutSlowInEasing))
        onFlashFinished()
    }

    val hue = state.position.hue
    val charge = (state.count.toFloat() / CHARGE_FULL).coerceIn(0f, 1f)
    val resting = Color.hsl(hue, 0.30f + 0.45f * charge, 0.90f - 0.45f * charge)
    val fill = lerp(resting, Color.hsl(hue, 1f, 0.96f), flash.value)
    val ink = if (charge > 0.55f) Color.White else Color.hsl(hue, 0.85f, 0.18f)

    Column(
        modifier =
            modifier
                .padding(6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(fill)
                .border(
                    width = (1 + 5 * flash.value).dp,
                    color = Color.hsl(hue, 0.80f, 0.45f).copy(alpha = 0.25f + 0.75f * flash.value),
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
