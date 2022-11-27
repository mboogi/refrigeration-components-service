package refrigeration.components.selector.components.tmp

import reactor.core.publisher.Mono

class OperationChain {

    private val plusOperators = listOf(Plus("first"), Plus("second"), Plus("third"))

    fun currently_doing_like_this(): Mono<Double> {
        val first = plusOperators.firstOrNull { it.name == "first" } ?: return Mono.empty()
        val second = plusOperators.firstOrNull { it.name == "second" } ?: return Mono.empty()
        val third = plusOperators.firstOrNull { it.name == "third" } ?: return Mono.empty()

        return first.apply(1.0, 1.0)
            .map { it * 1.0 }
            .flatMap { second.apply(it, 1.0) }
            .map { it * 1 }
            .flatMap { third.apply(it, 1.0) }
            .map { it * 1 }
    }

    fun different2() {
        val numners = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val blah = numners.fold(0.0) { acc, i -> acc.plus(i) }
        println(blah)
    }

    fun diffrent() {
        val first = plusOperators.filter { it.name == "first" }.firstOrNull() ?: return
        val blah = plusOperators.fold(Mono.just(1.0)) { acc, plus ->
            acc.flatMap { plus.apply(it, 1.0) }
        }.subscribe { it -> println(it) }
    }
}
