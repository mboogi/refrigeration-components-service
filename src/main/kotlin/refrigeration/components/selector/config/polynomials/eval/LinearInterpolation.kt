package refrigeration.components.selector.config.polynomials.eval

import refrigeration.components.selector.config.polynomials.db.PolynomialCoefficientsEntity
import java.math.BigDecimal
import java.math.RoundingMode

class LinearInterpolation {

    /** Simple linear interpolation returns Long.MaxValue in case that interpolation is not possible*/
    fun linearInterpolation(
        y0: BigDecimal?,
        x0: BigDecimal?,
        y1: BigDecimal?,
        x1: BigDecimal?,
        x: BigDecimal?
    ): BigDecimal {
        val areInputsComplete = (y0 != null) and (y1 != null) and (x0 != null) and (x1 != null) and (x != null)
        if (areInputsComplete) return localLinearInterpolation(y0!!, x0!!, y1!!, x1!!, x!!)

        if ((y0 == null).and(y1 != null)) return y1!!
        if ((y1 == null).and(y0 != null)) return y0!!

        if ((y1 != null) and (y0 != null)) {
            if (y1!!.equals(y0)) return y0!!
        }
        if (x1 != null) {
            val result = (x1.equals(x0))
        }

        return BigDecimal(Long.MAX_VALUE)
    }

    private fun checkLimits(): Boolean {
        return false
    }

    private fun localLinearInterpolation(
        y0: BigDecimal,
        x0: BigDecimal,
        y1: BigDecimal,
        x1: BigDecimal,
        x: BigDecimal
    ): BigDecimal {
        if (y1.equals(x0)) return y1
        val x1MinusX0 = x1.subtract(x0)
        if (x1MinusX0.compareTo(BigDecimal.ZERO) == 0) return y1

        val xMinusX0 = x.subtract(x0)
        val y1MinY0 = y1.subtract(y0)

        if (y1MinY0.compareTo(BigDecimal.ZERO) == 0) return y1

        val xIntervalDivYInterval = y1MinY0.divide(x1MinusX0)
        val xDistanceMultipliedIntervals = xMinusX0.multiply(xIntervalDivYInterval)

        val y = y0.plus(xDistanceMultipliedIntervals)
        return y
    }

    fun evaluate(
        evaporationTemperature: BigDecimal,
        condensingTemperature: BigDecimal,
        request: PolynomialCoefficientsEntity
    ): BigDecimal {
        val to = evaporationTemperature.setScale(2, RoundingMode.HALF_UP)
        val tc = condensingTemperature.setScale(2, RoundingMode.HALF_UP)
        val c1 = BigDecimal(request.c1).setScale(15, RoundingMode.HALF_UP)
        val c2 = BigDecimal(request.c2).setScale(15, RoundingMode.HALF_UP)
        val c3 = BigDecimal(request.c3).setScale(15, RoundingMode.HALF_UP)
        val c4 = BigDecimal(request.c4).setScale(15, RoundingMode.HALF_UP)
        val c5 = BigDecimal(request.c5).setScale(15, RoundingMode.HALF_UP)
        val c6 = BigDecimal(request.c6).setScale(15, RoundingMode.HALF_UP)
        val c7 = BigDecimal(request.c7).setScale(15, RoundingMode.HALF_UP)
        val c8 = BigDecimal(request.c8).setScale(15, RoundingMode.HALF_UP)
        val c9 = BigDecimal(request.c9).setScale(15, RoundingMode.HALF_UP)
        val c10 = BigDecimal(request.c10).setScale(15, RoundingMode.HALF_UP)
        val c2to = c2.multiply(to)
        val c3tc = c3.multiply(tc)
        val c4toPow2 = to.pow(2).multiply(c4)
        val c5toTc = c5.multiply(to).multiply(tc)
        val c6tcPow2 = tc.pow(2).multiply(c6)
        val c7toPow3 = to.pow(3).multiply(c7)
        val c8tcT0Pow2 = to.pow(2).multiply(tc).multiply(c8)
        val c9toTcPow2 = tc.pow(2).multiply(to).multiply(c9)
        val c10TcPow3 = tc.pow(3).multiply(c10)

        return c1.add(c2to).add(c3tc).add(c4toPow2).add(c5toTc).add(c6tcPow2).add(c7toPow3)
            .add(c8tcT0Pow2).add(c9toTcPow2).add(c10TcPow3)
    }
}
