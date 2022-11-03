package refrigeration.components.selector.api

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EvaluationApi {
    fun getAvailableEvaluators(): Flux<String>
    fun getRequiredKeysForEvaluator(evaluatorName: String): Mono<Set<String>>
    fun evaluate(evaluatorName: String, input: List<EvaluationInput>): Flux<EvalResult>
}
