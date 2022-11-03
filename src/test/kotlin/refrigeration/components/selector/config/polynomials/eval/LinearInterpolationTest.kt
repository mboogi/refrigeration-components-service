package refrigeration.components.selector.config.polynomials.eval

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import refrigeration.components.selector.config.polynomials.db.PolynomialCoefficientsEntity
import java.math.BigDecimal
import java.math.RoundingMode

internal class LinearInterpolationTest {

    @Test
    fun `linear interpolation should be at half of the interval`() {
        val linearInterpolation = LinearInterpolation()
        val x0 = BigDecimal(0.00)
        val x1 = BigDecimal(4.0)
        val y0 = BigDecimal(0.0)
        val y1 = BigDecimal(4.0)
        val x = BigDecimal(2.00)
        val result = linearInterpolation.linearInterpolation(y0, x0, y1, x1, x)
        Assertions.assertEquals(BigDecimal(2.00), result)
    }

    @Test
    fun `linear interpolation should return highest value when y0 is null`() {
        val linearInterpolation = LinearInterpolation()
        val x0 = BigDecimal(0.00)
        val x1 = BigDecimal(4.0)
        val y0 = null
        val y1 = BigDecimal(4.0)
        val x = BigDecimal(2.00)
        val result = linearInterpolation.linearInterpolation(y0, x0, y1, x1, x)
        Assertions.assertEquals(BigDecimal(4.00), result)
    }

    @Test
    fun `linear interpolation should return lowest value when y1 is null`() {
        val linearInterpolation = LinearInterpolation()
        val x0 = BigDecimal(0.00)
        val x1 = BigDecimal(4.0)
        val y0 = BigDecimal(0.0)
        val y1 = null
        val x = BigDecimal(2.00)
        val result = linearInterpolation.linearInterpolation(y0, x0, y1, x1, x)
        Assertions.assertEquals(BigDecimal(0.00), result)
    }

    @Test
    fun `linear interpolation should return greater value of both when x0 equals x1`() {
        val linearInterpolation = LinearInterpolation()
        val x0 = BigDecimal(4.00)
        val x1 = BigDecimal(4.00)
        val y0 = BigDecimal(0.0)
        val y1 = BigDecimal(4.0)
        val x = BigDecimal(2.00)
        val result = linearInterpolation.linearInterpolation(y0, x0, y1, x1, x)
        Assertions.assertEquals(BigDecimal(4.0), result)
    }

    @Test
    fun `evaluate should calculate mass flow of 617 16 kg per h for known polynomial `() {
        val linearInterpolation = LinearInterpolation()
        val entity = PolynomialCoefficientsEntity(
            0L,
            "4GE-30Y capacity 100, frequency 50",
            1128.11737402613,
            49.7300426924752,
            -3.0430479647507,
            0.852244242295651,
            -0.156959299294807,
            0.012391136545977,
            0.006032498889193,
            -0.002713806319848,
            0.000347801277134,
            -0.000355284702052
        )

        val result = linearInterpolation.evaluate(BigDecimal(-10.0), BigDecimal(45.0), entity)
        Assertions.assertEquals(
            BigDecimal(617.17).setScale(2, RoundingMode.HALF_DOWN),
            result.setScale(2, RoundingMode.HALF_DOWN)
        )
    }
}
