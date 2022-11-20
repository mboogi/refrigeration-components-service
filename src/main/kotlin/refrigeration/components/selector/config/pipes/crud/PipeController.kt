package refrigeration.components.selector.config.pipes.crud

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import refrigeration.components.selector.config.pipes.db.PipeEntity

@RestController
class PipeController(private val service: PipeService) {

    @GetMapping("/pipes")
    fun findAll(): Flux<PipeEntity> {
        return service.findAll()
    }
}
