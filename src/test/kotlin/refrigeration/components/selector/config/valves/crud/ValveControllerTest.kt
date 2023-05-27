package refrigeration.components.selector.config.valves.crud

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.TestDataProvider

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ValveControllerTest : TestContainersSetup() {
    private val logger = LoggerFactory.getLogger(ValveControllerTest::class.java)


    @Autowired
    private lateinit var controller: ValveController


    @Test
    fun getAll() {
        val result = controller.getAll().collectList().block()
    }

    @Test
    fun saveAll() {
        val valves = testDataProvider.getValves("valves_r134a.json")
        val result = controller.saveAll(valves).collectList().block()
        println(result)
    }

    @Test
    fun findByCapacity() {
        val valves = testDataProvider.getValves("valves_r134a.json")
        controller.saveAll(valves).collectList().block()
        val result = controller
            .findByRefrigerantAndCapacity("R134a", 10.0, 15.0)
            .collectList()
            .block()
        println(result)
    }
}
