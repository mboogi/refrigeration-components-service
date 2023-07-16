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
    val number= input[ComponentsConfig.evapTempKey]
    return getDouble(number)
}

fun getEvaporationPressure(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.evaporationPressureKey]
    return getDouble(number)
}

fun getCondensingTemperature(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.condTempKey]
    return getDouble(number)
}

fun getCondensingPressure(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.condensingPressureKey]
    return getDouble(number)
}

fun getElectricPower(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.electricPowerKey]
    return getDouble(number)
}

fun getRefrigerationPower(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.evaporatorPower]
    return getDouble(number)
}
fun getCondenserPower(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.condenserPower]
    return getDouble(number)
}

fun getVolumeFlow(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.volumeFlow]
    return getDouble(number)
}

fun getRealMassFlow(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.massFlowRealKeyStandard]
    return getDouble(number)
}

fun getCapacity(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.capacity]
    return getDouble(number)
}

fun getFrequency(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.frequency]
    return getDouble(number)
}

fun getTransCritical(input: Map<String, Any>): Boolean? {
    return input[ComponentsConfig.transCritical] as? Boolean
}

fun getCompressorType(input: Map<String, Any>): String? {
    return input[ComponentsConfig.compressorType] as? String
}

fun getCompressorOutletTemperature(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.compressorOutletTemperature]
    return getDouble(number)
}

fun getSuperHeat(input: Map<String, Any>): Double? {
    val number = input[ComponentsConfig.superheat]
    return getDouble(number)
}

fun getSubCool(input: Map<String, Any>): Double? {
    val number= input[ComponentsConfig.subcool]
    return getDouble(number)
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
fun getDoubleValue(input: Map<String, Any>, key: String):Double?{
    val number= input[key]?:return null
    return getDouble(number)
}

private fun getDouble(number: Any?): Double? {
    number ?: return null
    val isInt = number is Int
    if (isInt) {
        return (number as Int).toDouble()
    } else {
        return number as? Double
    }
}
