package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.Evaluator
import refrigeration.components.selector.config.pipes.crud.PipeService
import refrigeration.components.selector.util.getMonoError
import refrigeration.components.selector.util.getValueForKey
@Service
class PipeSizeEvaluation(private val pipeService: PipeService) : Evaluator {
    override var id: String = "default"

    override fun setUniqueId(id: String) {
        this.id = id
    }

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "Pipe Size Evaluation"
    }

    override fun getRequiredInputKeys(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        return Flux.empty()
    }

    fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val materialError = "no pipe material found"
        val volumeFlowError = "no volume flow found"
        val material =
            getValueForKey<String>(input.anyInputs, ComponentsConfig.pipeMaterial)
                ?: return getMonoError(materialError, input, id)
        val volumeFlow = getValueForKey<Double>(input.anyInputs, ComponentsConfig.volumeFlow)
            ?: return getMonoError(volumeFlowError, input, id)

        val maxVelocityMainSuction = getValueForKey<Double>(input.anyInputs, ComponentsConfig.maxVelocitySuction)
        val maxVelocityMainDischarge = getValueForKey<Double>(input.anyInputs, ComponentsConfig.maxVelocityDischarge)
        val maxVelocityMainLiquid = getValueForKey<Double>(input.anyInputs, ComponentsConfig.maxVelocityLiquid)

        val massFlowMainSuction = 10.0
        val massFlowMainDischarge = 10.0
        val massFlowMainLiquid = 10.0

        val requiredInnerDiameterSuction=10.0
        val requiredInnerDiameterDischarge=10.0
        val requiredInnerDiameterLiquid=10.0

       return Mono.empty()
    }

    override fun outputValues(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun outputTypes(): Map<String, String> {
        TODO("Not yet implemented")
    }

    override fun mapRequiredKeys(requiredKeyMapping: Map<String, String>) {
        TODO("Not yet implemented")
    }
}
