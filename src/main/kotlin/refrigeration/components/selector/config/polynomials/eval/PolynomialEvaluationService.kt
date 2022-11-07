package refrigeration.components.selector.config.polynomials.eval

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.config.polynomials.db.PolynomialCoefficientsEntity
import refrigeration.components.selector.config.polynomials.db.PolynomialSearchResult
import refrigeration.components.selector.config.polynomials.search.PolynomialCoefficientsService
import refrigeration.components.selector.config.polynomials.search.PolynomialSearchService
import refrigeration.components.selector.util.*
import java.math.BigDecimal

class PolynomialEvaluationService(
    private val purposeName: String,
    private val service: PolynomialSearchService,
    private val coefficientsService: PolynomialCoefficientsService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PolynomialCoefficientsService::class.java)
    }

    private val interpolation = LinearInterpolation()

    fun privateEvaluation(): Boolean {
        return true
    }

    fun getName(): String {
        return "PolynomialEvaluator"
    }

    fun getRequiredInputKeys(): Set<String> {
        return setOf(
            ComponentsConfig.refrigerantKey,
            ComponentsConfig.evapTempKey,
            ComponentsConfig.condTempKey,
            ComponentsConfig.capacity,
            ComponentsConfig.frequency,
            ComponentsConfig.transCritical
        )
    }

    fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        val result = input.map { evaluate(it) }
        return Flux.concat(result)
    }

    private fun errorEvalResult(text: String, input: EvaluationInput): EvalResult {
        return EvalResult(EvalResultInfo.FAILURE, input, mapOf(), mapOf(), text)
    }

    private fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val evalResult = EvalResult(EvalResultInfo.FAILURE, input, mapOf(), mapOf(), "")

        val refrigerantErrorMsg = "refrigerant could not be found"
        val refrigerant = getRefrigerant(input) ?: return getMonoError(refrigerantErrorMsg, input)

        val evapTempErrorMsg = "evap temp could not be found"
        val evapTemp = getEvaporationTemperature(input) ?: return getMonoError(evapTempErrorMsg, input)

        val condTempErrorMsg = "cond temp could not be found"
        val condensingTemp = getCondensingTemperature(input) ?: return getMonoError(condTempErrorMsg, input)

        val capacityErrorMsg = "capacity could not be found"
        val capacity = getCapacity(input) ?: return getMonoError(capacityErrorMsg, input)

        val frequencyErrorMsg = "frequency could not be found"
        val frequency = getFrequency(input) ?: return getMonoError(frequencyErrorMsg, input)

        val transCriticalErrorMsg = "transcricital operation type could not be found"
        val transCritical = getTransCritical(input) ?: return getMonoError(transCriticalErrorMsg, input)

        val compressorTypeErrorMsg = "compressor type could not be found"
        val compressorType = getCompressorType(input) ?: return getMonoError(compressorTypeErrorMsg, input)

        val polynomialGroup =
            service.getPolynomialGroups(compressorType, refrigerant, capacity, frequency, transCritical, purposeName)
        logger.info("polynomial group: $polynomialGroup")
        if (polynomialGroup.isEmpty) return return return Mono.just(
            errorEvalResult(
                "polynomials could not be found",
                input
            )
        )
        val ids = mutableListOf<Long>()

        val first = polynomialGroup.lowCapacityLowFrequency.polynomial
        val second = polynomialGroup.lowCapacityHighFrequency.polynomial
        val third = polynomialGroup.highCapacityLowFrequency.polynomial
        val fourth = polynomialGroup.highCapacityHighFrequency.polynomial

        if (first != null) ids.add(first.polynomialId)
        if (second != null) ids.add(second.polynomialId)
        if (third != null) ids.add(third.polynomialId)
        if (fourth != null) ids.add(fourth.polynomialId)

// cache this thing

        val mappings = coefficientsService
            .findPolynomialMappingByIdIn(ids.toList())
            .publishOn(Schedulers.boundedElastic())
            .collectList()
            .toFuture().get() ?: return Mono.just(evalResult)
        logger.info("polynomial coefficients $mappings")

        val firstResult = calculate(first, mappings, evapTemp, condensingTemp)
        val secondResult = calculate(second, mappings, evapTemp, condensingTemp)
        val thirdResult = calculate(third, mappings, evapTemp, condensingTemp)
        val fourthResult = calculate(fourth, mappings, evapTemp, condensingTemp)

        val lowCapacity = getBigDecimalFromNullable(polynomialGroup.lowCapacity)
        val highCapacity = getBigDecimalFromNullable(polynomialGroup.highCapacity)
        val capacityScaled = getBigDecimalFromNullable(capacity)

        val lowFrequency = getBigDecimalFromNullable(polynomialGroup.lowFrequency)
        val highFrequency = getBigDecimalFromNullable(polynomialGroup.highFrequency)
        val frequencyScaled = getBigDecimalFromNullable(frequency)

        val lowFrequencyInterpolation =
            interpolation.linearInterpolation(firstResult, lowCapacity, thirdResult, highCapacity, capacityScaled)
        val highCapacityInterpolation =
            interpolation.linearInterpolation(secondResult, lowCapacity, fourthResult, highCapacity, capacityScaled)

        val result = interpolation.linearInterpolation(
            lowFrequencyInterpolation,
            lowFrequency,
            highCapacityInterpolation,
            highFrequency,
            frequencyScaled
        )

        if (result.equals(Long.MAX_VALUE)) return Mono.just(errorEvalResult("value calculated out of scope", input))
        val resultsMap = mapOf(ComponentsConfig.polynomialEvaluationValue to result.toDouble())
        val resultsMapping = mapOf(ComponentsConfig.polynomialEvaluationValue to Double::class)

        val successResult =
            EvalResult(EvalResultInfo.SUCCESS, input, resultsMap, resultsMapping, "successfully evaluated $purposeName")
        return Mono.just(successResult)
    }

    private fun calculate(
        polynomial: PolynomialSearchResult?,
        mappings: List<PolynomialCoefficientsEntity>,
        evapTemp: Double,
        condTemp: Double
    ): BigDecimal? {
        if (polynomial != null) {
            val standardScale = getStandardScale()
            val firstMapping = mappings.filter { it.id == polynomial.polynomialId }.firstOrNull() ?: return null
            val evapTempBigDecimal = getBigDecimalFromDouble(evapTemp, standardScale)
            val condTempBigDecimal = getBigDecimalFromDouble(condTemp, standardScale)
            return interpolation.evaluate(evapTempBigDecimal, condTempBigDecimal, firstMapping)
        }
        return null
    }
}
