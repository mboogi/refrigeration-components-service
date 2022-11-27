package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.api.*
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
        return Flux.fromIterable(input).flatMap { evaluators(it) }
    }

    override fun mapRequiredKeys(requiredKeyMapping: Map<String, String>) {
        TODO("Not yet implemented")
    }
//
//    private fun evaluate(input: EvaluationInput, evaluators: List<Evaluator>, id: String) {
//        val resutValues = ResultValues(id, mapOf(), mapOf())
//        val initialResult = EvalResult(EvalResultInfo.WARNING, input, listOf(resutValues), "initial result")
//        return evaluators()
//    }

    private fun evalChain(
        previous: Mono<EvalResult>,
        evaluator: Evaluator,
        input: EvaluationInput,
        evaluationContext: EvaluationContext
    ) {
        previous
            .map {
                getInputForRequiredKeys(
                    combineInputAndResult(input, it, "", evaluationContext),
                    evaluator.getRequiredInputKeys()
                )
            }
            .flatMap { evaluator.evaluate(listOf(input)).next() }
    }

    private fun evaluators(input: EvaluationInput): Mono<EvalResult> {
        val evaluationContext = EvaluationContext()
        val compressorEvaluator =
            evaluators.firstOrNull { it.getName() == "CompressorEvaluation" } ?: return Mono.empty()

        val evaporatorEvaluator =
            evaluators.firstOrNull { it.getName() == "EvaporatorEvaluation" } ?: return Mono.empty()
        val condenserEvaluator = evaluators.firstOrNull { it.getName() == "CondenserEvaluation" } ?: return Mono.empty()
        val valveEvaluation = evaluators.firstOrNull { it.getName() == "ValveEvaluation" } ?: return Mono.empty()

        val eval = compressorEvaluator
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
        val contextEvalList = listOf("compressor1", "evaporator1", "condenser1")
        val contextResults =
            contextEvalList.mapNotNull { evaluationContext.lookup<EvalResult>(it) }.map { it.resultValues }.flatten()
        return eval.map { EvalResult(EvalResultInfo.SUCCESS, input, contextResults, "") }
    }

    private fun mapEndResult(evalResult: EvalResult, context: EvaluationContext): EvalResult? {
        val compressor = context.lookup<Pair<EvaluationInput, EvalResult>>("compressor1")
        val evaporator = context.lookup<Pair<EvaluationInput, EvalResult>>("evaporator1")
        val condenser = context.lookup<Pair<EvaluationInput, EvalResult>>("condenser1")
        return null
    }

    private fun combineInputAndResult(
        input: EvaluationInput,
        evalResult: EvalResult,
        id: String,
        evaluationContext: EvaluationContext
    ): Map<String, Any> {
        val inputMap = input.anyInputs
        val evalMap = evalResult.resultValues.first().result
        val combined = mutableMapOf<String, Any>()
        combined.putAll(inputMap)
        combined.putAll(evalMap)
        evaluationContext.bind(id, Pair(input, evalResult))
        return combined
    }
}
