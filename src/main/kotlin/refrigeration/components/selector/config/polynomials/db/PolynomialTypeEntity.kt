package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.polynomials.PolynomialTypeMapping

@Table(name = "polynomial_type")
data class PolynomialTypeEntity(
    @Id
    val id: Long,
    val polynomialType: PolynomialTypesEnum,
    val polynomialId: Long
) {
    fun convert() = PolynomialTypeMapping(id, polynomialType, polynomialId)

    companion object {
        fun convert(request: PolynomialTypeMapping): PolynomialTypeEntity {
            return PolynomialTypeEntity(0L, request.type, request.polynomialId)
        }
    }
}
