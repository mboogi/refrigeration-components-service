package refrigeration.components.selector

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import refrigeration.components.selector.pools.CyclesThreadPool

@RestController
class EvaluatorController(private val evaluators: List<Evaluator>) {
    private val webClient = WebClient.create("http://localhost:8080")

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
        val keys = evaluator.getRequiredInputKeys()
        return Mono.just(keys)
    }

    @GetMapping("/value")
    fun getValue(): Double {
        return 5.0
    }

    @GetMapping("/mono")
    fun getMonoFromValue(): Mono<Double> {
        return webClient
            .get()
            .uri("/value")
            .retrieve()
            .bodyToMono<Double>()
            .doOnNext { Thread.sleep(5000) }
    }

    @GetMapping("/double")
    fun getDoubleFromClient(): Double? {
        val result = getMonoFromValue()
            .log()
            .publishOn(Schedulers.fromExecutor(pool))
            .subscribeOn(Schedulers.fromExecutor(pool))
            .log()
            .toFuture()
            .join()
        return result
    }
}
