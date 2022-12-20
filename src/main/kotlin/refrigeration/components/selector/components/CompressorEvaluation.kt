package refrigeration.components.selector.components

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.*
import refrigeration.components.selector.config.polynomials.crud.PolynomialCoefficientsService
import refrigeration.components.selector.config.polynomials.crud.PolynomialSearchService
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum
import refrigeration.components.selector.config.polynomials.eval.PolynomialEvaluationService
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.util.*
import java.math.BigDecimal

@Service
class CompressorEvaluation(
    private val searchService: PolynomialSearchService,
    private val coefficientsService: PolynomialCoefficientsService,
    private val fluidsService: FluidPropertyService
) : Evaluator {
    companion object {
        val logger = LoggerFactory.getLogger(CompressorEvaluation::class.java)
    }

    override var id: String = "default"
    override fun setUniqueId(id: String) {
        this.id = id
    }

    private val massFlowPolynomialEval =
        PolynomialEvaluationService(
            PolynomialTypesEnum.MASS_FLOW.toString(),
            searchService,
            coefficientsService
        )

    private val electricPowerPolynomialEval =
        PolynomialEvaluationService(
            PolynomialTypesEnum.ELECTRIC_POWER.toString(),
            searchService,
            coefficientsService
        )

    override fun privateEvaluation(): Boolean {
        return false
    }

    override fun getName(): String {
        return "CompressorEvaluation"
    }

    override fun getRequiredInputKeys(): Set<String> {
        return setOf(
            ComponentsConfig.refrigerantKey,
            ComponentsConfig.evapTempKey,
            ComponentsConfig.condTempKey,
            ComponentsConfig.capacity,
            ComponentsConfig.frequency,
            ComponentsConfig.transCritical,
            ComponentsConfig.compressorType,
            ComponentsConfig.subcool,
            ComponentsConfig.superheat
        )
    }

    override fun evaluate(input: List<EvaluationInput>): Flux<EvalResult> {
        return Flux.fromIterable(input).flatMap { evaluate(it) }
    }

    override fun outputValues(): Set<String> {
        return setOf(
            ComponentsConfig.volumeFlow,
            ComponentsConfig.massFlowKeyStandard,
            ComponentsConfig.electricPowerKey,
            ComponentsConfig.densityAtInletStandardKey,
            ComponentsConfig.enthalpyAtInletStandardKey,
            ComponentsConfig.evaporationPressureKey,
            ComponentsConfig.condensingPressureKey,
            ComponentsConfig.endEnthalpyRealConditions,
            ComponentsConfig.massFlowRealKeyStandard,
            ComponentsConfig.compressorOutletTemperature
        )
    }

    override fun outputTypes(): Map<String, String> {
        val doubleType = Double::class.simpleName ?: throw RuntimeException("Double type simple name not found")
        return mapOf(
            ComponentsConfig.volumeFlow to doubleType,
            ComponentsConfig.massFlowKeyStandard to doubleType,
            ComponentsConfig.electricPowerKey to doubleType,
            ComponentsConfig.densityAtInletStandardKey to doubleType,
            ComponentsConfig.enthalpyAtInletStandardKey to doubleType,
            ComponentsConfig.evaporationPressureKey to doubleType,
            ComponentsConfig.condensingPressureKey to doubleType,
            ComponentsConfig.endEnthalpyRealConditions to doubleType,
            ComponentsConfig.massFlowRealKeyStandard to doubleType,
            ComponentsConfig.compressorOutletTemperature to doubleType
        )
    }

    override fun wireInputs(requiredKeyMapping: Map<String, String>) {
        TODO("Not yet implemented")
    }

    fun evaluate(input: EvaluationInput): Mono<EvalResult> {
        val initialEval = initialEvaluation(input)
        val superHeat = getSuperHeat(input.anyInputs) ?: return getMonoError("superheat not found", input, id)
        val subCool = getSubCool(input.anyInputs) ?: return getMonoError("subcool not found", input, id)
        if ((superHeat < 0.0) or (subCool < 0.0)) return getMonoError("superheat or subcool lesser than 0.0", input, id)
        return realConditionsEvaluation(initialEval)
    }

    private fun realConditionsEvaluation(initialEval: Mono<EvalResult>): Mono<EvalResult> {
        return initialEval.flatMap { evalRealConditions(it) }
    }

    private fun evalRealConditions(evalResult: EvalResult): Mono<EvalResult> {
        val superheat =
            getSuperHeat(evalResult.input.anyInputs) ?: return getMonoError("superheat not found", evalResult.input, id)
        val refrigerant = getRefrigerant(evalResult.input.anyInputs) ?: return getMonoError(
            "refrigerant not found",
            evalResult.input,
            id
        )
        val evapTemp = getEvaporationTemperature(evalResult.input.anyInputs)
            ?: return getMonoError("evaporation temperature not found", evalResult.input, id)

        val evaporationPressure = getEvaporationPressure(evalResult.resultValues.first()?.result)
            ?: return getMonoError("evaporation pressure not found", evalResult.input, id)
        val condensingPressure = getCondensingPressure(evalResult.resultValues.first()?.result)
            ?: return getMonoError("condensing pressure not found", evalResult.input, id)
        val electricInput = getElectricPower(evalResult.resultValues.first()?.result) ?: return getMonoError(
            "electric power not found",
            evalResult.input,
            id
        )

        val inletTemp = evapTemp + 273.15 + superheat
        val densityInputRealConditions =
            fluidsService.getVapourDensity(superheat, refrigerant, inletTemp, evaporationPressure)
        val enthalpyInputRealConditions =
            fluidsService.getVapourEnthalpy(superheat, refrigerant, inletTemp, evaporationPressure)
        val volumeFlow = getVolumeFlow(evalResult.resultValues.first()?.result) ?: return Mono.empty()

        val massFlowReal = densityInputRealConditions.flatMap { massFlowRealConditions(volumeFlow, it) }

        val enthalpyDifferenceRealStandard = massFlowReal
            .flatMap { Mono.just(Pair(enthalpyDifference(electricInput, it), it)) }

        val endEnthalpy = Mono
            .zip(enthalpyInputRealConditions, enthalpyDifferenceRealStandard)
            .map {
                val difference = it.t1
                val suction = it.t2.first
                val endEnthalpy = (difference + suction)
                mapOf("endEnthalpy" to endEnthalpy, ComponentsConfig.massFlowRealKeyStandard to it.t2.second)
            }

        val compressorOutletTemperature = endEnthalpy
            .flatMap { it ->
                val newValuesMap = mutableMapOf<String, Double>()
                newValuesMap.putAll(it)
                fluidsService.getTemperatureForSuperheatedEnthalpy(
                    it["endEnthalpy"]!!,
                    condensingPressure,
                    refrigerant
                )
                    .map { et ->
                        appendResultsMap(newValuesMap, ComponentsConfig.compressorOutletTemperature, et)
                    }
            }
        val result = compressorOutletTemperature
            .map {
                appendResult(
                    evalResult,
                    it["endEnthalpy"]!!,
                    it[ComponentsConfig.massFlowRealKeyStandard]!!,
                    it[ComponentsConfig.compressorOutletTemperature]!!
                )
            }
        return result
    }

    private fun appendResultsMap(
        resultsMap: MutableMap<String, Double>,
        key: String,
        value: Double
    ): Map<String, Double> {
        resultsMap[key] = value
        return resultsMap.toMap()
    }

    private fun appendResult(
        evalResult: EvalResult,
        endEnthalpy: Double,
        massFlowRealConditions: Double,
        compressorOutletTemperature: Double
    ): EvalResult {
        val oldInputMap = evalResult.resultValues.first()?.result
        val newResultMap = mutableMapOf<String, Any>()
        newResultMap.putAll(oldInputMap)
        newResultMap[ComponentsConfig.endEnthalpyRealConditions] = endEnthalpy
        newResultMap[ComponentsConfig.massFlowRealKeyStandard] = massFlowRealConditions
        newResultMap[ComponentsConfig.compressorOutletTemperature] = compressorOutletTemperature
        val resultValues = ResultValues(evalResult.resultValues.first()?.id, newResultMap, outputTypes())
        return EvalResult(evalResult.evalInfo, evalResult.input, listOf(resultValues), evalResult.evalInfoMessage)
    }

    private fun enthalpyDifference(electricInput: Double, massFlowRealConditions: Double): Double {
        val result = (electricInput) / (massFlowRealConditions / 3600)
        return result
    }

    private fun massFlowRealConditions(volumeFlow: Double, suctionDensityReal: Double): Mono<Double> {
        val result = volumeFlow * suctionDensityReal
        return Mono
            .just(result)
    }

    private fun initialEvaluation(input: EvaluationInput): Mono<EvalResult> {
        val evapTemp = getEvaporationTemperature(input.anyInputs) ?: return Mono.empty()
        val condensingTemperature = getCondensingTemperature(input.anyInputs) ?: return Mono.empty()
        val refrigerant = getRefrigerant(input.anyInputs) ?: return Mono.empty()
        val checkTransCriticalHere = false

        val massFlow =
            massFlowPolynomialEval.evaluate(listOf(input)).next()
        val electricPower =
            electricPowerPolynomialEval.evaluate(listOf(input)).next()

        val inletTemperature = 293.15

        val evaporationPressure =
            fluidsService.getDryVapourPressure(evapTemp + 273.15, refrigerant)

        val enthalpyAtInlet = evaporationPressure
            .flatMap {
                fluidsService
                    .getSuperHeatedVapourEnthalpy(inletTemperature, it, refrigerant)
            }

        val densityAtInlet = evaporationPressure
            .flatMap {
                fluidsService.getSuperHeatedVapourDensity(inletTemperature, it, refrigerant)
            }
        val condensingPressure =
            fluidsService.getDryVapourPressure(condensingTemperature + 273.15, refrigerant)

        val compressorIOEvaluation =
            Mono.zip(densityAtInlet, massFlow, enthalpyAtInlet, condensingPressure, evaporationPressure, electricPower)
                .map { t ->
                    val density = t.t1
                    val massFlowBigDecimal =
                        t.t2.resultValues.first()?.result[ComponentsConfig.evalValue] as? BigDecimal
                            ?: throw RuntimeException("mass flow value could not be calculated from eval result")
                    val massflowValue = (massFlowBigDecimal.toDouble())
                    val volumetricFlow = massflowValue.div(density)
                    val enthalpy = t.t3
                    val condensingPressure = t.t4
                    val evapPressure = t.t5
                    val electricPower = t.t6.resultValues.first()?.result[ComponentsConfig.evalValue] as? BigDecimal
                        ?: throw RuntimeException("Electric Power value could not be calculated from eval result")
                    val electricPowerValue = electricPower.toDouble()

                    CompressorIO(
                        volumetricFlow,
                        massflowValue,
                        density,
                        enthalpy,
                        evapPressure,
                        condensingPressure,
                        "superheatedVapour",
                        electricPowerValue
                    )
                }
        return compressorIOEvaluation.map { getEvalResult(it, input) }
    }

    private fun getEvalResult(
        compressorIO: CompressorIO,
        input: EvaluationInput
    ): EvalResult {
        return EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            listOf(getResultValues(compressorIO)),
            "tempadkasdjas"
        )
    }

    private fun getResultValues(compressorIO: CompressorIO): ResultValues {
        val map = mapOf(
            ComponentsConfig.volumeFlow to compressorIO.volumeFlow,
            ComponentsConfig.massFlowKeyStandard to compressorIO.massFlow,
            ComponentsConfig.electricPowerKey to compressorIO.electricPower,
            ComponentsConfig.densityAtInletStandardKey to compressorIO.density,
            ComponentsConfig.enthalpyAtInletStandardKey to compressorIO.enthalpy,
            ComponentsConfig.evaporationPressureKey to compressorIO.evaporationPressure,
            ComponentsConfig.condensingPressureKey to compressorIO.condensingPressure
        )
        return ResultValues(id, map, mapOf())
    }
}
