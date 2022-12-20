package refrigeration.components.selector.config.valves.crud

import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.api.valves.Valve

@RestController
@RequestMapping("api/config/valve")
class ValveController(private val service: ValveService) {
    @GetMapping
    fun getAll(): Flux<Valve> {
        return service.findAll()
    }

    @PostMapping("file/upload"/*, consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]*/)
    fun fileUpload(@RequestPart("file") file: Mono<FilePart>): String {
        service.saveValvesFromFile(file)
        return "fasdasdawsd"
    }

    @PostMapping
    fun saveAll(@RequestBody valves: List<Valve>): Flux<Valve> {
        return service.saveAll(valves)
    }

    @GetMapping("/refrigerant/nominal-capacity")
    fun findByRefrigerantAndCapacity(refrigerant: String, minCapacity: Double, maxCapacity: Double): Flux<Valve> {
        return service.findByRefrigerantAndCapacity(refrigerant, minCapacity, maxCapacity)
    }
}
