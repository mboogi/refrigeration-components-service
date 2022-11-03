package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("polynomial_mappings_view")
data class PolynomialSearchResult(
    @Id
    val id: Long,
    val polynomialId: Long,
    val refrigerantType: String,
    val compressorType: String,
    val capacity: Double,
    val frequency: Double,
    val transCritical: Boolean,
    val polynomialType: String
)
