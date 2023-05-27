package refrigeration.components.selector.components

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.*
import refrigeration.components.selector.config.pipes.crud.PipeService
import refrigeration.components.selector.config.pipes.db.PipeEntity
import refrigeration.components.selector.util.getDoubleValue
import refrigeration.components.selector.util.getMonoError
import refrigeration.components.selector.util.getValueForKey

@Service
class PipeSizeEvaluation(private val pipeService: PipeService) : Evaluator {
    val pipeUtilities = PipeUtilities()
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

    override fun keyValuesAndTypes(): Map<String, String> {
        return mapOf(
            ComponentsConfig.pipeMaterial to "String",
            ComponentsConfig.maxVelocitySuction to "Double",
            ComponentsConfig.maxVelocityDischarge to "Double",
            ComponentsConfig.maxVelocityLiquid to "Double",
            ComponentsConfig.volumeFlow to "Double"
        )
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        return Flux.fromIterable(input).flatMap { evaluate(it) }
    }

    override fun outputTypes(): Map<String, String> {
        TODO("not yet implemented")
    }

    fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val materialError = "no pipe material found"
        val volumeFlowError = "no volume flow found"
        val material =
            getValueForKey<String>(input.anyInputs, ComponentsConfig.pipeMaterial)
                ?: return getMonoError(materialError, input, id)
        val volumeFlow = getDoubleValue(input.anyInputs, ComponentsConfig.volumeFlow)
            ?: return getMonoError(volumeFlowError, input, id)

        val maxVelocityMainSuction =
            getDoubleValue(input.anyInputs, ComponentsConfig.maxVelocitySuction) ?: return Mono.empty()
        val maxVelocityMainDischarge =
            getDoubleValue(input.anyInputs, ComponentsConfig.maxVelocityDischarge) ?: return Mono.empty()
        val maxVelocityMainLiquid =
            getDoubleValue(input.anyInputs, ComponentsConfig.maxVelocityLiquid) ?: return Mono.empty()

        val suction = sizePipe(maxVelocityMainSuction, volumeFlow)
        val discharge = sizePipe(maxVelocityMainDischarge, volumeFlow)
        val liquid = sizePipe(maxVelocityMainLiquid, volumeFlow)
        val pipeSizes = listOf(suction, discharge, liquid).sortedDescending().map { it }
        val lowerLimit = pipeSizes.last().minus(5)
        val upperLimit = pipeSizes.first().plus(5)

        return pipeService.findByInnerDiameterBetweenAndMaterial(lowerLimit, upperLimit, material)
            .collectList()
            .flatMap {
                getPipes(suction, discharge, liquid, it, volumeFlow, input)
            }
    }

    fun getPipes(
        suction: Double,
        discharge: Double,
        liquid: Double,
        sizes: List<PipeEntity>,
        volumeFlow: Double,
        input: EvaluationInput
    ): Mono<EvalResult> {
        val suctionSelected = pipeUtilities.findBestMatch(suction, sizes)
        val dischargeSelected = pipeUtilities.findBestMatch(discharge, sizes)
        val liquidSelected = pipeUtilities.findBestMatch(liquid, sizes)
        val result = mutableMapOf<String, Double>()
        if (suctionSelected != null) {
            val suctionVelocityCalculated =
                pipeUtilities.calculateRealVelocity(volumeFlow, suctionSelected.innerDiameter / 1000)
            result[ComponentsConfig.suctionVelocity] = suctionVelocityCalculated
            result[ComponentsConfig.suctionLineSize] = suctionSelected.outerDiameter
        }
        if (dischargeSelected != null) {
            val dischargeVelocityCalculated =
                pipeUtilities.calculateRealVelocity(volumeFlow, dischargeSelected.innerDiameter / 1000)
            result[ComponentsConfig.dischargeVelocity] = dischargeVelocityCalculated
            result[ComponentsConfig.dischargeLineSize] = dischargeSelected.outerDiameter
        }

        if (liquidSelected != null) {
            val liquidVelocityCalculated =
                pipeUtilities.calculateRealVelocity(volumeFlow, liquidSelected.innerDiameter / 1000)
            result[ComponentsConfig.liquidVelocity] = liquidVelocityCalculated
            result[ComponentsConfig.liquidLineSize] = liquidSelected.outerDiameter
        }
        val resultValues = listOf(ResultValues(id, result, mapOf()))
        val evalResult = EvalResult(EvalResultInfo.SUCCESS, input, resultValues, "")
        return Mono.just(evalResult)
    }

    fun sizePipe(maxVelocity: Double, volumeFlow: Double): Double {
        return pipeUtilities.sizePipe(maxVelocity, volumeFlow)
    }


    override fun wireInputs(requiredKeyMapping: Map<String, String>) {
        TODO("Not yet implemented")
    }

}
