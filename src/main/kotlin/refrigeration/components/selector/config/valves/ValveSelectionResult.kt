package refrigeration.components.selector.config.valves

import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.ResultValues
import java.math.BigDecimal

data class ValveSelectionResult(
    val valveCalculatedCapacity: BigDecimal,
    val valveName: String
) {

    fun convert(evaluationInput: EvaluationInput, id: String): EvalResult {
        val results = mapOf<String, Any>(
            ComponentsConfig.valveCapacity to valveCalculatedCapacity, ComponentsConfig.valveName to valveName
        )

        val resultsMap = ResultValues(id, results, mapOf())
        return EvalResult(
            EvalResultInfo.SUCCESS, evaluationInput, listOf(resultsMap), "Succesfully evaluated Valve"
        )
    }

}
