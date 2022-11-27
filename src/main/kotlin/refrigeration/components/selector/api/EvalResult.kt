
package refrigeration.components.selector.api
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EvalResult(
    @JsonProperty("evalInfo")
    val evalInfo: EvalResultInfo,
    @JsonProperty("input")
    val input: EvaluationInput,
    @JsonProperty("resultValues")
    val resultValues: List<ResultValues>,
    @JsonProperty("evalInfoMessage")
    val evalInfoMessage: String
)
