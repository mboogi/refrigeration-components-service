package refrigeration.components.selector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@SpringBootApplication
class ComponentsSelectorApplication

fun main(args: Array<String>) {
    runApplication<ComponentsSelectorApplication>(*args)
}
