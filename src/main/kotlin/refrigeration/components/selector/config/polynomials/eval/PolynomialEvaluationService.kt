package refrigeration.components.selector.config.polynomials.eval

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.ResultValues
import refrigeration.components.selector.config.polynomials.db.PolynomialCoefficientsEntity
import refrigeration.components.selector.config.polynomials.db.PolynomialSearchResult
import refrigeration.components.selector.config.polynomials.crud.PolynomialCoefficientsService
import refrigeration.components.selector.config.polynomials.crud.PolynomialGroups
import refrigeration.components.selector.config.polynomials.crud.PolynomialSearchService
import refrigeration.components.selector.util.*
import java.math.BigDecimal
import kotlin.reflect.KClass

class PolynomialEvaluationService(
    private val purposeName: String,
    private val service: PolynomialSearchService,
    private val coefficientsService: PolynomialCoefficientsService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PolynomialCoefficientsService::class.java)
    }

    private val interpolation = LinearInterpolation()

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
        val resultValues = ResultValues(purposeName, mapOf(), mapOf())
        return EvalResult(EvalResultInfo.FAILURE, input, resultValues, text)
    }

    private fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val refrigerantErrorMsg = "refrigerant could not be found"
        val refrigerant = getRefrigerant(input.anyInputs) ?: return getMonoError(refrigerantErrorMsg, input, purposeName)

        val evapTempErrorMsg = "evap temp could not be found"
        val evapTemp = getEvaporationTemperature(input.anyInputs) ?: return getMonoError(evapTempErrorMsg, input, purposeName)

        val condTempErrorMsg = "cond temp could not be found"
        val condensingTemp = getCondensingTemperature(input.anyInputs) ?: return getMonoError(condTempErrorMsg, input, purposeName)

        val capacityErrorMsg = "capacity could not be found"
        val capacity = getCapacity(input.anyInputs) ?: return getMonoError(capacityErrorMsg, input, purposeName)

        val frequencyErrorMsg = "frequency could not be found"
        val frequency = getFrequency(input.anyInputs) ?: return getMonoError(frequencyErrorMsg, input, purposeName)

        val transCriticalErrorMsg = "transcricital operation type could not be found"
        val transCritical = getTransCritical(input.anyInputs) ?: return getMonoError(transCriticalErrorMsg, input, purposeName)

        val compressorTypeErrorMsg = "compressor type could not be found"
        val compressorType = getCompressorType(input.anyInputs) ?: return getMonoError(compressorTypeErrorMsg, input, purposeName)

        val polynomialGroup =
            service.getPolynomialGroups(compressorType, refrigerant, capacity, frequency, transCritical, purposeName)
        logger.info("polynomial group: $polynomialGroup")

// cache this thing

        val mappings = polynomialGroup
            .map { ids(it) }
            .flatMap {
                coefficientsService.findPolynomialMappingByIdIn(it.polynomialIds)
                    .collectList()
                    .toMono()
                    .map { et -> Pair(it, et) }
            }
        logger.info("polynomial coefficients $mappings")

        val firstResult = mappings.flatMap { evaluateMono(it.first.first, it.second, evapTemp, condensingTemp) }
        val secondResult = mappings.flatMap { evaluateMono(it.first.second, it.second, evapTemp, condensingTemp) }
        val thirdResult = mappings.flatMap { evaluateMono(it.first.third, it.second, evapTemp, condensingTemp) }
        val fourthResult = mappings.flatMap { evaluateMono(it.first.fourth, it.second, evapTemp, condensingTemp) }

        val capacityFrequencyGroup = polynomialGroup
            .flatMap {
                val lowCapacity = getBigDecimalFromNullable(it.lowCapacity)
                val highCapacity = getBigDecimalFromNullable(it.highCapacity)
                val lowFrequency = getBigDecimalFromNullable(it.lowFrequency)
                val highFrequency = getBigDecimalFromNullable(it.highFrequency)
                Mono.just(CapacityFrequencyGroup(lowCapacity, highCapacity, lowFrequency, highFrequency))
            }

        val capacityScaled = getBigDecimalFromNullable(capacity)
        val frequencyScaled = getBigDecimalFromNullable(frequency)

        val result = Mono
            .zip(firstResult, secondResult, thirdResult, fourthResult, capacityFrequencyGroup)
            .flatMap { t ->
                val firstResult = t.t1
                val secondResult = t.t2
                val thirdResult = t.t3
                val fourthResult = t.t4
                val capFreqGroup = t.t5

                val lowFrequencyInterpolation =
                    interpolation.linearInterpolation(
                        firstResult,
                        capFreqGroup.lowCapacity,
                        thirdResult,
                        capFreqGroup.highCapacity,
                        capacityScaled
                    )
                val highCapacityInterpolation =
                    interpolation.linearInterpolation(
                        secondResult,
                        capFreqGroup.lowCapacity,
                        fourthResult,
                        capFreqGroup.highCapacity,
                        capacityScaled
                    )
                val result = interpolation.linearInterpolation(
                    lowFrequencyInterpolation,
                    capFreqGroup.lowFrequency,
                    highCapacityInterpolation,
                    capFreqGroup.highFrequency,
                    frequencyScaled
                )
                Mono.just(result)
            }
        val resultsMapping = mapOf(ComponentsConfig.evalValue to Double::class)
        return result.flatMap { getEvalResult(input, mapOf(ComponentsConfig.evalValue to it), resultsMapping) }
    }

    private fun getEvalResult(
        input: EvaluationInput,
        resultsMap: Map<String, Any>,
        resultsMapping: Map<String, KClass<*>>
    ): Mono<EvalResult> {
        val resultValues = ResultValues(purposeName, resultsMap, resultsMapping)
        val result =
            EvalResult(EvalResultInfo.SUCCESS, input, resultValues, "successfully evaluated $purposeName")
        return Mono.just(result)
    }

    private data class CapacityFrequencyGroup(
        val lowCapacity: BigDecimal?,
        val highCapacity: BigDecimal?,
        val lowFrequency: BigDecimal?,
        val highFrequency: BigDecimal?
    )

    private fun ids(polynomialGroup: PolynomialGroups): PolynomialsFound {
        val ids = mutableListOf<Long>()

        val first = polynomialGroup.lowCapacityLowFrequency.polynomial
        val second = polynomialGroup.lowCapacityHighFrequency.polynomial
        val third = polynomialGroup.highCapacityLowFrequency.polynomial
        val fourth = polynomialGroup.highCapacityHighFrequency.polynomial

        if (first != null) ids.add(first.polynomialId)
        if (second != null) ids.add(second.polynomialId)
        if (third != null) ids.add(third.polynomialId)
        if (fourth != null) ids.add(fourth.polynomialId)
        val polynomials = ids.toList()
        return PolynomialsFound(polynomials, first, second, third, fourth)
    }

    private data class PolynomialsFound(
        val polynomialIds: List<Long>,
        val first: PolynomialSearchResult?,
        val second: PolynomialSearchResult?,
        val third: PolynomialSearchResult?,
        val fourth: PolynomialSearchResult?
    )

    private fun evaluateMono(
        polynomial: PolynomialSearchResult?,
        mappings: List<PolynomialCoefficientsEntity>,
        evapTemp: Double,
        condTemp: Double
    ): Mono<BigDecimal> {
        val result = calculate(polynomial, mappings, evapTemp, condTemp)
            ?: return Mono.error(RuntimeException("could not calcualte polynomial value"))
        return Mono.just(result)
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
