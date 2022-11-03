package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OperationTypePolynomialMappingRepository :
    ReactiveCrudRepository<OperationTypePolynomialMappingEntity, Long>
