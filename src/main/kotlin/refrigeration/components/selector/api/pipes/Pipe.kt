package refrigeration.components.selector.api.pipes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Pipe(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("standard")
    val standard: String,
    @JsonProperty("outerDiameter")
    val outerDiameter: Double,
    @JsonProperty("innerDiameter")
    val innerDiameter: Double,
    @JsonProperty("material")
    val material: String,
    @JsonProperty("maxPressure")
    val maxPressure: Double
)
