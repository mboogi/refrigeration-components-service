package refrigeration.components.selector

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentsSelectorApplicationTests : TestContainersSetup() {

    @Autowired
    var context: ApplicationContext? = null

    @Test
    fun contextLoads() {
        val isNull = (context != null)
        Assertions.assertNotNull(isNull)
    }
}
