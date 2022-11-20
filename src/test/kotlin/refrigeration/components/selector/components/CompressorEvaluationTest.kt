package refrigeration.components.selector.components

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import refrigeration.components.selector.ComponentsConfig
import refrigeration.components.selector.api.EvaluationInput
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CompressorEvaluationTest /*: TestContainersSetup()*/ {
    @Autowired
    private lateinit var mapper: ObjectMapper

    @Test
    fun privateEvaluation() {
    }

    @Test
    fun getName() {
    }

    @Test
    fun getRequiredInputKeys() {
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
        val subCooling = 10.0
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
        val mapped = mapper.writeValueAsString(input)
        println(mapped)
    }
}
