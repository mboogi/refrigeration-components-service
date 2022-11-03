package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.polynomials.BarePolynomialCoefficient
import refrigeration.components.selector.api.polynomials.PolynomialCoefficientRequest

@Table(name = "polynomial_coefficients")
data class PolynomialCoefficientsEntity(
    @Id
    val id: Long,
    val description: String,
    val c1: Double,
    val c2: Double,
    val c3: Double,
    val c4: Double,
    val c5: Double,
    val c6: Double,
    val c7: Double,
    val c8: Double,
    val c9: Double,
    val c10: Double
) {
    fun convert() = BarePolynomialCoefficient(id, description, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10)

    companion object {
        fun convert(request: PolynomialCoefficientRequest): PolynomialCoefficientsEntity {
            return PolynomialCoefficientsEntity(
                request.id,
                request.description,
                request.c1,
                request.c2,
                request.c3,
                request.c4,
                request.c5,
                request.c6,
                request.c7,
                request.c8,
                request.c9,
                request.c10
            )
        }
    }
}
