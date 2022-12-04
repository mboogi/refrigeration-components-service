package refrigeration.components.selector.components

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ValveUtilsTest {

    @Test
    fun pressureDrop() {
        val valveUtil = ValveUtils()
        val result = valveUtil.pressureDrop(6.0, 2.0)
        assertEquals(9.0, result)
    }
}
