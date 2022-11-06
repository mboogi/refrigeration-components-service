package refrigeration.components.selector.cycles

import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum
import refrigeration.components.selector.config.polynomials.eval.PolynomialEvaluationService
import refrigeration.components.selector.config.polynomials.search.PolynomialCoefficientsService
import refrigeration.components.selector.config.polynomials.search.PolynomialSearchService
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.pools.CyclesThreadPool
import refrigeration.components.selector.util.*
import java.time.Duration

class CompressorEvaluation(
    private val searchService: PolynomialSearchService,
    private val coefficientsService: PolynomialCoefficientsService,
    private val fluidsService: FluidPropertyService,
    private val pool: CyclesThreadPool
) : Evaluator {
    private val duration=ComponentsConfig.duration

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
        return true
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
        val tmp = input[0]
        val evapTemp = getEvaporationTemperature(tmp) ?: return Flux.empty()
        val condensingTemperature = getCondensingTemperature(tmp) ?: return Flux.empty()
        val subCool = getSubCool(tmp) ?: return Flux.empty()
        val superHeat = getSuperHeat(tmp) ?: return Flux.empty()
        val refrigerant = getRefrigerant(tmp) ?: return Flux.empty()

        val massFlow =
            massFlowPolynomialEval.evaluate(listOf(tmp)).next()
        val electricPower =
            electricPowerPolynomialEval.evaluate(listOf(tmp)).next()

        val inletTemperature = evapTemp + superHeat

        val evaporationPressure =
            fluidsService.getDryVapourPressure(evapTemp, refrigerant).publishOn(Schedulers.fromExecutor(pool)).block()
                ?: return Flux.empty()

        val condensingPressure = fluidsService.getDryVapourPressure(condensingTemperature, refrigerant)
        val enthalpyAtInlet = fluidsService.getSuperHeatedVapourEnthalpy(evapTemp, evaporationPressure, refrigerant)
        val densityAtInlet = fluidsService.getSuperHeatedVapourDensity(evapTemp, evaporationPressure, refrigerant)

        val inletData = Flux.zip(condensingPressure, enthalpyAtInlet, densityAtInlet).map { t ->
            val condensingPressure = Pair("condensingPressure", t.t1)
            val enthalpyAtInlet = Pair("enthalpyAtInlet", t.t2)
            val densityAtInlet = Pair("densityAtInlet", t.t3)
            mutableMapOf(condensingPressure, enthalpyAtInlet, densityAtInlet)
        }.next().publishOn(Schedulers.fromExecutor(pool)).block(Duration.ofSeconds(duration))?:return Flux.empty()

        val polynomialEvaluation = Flux.zip(massFlow, electricPower).map { t ->
            val massFlow = Pair("massFlow", t.t1)
            val electricPower = Pair("electricPower", t.t2)
            mutableMapOf(massFlow, electricPower)
        }.next().publishOn(Schedulers.fromExecutor(pool)).block(Duration.ofSeconds(duration))?:return Flux.empty()
    }
}
