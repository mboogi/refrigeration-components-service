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
class EvaporatorEvaluation(private val fluidPropertyService: FluidPropertyService) : Evaluator {
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
        return Flux.fromIterable(input).flatMap { evaluate(it) }
    }

    override fun outputValues(): Set<String> {
        return setOf(ComponentsConfig.evaporatorPower)
    }


    override fun outputTypes(): Map<String, String> {
        return mapOf(ComponentsConfig.evaporatorPower to "Double")
    }

    override fun wireInputs(requiredKeyMapping: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun keyValuesAndTypes(): Map<String, String> {
        return mapOf(
            ComponentsConfig.condensingPressureKey to "Double",
            ComponentsConfig.condTempKey to "Double",
            ComponentsConfig.evapTempKey to "Double",
            ComponentsConfig.evaporationPressureKey to "Double",
            ComponentsConfig.superheat to "Double",
            ComponentsConfig.subcool to "Double",
            ComponentsConfig.massFlowRealKeyStandard to "Double",
            ComponentsConfig.refrigerantKey to "String"
        )
    }

    private fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val condensingPressure =
            getCondensingPressure(input.anyInputs) ?: return getMonoError("Condensing Pressure not found", input, id)
        val condensingTemperature = getCondensingTemperature(input.anyInputs) ?: return getMonoError(
            "Condensing Temperature not found",
            input,
            id
        )
        val evaporationTemperature = getEvaporationTemperature(input.anyInputs) ?: return getMonoError(
            "Evaporation Temperature not found",
            input,
            id
        )
        val evaporationPressure =
            getEvaporationPressure(input.anyInputs) ?: return getMonoError("Evaporation Pressure not found", input, id)
        val superHeat = getSuperHeat(input.anyInputs) ?: return getMonoError("Superheat  not found", input, id)
        val subCool = getSubCool(input.anyInputs) ?: return getMonoError("Sub-cool value  not found", input, id)
        val massFlow =
            getRealMassFlow(input.anyInputs) ?: return getMonoError("Mass flow real condition not found", input, id)
        val refrigerant = getRefrigerant(input.anyInputs) ?: return getMonoError("Refrigerant not found", input, id)

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
        val evapType = Double::class.simpleName ?: throw RuntimeException("qualified name could not be found")
        val resultValues = ResultValues(
            id,
            mapOf<String, Any>(ComponentsConfig.evaporatorPower to condenserPower),
            mapOf(ComponentsConfig.evaporatorPower to evapType)
        )
        return EvalResult(EvalResultInfo.SUCCESS, input, listOf(resultValues), "Evaporator Evaluation Finished")
    }
}
