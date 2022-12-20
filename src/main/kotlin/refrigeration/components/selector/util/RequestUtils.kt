package refrigeration.components.selector.util

import reactor.core.publisher.Mono
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.ResultValues

class RequestUtils

fun getRefrigerant(input: Map<String, Any>): String? {
    return input[ComponentsConfig.refrigerantKey] as? String
}

fun getEvaporationTemperature(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.evapTempKey] as? Double
}

fun getEvaporationPressure(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.evaporationPressureKey] as? Double
}

fun getCondensingTemperature(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.condTempKey] as? Double
}

fun getCondensingPressure(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.condensingPressureKey] as? Double
}

fun getElectricPower(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.electricPowerKey] as? Double
}

fun getRefrigerationPower(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.evaporatorPower] as? Double
}

fun getVolumeFlow(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.volumeFlow] as? Double
}

fun getRealMassFlow(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.massFlowRealKeyStandard] as? Double
}

fun getCapacity(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.capacity] as? Double
}

fun getFrequency(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.frequency] as? Double
}

fun getTransCritical(input: Map<String, Any>): Boolean? {
    return input[ComponentsConfig.transCritical] as? Boolean
}

fun getCompressorType(input: Map<String, Any>): String? {
    return input[ComponentsConfig.compressorType] as? String
}

fun getCompressorOutletTemperature(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.compressorOutletTemperature] as? Double
}

fun getSuperHeat(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.superheat] as? Double
}

fun getSubCool(input: Map<String, Any>): Double? {
    return input[ComponentsConfig.subcool] as? Double
}

fun errorEvalResult(text: String, input: EvaluationInput, id: String): EvalResult {
    val result = ResultValues(id, mapOf(), mapOf())
    return EvalResult(EvalResultInfo.FAILURE, input, listOf(result), text)
}

fun getMonoError(msg: String, input: EvaluationInput, id: String): Mono<EvalResult> {
    return Mono.just(errorEvalResult(msg, input, id))
}

fun <T> getValueForKey(values: Map<String, Any>, key: String): T? {
    return values[key] as? T?
}
