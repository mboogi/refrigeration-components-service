package refrigeration.components.selector.fluid

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.fluid.api.FluidPropertyRequest

@RestController
class FluidPropertiesController(private val service: FluidPropertyService) {

    @GetMapping("fluid/available")
    fun getFluids(): Flux<String> {
        return service.getFluids()
    }

    @PostMapping("/fluid/property")
    fun getFluidProperty(@RequestBody fluidPropertyRequest: FluidPropertyRequest): Mono<Double> {
        return service.getNumericProperty(fluidPropertyRequest)
    }
}
