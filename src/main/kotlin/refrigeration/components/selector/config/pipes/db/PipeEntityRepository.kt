package refrigeration.components.selector.config.pipes.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PipeEntityRepository : ReactiveCrudRepository<PipeEntity, String>
