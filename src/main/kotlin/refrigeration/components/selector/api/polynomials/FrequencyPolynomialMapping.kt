package refrigeration.components.selector.api.polynomials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class FrequencyPolynomialMapping(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("frequency")
    val frequency: Int,
    @JsonProperty("polynomialId")
    val polynomialId: Long
)
