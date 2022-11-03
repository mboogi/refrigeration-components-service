package refrigeration.components.selector.api.polynomials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CompressorPolynomialMapping(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("compressorType")
    val compressorType: String,
    @JsonProperty("polynomialId")
    val polynomialId: Long
)
