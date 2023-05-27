package refrigeration.components.selector.config.polynomials.eval

import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.api.ResultValues
import refrigeration.components.selector.config.polynomials.crud.PolynomialCoefficientsService
import refrigeration.components.selector.config.polynomials.crud.PolynomialSearchService
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum
import java.lang.RuntimeException
import java.math.BigDecimal

@SpringBootTest
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

    @Test
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

        StepVerifier
            .create(evaluationService.evaluate(listOf(input)))
            .expectError(RuntimeException::class.java)
            .verify()

    }

    @Test
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
        StepVerifier.create(evaluationService.evaluate(listOf(input)))
            .expectNextMatches { extractAndAssertResult(it,308.58) }


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
        val input = EvaluationInput("", inputMap)
        StepVerifier.create(evaluationService.evaluate(listOf(input)))
            .expectNextMatches { extractAndAssertResult(it,681.00) }

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
        StepVerifier.create(evaluationService.evaluate(listOf(input)))
            .expectNextMatches { extractAndAssertResult(it,462.87) }

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
//        val tmp=evaluationService.evaluate(listOf(input)).collectList().block()
//        println(tmp?.first())
        StepVerifier
            .create(evaluationService.evaluate(listOf(input)))
            .expectNextMatches { extractAndAssertResult(it,617.16) }
    }

    fun extractAndAssertResult(evalResult: EvalResult, expected:Double):Boolean{
        val info=evalResult.evalInfo
        val resultRaw=evalResult.resultValues.first().result.get("evalResult") as BigDecimal
        val result=resultRaw.toDouble()
        Assertions.assertEquals(expected,result,0.2)
        Assertions.assertEquals(EvalResultInfo.SUCCESS,info)
        return true
    }

    @BeforeAll
    fun beforeAll() {
        saveCoefficients(coefficientsService)
    }
}
