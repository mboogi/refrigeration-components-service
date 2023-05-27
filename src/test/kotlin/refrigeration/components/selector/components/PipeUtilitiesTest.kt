package refrigeration.components.selector.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PipeUtilitiesTest {

    @Test
    fun sizePipe() {
        val pipeUtilities = PipeUtilities()
        val result = pipeUtilities.sizePipe(4.7422, 2.48) * 1000
        val expected = 13600.0
        assertEquals(expected, result, 0.03)
    }

    @Test
    fun `get real velocity`() {
        val pipeUtilities = PipeUtilities()
        val innerDiameter = 13.601
        val volumeFlow = 2.48
        val realVelocity = pipeUtilities.calculateRealVelocity(volumeFlow, innerDiameter / 1000)
        assertEquals(4.7422, realVelocity, 0.01)
    }
}
