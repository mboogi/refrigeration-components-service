package refrigeration.components.selector.components.tmp

import java.util.function.Function

class Multiplier:Function<Double,Double> {
    override fun apply(t: Double): Double {
        return t*2
    }
}