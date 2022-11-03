package refrigeration.components.selector.api.polynomials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OperationTypePolynomialMapping(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("transCritical")
    val transCritical: Boolean,
    @JsonProperty("polynomialId")
    val polynomialId: Long
)
