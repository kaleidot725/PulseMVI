package jp.kaleidot725.pulse.demo.count.content.area

enum class PulseAreaPosition(
    val label: String,
) {
    TopLeft("Top Left"),
    TopRight("Top Right"),
    BottomLeft("Bottom Left"),
    BottomRight("Bottom Right"),
    ;

    val neighbors: Set<PulseAreaPosition>
        get() =
            when (this) {
                TopLeft -> setOf(TopRight, BottomLeft)
                TopRight -> setOf(TopLeft, BottomRight)
                BottomLeft -> setOf(TopLeft, BottomRight)
                BottomRight -> setOf(TopRight, BottomLeft)
            }
}
