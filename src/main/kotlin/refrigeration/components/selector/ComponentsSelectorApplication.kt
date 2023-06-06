package refrigeration.components.selector

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@SpringBootApplication
@EnableAutoConfiguration
@Configuration
@ComponentScan
class ComponentsSelectorApplication

fun main(args: Array<String>) {
    runApplication<ComponentsSelectorApplication>(*args)
}
