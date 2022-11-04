package refrigeration.components.selector.fluid

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.fluid.api.FluidPropertiesApi
import refrigeration.components.selector.fluid.api.FluidPropertyRequest
import refrigeration.components.selector.fluid.external.FluidPropertyClient

@Service
class FluidPropertyService(private val client: FluidPropertyClient) : FluidPropertiesApi {

    override fun getFluids(): Flux<String> {
        return client.getFluids()
    }

    override fun getNumericProperty(request: FluidPropertyRequest): Mono<Double> {
        return client.getNumericProperty(request)
    }

    override fun getNumericProperties(request: List<FluidPropertyRequest>): Flux<Double> {
        return client.getNumericProperties(request)
    }
}
