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
import refrigeration.components.selector.pools.WorkerPool
import refrigeration.components.selector.util.*
import java.math.BigDecimal

@Service
class CompressorEvaluation(
    private val searchService: PolynomialSearchService,
    private val coefficientsService: PolynomialCoefficientsService,
    private val fluidsService: FluidPropertyService,
    private val pool: WorkerPool
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
        val initialEval = initialEvaluation(input[0])
        val superHeat = getSuperHeat(input[0]) ?: return Flux.empty()
        val subCool = getSubCool(input[0]) ?: return Flux.empty()
        if ((superHeat < 0.0) or (subCool < 0.0)) return Flux.empty()
        if (superHeat == 20.0) return initialEval
        // do it for other superheat
        return Flux.empty()
    }

    private fun realConditionsEvaluation(input: EvaluationInput, initialEval: Mono<EvalResult>): Mono<EvalResult> {
        val evapTemp = getEvaporationTemperature(input) ?: return Mono.empty()
        val condensingTemperature = getCondensingTemperature(input) ?: return Mono.empty()
        val superheat = getSuperHeat(input) ?: return Mono.empty()
        val subCool = getSubCool(input) ?: return Mono.empty()
        val refrigerant = getRefrigerant(input) ?: return Mono.empty()

        val suffix = "superheat_20K"
//        val evaporationPressure = initialEval.map { it.result["${getName()}_evaporationPressure_$suffix"] as? Double }
//        val condensingPressure = initialEval.map { it.result["${getName()}_condensingPressure_$suffix"] as? Double }
        var enthalpyAtInlet: Mono<Double>? = null
        val densityAtInlet:Mono<Double>?
        val inletTemp=evapTemp+273.15+superheat
        if (superheat==0.0){
            enthalpyAtInlet=fluidsService.getDryVapourEnthalpy(inletTemp,refrigerant)
            densityAtInlet=fluidsService.getDryVapourDensity(inletTemp,refrigerant)
        }else{
            enthalpyAtInlet=fluidsService.getSuperHeatedVapourEnthalpy(inletTemp,refrigerant)
            densityAtInlet=fluidsService.getSuperHeatedVapourDensity(inletTemp,refrigerant)
        }
        enthalpyAtInlet?:return Mono.empty()

        val it1=initialEval.flatMap { enthalpyAtInlet }

        return Mono.empty()
    }

    private fun initialEvaluation(input: EvaluationInput): Flux<EvalResult> {
        val evapTemp = getEvaporationTemperature(input) ?: return Flux.empty()
        val condensingTemperature = getCondensingTemperature(input) ?: return Flux.empty()
        val superHeat = 20.0
        val refrigerant = getRefrigerant(input) ?: return Flux.empty()
        val checkTransCriticalHere = false

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
        val condensingPressure =
            fluidsService.getDryVapourPressure(condensingTemperature + 273.15, refrigerant)

        val compressorIOEvaluation =
            Mono.zip(densityAtInlet, massFlow, enthalpyAtInlet, condensingPressure, evaporationPressure)
                .map { t ->
                    val density = t.t1
                    val massFlowBigDecimal = t.t2.result[ComponentsConfig.evalValue] as? BigDecimal
                        ?: throw RuntimeException("mass flow value could not be calculated from eval result")
                    val massflowValue = (massFlowBigDecimal.toDouble())
                    val volumetricFlow = massflowValue.div(density)
                    val enthalpy = t.t3
                    val condensingPressure = t.t4
                    val evapPressure = t.t5

                    CompressorIO(
                        volumetricFlow,
                        massflowValue,
                        density,
                        enthalpy,
                        evapPressure,
                        condensingPressure,
                        "superheatedVapour"
                    )
                }

        val evalResult = Mono.zip(compressorIOEvaluation, electricPower)
            .map { t ->
                convert(t, input, "superheat_20K")
            }

        return evalResult.toFlux()
    }

    private fun convert(t: Tuple2<CompressorIO, EvalResult>, input: EvaluationInput, suffix: String): EvalResult {
        if ((t.t1 == null) or (t.t2 == null)) throw RuntimeException("Either volume flow not found or electric eval")
        val electricPowerValue = t.t2.result[ComponentsConfig.evalValue] as? BigDecimal
            ?: throw RuntimeException("Electric eval not found")
        return getEvalResult(
            t.t1.volumeFlow,
            t.t1.massFlow,
            t.t1.density,
            t.t1.enthalpy,
            electricPowerValue.toDouble(),
            t.t1.evaporationPressure,
            t.t1.condensingPressure,
            input,
            suffix
        )
    }

    private fun getEvalResult(
        volumeFlow: Double,
        massFlow: Double,
        densityStandardInput: Double,
        enthalpyAtInlet: Double,
        electricPowerValue: Double,
        evaporationPressure: Double,
        condensingPressure: Double,
        input: EvaluationInput,
        suffix: String
    ): EvalResult {
        return EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            mapOf(
                "${getName()}_volumetricFlow_$suffix" to volumeFlow,
                "${getName()}_massFlow_$suffix" to massFlow,
                "${getName()}_electricPower_$suffix" to electricPowerValue,
                "${getName()}_densityStandardInput_$suffix" to densityStandardInput,
                "${getName()}_enthalpy_$suffix" to enthalpyAtInlet,
                "${getName()}_evaporationPressure_$suffix" to evaporationPressure,
                "${getName()}condensingPressure$suffix" to condensingPressure
            ),
            mapOf(),
            "tempadkasdjas"
        )
    }
}
