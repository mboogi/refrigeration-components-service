package refrigeration.components.selector.config.pipes.crud

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.config.pipes.db.PipeEntity

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PipeServiceTest : TestContainersSetup() {
    @Autowired
    private lateinit var service: PipeService
    @BeforeAll
    fun beforeAll(){
        service.deleteAll().block()
        savePipes(service)
    }
    @AfterAll
    fun afterAll(){
        service.deleteAll().block()
    }

    @Test
    fun findAll() {
        val pipes = service.findAll().collectList().block()
            ?.toList()
            ?.filterNotNull()
            ?.sortedBy { it.name }
            ?.map { convert(it) }
        val expected = testDataProvider.getPipes("PipeEntities.json").sortedBy { it.name }
        Assertions.assertEquals(expected, pipes)
    }

    @Test
    fun findByInnerDiameterBetween() {
        val expectedLowerDiameterPipe=5.08
        val expectedHigherDiameterPipe=8.0
        val expected= listOf(expectedLowerDiameterPipe,expectedHigherDiameterPipe)
        val result = service
            .findByInnerDiameterBetween(4.0, 10.0)
            .collectList()
            .block()
            ?.toList()
            ?.map { it.innerDiameter }

        Assertions.assertEquals(expected,result)

        val resultFoundByMaterial =
            service.findByInnerDiameterBetweenAndMaterial(4.0, 10.0, "WN 2.0090, EN CW024A")
                .collectList()
                .block()
                ?.toList()
                ?.map { it.innerDiameter }

        Assertions.assertEquals(expected,resultFoundByMaterial)

    }

    private fun convert(entity: PipeEntity): PipeEntity {
        return PipeEntity(
            0L,
            entity.name,
            entity.standard,
            entity.outerDiameter,
            entity.innerDiameter,
            entity.material,
            entity.maxPressure
        )
    }
}
