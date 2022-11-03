package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface FrequencyPolynomialMappingRepository : ReactiveCrudRepository<FrequencyPolynomialMappingEntity, Long>
