package refrigeration.components.selector


import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationApi
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator

@RestController
@RequestMapping("api/eval")
class EvaluatorController(private val evaluators: List<Evaluator>) : EvaluationApi {
    companion object {
        private val logger = LoggerFactory.getLogger(EvaluatorController::class.java)
    }

    @CrossOrigin(origins = ["http://localhost:3000", "evaluation-frontend.components-selector.svc.cluster.local"])
    @GetMapping("/names")
    @Operation(summary = "all evaluators are listed")
    override fun getAvailableEvaluators(): Flux<Any> {
        val evaluators = evaluators.map { it.getName() }
        return Flux.fromIterable(evaluators)
    }

    @CrossOrigin(origins = ["http://localhost:3000", "evaluation-frontend.components-selector.svc.cluster.local"])
    @GetMapping("/{name}/form-values")
    @Operation(summary = "input values required for named evaluator")
    override fun getRequiredKeysForEvaluator(@PathVariable("name") evaluatorName: String): Map<String, String> {
        val availableEvaluators = evaluators.filter { !it.privateEvaluation() }.groupBy { it.getName() }
        val evaluators = availableEvaluators[evaluatorName] ?: return emptyMap()
        val evaluator = evaluators.firstOrNull() ?: return emptyMap()
        return evaluator.keyValuesAndTypes()
    }
    @CrossOrigin(origins = ["http://localhost:3000", "evaluation-frontend.components-selector.svc.cluster.local"])
    @PostMapping("/evaluation/{name}")
    @Operation(summary = "evaluation for named evaluator")
    override fun evaluate(
        @PathVariable("name") evaluatorName: String,
        @RequestBody input: List<EvaluationInput>
    ): Flux<EvalResult> {
        logger.info("Evaluation request received: $input")
        val availableEvaluators = evaluators.filter { !it.privateEvaluation() }.groupBy { it.getName() }
        val evaluators = availableEvaluators[evaluatorName] ?: return Flux.empty()
        val evaluator = evaluators.firstOrNull() ?: return Flux.empty()
        return evaluator.evaluate(input)
    }

}
