package refrigeration.components.selector.config.pipes.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PipeEntityRepository : ReactiveCrudRepository<PipeEntity, Long> {
    fun findByInnerDiameterBetween(lowerValue: Double, upperValue: Double): Flux<PipeEntity>
    fun findByInnerDiameterBetweenAndMaterial(lowerValue: Double, upperValue: Double, material: String): Flux<PipeEntity>
}
