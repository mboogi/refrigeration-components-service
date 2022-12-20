package refrigeration.components.selector.components.tmp

import org.junit.jupiter.api.Test

internal class OperationChainTest {

    @Test
    fun operation_chain_test() {
        val chain = OperationChain()
        chain.currently_doing_like_this().subscribe { println(it) }
    }

    @Test
    fun test2() {
        val chain = OperationChain()
        chain.diffrent()
    }
}
