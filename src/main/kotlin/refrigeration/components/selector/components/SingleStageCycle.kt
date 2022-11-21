package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import refrigeration.components.selector.util.getValueForKey

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
        val compressorEvaluator = evaluators.firstOrNull { it.getName() == "CompressorEvaluation" } ?: return
        val evaporatorEvaluator = evaluators.firstOrNull { it.getName() == "EvaporatorEvaluation" } ?: return
        val condenserEvaluator = evaluators.firstOrNull { it.getName() == "CondenserEvaluation" } ?: return

        val compressorEvaluation = compressorEvaluator.evaluate(listOf(input)).next()
            .flatMap { evaluateEvaporator(it, evaporatorEvaluator) }
    }

    private fun evaluateEvaporator(evalResult: EvalResult?, evaluator: Evaluator): Mono<EvalResult> {
        evalResult ?: RuntimeException("previous evaluation should not be null")
        val refrigerant =
            getValueForKey<String>(evalResult!!.input, evalResult, ComponentsConfig.refrigerantKey) ?: RuntimeException(
                "refrigerant not found"
            )
        val evapTemp = getValueForKey<Double>(evalResult.input, evalResult, ComponentsConfig.evapTempKey)
            ?: RuntimeException("evaporating temperature not found")
        val condTemp = getValueForKey<Double>(evalResult.input, evalResult, ComponentsConfig.condTempKey)
            ?: RuntimeException("condensing temperature  not found")
        val subCool = getValueForKey<Double>(evalResult.input, evalResult, ComponentsConfig.subcool)
            ?: RuntimeException("subcool not found")
        val superHeat = getValueForKey<Double>(evalResult.input, evalResult, ComponentsConfig.superheat)
            ?: RuntimeException("superheat not found")
        val condensingPressure =
            getValueForKey<Double>(evalResult.input, evalResult, ComponentsConfig.condensingPressureKey)
                ?: RuntimeException("condensing pressure not found")
        val evaporatingPressure =
            getValueForKey<Double>(evalResult.input, evalResult, ComponentsConfig.evaporationPressureKey)
                ?: RuntimeException("evaporating pressure not found")
        val massFlowReal =
            getValueForKey<Double>(evalResult.input, evalResult, ComponentsConfig.massFlowRealKeyStandard)
                ?: RuntimeException("mass flow real not found")

        val inputMap = mutableMapOf<String, Any>()
        inputMap[ComponentsConfig.refrigerantKey] = refrigerant
        inputMap[ComponentsConfig.evapTempKey] = evapTemp
        inputMap[ComponentsConfig.condTempKey] = condTemp
        inputMap[ComponentsConfig.subcool] = subCool
        inputMap[ComponentsConfig.superheat] = superHeat
        inputMap[ComponentsConfig.condensingPressureKey] = condensingPressure
        inputMap[ComponentsConfig.evaporationPressureKey] = evaporatingPressure
        inputMap[ComponentsConfig.massFlowRealKeyStandard] = massFlowReal

        val result = EvaluationInput("EvaporatorEvaluation", inputMap)
        return evaluator.evaluate(listOf(result)).next()
    }
}
