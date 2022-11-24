package refrigeration.components.selector.util

import reactor.core.publisher.Mono
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.polynomials.*
import java.math.BigDecimal
import java.math.RoundingMode

class PolynomialCoefficientRequestUtil

fun getCompressorMapping(
    request: PolynomialCoefficientRequest,
    polynomialId: Long
): CompressorPolynomialMapping {
    return CompressorPolynomialMapping(polynomialId, request.compressorType, polynomialId)
}

fun getRefrigerantMapping(
    request: PolynomialCoefficientRequest,
    polynomialId: Long
): RefrigerantPolynomialMapping {
    return RefrigerantPolynomialMapping(polynomialId, request.refrigerantType, polynomialId)
}

fun getCapacityMapping(
    request: PolynomialCoefficientRequest,
    polynomialId: Long
): CapacityPolynomialMapping {
    return CapacityPolynomialMapping(polynomialId, request.capacity, polynomialId)
}

fun getFrequencyMapping(
    request: PolynomialCoefficientRequest,
    polynomialId: Long
): FrequencyPolynomialMapping {
    return FrequencyPolynomialMapping(polynomialId, request.frequency, polynomialId)
}

fun getPolynomialTypeMapping(
    request: PolynomialCoefficientRequest,
    polynomialId: Long
): PolynomialTypeMapping {
    return PolynomialTypeMapping(polynomialId, request.polynomialType, polynomialId)
}

fun getOperationTypeMapping(
    request: PolynomialCoefficientRequest,
    polynomialId: Long
): OperationTypePolynomialMapping {
    return OperationTypePolynomialMapping(polynomialId, request.transCritical, polynomialId)
}

/** checks whether Int is lesser than number */
infix fun Int.lt(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this < value
    if (value is Double) return this < value
    if (value is Float) return this < value
    if (value is Int) return this < value
    return false
}

/** checks whether Int is lesser or equal than number */
infix fun Int.lte(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this <= value
    if (value is Double) return this <= value
    if (value is Float) return this <= value
    if (value is Int) return this <= value
    return false
}

/** checks whether Int is greater than number */
infix fun Int.gt(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this > value
    if (value is Double) return this > value
    if (value is Float) return this > value
    if (value is Int) return this > value
    return false
}

/** checks whether Int is greater  or equal than number */
infix fun Int.gte(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this >= value
    if (value is Double) return this >= value
    if (value is Float) return this >= value
    if (value is Int) return this >= value
    return false
}

/** checks whether Int is  equal than number */
infix fun Int.eq(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this == value
    if (value is Double) return this == value
    if (value is Float) return this == value
    if (value is Int) return this == value
    return false
}
// --------------------------------------------

/** checks whether Int is lesser than number */
infix fun Double.lt(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this < value
    if (value is Double) return this < value
    if (value is Float) return this < value
    if (value is Int) return this < value
    return false
}

/** checks whether Int is lesser or equal than number */
infix fun Double.lte(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this <= value
    if (value is Double) return this <= value
    if (value is Float) return this <= value
    if (value is Int) return this <= value
    return false
}

/** checks whether Int is greater than number */
infix fun Double.gt(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this > value
    if (value is Double) return this > value
    if (value is Float) return this > value
    if (value is Int) return this > value
    return false
}

/** checks whether Int is greater  or equal than number */
infix fun Double.gte(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this >= value
    if (value is Double) return this >= value
    if (value is Float) return this >= value
    if (value is Int) return this >= value
    return false
}

/** checks whether Int is  equal than number */
infix fun Double.eq(value: Number?): Boolean {
    value ?: return false
    if (value is Short) return this == value
    if (value is Double) return this == value
    if (value is Float) return this == value
    if (value is Int) return this == value
    return false
}

fun getStandardScale(): ScalePair {
    return ScalePair(
        20,
        RoundingMode.HALF_UP,
        "Polynomial coefficients are approximate values and greater fixed position is required"
    )
}

fun getBigDecimalFromNullable(value: Double?): BigDecimal? {
    return if (value != null) {
        val scale = getStandardScale()
        BigDecimal(value).setScale(scale.scale, scale.roundingMode)
    } else {
        null
    }
}

fun getBigDecimalMonoFromNullable(value: Double?): Mono<BigDecimal> {
    return if (value != null) {
        val scale = getStandardScale()
        val result = BigDecimal(value).setScale(scale.scale, scale.roundingMode)
        Mono.just(result)
    } else {
        Mono.empty()
    }
}

fun getBigDecimalFromDouble(value: Double, scale: ScalePair): BigDecimal {
    return BigDecimal(value).setScale(scale.scale, scale.roundingMode)
}

fun getInputForRequiredKeys(values: Map<String, Any>, requiredKeys: Set<String>): EvaluationInput {
    val inputMap = requiredKeys
        .associateWith { getValueForKey<Any>(values, it) }
        .filterValues { it != null }
        .mapValues { it.value as Any }
    return EvaluationInput("EvaporatorEvaluation", inputMap)
}
