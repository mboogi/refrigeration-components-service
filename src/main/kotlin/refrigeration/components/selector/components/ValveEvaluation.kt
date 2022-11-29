package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator

@Service
class ValveEvaluation : Evaluator {
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

    override fun getRequiredInputKeys(): Set<String> {
        return setOf(ComponentsConfig.massFlowRealKeyStandard)
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        return Flux.empty()
    }

    override fun outputValues(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun outputTypes(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun mapRequiredKeys(requiredKeyMapping: Map<String, String>) {
    }
}
