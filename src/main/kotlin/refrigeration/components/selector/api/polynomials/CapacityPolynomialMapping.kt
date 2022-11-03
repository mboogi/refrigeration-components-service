package refrigeration.components.selector.api.polynomials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CapacityPolynomialMapping(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("capacity")
    val capacity: Int,
    @JsonProperty("polynomialId")
    val polynomialId: Long
)
