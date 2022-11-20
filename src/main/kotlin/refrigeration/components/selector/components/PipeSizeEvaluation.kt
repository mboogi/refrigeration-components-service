package refrigeration.components.selector.components

import reactor.core.publisher.Flux
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator

class PipeSizeEvaluation() : Evaluator {
    override var id: String = "default"

    override fun setUniqueId(id: String) {
        this.id = id
    }

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "Pipe Size Evaluation"
    }

    override fun getRequiredInputKeys(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        TODO("Not yet implemented")
    }
}
