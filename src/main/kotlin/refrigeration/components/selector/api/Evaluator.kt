package refrigeration.components.selector.api

import reactor.core.publisher.Flux

interface Evaluator {

    var id: String
    fun setUniqueId(id: String)
    fun privateEvaluation(): Boolean
    fun getName(): String
    fun getRequiredInputKeys(): Set<String>
    fun evaluate(input: List<EvaluationInput>): Flux<EvalResult>

    /** When for example this evaluation needs key named FIRST as * an input which is a key named SECOND used in previous evaluation
     * than it should map * */
    fun mapRequiredKeys(requiredKeyMapping: Map<String, String>)
}
