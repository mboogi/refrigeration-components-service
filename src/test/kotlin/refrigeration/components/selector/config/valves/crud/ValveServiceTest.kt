package refrigeration.components.selector.config.valves.crud

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import refrigeration.components.selector.TestContainersSetup


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValveServiceTest : TestContainersSetup() {


    @Autowired
    private lateinit var service: ValveService

    @Test
    fun `get interpolation group from list of elements`() {
        val valvesTestData = testDataProvider.getValves("valves_r134a.json")
        service.saveAll(valvesTestData).collectList().block()
        val valves = service.findByRefrigerantAndRefrigerationCapacityBetweenAndValveName("R134a", 10.0, 15.0, "EX4")
            .collectList().block()?.filterNotNull() ?: listOf()

        val interpolationGroup = service.createInterpolationGroups(valves, 47.0, -2.0)
        println(interpolationGroup)
    }


    @Test
    fun `get best match `() {
        val valvesTestData = testDataProvider.getValves("valves_r134a.json")
        service.saveAll(valvesTestData).collectList().block()
        val result = service.getValveBestMatchCandidate("R134a", 25.47, 47.5, -2.5)
            .block()
        println(result)
    }


    @Test
    fun `get valve best match repo `() {
        val valvesTestData = testDataProvider.getValves("valves_r134a.json")
        service.saveAll(valvesTestData).collectList().block()
        val result = service.getValveBestMatchRaw("R134a", 25.47, 47.5, -2.5)
            .block()
        result!!.forEach { println(it) }
    }
}