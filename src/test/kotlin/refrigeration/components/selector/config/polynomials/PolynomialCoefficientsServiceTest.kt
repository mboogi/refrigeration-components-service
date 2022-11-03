package refrigeration.components.selector.config.polynomials

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.TestDataProvider
import refrigeration.components.selector.config.polynomials.search.PolynomialCoefficientsService

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PolynomialCoefficientsServiceTest(
    @Autowired
    val service: PolynomialCoefficientsService
) : TestContainersSetup() {

    private val testDataProvider = TestDataProvider()

    companion object {
        val logger = LoggerFactory.getLogger(PolynomialCoefficientsServiceTest::class.java)
    }

    @BeforeAll
    fun beforeAll() {
        saveCoefficients(service)
    }

    @Test
    fun `save all polynomial coefficients in the database`() {
        val request = testDataProvider.getPolynomialCoefficientRequests("PolynomialCoefficients.json")
            .map { it.description }
        val coefficients = service.findAllPolynomialMappings()

        StepVerifier
            .create(coefficients)
            .thenConsumeWhile { it.description in request }
            .expectComplete()
            .verify()
    }
}
