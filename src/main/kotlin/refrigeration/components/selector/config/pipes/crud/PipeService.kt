package refrigeration.components.selector.config.pipes.crud

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import refrigeration.components.selector.api.pipes.Pipe
import refrigeration.components.selector.config.pipes.db.PipeEntity
import refrigeration.components.selector.config.pipes.db.PipeEntityRepository

@Service
class PipeService(private val pipeEntityRepository: PipeEntityRepository) {

    fun saveAll(entities: List<Pipe>): Flux<PipeEntity> {
        val pipes = entities.map { PipeEntity.convert(it) }
        return pipeEntityRepository.saveAll(pipes)
    }

    fun update(pipe: Pipe) {
        pipeEntityRepository.deleteById(pipe.id)
        pipeEntityRepository.save(PipeEntity.convert(pipe))
    }

    fun findAll(): Flux<PipeEntity> {
        return pipeEntityRepository.findAll()
    }

    fun findByInnerDiameterBetween(lowerValue: Double, upperValue: Double): Flux<PipeEntity> {
        return pipeEntityRepository.findByInnerDiameterBetween(lowerValue, upperValue)
    }

    fun findByInnerDiameterBetweenAndMaterial(lowerValue: Double, upperValue: Double, material: String): Flux<PipeEntity> {
        return pipeEntityRepository.findByInnerDiameterBetweenAndMaterial(lowerValue, upperValue, material)
    }
}
