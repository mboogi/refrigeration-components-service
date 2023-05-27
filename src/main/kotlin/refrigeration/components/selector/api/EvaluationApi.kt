package refrigeration.components.selector.api

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EvaluationApi {
    fun getAvailableEvaluators(): Flux<Any>
    fun getRequiredKeysForEvaluator(evaluatorName: String): Map<String,String>
    fun evaluate(evaluatorName: String, input: List<EvaluationInput>): Flux<EvalResult>
}
