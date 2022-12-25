package refrigeration.components.selector.api

import reactor.core.publisher.Flux

interface Evaluator {

    var id: String
    fun setUniqueId(id: String)
    fun privateEvaluation(): Boolean
    fun getName(): String
    fun getRequiredInputKeys(): Set<String>
    fun evaluate(input: List<EvaluationInput>): Flux<EvalResult>
    fun outputValues(): Set<String>
    fun outputTypes(): Map<String, String>

    /** When for example this evaluation needs key named FIRST as * an input which is a key named SECOND used in previous evaluation
     * than it should map * */
    fun wireInputs(requiredKeyMapping: Map<String, String>)

    fun keyValuesAndTypes():Map<String,String>
}
