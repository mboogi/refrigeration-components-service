package refrigeration.components.selector.config.polynomials.db

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.scheduler.Schedulers
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.config.polynomials.search.PolynomialCoefficientsService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PolynomialSearchResultRepositoryTest(
    @Autowired
    private val coefficientsService: PolynomialCoefficientsService
) : TestContainersSetup() {

    @Autowired
    lateinit var repo: PolynomialSearchResultRepository

    @Test
    fun `search should return results`() {
        val result = repo.findByCompressorTypeAndRefrigerantTypeAndTransCriticalAndPolynomialType(
            "4GE-30Y",
            "R134a",
            false,
            PolynomialTypesEnum.MASS_FLOW.toString()
        )
            .publishOn(Schedulers.boundedElastic())
            .collectList()
            .block()
        println(result)
    }

    @BeforeAll
    fun beforeAll() {
        saveCoefficients(coefficientsService)
    }
}
