package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.polynomials.OperationTypePolynomialMapping

@Table(name = "operation_type_polynomial_mapping")
data class OperationTypePolynomialMappingEntity(
    @Id
    val id: Long,
    val transCritical: Boolean,
    val polynomialId: Long
) {
    fun convert() = OperationTypePolynomialMapping(id, transCritical, polynomialId)

    companion object {
        fun convert(request: OperationTypePolynomialMapping): OperationTypePolynomialMappingEntity {
            return OperationTypePolynomialMappingEntity(0L, request.transCritical, request.polynomialId)
        }
    }
}
