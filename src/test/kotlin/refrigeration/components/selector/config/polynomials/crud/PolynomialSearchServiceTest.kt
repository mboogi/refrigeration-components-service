package refrigeration.components.selector.config.polynomials.crud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import refrigeration.components.selector.TestContainersSetup
import refrigeration.components.selector.config.InterpolationGroup
import refrigeration.components.selector.config.polynomials.db.PolynomialTypesEnum

@SpringBootTest
@Testcontainers
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
        ).block()
        println(result)
        // TO-DO MORE ASSERTIONS HERE
    }

    @Test
    fun `when no polynomial groups  are found return default group`() {
        StepVerifier
            .create(searchService.getPolynomialGroups(
            "fake compressor name, not in the database",
            "R134a",
            100.0,
            50.0,
            false,
            PolynomialTypesEnum.MASS_FLOW.toString()
        )).expectNextMatches { assert(it) }

    }
    fun assert(group:InterpolationGroup<PolynomialSearchGroup>): Boolean {
        return group.isEmpty
    }

    @BeforeAll
    fun `populate database`() {
        saveCoefficients(coefficientsService)
    }
}
