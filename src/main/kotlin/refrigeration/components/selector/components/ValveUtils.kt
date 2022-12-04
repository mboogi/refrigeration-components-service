package refrigeration.components.selector.components

import kotlin.math.pow

class ValveUtils {

    fun pressureDrop(volumeFlow: Double, kvs: Double): Double {
        return (volumeFlow / kvs).pow(2.0)
    }
}
