package refrigeration.components.selector.config.valves.crud

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import refrigeration.components.selector.api.valves.Valve
import refrigeration.components.selector.config.valves.db.ValveEntity
import refrigeration.components.selector.config.valves.db.ValveRepository

@Service
class ValveService(private val repo: ValveRepository) {

    fun saveAll(entities: List<Valve>): Flux<Valve> {
        val converted = entities.map { ValveEntity.convert(it) }
        val monoEntities = Flux.fromIterable(converted)
        return repo.saveAll(monoEntities).map { it.convert() }
    }

    fun findAll(): Flux<Valve> {
        return repo.findAll().map { it.convert() }
    }
}
