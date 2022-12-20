package refrigeration.components.selector.components

import refrigeration.components.selector.config.pipes.db.PipeEntity
import kotlin.math.pow
import kotlin.math.sqrt

class PipeUtilities {
    fun sizePipe(maxVelocity: Double, volumeFlow: Double): Double {
        val flowRate = volumeFlow * 4
        val velocity = maxVelocity * Math.PI * 3600
        val divided = flowRate / velocity
        return sqrt(divided) * 1000
    }

    fun calculateRealVelocity(volumeFlow: Double, innerDiameter: Double): Double {
        val flow = 4 * volumeFlow
        val diameter = 3600 * Math.PI * innerDiameter.pow(2)
        return flow / diameter
    }

    fun findBestMatch(requiredDiameter: Double, pipes: List<PipeEntity>): PipeEntity? {
        return pipes.sortedBy { it.innerDiameter }.firstOrNull { it.innerDiameter >= requiredDiameter }
            ?: return pipes.sortedBy { it.innerDiameter }.lastOrNull { it.innerDiameter <= requiredDiameter }
    }
}
