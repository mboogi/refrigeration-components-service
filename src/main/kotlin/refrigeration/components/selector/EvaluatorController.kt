package refrigeration.components.selector

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import java.time.Duration
import kotlin.math.log

@RestController
@RequestMapping("api/eval")
@Api("EvaluationController")
//@CrossOrigin(origins = ["http://localhost:3000"])
class EvaluatorController(private val evaluators: List<Evaluator>) {
    companion object{
        private val logger=LoggerFactory.getLogger(EvaluatorController::class.java)
    }
    @ApiOperation("get available evaluators")
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping("/names")
    fun getEvaluators(): Flux<Any> {
        val evaluators = evaluators.map { it.getName() }
        return Flux.fromIterable(evaluators)
    }

    @ApiOperation("evaluation endpoint")
    @PostMapping("/evaluation/{name}")
//    @CrossOrigin(origins = ["http://localhost:3001"])
    fun evaluate(
        @RequestBody input: List<EvaluationInput>,
        @PathVariable("name") evaluatorName: String
    ): Flux<EvalResult> {
        logger.info("Evaluation request received: $input")
        val availableEvaluators = evaluators.filter { !it.privateEvaluation() }.groupBy { it.getName() }
        val evaluators = availableEvaluators[evaluatorName] ?: return Flux.empty()
        val evaluator = evaluators.firstOrNull() ?: return Flux.empty()
        return evaluator.evaluate(input)
    }

    @ApiOperation("required keys for named evaluator")
//    @CrossOrigin(origins = ["http://localhost:3001"])
    @GetMapping("/{name}/keys")
    fun getRequiredKeys(@PathVariable("name") evaluatorName: String): Mono<Set<String>> {
        val availableEvaluators = evaluators.filter { !it.privateEvaluation() }.groupBy { it.getName() }
        val evaluators = availableEvaluators[evaluatorName] ?: return Mono.empty()
        val evaluator = evaluators.firstOrNull() ?: return Mono.empty()
        val keys = evaluator.getRequiredInputKeys()
        return Mono.just(keys)
    }

    @ApiOperation("required keys with respective types for named evaluator")
    @GetMapping("/{name}/form-values")
//    @CrossOrigin(origins = ["http://localhost:3001"])
    fun getFormValues(@PathVariable("name") evaluatorName: String): Map<String, String> {
        val availableEvaluators = evaluators.filter { !it.privateEvaluation() }.groupBy { it.getName() }
        val evaluators = availableEvaluators[evaluatorName] ?: return emptyMap()
        val evaluator = evaluators.firstOrNull() ?: return emptyMap()
        return evaluator.keyValuesAndTypes()
    }


}
