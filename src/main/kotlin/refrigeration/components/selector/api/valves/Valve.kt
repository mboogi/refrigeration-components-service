package refrigeration.components.selector.api.valves

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Valve(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("refrigerant")
    val refrigerant: String,
    @JsonProperty("valveName")
    val valveName: String,
    @JsonProperty("pressureDrop")
    val pressureDrop: Double,
    @JsonProperty("kvs")
    val kvs: Double,
    @JsonProperty("condensingTemperature")
    val condensingTemperature: Double,
    @JsonProperty("evaporatingTemperature")
    val evaporatingTemperature: Double,
    @JsonProperty("maxPressure")
    val maxPressure: Double,
    @JsonProperty("type")
    val type: ValveType,
    @JsonProperty("flowPattern")
    val flowPattern: FlowPattern,
    @JsonProperty("capacityRange")
    val capacityRange: Double,
    @JsonProperty("inletConnection")
    val inletConnection: String,
    @JsonProperty("outletConnection")
    val outletConnection: String,
    @JsonProperty("maxLiquidTemperature")
    val maxLiquidTemperature: Double,
    @JsonProperty("maxGasTemperature")
    val maxGasTemperature: Double,
    @JsonProperty("refrigerationCapacity")
    val refrigerationCapacity: Double
)
