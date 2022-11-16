package refrigeration.components.selector.cycles

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.util.function.Tuple2
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
import java.math.BigDecimal

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

        val enthalpyAtInlet = evaporationPressure
            .flatMap {
                fluidsService
                    .getSuperHeatedVapourEnthalpy(evapTemp + 273.15, it, refrigerant)
            }

        val densityAtInlet = evaporationPressure
            .flatMap {
                fluidsService.getSuperHeatedVapourDensity(evapTemp + 273.15, it, refrigerant)
            }

        val compressorIOEvaluation = Mono.zip(densityAtInlet, massFlow, enthalpyAtInlet, evaporationPressure).map { t ->
            val density = t.t1
            val massFlowBigDecimal = t.t2.result[ComponentsConfig.evalValue] as? BigDecimal?: throw RuntimeException("mass flow value could not be calculated from eval result")
            val massflowValue = (massFlowBigDecimal.toDouble())
            val volumetricFlow = massflowValue.div(density)
            val enthalpy = t.t3
            val evapPressure = t.t4

            CompressorIO(
                volumetricFlow,
                massflowValue,
                density,
                enthalpy,
                evapPressure,
                "superheatedVapour"
            )
        }

        val evalResult = Mono.zip(compressorIOEvaluation, electricPower)
            .map { t ->
                convert(t, input)
            }

        return evalResult.toFlux()
    }

    private fun convert(t: Tuple2<CompressorIO, EvalResult>, input: EvaluationInput): EvalResult {
        if ((t.t1 == null) or (t.t2 == null)) throw RuntimeException("Either volume flow not found or electric eval")
        val electricPowerValue = t.t2.result[ComponentsConfig.evalValue] as? BigDecimal
            ?: throw RuntimeException("Electric eval not found")
        return getEvalResult(t.t1.volumeFlow, t.t1.massFlow, t.t1.density, t.t1.enthalpy, electricPowerValue.toDouble(), t.t1.pressure, input)
    }

    private fun getEvalResult(
        volumeFlow: Double,
        massFlow: Double,
        densityStandardInput: Double,
        enthalpyAtInlet: Double,
        electricPowerValue: Double,
        evaporationPressure: Double,
        input: EvaluationInput
    ): EvalResult {
        return EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            mapOf(
                "volumetricFlow" to volumeFlow,
                "massFlow" to massFlow,
                "electricPower" to electricPowerValue,
                "densityStandardInput" to densityStandardInput,
                "enthalpy" to enthalpyAtInlet,
                "evaporationPressure" to evaporationPressure
            ),
            mapOf(),
            "tempadkasdjas"
        )
    }
}
