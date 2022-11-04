package refrigeration.components.selector.fluid.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class FluidPropertyRequest(
    val output: String,
    val name1: String,
    val prop1: Double,
    val name2: String,
    val prop2: Double,
    val fluid: String
)
