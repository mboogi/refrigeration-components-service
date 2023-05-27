package refrigeration.components.selector.components

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
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
import refrigeration.components.selector.config.pipes.crud.PipeService
import refrigeration.components.selector.config.polynomials.crud.PolynomialCoefficientsService
import refrigeration.components.selector.config.valves.crud.ValveService
import refrigeration.components.selector.fluid.FluidPropertyService
import refrigeration.components.selector.util.*
@Disabled
@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingleStageCycleTest : TestContainersSetup() {
    @MockBean
    private lateinit var fluidApi: FluidPropertyService

    @Autowired
    private lateinit var polynomialService: PolynomialCoefficientsService

    @Autowired
    private lateinit var valveService: ValveService

    @Autowired
    private lateinit var pipeService: PipeService

    @Autowired
    private lateinit var evaluator: SingleStageCycle

    @BeforeAll
    fun beforeAll() {
        pipeService.deleteAll().block()
        mocks()
        savePipes(
            pipeService
        )
        saveValves(valveService)
        saveCoefficients(polynomialService)
    }
    @AfterAll
    fun afterEach(){
        pipeService.deleteAll().block()
        valveService.deleteAll().block()
        polynomialService.deleteAll()
    }

    @Test
    fun getId() {
        val id = "default"
        val actual = evaluator.id
        assertEquals(id, actual)
    }

    @Test
    fun getName() {
        val expected = "SingleStageCycleEvaluation"
        val actual = evaluator.getName()
        assertEquals(expected, actual)
    }

    @Test
    fun evaluate() {
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
            ComponentsConfig.subcool to subCooling,
            ComponentsConfig.superheat to superHeat,
            ComponentsConfig.compressorType to compressorType,
            ComponentsConfig.frequency to frequency,
            ComponentsConfig.capacity to capacity,
            ComponentsConfig.transCritical to transCritical,
            ComponentsConfig.maxVelocitySuction to 6.0,
            ComponentsConfig.maxVelocityLiquid to 12.0,
            ComponentsConfig.maxVelocityDischarge to 2.0,
            ComponentsConfig.pipeMaterial to "WN 2.0090, EN CW024A"

        )
        val input = EvaluationInput("", inputMap)

        val result=evaluator.evaluate(listOf(input)).collectList().block()
        println(result)

        StepVerifier.create(evaluator.evaluate(listOf(input)))
            .expectNextMatches { assertResult(it) }
            .verifyComplete()
    }

    private fun assertResult(result: EvalResult): Boolean {
        val evaluationInfo = result.evalInfo
        val resultsMap =
            result.resultValues.firstOrNull { it.id == "compressor1" } ?: fail { "no compressor evaluation" }
        val volumeFlow = getVolumeFlow(resultsMap.result) ?: fail { "no volumeFlow evaluation" }
        val massFlow = getRealMassFlow(resultsMap.result) ?: fail { "no massFlow evaluation" }
        val electricPower = getElectricPower(resultsMap.result) ?: fail { "no electricPower evaluation" }

        val expectedEvalResultInfo = EvalResultInfo.SUCCESS
        val expectedVolumeFlow = 77.51
        val expectedMassFlow = 709.82
        val expectedElectricPower = 11404.05

        assertEquals(expectedEvalResultInfo, evaluationInfo)
        assertEquals(expectedVolumeFlow, volumeFlow, 0.2)
        assertEquals(expectedMassFlow, massFlow, 0.2)
        assertEquals(expectedElectricPower, electricPower, 0.2)

        val resultsMapEvaporator =
            result.resultValues.firstOrNull { it.id == "evaporator1" } ?: fail { "no evaporator1 evaluation" }
        val evaporatorPower =
            getDoubleValue(resultsMapEvaporator.result, ComponentsConfig.evaporatorPower)
                ?: fail { "no evaporatorPower evaluation" }

        val expectedEvaporator = 28741.81313558654

        assertEquals(expectedEvalResultInfo, evaluationInfo)
        assertEquals(expectedEvaporator, evaporatorPower)

        val expectedCondenserPower = 40145.87223525322

        val resultsMapCondenser =
            result.resultValues.firstOrNull { it.id == "condenser1" } ?: fail { "no condenser1 evaluation" }
        val condenserPower = getDoubleValue(resultsMapCondenser.result, ComponentsConfig.condenserPower)

        assertEquals(expectedCondenserPower, condenserPower)

        val resultsMapValve = result.resultValues.firstOrNull { it.id == "valves" } ?: fail { "no valves evaluation" }
        val valveCapacity = getDoubleValue(resultsMapValve.result, ComponentsConfig.valveCapacity)
            ?: fail { "no valveCapacity evaluation" }
        val valveName = getValueForKey<String>(resultsMapValve.result, ComponentsConfig.valveName)
            ?: fail { "no valveName evaluation" }

        val expectedValveCapacity = 38.0
        val expectedValveName = "EX5"

        assertEquals(expectedValveCapacity, valveCapacity, 0.2)
        assertEquals(expectedValveName, valveName)

        val resultsMapPipe = result.resultValues.firstOrNull { it.id == "pipes" } ?: fail { "no pipes evaluation" }
        val suctionVelocity = getDoubleValue(resultsMapPipe.result, ComponentsConfig.suctionVelocity)
            ?: fail { "no suctionVelocity evaluation" }
        val suctionSize = getDoubleValue(resultsMapPipe.result, ComponentsConfig.suctionLineSize)
            ?: fail { "no suctionSize evaluation" }
        val expectedSuctionVelocity = 4.900004979175761
        val expectedSuctionSize = 79.38

        assertEquals(expectedSuctionVelocity, suctionVelocity)
        assertEquals(expectedSuctionSize, suctionSize)

        return true
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
            ComponentsConfig.superheat to "Double",
            ComponentsConfig.maxVelocityDischarge to "Double",
            ComponentsConfig.maxVelocityLiquid to "Double",
            ComponentsConfig.maxVelocitySuction to "Double",
            ComponentsConfig.pipeMaterial to "String"
        )
        val actual = evaluator.keyValuesAndTypes()
        assertEquals(expected, actual)
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

        Mockito.`when`(fluidApi.getLiquidEnthalpy(00.0, refrigerant, 318.15, condensingPressure))
            .thenReturn(Mono.just(263942.92654446454))

        Mockito.`when`(fluidApi.getVapourEnthalpy(20.0, refrigerant, 283.15, evaporationPressure))
            .thenReturn(Mono.just(409711.5727677058))
    }
}