package refrigeration.components.selector.config.valves.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ValveRepository : ReactiveCrudRepository<ValveEntity, Long> {

//    fun findByRefrigerantAndRefrigerationcapacityAndMaxpressureAndTypeAndFlowpattern(
//        refrigerant: String,
//        refrigerationCapacity:Double,
//
//
//    ): Flux<ValveEntity>
}
