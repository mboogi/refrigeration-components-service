package refrigeration.components.selector.components.tmp

import reactor.core.publisher.Mono

class Plus(val name: String) : NumberOperator {
    override fun apply(value: Double, value2: Double): Mono<Double> {
        val result = value + value2
        return Mono.just(result)
    }
}
