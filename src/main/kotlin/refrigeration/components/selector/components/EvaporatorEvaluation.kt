package refrigeration.components.selector.components

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.*
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.util.*

@Service
abstract class EvaporatorEvaluation(private val fluidPropertyService: FluidPropertyService) : Evaluator {
    companion object {
        val logger = LoggerFactory.getLogger(EvaporatorEvaluation::class.java)
    }

    override var id: String = "default"

    override fun setUniqueId(id: String) {
        this.id = id
    }

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "EvaporatorEvaluation"
    }

    override fun getRequiredInputKeys(): Set<String> {
        return setOf(
            ComponentsConfig.condensingPressureKey,
            ComponentsConfig.condTempKey,
            ComponentsConfig.evapTempKey,
            ComponentsConfig.evaporationPressureKey,
            ComponentsConfig.subcool,
            ComponentsConfig.superheat,
            ComponentsConfig.massFlowRealKeyStandard,
            ComponentsConfig.refrigerantKey
        )
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        val result = input.map { evaluate(it) }
        return Flux.concat(result)
    }

    private fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val condensingPressure = getCondensingPressure(input.anyInputs) ?: return Mono.empty()
        val condensingTemperature = getCondensingTemperature(input.anyInputs) ?: return Mono.empty()
        val evaporationTemperature = getEvaporationTemperature(input.anyInputs) ?: return Mono.empty()
        val evaporationPressure = getEvaporationPressure(input.anyInputs) ?: return Mono.empty()
        val superHeat = getSuperHeat(input.anyInputs) ?: return Mono.empty()
        val subCool = getSubCool(input.anyInputs) ?: return Mono.empty()
        val massFlow = getRealMassFlow(input.anyInputs) ?: return Mono.empty()
        val refrigerant = getRefrigerant(input.anyInputs) ?: return Mono.empty()

        val enthalpyLiquid =
            fluidPropertyService.getLiquidEnthalpy(
                subCool,
                refrigerant,
                condensingTemperature - subCool + 273.15,
                condensingPressure
            )
        val evaporatorOutletTemperature = evaporationTemperature + superHeat + 273.15

        val enthalpyEvaporatorOutlet = fluidPropertyService.getVapourEnthalpy(
            superHeat,
            refrigerant,
            evaporatorOutletTemperature,
            evaporationPressure
        )

        val result = Mono
            .zip(enthalpyLiquid, enthalpyEvaporatorOutlet)
            .map {
                val enthalpyInlet = it.t1
                val enthalpyOutlet = it.t2
                val refrigerationPower = refrigerationPower(enthalpyOutlet, enthalpyInlet, massFlow)
                refrigerationPower
            }
            .map { getEvalResult(it, input) }

        return result
    }
    private fun refrigerationPower(enthalpyOutlet: Double, enthalpyInlet: Double, massFlow: Double): Double {
        val result = (enthalpyOutlet - enthalpyInlet) / 1000 * (massFlow / 3600)
        logger.info("refrigeration power $result")
        return result * 1000
    }

    private fun getEvalResult(condenserPower: Double, input: EvaluationInput): EvalResult {
        val resultValues = ResultValues(
            id,
            mapOf<String, Any>(ComponentsConfig.evaporatorPower to condenserPower),
            mapOf()
        )
        return EvalResult(EvalResultInfo.SUCCESS, input, listOf(resultValues), "Evaporator Evaluation Finished")
    }
}
