package refrigeration.components.selector.config.polynomials.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface RefrigerantPolynomialMappingRepository : ReactiveCrudRepository<RefrigerantPolynomialMappingEntity, Long> {

    fun findByRefrigerantType(refrigerantType: String): Flux<RefrigerantPolynomialMappingEntity>
}
// SELECT * from public.refrigerant_polynomial_mapping where refrigerant_type='R134a'
// SELECT * FROM public.operation_type_polynomial_mapping where trans_critical=true
// SELECT * FROM public.compressor_polynomial_mappings where compressor_type='4GE-20';
// SELECT * FROM public.capacity_polynomial_mapping where capacity=50;
