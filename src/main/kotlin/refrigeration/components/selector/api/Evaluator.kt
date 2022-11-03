package refrigeration.components.selector.api

import reactor.core.publisher.Flux

interface Evaluator {
    fun privateEvaluation(): Boolean
    fun getName(): String
    fun getRequiredInputKeys(): Set<String>
    fun evaluate(input: List<EvaluationInput>): Flux<EvalResult>
}
