package refrigeration.components.selector

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import refrigeration.components.selector.pools.CyclesThreadPool

@RestController
class EvaluatorController(private val evaluators: List<Evaluator>) {

    @Autowired
    lateinit var pool: CyclesThreadPool

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
        println(evaluator.getName())
        val keys = evaluator.getRequiredInputKeys()
        return Mono.just(keys)
    }

    @GetMapping("/simple")
    fun simple(): Flux<Int> {
        val first = Flux.just(1,2,3,4)
            .collectList().toFuture().get()

        return Flux.fromIterable(first).map { it*3 }
    }
}
