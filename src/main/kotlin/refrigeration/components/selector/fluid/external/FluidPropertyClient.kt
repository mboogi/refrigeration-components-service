package refrigeration.components.selector.fluid.external

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.fluid.api.FluidPropertiesApi
import refrigeration.components.selector.fluid.api.FluidPropertyRequest

@Service
class FluidPropertyClient : FluidPropertiesApi {
    private val webClient = WebClient.create("http://localhost:5000")
    override fun getFluids(): Flux<String> {
        return webClient
            .get()
            .uri("/fluids")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(String::class.java)
            .onErrorResume { Flux.empty() }
    }

    override fun getNumericProperty(request: FluidPropertyRequest): Mono<Double> {
        val result = webClient
            .get().uri { it ->
                it.path("/property")
                    .queryParam("name1", request.name1)
                    .queryParam("name2", request.name2)
                    .queryParam("output", request.output)
                    .queryParam("prop1", request.prop1)
                    .queryParam("prop2", request.prop2)
                    .queryParam("fluid", request.fluid)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<Double>()
            .onErrorComplete()
        return result
    }

    override fun getNumericProperties(request: List<FluidPropertyRequest>): Flux<Double> {
        TODO("Not yet implemented")
    }
}
