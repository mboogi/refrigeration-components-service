package refrigeration.components.selector.api.polynomials

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum

@JsonIgnoreProperties(ignoreUnknown = true)
data class PolynomialCoefficientRequest(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("compressorType")
    val compressorType: String,
    @JsonProperty("refrigerantType")
    val refrigerantType: String,
    @JsonProperty("capacity")
    val capacity: Int,
    @JsonProperty("frequency")
    val frequency: Int,
    @JsonProperty("transCritical")
    val transCritical: Boolean,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("polynomialType")
    val polynomialType: PolynomialTypesEnum,
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
