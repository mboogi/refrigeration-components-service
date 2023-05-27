package refrigeration.components.selector.components

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.config.pipes.crud.PipeService
import refrigeration.components.selector.util.*
import java.time.Duration

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

        StepVerifier.create(pipeSizeEvaluation.evaluate(listOf(input)))
            .expectNextMatches { assertResult(it) }
            .verifyComplete()
    }

    private fun assertResult(result: EvalResult): Boolean {
        val resultsMap = result.resultValues.first().result
        val suctionVelocity = getDoubleValue(resultsMap, ComponentsConfig.suctionVelocity) ?: return false
        val suctionSize = getDoubleValue(resultsMap, ComponentsConfig.suctionLineSize)?: return false
        val expectedSuctionVelocity = 4.579173707263446
        val expectedSuctionSize = 15.88
        val expectedEvalResultInfo = EvalResultInfo.SUCCESS

        Assertions.assertEquals(expectedSuctionVelocity, suctionVelocity)
        Assertions.assertEquals(expectedSuctionSize, suctionSize)
        Assertions.assertEquals(expectedEvalResultInfo, result.evalInfo)

        return true
    }

    @Test
    fun keyValuesAndTypes() {
        val expected = mapOf(
            ComponentsConfig.pipeMaterial to "String",
            ComponentsConfig.maxVelocitySuction to "Double",
            ComponentsConfig.maxVelocityDischarge to "Double",
            ComponentsConfig.maxVelocityLiquid to "Double",
            ComponentsConfig.volumeFlow to "Double"
        )
        val actual = pipeSizeEvaluation.keyValuesAndTypes()
        Assertions.assertEquals(expected, actual)
    }
}
