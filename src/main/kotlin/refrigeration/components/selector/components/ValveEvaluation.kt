package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import refrigeration.components.selector.config.valves.crud.ValveService
import refrigeration.components.selector.util.*

@Service
class ValveEvaluation(private val valveService: ValveService) : Evaluator {
    override var id: String = "default"

    override fun setUniqueId(id: String) {
        this.id = id
    }

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "ValveEvaluation"
    }


    override fun keyValuesAndTypes(): Map<String, String> {
        return mapOf(
            ComponentsConfig.refrigerantKey to "String",
            ComponentsConfig.evapTempKey to "Double",
            ComponentsConfig.condTempKey to "Double",
            ComponentsConfig.evaporatorPower to "Double"
        )

    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        return Flux.fromIterable(input).flatMap { evaluate(it) }
    }

    private fun evaluate(evaluationInput: EvaluationInput): Mono<EvalResult> {
        val refrigerant = getRefrigerant(evaluationInput.anyInputs) ?: return getMonoError(
            "refrigerant not found",
            evaluationInput,
            id
        )
        val evapTemp = getEvaporationTemperature(evaluationInput.anyInputs)
            ?: return getMonoError("evaporation temperature not found", evaluationInput, id)
        val condensingTemperature = getCondensingTemperature(evaluationInput.anyInputs)
            ?: return getMonoError("condensing temperature not found", evaluationInput, id)
        val refrigerationPower =
            getRefrigerationPower(evaluationInput.anyInputs)?.div(1000) ?: return getMonoError(
                "refrigeration power not found",
                evaluationInput,
                id
            )
        return valveService.getValveBestMatchCandidate(refrigerant, refrigerationPower, condensingTemperature, evapTemp)
            .map { it.convert(evaluationInput, id) }
    }

    override fun outputTypes(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun wireInputs(requiredKeyMapping: Map<String, String>) {
    }

}
