package refrigeration.components.selector

import org.springframework.context.annotation.Configuration

@Configuration
class ComponentsConfig {
    companion object {
        val refrigerantKey = "refrigerant"
        val evapTempKey = "evapTemp"
        val condTempKey = "condTemp"
        val capacity = "capacity"
        val frequency = "frequency"
        val transCritical = "transCritical"
        val compressorType = "compressorType"
        val polynomialEvaluationValue = "evalValue"
    }
}
