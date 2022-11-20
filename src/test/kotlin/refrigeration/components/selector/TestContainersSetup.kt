package refrigeration.components.selector

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import reactor.core.scheduler.Schedulers
import refrigeration.components.selector.config.polynomials.crud.PolynomialCoefficientsService
import java.util.function.Supplier

fun postgres(imageName: String, opts: JdbcDatabaseContainer<Nothing>.() -> Unit) =
    PostgreSQLContainer(DockerImageName.parse(imageName)).apply { opts }

open class TestContainersSetup {
    private val testDataProvide = TestDataProvider()

    protected fun saveCoefficients(service: PolynomialCoefficientsService) {
        val result = testDataProvide.getPolynomialCoefficientRequests("PolynomialCoefficients.json")

        service
            .save(result)
            .publishOn(Schedulers.boundedElastic())
            .collectList()
            .block()
    }

    companion object {
        private val container = postgres("postgres:13.6") {
            withDatabaseName("postgres")
            withUsername("postgres")
            withPassword("password")
        }

        @JvmStatic
        @DynamicPropertySource
        fun dataSourceConfig(registry: DynamicPropertyRegistry) {
            container.start()
            val rdbc = container::getJdbcUrl.invoke().replace("jdbc", "r2dbc")
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.username", container::getUsername)

            registry.add("spring.r2dbc.url", UrlSupplier(rdbc) as Supplier<Any>)
            registry.add("spring.r2dbc.password", container::getPassword)
            registry.add("spring.r2dbc.username", container::getUsername)

            registry.add("spring.flyway.url", container::getJdbcUrl)
            registry.add("spring.flyway.password", container::getPassword)
            registry.add("spring.flyway.user", container::getUsername)
        }
    }
}
