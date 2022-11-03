package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.polynomials.FrequencyPolynomialMapping

@Table(name = "frequency_polynomial_mapping")
data class FrequencyPolynomialMappingEntity(
    @Id
    val id: Long,
    val frequency: Int,
    val polynomialId: Long
) {
    fun convert() = FrequencyPolynomialMapping(id, frequency, polynomialId)

    companion object {
        fun convert(request: FrequencyPolynomialMapping): FrequencyPolynomialMappingEntity {
            return FrequencyPolynomialMappingEntity(0L, request.frequency, request.polynomialId)
        }
    }
}
