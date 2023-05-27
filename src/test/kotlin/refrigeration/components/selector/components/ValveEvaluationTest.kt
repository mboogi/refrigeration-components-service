package refrigeration.components.selector.components

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.config.valves.crud.ValveService
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.util.*

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValveEvaluationTest : TestContainersSetup() {
    @Autowired
    private lateinit var evaluator: ValveEvaluation

    @Autowired
    private lateinit var service: ValveService


    fun beforeAll() {
        saveValves(service)
    }
    fun afterEach(){
        service.deleteAll().block()
    }

    @Test
    fun getId() {
        val actual = evaluator.id
        val expected = "default"
        assertEquals(expected, actual)
    }

    @Test
    fun privateEvaluation() {
        val actual = evaluator.privateEvaluation()
        assertFalse(actual)
    }

    @Test
    fun getName() {
        val expected = "ValveEvaluation"
        val actual = evaluator.getName()
        assertEquals(expected, actual)
    }

    @Test
    fun keyValuesAndTypes() {
        val expected = mapOf(
            ComponentsConfig.refrigerantKey to "String",
            ComponentsConfig.evapTempKey to "Double",
            ComponentsConfig.condTempKey to "Double",
            ComponentsConfig.evaporatorPower to "Double"
        )
        val actual = evaluator.keyValuesAndTypes()
        assertEquals(expected, actual)
    }

    @Test
    fun evaluate() {
        beforeAll()
        val input = EvaluationInput(
            "", mapOf(
                ComponentsConfig.refrigerantKey to "R134a",
                ComponentsConfig.evapTempKey to -5.0,
                ComponentsConfig.condTempKey to 60.0,
                ComponentsConfig.evaporatorPower to 24000.0
            )
        )

        StepVerifier.create(evaluator.evaluate(listOf(input)))
            .expectNextMatches { assertResult(it) }
            .verifyComplete()

        afterEach()
    }

    private fun assertResult(result: EvalResult): Boolean {
        val evaluationInfo = result.evalInfo
        val resultsMap = result.resultValues.first().result
        val valveCapacity = getDoubleValue(resultsMap, ComponentsConfig.valveCapacity) ?: return false
        val valveName = getValueForKey<String>(resultsMap, ComponentsConfig.valveName) ?: return false

        val expectedEvalResultInfo = EvalResultInfo.SUCCESS
        val expectedValveCapacity = 39.0
        val expectedValveName = "EX5"

        assertEquals(expectedEvalResultInfo, evaluationInfo)
        assertEquals(expectedValveCapacity, valveCapacity, 0.2)
        assertEquals(expectedValveName, valveName)

        return true
    }

}