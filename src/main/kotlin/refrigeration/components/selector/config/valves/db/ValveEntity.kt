package refrigeration.components.selector.config.valves.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.valves.FlowPattern
import refrigeration.components.selector.api.valves.Valve
import refrigeration.components.selector.api.valves.ValveType

@Table(name = "valve_entity")
data class ValveEntity(
    @Id
    val id: Long,
    val refrigerant: String,
    val valveName: String,
    val pressureDrop: Double,
    val kvs: Double,
    val condensingTemperature: Double,
    val evaporatingTemperature: Double,
    val maxPressure: Double,
    val type: ValveType,
    val flowPattern: FlowPattern,
    val capacityRange: Double,
    val inletConnection: String,
    val outletConnection: String,
    val maxLiquidTemperature: Double,
    val maxGasTemperature: Double,
    val refrigerationCapacity: Double
) {

    companion object {
        fun convert(request: Valve): ValveEntity {
            return ValveEntity(
                request.id,
                request.refrigerant,
                request.valveName,
                request.pressureDrop,
                request.kvs,
                request.condensingTemperature,
                request.evaporatingTemperature,
                request.maxPressure,
                request.type,
                request.flowPattern,
                request.capacityRange,
                request.inletConnection,
                request.outletConnection,
                request.maxLiquidTemperature,
                request.maxGasTemperature,
                request.refrigerationCapacity
            )
        }
    }

    fun convert() = Valve(
        id,
        refrigerant,
        valveName,
        pressureDrop,
        kvs,
        condensingTemperature,
        evaporatingTemperature,
        maxPressure,
        type,
        flowPattern,
        capacityRange,
        inletConnection,
        outletConnection,
        maxLiquidTemperature,
        maxGasTemperature,
        refrigerationCapacity
    )
}
