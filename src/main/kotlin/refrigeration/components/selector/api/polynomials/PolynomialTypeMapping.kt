package refrigeration.components.selector.api.polynomials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PolynomialTypeMapping(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("type")
    val type: PolynomialTypesEnum,
    @JsonProperty("polynomialId")
    val polynomialId: Long
)
