package refrigeration.components.selector

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import java.util.*


@Configuration
class SpringConfig(private val env: Environment) {
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration()
        corsConfig.allowedOrigins = listOf("http://localhost:3000","http://localhost:3001")
        corsConfig.maxAge = 3600L
        corsConfig.addAllowedMethod("*")
        corsConfig.addAllowedHeader("content-type")
        corsConfig.addAllowedHeader("Requestor-Type")
        corsConfig.addExposedHeader("X-Get-Header")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsWebFilter(source)
    }
    @Bean(initMethod = "migrate")
    fun flyway(): Flyway {
        return Flyway(
            Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(
                    env.getRequiredProperty("spring.flyway.url"),
                    env.getRequiredProperty("spring.flyway.user"),
                    env.getRequiredProperty("spring.flyway.password")
                )
        )
    }
}
