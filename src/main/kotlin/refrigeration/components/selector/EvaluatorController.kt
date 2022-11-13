package refrigeration.components.selector

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator

@RestController
class EvaluatorController(private val evaluators: List<Evaluator>) {

    @GetMapping("eval/names")
    fun getEvaluators(): Flux<String> {
        val evaluators = evaluators.map { it.getName() }
        return Flux.fromIterable(evaluators)
    }

    @PostMapping("eval/evaluation/{name}")
    fun evaluate(
        @RequestBody input: List<EvaluationInput>,
        @PathVariable("name") evaluatorName: String
    ): Flux<EvalResult> {
        val availableEvaluators = evaluators.filter { !it.privateEvaluation() }.groupBy { it.getName() }
        val evaluators = availableEvaluators[evaluatorName] ?: return Flux.empty()
        val evaluator = evaluators.firstOrNull() ?: return Flux.empty()
        return evaluator.evaluate(input)
    }

    @GetMapping("eval/{name}/keys")
    fun getRequiredKeys(@PathVariable("name") evaluatorName: String): Mono<Set<String>> {
        val availableEvaluators = evaluators.filter { !it.privateEvaluation() }.groupBy { it.getName() }
        val evaluators = availableEvaluators[evaluatorName] ?: return Mono.empty()
        val evaluator = evaluators.firstOrNull() ?: return Mono.empty()
        val keys = evaluator.getRequiredInputKeys()
        return Mono.just(keys)
    }
}
