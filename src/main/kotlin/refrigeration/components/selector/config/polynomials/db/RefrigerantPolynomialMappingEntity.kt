package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.polynomials.RefrigerantPolynomialMapping

@Table(name = "refrigerant_polynomial_mapping")
data class RefrigerantPolynomialMappingEntity(
    @Id
    val id: Long,
    val refrigerantType: String,
    val polynomialId: Long
) {
    fun convert() = RefrigerantPolynomialMapping(id, refrigerantType, polynomialId)

    companion object {
        fun convert(request: RefrigerantPolynomialMapping): RefrigerantPolynomialMappingEntity {
            return RefrigerantPolynomialMappingEntity(0L, request.refrigerantType, request.polynomialId)
        }
    }
}
