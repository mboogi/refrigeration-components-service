package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.*
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.util.*

@Service
class CondenserEvaluation(private val fluidPropertyService: FluidPropertyService) : Evaluator {

    override var id: String = "default"

    override fun setUniqueId(id: String) {
        this.id = id
    }

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "CondenserEvaluation"
    }

    override fun getRequiredInputKeys(): Set<String> {
        return setOf(
            ComponentsConfig.electricPowerKey,
            ComponentsConfig.evaporatorPower
        )
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        val result = input.map { evaluate(it) }
        return Flux.concat(result)
    }

    private fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val electricPower= getElectricPower(input.anyInputs)?:return Mono.empty()
        val refrigerationPower= getRefrigerationPower(input.anyInputs) ?:return Mono.empty()
        val result=electricPower+refrigerationPower
        return Mono.just(getEvalResult(result,input))
    }

    private fun getEvalResult(power: Double, input: EvaluationInput): EvalResult {
        val resultValues = ResultValues(
            id,
            mapOf<String, Any>(ComponentsConfig.condenserPower to power),
            mapOf()
        )
        return EvalResult(EvalResultInfo.SUCCESS, input, resultValues, "Condenser Evaluation Finished")
    }
}
