package refrigeration.components.selector.config.polynomials.crud

data class PolynomialGroups<T>(
    val isEmpty: Boolean,
    val xValueName: String,
    val yValueName: String,
    val lowX: Double?,
    val highX: Double?,
    val lowY: Double?,
    val highY: Double?,

    val lowXLowY: T,
    val lowXHighY: T,
    val highXLowY: T,
    val highXHighY: T
)
