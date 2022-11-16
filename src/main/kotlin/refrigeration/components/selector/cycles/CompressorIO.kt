package refrigeration.components.selector.cycles

data class CompressorIO(
    val volumeFlow: Double,
    val massFlow: Double,
    val density: Double,
    val enthalpy: Double,
    val evaporationPressure: Double,
    val condensingPressure:Double,
    val refrigerantArea: String
)
