package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.*
import refrigeration.components.selector.util.getCondenserPower
import refrigeration.components.selector.util.getElectricPower
import refrigeration.components.selector.util.getMonoError
import refrigeration.components.selector.util.getRefrigerationPower
@Service
class COPEvaluator : Evaluator {
    override var id: String = "default"

    override fun setUniqueId(id: String) {
        this.id = id
    }

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "COPEvaluator"
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        val result = input.map { evaluate(it) }
        return Flux.concat(result)
    }

    private fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val electricPower =
            getElectricPower(input.anyInputs) ?: return getMonoError("Electric Power not found", input, id)
        val refrigerationPower =
            getRefrigerationPower(input.anyInputs) ?: return getMonoError("Refrigeration Power not found", input, id)
        val condenserPower =
            getCondenserPower(input.anyInputs) ?: return getMonoError("Refrigeration Power not found", input, id)
        val cop = refrigerationPower.div(electricPower)
        val eer = condenserPower.div(electricPower)
        return Mono.just(getEvalResult(cop,eer, input))

    }

    private fun getEvalResult(cop: Double,eer:Double, input: EvaluationInput): EvalResult {
        val resultsMap= mutableMapOf<String,Any>()
        resultsMap.put(ComponentsConfig.cop,cop)
        resultsMap.put(ComponentsConfig.eer,eer)

        val resultsMapping= mutableMapOf<String,String>()
        resultsMapping.put(ComponentsConfig.cop, "Double")
        resultsMapping.put(ComponentsConfig.eer, "Double")

        val resultValues = ResultValues(
            id,
            resultsMap,
            resultsMapping
        )
        return EvalResult(EvalResultInfo.SUCCESS, input, listOf(resultValues), "COP Evaluation Finished")
    }

    override fun outputTypes(): Map<String, String> {
        return mapOf()
    }

    override fun wireInputs(requiredKeyMapping: Map<String, String>) {

    }

    override fun keyValuesAndTypes(): Map<String, String> {
        return mapOf(
            ComponentsConfig.electricPowerKey to "Double",
            ComponentsConfig.evaporatorPower to "Double",
            ComponentsConfig.condenserPower to "Double"
        )
    }
}