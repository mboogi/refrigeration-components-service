package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PolynomialCoefficientsRepository : ReactiveCrudRepository<PolynomialCoefficientsEntity, Long> {
    fun findByIdIn(ids: List<Long>): Flux<PolynomialCoefficientsEntity>
}
