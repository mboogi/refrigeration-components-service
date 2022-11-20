package refrigeration.components.selector.cycles

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.*
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.util.*
import kotlin.math.abs

@Service
class CondenserEvaluation(private val fluidPropertyService: FluidPropertyService) : Evaluator {

    override var id: String = "default"

    override fun setUniqueId(id: String) {
        this.id = id
    }

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "CondenserEvaluation"
    }

    override fun getRequiredInputKeys(): Set<String> {
        return setOf(
            ComponentsConfig.compressorOutletTemperature,
            ComponentsConfig.condensingPressureKey,
            ComponentsConfig.subcool,
            ComponentsConfig.massFlowRealKeyStandard
        )
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        val result = input.map { evaluate(it) }
        return Flux.concat(result)
    }

    private fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val compressorOutletTemperature = getCompressorOutletTemperature(input.anyInputs) ?: return Mono.empty()
        val condensingPressure = getCondensingPressure(input.anyInputs) ?: return Mono.empty()
        val condensingTemperature = getCondensingTemperature(input.anyInputs) ?: return Mono.empty()
        val subCool = getSubCool(input.anyInputs) ?: return Mono.empty()
        val massFlow = getRealMassFlow(input.anyInputs) ?: return Mono.empty()
        val refrigerant = getRefrigerant(input.anyInputs) ?: return Mono.empty()

        val enthalpyStart = fluidPropertyService.getSuperHeatedVapourEnthalpy(
            compressorOutletTemperature,
            condensingTemperature + 273.15,
            refrigerant
        )

        val enthalpyEnd =
            fluidPropertyService.getLiquidEnthalpy(
                subCool,
                refrigerant,
                condensingTemperature - subCool + 273.15,
                condensingPressure
            )

        val result = Mono
            .zip(enthalpyStart, enthalpyEnd)
            .map {
                val enthalpyFirst = it.t1
                val enthalpySecond = it.t2
                (massFlow / 3600) * abs(enthalpyFirst.minus(enthalpySecond))
            }.map { getEvalResult(it, input) }
        return result
    }

    private fun getEvalResult(condenserPower: Double, input: EvaluationInput): EvalResult {
        val resultValues = ResultValues(
            id,
            mapOf<String, Any>(ComponentsConfig.condenserPower to condenserPower),
            mapOf()
        )
        return EvalResult(EvalResultInfo.SUCCESS, input, resultValues, "Condenser Evaluation Finished")
    }
}
