package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationContext
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import refrigeration.components.selector.util.getInputForRequiredKeys

@Service
class SingleStageCycle(private val evaluators: List<Evaluator>) : Evaluator {

    override var id: String = "default"

    override fun setUniqueId(id: String) {
        this.id = id
    }

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "SingleStageCycleEvaluation"
    }

    override fun getRequiredInputKeys(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        TODO("Not yet implemented")
    }

    private fun evaluators(input: EvaluationInput) {
        val evaluationContext = EvaluationContext()
        val compressorEvaluator = evaluators.firstOrNull { it.getName() == "CompressorEvaluation" } ?: return
        val evaporatorEvaluator = evaluators.firstOrNull { it.getName() == "EvaporatorEvaluation" } ?: return
        val condenserEvaluator = evaluators.firstOrNull { it.getName() == "CondenserEvaluation" } ?: return
        val valveEvaluation = evaluators.firstOrNull { it.getName() == "ValveEvaluation" } ?: return

        val compressorEvaluation = compressorEvaluator
            .evaluate(listOf(input)).next()
            .map {
                getInputForRequiredKeys(
                    combineInputAndResult(input, it, "compressor1", evaluationContext),
                    evaporatorEvaluator.getRequiredInputKeys()
                )
            }
            .flatMap { evaporatorEvaluator.evaluate(listOf(it)).next() }
            .map {
                getInputForRequiredKeys(
                    combineInputAndResult(input, it, "evaporator1", evaluationContext),
                    condenserEvaluator.getRequiredInputKeys()
                )
            }
            .flatMap { condenserEvaluator.evaluate(listOf(it)).next() }
            .map {
                getInputForRequiredKeys(
                    combineInputAndResult(input, it, "condenser1", evaluationContext),
                    valveEvaluation.getRequiredInputKeys()
                )
            }
    }

    private fun mapEndResult(evalResult: EvalResult, context: EvaluationContext): EvalResult {
        val compressor = context.lookup<Pair<EvaluationInput, EvalResult>>("compressor1")
        val evaporator = context.lookup<Pair<EvaluationInput, EvalResult>>("evaporator1")
        val condenser = context.lookup<Pair<EvaluationInput, EvalResult>>("condenser1")
    }

    private fun combineInputAndResult(
        input: EvaluationInput,
        evalResult: EvalResult,
        id: String,
        evaluationContext: EvaluationContext
    ): Map<String, Any> {
        val inputMap = input.anyInputs
        val evalMap = evalResult.resultValues.result
        val combined = mutableMapOf<String, Any>()
        combined.putAll(inputMap)
        combined.putAll(evalMap)
        evaluationContext.bind(id, Pair(input, evalResult))
        return combined
    }
}
