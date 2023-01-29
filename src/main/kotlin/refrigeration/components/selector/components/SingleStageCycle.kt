package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
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

    override fun outputValues(): Set<String> {
        return setOf()
    }

    override fun outputTypes(): Map<String, String> {
        return mapOf()
    }

    override fun wireInputs(requiredKeyMapping: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun keyValuesAndTypes(): Map<String, String> {
        return mapOf(
            ComponentsConfig.refrigerantKey to "String",
            ComponentsConfig.evapTempKey to "Double",
            ComponentsConfig.condTempKey to "Double",
            ComponentsConfig.capacity to "Double",
            ComponentsConfig.frequency to "Double",
            ComponentsConfig.transCritical to "Boolean",
            ComponentsConfig.compressorType to "String",
            ComponentsConfig.subcool to "Double",
            ComponentsConfig.superheat to "Double",
            ComponentsConfig.maxVelocityDischarge to "Double",
            ComponentsConfig.maxVelocityLiquid to "Double",
            ComponentsConfig.maxVelocitySuction to "Double",
            ComponentsConfig.pipeMaterial to "String"
        )
    }

    private fun evaluators(input: EvaluationInput): Mono<EvalResult> {
        val evaluationContext = EvaluationContext()
        val compressorEvaluator =
            evaluators.filter { it.getName() == "CompressorEvaluation" }.firstOrNull() ?: return Mono.empty()

        val evaporatorEvaluator =
            evaluators.filter { it.getName() == "EvaporatorEvaluation" }.firstOrNull() ?: return Mono.empty()
        val condenserEvaluator =
            evaluators.filter { it.getName() == "CondenserEvaluation" }.firstOrNull() ?: return Mono.empty()
        val valveEvaluation =
            evaluators.filter { it.getName() == "ValveEvaluation" }.firstOrNull() ?: return Mono.empty()
        val pipesEvaluation =
            evaluators.filter { it.getName() == "Pipe Size Evaluation" }.firstOrNull() ?: return Mono.empty()
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
            .flatMap { valveEvaluation.evaluate(listOf(it)).next() }
            .map {
                getInputForRequiredKeys(
                    combineInputAndResult(input, it, "valves", evaluationContext),
                    pipesEvaluation.getRequiredInputKeys()
                )
            }
            .flatMap { pipesEvaluation.evaluate(listOf(it)).next() }
            .map {
                getInputForRequiredKeys(
                    combineInputAndResult(input, it, "pipes", evaluationContext),
                    pipesEvaluation.getRequiredInputKeys()
                )
            }

        val contextEvalList = listOf("compressor1", "evaporator1", "condenser1","valves","pipes")

        return eval.map {
            EvalResult(
                EvalResultInfo.SUCCESS,
                input,
                getContextResults(evaluationContext, contextEvalList),
                "Single Stage subcritical cycle evaluated"
            )
        }
    }

    private fun getContextResults(
        evaluationContext: EvaluationContext,
        contextEvalList: List<String>
    ): List<ResultValues> {
        return contextEvalList.mapNotNull { evaluationContext.lookup<Pair<EvaluationInput, EvalResult>>(it) }
            .map { it.second }.map { it.resultValues }.flatten()
    }

    private fun combineInputAndResult(
        input: EvaluationInput,
        evalResult: EvalResult,
        id: String,
        evaluationContext: EvaluationContext
    ): Map<String, Any> {
        val previousKeysContext = evaluationContext.keys()
        val previousValuesContext = previousKeysContext
            .mapNotNull { evaluationContext.lookup<Pair<EvaluationInput, EvalResult>>(it) }
            .map { it.second }
            .map { it.resultValues }
            .flatten()
            .map { it.result }
            .flatMap { it.entries }
            .associate { it.key to it.value }

        val inputMap = input.anyInputs
        val evalMap = changeId(evalResult, id).resultValues.first().result
        val combined = mutableMapOf<String, Any>()
        combined.putAll(inputMap)
        combined.putAll(evalMap)
        combined.putAll(previousValuesContext)
        evaluationContext.bind(id, Pair(input, changeId(evalResult, id)))
        return combined
    }

    private fun changeId(evalResult: EvalResult, id: String): EvalResult {
        val resultValues = evalResult.resultValues.map { ResultValues(id, it.result, it.resultValuesMapping) }
        return EvalResult(evalResult.evalInfo, evalResult.input, resultValues, evalResult.evalInfoMessage)
    }
}
