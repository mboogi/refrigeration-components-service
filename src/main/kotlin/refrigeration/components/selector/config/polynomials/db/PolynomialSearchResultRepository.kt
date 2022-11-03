package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface PolynomialSearchResultRepository : ReactiveCrudRepository<PolynomialSearchResult, Long> {

    fun findByCompressorTypeAndRefrigerantTypeAndTransCriticalAndPolynomialType(
        compressor: String,
        refrigerant: String,
        transCritical: Boolean,
        polynomialType: String
    ): Flux<PolynomialSearchResult>
}
