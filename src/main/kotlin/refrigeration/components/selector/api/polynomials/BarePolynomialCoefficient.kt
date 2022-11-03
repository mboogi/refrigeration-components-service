package refrigeration.components.selector.api.polynomials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class BarePolynomialCoefficient(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("c1")
    val c1: Double,
    @JsonProperty("c2")
    val c2: Double,
    @JsonProperty("c3")
    val c3: Double,
    @JsonProperty("c4")
    val c4: Double,
    @JsonProperty("c5")
    val c5: Double,
    @JsonProperty("c6")
    val c6: Double,
    @JsonProperty("c7")
    val c7: Double,
    @JsonProperty("c8")
    val c8: Double,
    @JsonProperty("c9")
    val c9: Double,
    @JsonProperty("c10")
    val c10: Double

)
