package refrigeration.components.selector.components

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import reactor.test.StepVerifier
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.ResultValues

@SpringBootTest
class CondenserEvaluationTest {

     var mapper:ObjectMapper=ObjectMapper()
    @Autowired
    lateinit var service: CondenserEvaluation

    @Test
    fun getId() {
        assertEquals("default", service.id)
    }

    @Test
    fun privateEvaluation() {
        val actual = service.privateEvaluation()
        assertFalse(actual)
    }

    @Test
    fun getName() {
        assertEquals("CondenserEvaluation", service.getName())
    }

    @Test
    fun evaluate() {
        val input = EvaluationInput(
            "CondenserEvaluation",
            mapOf(ComponentsConfig.electricPowerKey to 20.0, ComponentsConfig.evaporatorPower to 40.0)
        )
        val tmp=mapper.writeValueAsString(input)
        println(tmp)
        StepVerifier
            .create(service.evaluate(listOf(input)))
            .expectNext(getResult(input))
            .verifyComplete()
    }

    private fun getResult(input: EvaluationInput): EvalResult {
        val resultValue = ResultValues(
            "default",
            mapOf(ComponentsConfig.condenserPower to 60.0),
            mapOf(ComponentsConfig.condenserPower to "Double")
        )
        return  EvalResult(EvalResultInfo.SUCCESS, input, listOf(resultValue), "Condenser Evaluation Finished")

    }
    @Test
    fun keyValuesAndTypes() {
        val expected = mapOf(
            ComponentsConfig.electricPowerKey to "Double",
            ComponentsConfig.evaporatorPower to "Double"
        ).toSortedMap()
        val actual = service.keyValuesAndTypes()
        assertEquals(expected, actual)
    }
}