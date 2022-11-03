package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.polynomials.CompressorPolynomialMapping

@Table(name = "compressor_polynomial_mappings")
data class CompressorPolynomialMappingEntity(
    @Id
    val id: Long,
    val compressorType: String,
    val polynomialId: Long
) {
    fun convert() = CompressorPolynomialMapping(id, compressorType, polynomialId)

    companion object {
        fun convert(request: CompressorPolynomialMapping): CompressorPolynomialMappingEntity {
            return CompressorPolynomialMappingEntity(0L, request.compressorType, request.polynomialId)
        }
    }
}
