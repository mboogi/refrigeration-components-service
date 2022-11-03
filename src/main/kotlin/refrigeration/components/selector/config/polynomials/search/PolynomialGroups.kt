package refrigeration.components.selector.config.polynomials.search

data class PolynomialGroups(
    val isEmpty: Boolean,
    val lowCapacity: Double?,
    val highCapacity: Double?,
    val lowFrequency: Double?,
    val highFrequency: Double?,

    val lowCapacityLowFrequency: PolynomialSearchGroup,
    val lowCapacityHighFrequency: PolynomialSearchGroup,
    val highCapacityLowFrequency: PolynomialSearchGroup,
    val highCapacityHighFrequency: PolynomialSearchGroup
)
