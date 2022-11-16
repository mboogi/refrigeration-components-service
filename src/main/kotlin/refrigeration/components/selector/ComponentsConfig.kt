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
        val evalValue = "evalValue"
        val superheat = "superheat"
        val subcool = "subcool"
        val duration = 3L
        val condensingPressureKey="condensingPressure"
        val evaporationPressureKey="evaporationPressure"
        val enthalpyAtInletKey="enthalpyAtInlet"
        val densityAtInletKey="enthalpyAtInlet"
        val massFlowKey="massFlow"
        val electricPowerKey="electricPower"
    }
}
