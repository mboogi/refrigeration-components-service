package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.polynomials.CapacityPolynomialMapping

@Table(name = "capacity_polynomial_mapping")
data class CapacityPolynomialMappingEntity(
    @Id
    val id: Long,
    val capacity: Int,
    val polynomialId: Long
) {
    fun convert() = CapacityPolynomialMapping(id, capacity, polynomialId)

    companion object {
        fun convert(request: CapacityPolynomialMapping): CapacityPolynomialMappingEntity {
            return CapacityPolynomialMappingEntity(0L, request.capacity, request.polynomialId)
        }
    }
}
