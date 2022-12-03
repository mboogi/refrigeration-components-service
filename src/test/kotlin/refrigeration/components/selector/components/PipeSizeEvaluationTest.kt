package refrigeration.components.selector.components

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.config.pipes.crud.PipeService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PipeSizeEvaluationTest : TestContainersSetup() {
    @Autowired
    private lateinit var service: PipeService

    @Autowired
    private lateinit var pipeSizeEvaluation: PipeSizeEvaluation

    @Test
    fun evaluate() {
        savePipes(service)
        val material = "WN 2.0090, EN CW024A"
        val volumeFlow = 2.48
        val maxVelocityMainSuction = 6.0
        val maxVelocityMainDischarge = 12.0
        val maxVelocityMainLiquid = 2.0
        val inputs = mapOf(
            ComponentsConfig.maxVelocitySuction to maxVelocityMainSuction,
            ComponentsConfig.maxVelocityDischarge to maxVelocityMainDischarge,
            ComponentsConfig.maxVelocityLiquid to maxVelocityMainLiquid,
            ComponentsConfig.volumeFlow to volumeFlow,
            ComponentsConfig.pipeMaterial to material

        )
        val input = EvaluationInput("", inputs as Map<String, Any>)
        val pipes = pipeSizeEvaluation.evaluate(listOf(input)).collectList().block()
        print(pipes)
    }
}
