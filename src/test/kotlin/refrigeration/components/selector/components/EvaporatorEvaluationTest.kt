package refrigeration.components.selector.components

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.api.EvalResult
import refrigeration.components.selector.api.EvalResultInfo
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.util.getDoubleValue

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EvaporatorEvaluationTest :TestContainersSetup(){
    @MockBean
    private lateinit var fluidApi: FluidPropertyService
    @Autowired
    private lateinit var service: EvaporatorEvaluation

    @Test
    fun getId() {
        val expected = "default"
        val actual = service.id
        assertEquals(expected, actual)
    }

    @Test
    fun privateEvaluation() {
        assertFalse(service.privateEvaluation())
    }

    @Test
    fun getName() {
        val expected = "EvaporatorEvaluation"
        val actual = service.getName()
        assertEquals(expected, actual)
    }

    @Test
    fun evaluate() {
        mocks()
        val inputMap = mapOf(
            ComponentsConfig.refrigerantKey to "R134a",
            ComponentsConfig.evapTempKey to -10.0,
            ComponentsConfig.condTempKey to 45.0,
            ComponentsConfig.superheat to 20.0,
            ComponentsConfig.subcool to 0.0,
            ComponentsConfig.condensingPressureKey to 1159924.2383423727,
            ComponentsConfig.massFlowRealKeyStandard to 709.8270442166889,
            ComponentsConfig.evaporationPressureKey to 200603.3074727022
        )
        val input = EvaluationInput("", inputMap)

        StepVerifier
            .create(service.evaluate(listOf(input)))
            .expectNextMatches { assertResult(it) }
            .verifyComplete()
    }

    @Test
    fun keyValuesAndTypes() {
        val expected = mapOf(
            ComponentsConfig.condensingPressureKey to "Double",
            ComponentsConfig.condTempKey to "Double",
            ComponentsConfig.evapTempKey to "Double",
            ComponentsConfig.evaporationPressureKey to "Double",
            ComponentsConfig.superheat to "Double",
            ComponentsConfig.subcool to "Double",
            ComponentsConfig.massFlowRealKeyStandard to "Double",
            ComponentsConfig.refrigerantKey to "String"
        )
        val actual = service.keyValuesAndTypes()
        assertEquals(expected, actual)
    }

    private fun mocks() {
        val evaporationPressure = 200603.3074727022
        val condensingPressure = 1159924.2383423727
        val refrigerant = "R134a"
        Mockito.`when`(fluidApi.getLiquidEnthalpy(0.0, refrigerant, 318.15, condensingPressure))
            .thenReturn(Mono.just(263942.92654446454))
        Mockito.`when`(fluidApi.getVapourEnthalpy(20.0, refrigerant, 283.15, evaporationPressure))
            .thenReturn(Mono.just(409711.5727677058))
    }

    private fun assertResult(result: EvalResult): Boolean {
        val evaluationInfo = result.evalInfo
        val expectedEvalResultInfo = EvalResultInfo.SUCCESS
        val resultsMap = result.resultValues.first().result
        val evaporatorPower = getDoubleValue(resultsMap, ComponentsConfig.evaporatorPower) ?: return false

        val expected = 28741.81313558654

        assertEquals(expectedEvalResultInfo, evaluationInfo)
        assertEquals(expected, evaporatorPower)

        return true
    }
}