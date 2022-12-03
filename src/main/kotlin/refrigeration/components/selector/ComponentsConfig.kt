package refrigeration.components.selector

import org.springframework.context.annotation.Configuration

@Configuration
class ComponentsConfig {
    companion object {
        const val liquidVelocity="liquidVelocity"
        const val dischargeVelocity="dischargeVelocity"
        const val suctionVelocity="suctionVelocity"
        const val suctionLineSize = "suctionSize"
        const val liquidLineSize = "liquidLineSize"
        const val dischargeLineSize = "dischargeLineSize"
        const val maxVelocitySuction = "maxVelocitySuctionLine"
        const val maxVelocityLiquid = "maximalVelocityLiquidLine"
        const val maxVelocityDischarge = "maximalVelocityDischargeLine"
        const val pipeMaterial = "pipeMaterial"
        const val evaporatorPower = "evaporatorPower"
        const val condenserPower = "condenserPower"
        const val refrigerantKey = "refrigerant"
        const val evapTempKey = "evapTemp"
        const val condTempKey = "condTemp"
        const val capacity = "capacity"
        const val frequency = "frequency"
        const val transCritical = "transCritical"
        const val compressorType = "compressorType"
        const val evalValue = "evalValue"
        const val superheat = "superheat"
        const val subcool = "subcool"

        const val criticalPointR744 = 80.0
        const val condensingPressureKey = "condensingPressure"
        const val evaporationPressureKey = "evaporationPressure"
        const val enthalpyAtInletStandardKey = "enthalpyAtInlet"
        const val densityAtInletStandardKey = "densityAtInlet"
        const val massFlowKeyStandard = "massFlow"
        const val massFlowRealKeyStandard = "massFlowReal"
        const val electricPowerKey = "electricPower"
        const val volumeFlow = "volumeFlow"

        const val enthalpyRealConditionsKey = "enthalpyRealConditions"
        const val endEnthalpyRealConditions = "endEnthalpyRealConditions"
        const val compressorOutletTemperature = "compressorOutletTemperature"
    }
}
