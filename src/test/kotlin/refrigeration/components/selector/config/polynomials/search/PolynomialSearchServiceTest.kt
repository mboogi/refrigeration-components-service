package refrigeration.components.selector.config.polynomials.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PolynomialSearchServiceTest(
    @Autowired
    private val coefficientsService: PolynomialCoefficientsService
) : TestContainersSetup() {

    @Autowired
    lateinit var searchService: PolynomialSearchService

    @Test
    fun `get all polynomials `() {
        val result = searchService.getPolynomialGroups(
            "4GE-30Y",
            "R134a",
            100.0,
            50.0,
            false,
            PolynomialTypesEnum.MASS_FLOW.toString()
        )

        // TO-DO MORE ASSERTIONS HERE
    }

    @Test
    fun `when no polynomial groups  are found return default group`() {
        val result = searchService.getPolynomialGroups(
            "fake compressor name, not in the database",
            "R134a",
            100.0,
            50.0,
            false,
            PolynomialTypesEnum.MASS_FLOW.toString()
        )

        //Assertions.assertTrue(result.isEmpty)
    }

    @BeforeAll
    fun `populate database`() {
        saveCoefficients(coefficientsService)
    }
}
