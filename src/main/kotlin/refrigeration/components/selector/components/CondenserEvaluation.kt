package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.*
import refrigeration.components.selector.util.getElectricPower
import refrigeration.components.selector.util.getMonoError
import refrigeration.components.selector.util.getRefrigerationPower

@Service
class CondenserEvaluation() : Evaluator {

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

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        val result = input.map { evaluate(it) }
        return Flux.concat(result)
    }


    override fun outputTypes(): Map<String, String> {
        return mapOf(ComponentsConfig.condenserPower to "Double")
    }

    override fun wireInputs(requiredKeyMapping: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun keyValuesAndTypes(): Map<String, String> {
        return mapOf(
            ComponentsConfig.electricPowerKey to "Double",
            ComponentsConfig.evaporatorPower to "Double"
        )
    }

    private fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val electricPower =
            getElectricPower(input.anyInputs) ?: return getMonoError("electric power not found", input, id)
        val refrigerationPower =
            getRefrigerationPower(input.anyInputs) ?: return getMonoError("refrigeration power not found", input, id)
        val result = electricPower + refrigerationPower
        return Mono.just(getEvalResult(result, input))
    }

    private fun getEvalResult(power: Double, input: EvaluationInput): EvalResult {
        val condensingPowerType =
            Double::class.simpleName ?: throw RuntimeException("qualified name could not be found")
        val resultValues = ResultValues(
            id,
            mapOf<String, Any>(ComponentsConfig.condenserPower to power),
            mapOf(ComponentsConfig.condenserPower to condensingPowerType)
        )
        return EvalResult(EvalResultInfo.SUCCESS, input, listOf(resultValues), "Condenser Evaluation Finished")
    }
}
