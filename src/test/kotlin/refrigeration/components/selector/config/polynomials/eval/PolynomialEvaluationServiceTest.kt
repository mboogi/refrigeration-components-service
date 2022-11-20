package refrigeration.components.selector.config.polynomials.eval

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.ResultValues
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum
import refrigeration.components.selector.config.polynomials.crud.PolynomialCoefficientsService
import refrigeration.components.selector.config.polynomials.crud.PolynomialSearchService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PolynomialEvaluationServiceTest(
    @Autowired
    private val coefficientsService: PolynomialCoefficientsService,
    @Autowired
    private val searchService: PolynomialSearchService
) : TestContainersSetup() {

    private val evaluationService =
        PolynomialEvaluationService(PolynomialTypesEnum.MASS_FLOW.toString(), searchService, coefficientsService)

    fun `evaluate polynomial when values are outside of polynomial areas`() {
        val refrigerant = "R134a"
        val evaporatingTemperature = -10.0
        val condensingTemp = 45.0
        val capacity = 175.0
        val frequency = 155.0
        val transCritical = false
        val compressorType = "4GE-30Y"
        val inputMap = mutableMapOf<String, Any>(
            ComponentsConfig.refrigerantKey to refrigerant,
            ComponentsConfig.evapTempKey to evaporatingTemperature,
            ComponentsConfig.condTempKey to condensingTemp,
            ComponentsConfig.capacity to capacity,
            ComponentsConfig.frequency to frequency,
            ComponentsConfig.transCritical to transCritical,
            ComponentsConfig.compressorType to compressorType
        )
        val input = EvaluationInput(PolynomialTypesEnum.MASS_FLOW.toString(), inputMap)
        val result = evaluationService.evaluate(listOf(input)).blockFirst()
        val expectedResultValues = ResultValues(
            "",
            mapOf("evalValue" to 308.582513720247),
            mapOf("evalValue" to Double::class)
        )
        val expectedResult = EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            expectedResultValues,
            "successfully evaluated MASS_FLOW"
        )
        Assertions.assertEquals(expectedResult, result)
    }

    fun `the evaluation for mass flow polynomial should be interpolated for frequency and capacity`() {
        val refrigerant = "R134a"
        val evaporationTemp = -10.0
        val condensingTemp = 45.0
        val capacity = 75.0
        val frequency = 55.0
        val transCritical = false
        val compressorType = "4GE-30Y"
        val inputMap = mutableMapOf<String, Any>(
            ComponentsConfig.refrigerantKey to refrigerant,
            ComponentsConfig.evapTempKey to evaporationTemp,
            ComponentsConfig.condTempKey to condensingTemp,
            ComponentsConfig.capacity to capacity,
            ComponentsConfig.frequency to frequency,
            ComponentsConfig.transCritical to transCritical,
            ComponentsConfig.compressorType to compressorType
        )
        val input = EvaluationInput(PolynomialTypesEnum.MASS_FLOW.toString(), inputMap)
        val result = evaluationService.evaluate(listOf(input)).blockFirst()
        val expectedResultValues = ResultValues(
            "",
            mapOf("evalValue" to 308.582513720247),
            mapOf("evalValue" to Double::class)
        )
        val expectedResult = EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            expectedResultValues,
            "successfully evaluated MASS_FLOW"
        )
        Assertions.assertEquals(expectedResult, result)
    }

    @Test
    fun `the evaluation for mass flow polynomial should be interpolated for frequency`() {
        val refrigerant = "R134a"
        val evaporationTemp = -10.0
        val condensingTemp = 45.0
        val capacity = 100.0
        val frequency = 55.0
        val transCritical = false
        val compressorType = "4GE-30Y"
        val inputMap = mutableMapOf<String, Any>(
            ComponentsConfig.refrigerantKey to refrigerant,
            ComponentsConfig.evapTempKey to evaporationTemp,
            ComponentsConfig.condTempKey to condensingTemp,
            ComponentsConfig.capacity to capacity,
            ComponentsConfig.frequency to frequency,
            ComponentsConfig.transCritical to transCritical,
            ComponentsConfig.compressorType to compressorType
        )
        val input = EvaluationInput(PolynomialTypesEnum.MASS_FLOW.toString(), inputMap)
        val result = evaluationService.evaluate(listOf(input)).blockFirst()
        val expectedResultValues = ResultValues(
            "",
            mapOf("evalValue" to 681.0096854515511),
            mapOf("evalValue" to Double::class)
        )
        val expectedResult = EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            expectedResultValues,
            "successfully evaluated MASS_FLOW"
        )
        Assertions.assertEquals(expectedResult, result)
    }

    @Test
    fun `the evaluation for mass flow polynomial type should be interpolated for capacity`() {
        val refrigerant = "R134a"
        val evaporationTemp = -10.0
        val condensingTemp = 45.0
        val capacity = 75.0
        val frequency = 50.0
        val transCritical = false
        val compressorType = "4GE-30Y"
        val inputMap = mutableMapOf<String, Any>(
            ComponentsConfig.refrigerantKey to refrigerant,
            ComponentsConfig.evapTempKey to evaporationTemp,
            ComponentsConfig.condTempKey to condensingTemp,
            ComponentsConfig.capacity to capacity,
            ComponentsConfig.frequency to frequency,
            ComponentsConfig.transCritical to transCritical,
            ComponentsConfig.compressorType to compressorType
        )
        val input = EvaluationInput(PolynomialTypesEnum.MASS_FLOW.toString(), inputMap)
        val result = evaluationService.evaluate(listOf(input)).blockFirst()
        val expectedResultValues = ResultValues(
            "",
            mapOf("evalValue" to 462.8737705803499),
            mapOf("evalValue" to Double::class)
        )
        val expectedResult = EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            expectedResultValues,
            "successfully evaluated MASS_FLOW"
        )
        Assertions.assertEquals(expectedResult, result)
    }

    @Test
    fun `the evaluation for mass flow polynomial type should be executed with success`() {
        val refrigerant = "R134a"
        val evaporationTemp = -10.0
        val condensingTemp = 45.0
        val capacity = 100.0
        val frequency = 50.0
        val transCritical = false
        val compressorType = "4GE-30Y"
        val inputMap = mutableMapOf<String, Any>(
            ComponentsConfig.refrigerantKey to refrigerant,
            ComponentsConfig.evapTempKey to evaporationTemp,
            ComponentsConfig.condTempKey to condensingTemp,
            ComponentsConfig.capacity to capacity,
            ComponentsConfig.frequency to frequency,
            ComponentsConfig.transCritical to transCritical,
            ComponentsConfig.compressorType to compressorType
        )
        val input = EvaluationInput(PolynomialTypesEnum.MASS_FLOW.toString(), inputMap)
        val expectedResultValues = ResultValues(
            "",
            mapOf("evalValue" to 617.165027440467),
            mapOf("evalValue" to Double::class),
        )
        val result = evaluationService.evaluate(listOf(input)).blockFirst()
        val expectedResult = EvalResult(
            EvalResultInfo.SUCCESS,
            input,
            expectedResultValues,
            "successfully evaluated MASS_FLOW"
        )

        Assertions.assertEquals(expectedResult, result)
    }

    @BeforeAll
    fun beforeAll() {
        saveCoefficients(coefficientsService)
    }
}
