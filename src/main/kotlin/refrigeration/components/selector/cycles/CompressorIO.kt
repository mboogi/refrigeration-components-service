package refrigeration.components.selector.cycles

data class CompressorIO(
    val volumeFlow: Double,
    val massFlow: Double,
    val density: Double,
    val enthalpy: Double,
    val pressure: Double,
    val refrigerantArea: String
)
