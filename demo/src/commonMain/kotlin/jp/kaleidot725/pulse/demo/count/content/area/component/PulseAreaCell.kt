package jp.kaleidot725.pulse.demo.count.content.area.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaPosition
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaState

@Composable
fun PulseAreaCell(
    state: PulseAreaState,
    onPulse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hue = state.position.hue
    val charge = chargeOf(state.count)
    val fill = fillColor(hue = hue, charge = charge)
    val ink = inkColor(hue = hue, charge = charge)

    Column(
        modifier =
            modifier
                .padding(6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(fill)
                .border(
                    width = 1.dp,
                    color = borderColor(hue = hue),
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

@Preview
@Composable
private fun PulseAreaCellPreview() {
    PulseAreaCell(
        state = PulseAreaState(position = PulseAreaPosition.TopRight, count = 7),
        onPulse = {},
        modifier = Modifier.size(width = 320.dp, height = 220.dp),
    )
}

private val PulseAreaPosition.hue: Float
    get() =
        when (this) {
            PulseAreaPosition.TopLeft -> 196f
            PulseAreaPosition.TopRight -> 274f
            PulseAreaPosition.BottomLeft -> 158f
            PulseAreaPosition.BottomRight -> 336f
        }

private fun chargeOf(count: Int): Float = (count.toFloat() / CHARGE_FULL).coerceIn(0f, 1f)

private fun fillColor(
    hue: Float,
    charge: Float,
): Color = Color.hsl(hue = hue, saturation = 0.30f + 0.45f * charge, lightness = 0.90f - 0.45f * charge)

private fun inkColor(
    hue: Float,
    charge: Float,
): Color = if (charge > 0.55f) Color.White else Color.hsl(hue = hue, saturation = 0.85f, lightness = 0.18f)

private fun borderColor(hue: Float): Color = Color.hsl(hue = hue, saturation = 0.80f, lightness = 0.45f).copy(alpha = 0.25f)

private const val CHARGE_FULL = 16f
