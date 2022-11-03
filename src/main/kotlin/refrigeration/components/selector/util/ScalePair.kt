package refrigeration.components.selector.util

import java.math.RoundingMode

data class ScalePair(
    val scale: Int,
    val roundingMode: RoundingMode,
    val description: String
)
