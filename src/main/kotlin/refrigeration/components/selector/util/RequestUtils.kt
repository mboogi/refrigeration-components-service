package refrigeration.components.selector.util

import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.ResultValues

class RequestUtils

fun getRefrigerant(input: EvaluationInput): String? {
    return input.anyInputs[ComponentsConfig.refrigerantKey] as? String
}

fun getEvaporationTemperature(input: EvaluationInput): Double? {
    return input.anyInputs[ComponentsConfig.evapTempKey] as? Double
}
fun getEvaporationPressure(input: ResultValues): Double? {
    return input.result[ComponentsConfig.evaporationPressureKey] as? Double
}

fun getCondensingTemperature(input: EvaluationInput): Double? {
    return input.anyInputs[ComponentsConfig.condTempKey] as? Double
}
fun getCondensingPressure(input: ResultValues): Double? {
    return input.result[ComponentsConfig.condensingPressureKey] as? Double
}
fun getElectricPower(input: ResultValues): Double? {
    return input.result[ComponentsConfig.electricPowerKey] as? Double
}
fun getVolumeFlow(input: ResultValues): Double? {
    return input.result[ComponentsConfig.volumeFlow] as? Double
}

fun getCapacity(input: EvaluationInput): Double? {
    return input.anyInputs[ComponentsConfig.capacity] as? Double
}

fun getFrequency(input: EvaluationInput): Double? {
    return input.anyInputs[ComponentsConfig.frequency] as? Double
}

fun getTransCritical(input: EvaluationInput): Boolean? {
    return input.anyInputs[ComponentsConfig.transCritical] as? Boolean
}

fun getCompressorType(input: EvaluationInput): String? {
    return input.anyInputs[ComponentsConfig.compressorType] as? String
}

fun getSuperHeat(input: EvaluationInput): Double? {
    return input.anyInputs[ComponentsConfig.superheat] as? Double
}

fun getSubCool(input: EvaluationInput): Double? {
    return input.anyInputs[ComponentsConfig.subcool] as? Double
}

fun errorEvalResult(text: String, input: EvaluationInput, id: String): EvalResult {
    val result = ResultValues(id, mapOf(), mapOf())
    return EvalResult(EvalResultInfo.FAILURE, input, result, text)
}

fun getMonoError(msg: String, input: EvaluationInput, id: String): Mono<EvalResult> {
    return Mono.just(errorEvalResult(msg, input, id))
}
