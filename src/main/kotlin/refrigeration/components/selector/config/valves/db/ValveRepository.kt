package refrigeration.components.selector.config.valves.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ValveRepository : ReactiveCrudRepository<ValveEntity, Long> {

    fun findByRefrigerantAndRefrigerationCapacityBetween(
        refrigerant: String,
        refrigerationCapacityLower: Double,
        refrigerationCapacityUpper: Double
    ): Flux<ValveEntity>
//AndCondensingTemperatureGreaterThanEqualAndEvaporatingTemperatureGreaterThanEqual
    fun findByRefrigerantAndRefrigerationCapacityBetweenAndCondensingTemperatureGreaterThanEqualAndEvaporatingTemperatureGreaterThanEqual(
        refrigerant: String,
        refrigerationCapacityLower: Double,
        refrigerationCapacityUpper: Double,
        condensingTemperature:Double,
        evaporatingTemperature:Double
    ): Flux<ValveEntity>

    fun findByRefrigerantAndRefrigerationCapacityBetweenAndValveName(
        refrigerant: String,
        refrigerationCapacityLower: Double,
        refrigerationCapacityUpper: Double,
        valveName: String
    ): Flux<ValveEntity>
}
