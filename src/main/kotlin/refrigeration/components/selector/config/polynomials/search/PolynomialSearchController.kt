package refrigeration.components.selector.config.polynomials.search

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.api.polynomials.PolynomialCoefficientRequest
import refrigeration.components.selector.config.polynomials.db.PolynomialCoefficientsEntity
import refrigeration.components.selector.config.polynomials.db.PolynomialSearchResult

@RestController
class PolynomialSearchController(private val service: PolynomialCoefficientsService) : PolynomialApi {

    @GetMapping("polynomials")
    override fun findAll(): Flux<PolynomialCoefficientsEntity> {
        return service.findAllPolynomialMappings()
    }

    @GetMapping("/polynomials/compressor/refrigerant")
    fun findByCompressorAndRefrigerant(
        @RequestParam compressor: String,
        @RequestParam refrigerant: String,
        @RequestParam transCritical: Boolean,
        @RequestParam polynomialType: String
    ): Flux<PolynomialSearchResult> {
        return service.findPolynomialsForCompressorAndRefrigerant(compressor, refrigerant, transCritical, polynomialType)
    }

    @PostMapping("polynomials")
    override fun saveAll(@RequestBody requests: List<PolynomialCoefficientRequest>): Flux<Pair<PolynomialCoefficientsEntity, PolynomialCoefficientRequest>> {
        return service.save(requests)
    }

    @GetMapping("polynomials/{id}")
    override fun findPolynomialById(@PathVariable id: Long): Mono<PolynomialCoefficientsEntity> {
        return service.getPolynomialById(id)
    }
}
