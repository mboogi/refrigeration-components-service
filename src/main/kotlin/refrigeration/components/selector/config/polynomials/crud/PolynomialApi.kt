package refrigeration.components.selector.config.polynomials.crud

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.api.polynomials.PolynomialCoefficientRequest
import refrigeration.components.selector.config.polynomials.db.PolynomialCoefficientsEntity

interface PolynomialApi {
    fun findAll(): Flux<PolynomialCoefficientsEntity>

    fun saveAll(requests: List<PolynomialCoefficientRequest>): Flux<Pair<PolynomialCoefficientsEntity, PolynomialCoefficientRequest>>

    fun findPolynomialById(id: Long): Mono<PolynomialCoefficientsEntity>
}
