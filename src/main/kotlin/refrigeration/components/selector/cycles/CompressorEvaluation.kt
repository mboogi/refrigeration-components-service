package refrigeration.components.selector.cycles

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum
import refrigeration.components.selector.config.polynomials.eval.PolynomialEvaluationService
import refrigeration.components.selector.config.polynomials.search.PolynomialCoefficientsService
import refrigeration.components.selector.config.polynomials.search.PolynomialSearchService
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.pools.CyclesThreadPool
import refrigeration.components.selector.util.*

@Service
class CompressorEvaluation(
    private val searchService: PolynomialSearchService,
    private val coefficientsService: PolynomialCoefficientsService,
    private val fluidsService: FluidPropertyService,
    private val pool: CyclesThreadPool
) : Evaluator {
    companion object {
        val logger = LoggerFactory.getLogger(CompressorEvaluation::class.java)
    }

    private val duration = ComponentsConfig.duration

    private val massFlowPolynomialEval =
        PolynomialEvaluationService(
            PolynomialTypesEnum.MASS_FLOW.toString(),
            searchService,
            coefficientsService
        )

    private val electricPowerPolynomialEval =
        PolynomialEvaluationService(
            PolynomialTypesEnum.ELECTRIC_POWER.toString(),
            searchService,
            coefficientsService
        )

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "CompressorEvaluation"
    }

    override fun getRequiredInputKeys(): Set<String> {
        return setOf(
            ComponentsConfig.refrigerantKey,
            ComponentsConfig.evapTempKey,
            ComponentsConfig.condTempKey,
            ComponentsConfig.capacity,
            ComponentsConfig.frequency,
            ComponentsConfig.transCritical,
            ComponentsConfig.compressorType,
            ComponentsConfig.subcool,
            ComponentsConfig.superheat
        )
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        // TODO remove gets with something more resilient, implement on error, onErrorComplete should be replaced
        val result = initialEvaluation(input[0])
        val superHeat = getSuperHeat(input[0]) ?: return Flux.empty()
        val subCool = getSubCool(input[0]) ?: return Flux.empty()
        if ((superHeat < 0.0) or (subCool < 0.0)) return Flux.empty()
        if (superHeat == 20.0) return result
        // do it for other superheat
        return Flux.empty()
    }

    private fun evaluate(initalEval: Flux<EvalResult>, input: EvaluationInput) {
    }

    private fun initialEvaluation(input: EvaluationInput): Flux<EvalResult> {
        val evapTemp = getEvaporationTemperature(input) ?: return Flux.empty()
        val condensingTemperature = getCondensingTemperature(input) ?: return Flux.empty()
        val superHeat = 20.0
        val refrigerant = getRefrigerant(input) ?: return Flux.empty()

        val massFlow =
            massFlowPolynomialEval.evaluate(listOf(input)).next()
        val electricPower =
            electricPowerPolynomialEval.evaluate(listOf(input)).next()

        val inletTemperature = evapTemp + superHeat + 273.15

        val evaporationPressure =
            fluidsService.getDryVapourPressure(inletTemperature, refrigerant)

        val condensingPressure =
            fluidsService.getDryVapourPressure(condensingTemperature + 273.15, refrigerant)

        val enthalpyAtInlet = evaporationPressure
            .flatMap {
                fluidsService
                    .getSuperHeatedVapourEnthalpy(evapTemp + 273.15, it, refrigerant)
            }

        val densityAtInlet = evaporationPressure
            .flatMap { fluidsService.getSuperHeatedVapourDensity(evapTemp + 273.15, it, refrigerant) }

        val polynomialEvaluation = evaluatePolynomials(massFlow, electricPower)

        val massFlowEval = polynomialEvaluation[ComponentsConfig.massFlowKey] ?: return Flux.empty()
        val massFlowResult =
            massFlowEval.result[ComponentsConfig.polynomialEvaluationValue] as? Double ?: return Flux.empty()

        val volumetricFlow = densityAtInlet.map { massFlowResult.div(it) }

        val electricPowerResult = polynomialEvaluation[ComponentsConfig.electricPowerKey] ?: return Flux.empty()
        val electricPowerValue =
            electricPowerResult.result[ComponentsConfig.polynomialEvaluationValue] as? Double ?: return Flux.empty()
        val evalResult = volumetricFlow.flatMap { getEvalResult(it, massFlowResult, electricPowerValue, input) }

        return evalResult.toFlux()
    }

    private fun getEvalResult(
        volumetricFlow: Double,
        massFlowResult: Double,
        electricPowerValue: Double,
        input: EvaluationInput
    ): Mono<EvalResult> {
        val result = EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            mapOf(
                "volumetricFlow" to volumetricFlow,
                "massFlow" to massFlowResult,
                "electricPower" to electricPowerValue
            ),
            mapOf(),
            "tempadkasdjas"
        )
        return Mono.just(result)
    }

    private fun fetchInletData(
        evaporationPressure: Mono<Double>,
        enthalpyAtInlet: Mono<Double>,
        densityAtInlet: Mono<Double>
    ): Mono<Map<String, Double>> {
        return Mono.zip(evaporationPressure, enthalpyAtInlet, densityAtInlet).map { t ->
            val condensingPressure = Pair(ComponentsConfig.condensingPressureKey, t.t1)
            val enthalpyAtInlet = Pair(ComponentsConfig.enthalpyAtInletKey, t.t2)
            val densityAtInlet = Pair(ComponentsConfig.densityAtInletKey, t.t3)
            mapOf(condensingPressure, enthalpyAtInlet, densityAtInlet)
        }
    }

    private fun evaluatePolynomials(
        massFlow: Mono<EvalResult>,
        electricPower: Mono<EvalResult>
    ): Map<String, EvalResult> {
        return Mono.zip(massFlow, electricPower).map { t ->
            val massFlow = Pair(ComponentsConfig.massFlowKey, t.t1)
            val electricPower = Pair(ComponentsConfig.electricPowerKey, t.t2)
            mutableMapOf(massFlow, electricPower)
        }
            .onErrorComplete()
            .publishOn(Schedulers.fromExecutor(pool))
            .toFuture()
            .join()
            ?: return mapOf()
    }

    private fun evaluatePolynomialsMono(
        massFlow: Mono<EvalResult>,
        electricPower: Mono<EvalResult>
    ): Mono<Map<String, EvalResult>> {
        return Mono.zip(massFlow, electricPower).map { t ->
            val massFlow = Pair(ComponentsConfig.massFlowKey, t.t1)
            val electricPower = Pair(ComponentsConfig.electricPowerKey, t.t2)
            mapOf(massFlow, electricPower)
        }
    }
}
