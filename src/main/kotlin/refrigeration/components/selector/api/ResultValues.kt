package refrigeration.components.selector.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResultValues(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("result")
    val result: Map<String, Any>,
    @JsonProperty("resultValuesMapping")
    val resultValuesMapping: Map<String, String>
)
