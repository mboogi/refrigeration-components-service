package refrigeration.components.selector.config.pipes.crud

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import refrigeration.components.selector.api.pipes.Pipe

@RestController
class PipeController(private val service: PipeService) {

    @GetMapping("/pipes")
    fun findAll(): Flux<Pipe> {
        return service.findAll().map { it.convert() }
    }

    @PostMapping("/pipes")
    fun save(pipes: List<Pipe>): Flux<Pipe> {
        return service.saveAll(pipes).map { it.convert() }
    }
}
