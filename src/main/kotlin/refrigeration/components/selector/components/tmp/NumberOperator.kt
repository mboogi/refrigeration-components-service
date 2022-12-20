package refrigeration.components.selector.components.tmp

import reactor.core.publisher.Mono

interface NumberOperator {

    fun apply(value: Double, value2: Double): Mono<Double>
}
