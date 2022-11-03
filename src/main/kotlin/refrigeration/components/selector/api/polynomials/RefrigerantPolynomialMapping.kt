package refrigeration.components.selector.api.polynomials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RefrigerantPolynomialMapping(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("refrigerantType")
    val refrigerantType: String,
    @JsonProperty("polynomialId")
    val polynomialId: Long
)
