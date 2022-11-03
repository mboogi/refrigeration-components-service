package refrigeration.components.selector.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EvaluationInput(
    @JsonProperty("evaluatorName")
    val evaluatorName: String,
    @JsonProperty("anyInputs")
    val anyInputs: Map<String, Any>
)
