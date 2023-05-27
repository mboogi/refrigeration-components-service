package refrigeration.components.selector.components

import org.junit.jupiter.api.*
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
import refrigeration.components.selector.config.polynomials.crud.PolynomialCoefficientsService
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.util.getElectricPower
import refrigeration.components.selector.util.getRealMassFlow
import refrigeration.components.selector.util.getVolumeFlow


@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CompressorEvaluationTest : TestContainersSetup() {
    @MockBean
    private lateinit var fluidApi: FluidPropertyService

    @Autowired
    private lateinit var service: CompressorEvaluation

    @Autowired
    private lateinit var polynomialService: PolynomialCoefficientsService

    @BeforeAll
    fun beforeAll() {
        mocks()
        saveCoefficients(polynomialService)
    }

    @AfterEach
    fun afterEach() {
        polynomialService.deleteAll()
    }

    @Test
    fun privateEvaluation() {
        val evaluation = service.privateEvaluation()
        Assertions.assertFalse(evaluation)
    }

    @Test
    fun getName() {
        val name = service.getName()
        Assertions.assertEquals("CompressorEvaluation", service.getName())
    }

    @Test
    fun getId() {
        val name = service.id
        Assertions.assertEquals("default", service.id)
    }

    @Test
    fun keyValuesAndTypes() {
        val expected = mapOf(
            ComponentsConfig.refrigerantKey to "String",
            ComponentsConfig.evapTempKey to "Double",
            ComponentsConfig.condTempKey to "Double",
            ComponentsConfig.capacity to "Double",
            ComponentsConfig.frequency to "Double",
            ComponentsConfig.transCritical to "Boolean",
            ComponentsConfig.compressorType to "String",
            ComponentsConfig.subcool to "Double",
            ComponentsConfig.superheat to "Double"
        ).toSortedMap()
        val actual = service.keyValuesAndTypes().toSortedMap()
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun evaluate() {
        mocks()
        val refrigerant = "R134a"
        val evaporationTemp = -10.0
        val condensingTemp = 45.0
        val capacity = 100.0
        val frequency = 55.0
        val transCritical = false
        val compressorType = "4GE-30Y"
        val subCooling = 0.0
        val superHeat = 20.0
        val inputMap = mutableMapOf<String, Any>(
            ComponentsConfig.refrigerantKey to refrigerant,
            ComponentsConfig.evapTempKey to evaporationTemp,
            ComponentsConfig.condTempKey to condensingTemp,
            ComponentsConfig.capacity to capacity,
            ComponentsConfig.frequency to frequency,
            ComponentsConfig.transCritical to transCritical,
            ComponentsConfig.compressorType to compressorType,
            ComponentsConfig.subcool to subCooling,
            ComponentsConfig.superheat to superHeat
        )
        val input = EvaluationInput(PolynomialTypesEnum.MASS_FLOW.toString(), inputMap)
        StepVerifier
            .create(service.evaluate(listOf(input)))
            .expectNextMatches { assertResult(it) }
            .verifyComplete()
    }

    private fun mocks() {
        val evaporationPressure = 200603.3074727022
        val condensingPressure = 1159924.2383423727
        val refrigerant = "R134a"
        Mockito.`when`(fluidApi.getDryVapourPressure(263.15, refrigerant)).thenReturn(Mono.just(evaporationPressure))
        Mockito.`when`(fluidApi.getDryVapourPressure(318.15, refrigerant)).thenReturn(Mono.just(condensingPressure))
        Mockito.`when`(fluidApi.getSuperHeatedVapourEnthalpy(293.15, evaporationPressure, refrigerant))
            .thenReturn(Mono.just(418317.03512830584))
        Mockito.`when`(fluidApi.getSuperHeatedVapourDensity(293.15, evaporationPressure, refrigerant))
            .thenReturn(Mono.just(8.78539291229285))

        Mockito.`when`(fluidApi.getVapourEnthalpy(20.0, "R134a", 283.15, evaporationPressure))
            .thenReturn(Mono.just(409711.5727677058))
        Mockito.`when`(fluidApi.getVapourDensity(20.0, "R134a", 283.15, evaporationPressure))
            .thenReturn(Mono.just(9.157152411246777))

        Mockito.`when`(
            fluidApi.getTemperatureForSuperheatedEnthalpy(
                467549.06021382695,
                condensingPressure,
                refrigerant
            )
        ).thenReturn(Mono.just(360.1398348529394))
    }

    private fun assertResult(result: EvalResult): Boolean {
        val evaluationInfo = result.evalInfo
        val resultsMap = result.resultValues.first().result
        val volumeFlow = getVolumeFlow(resultsMap) ?: return false
        val massFlow = getRealMassFlow(resultsMap) ?: return false
        val electricPower = getElectricPower(resultsMap) ?: return false

        val expectedEvalResultInfo = EvalResultInfo.SUCCESS
        val expectedVolumeFlow = 77.51
        val expectedMassFlow = 709.82
        val expectedElectricPower = 11404.05

        Assertions.assertEquals(expectedEvalResultInfo, evaluationInfo)
        Assertions.assertEquals(expectedVolumeFlow, volumeFlow, 0.2)
        Assertions.assertEquals(expectedMassFlow, massFlow, 0.2)
        Assertions.assertEquals(expectedElectricPower, electricPower, 0.2)
        return true
    }
}
