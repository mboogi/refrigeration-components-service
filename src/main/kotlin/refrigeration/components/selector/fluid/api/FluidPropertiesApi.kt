package refrigeration.components.selector.fluid.api

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FluidPropertiesApi {
    fun getFluids(): Flux<String>
    fun getNumericProperty(request: FluidPropertyRequest): Mono<Double>
    fun getNumericProperties(request: List<FluidPropertyRequest>): Flux<Double>
}
