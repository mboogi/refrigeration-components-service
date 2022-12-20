package refrigeration.components.selector.config.pipes.crud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.TestDataProvider

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PipeServiceTest : TestContainersSetup() {


    @Autowired
    private lateinit var service: PipeService

    @Test
    fun saveAll() {
    }

    @Test
    fun update() {
    }

    @Test
    fun findAll() {
        savePipes(service)
        val pipes = service.findAll().collectList().block()?.toList()?.filterNotNull()
        val expected = testDataProvider.getPipes("PipeEntities.json")
        Assertions.assertEquals(expected, pipes)
    }

    @Test
    fun findByInnerDiameterBetween() {
        savePipes(service)
        val result = service.findByInnerDiameterBetween(4.0, 10.0).collectList().block()?.toList()
        println(result)
        val result2 = service.findByInnerDiameterBetweenAndMaterial(4.0, 10.0,"WN 2.0090, EN CW024A").collectList().block()?.toList()
        println(result2)
    }
}
