package refrigeration.components.selector.config.pipes.crud

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import refrigeration.components.selector.config.pipes.db.PipeEntity
import refrigeration.components.selector.config.pipes.db.PipeEntityRepository

@Service
class PipeService(private val pipeEntityRepository: PipeEntityRepository) {

    fun findAll(): Flux<PipeEntity> {
        return pipeEntityRepository.findAll()
    }
}
