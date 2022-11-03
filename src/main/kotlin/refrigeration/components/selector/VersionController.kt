package refrigeration.components.selector

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class VersionController {

    @GetMapping("/version")
    fun version(): Mono<String> {
        return Mono.just("1.00")
    }
}
