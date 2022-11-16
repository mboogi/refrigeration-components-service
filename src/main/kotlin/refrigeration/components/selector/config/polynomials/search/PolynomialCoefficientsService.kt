package refrigeration.components.selector.config.polynomials.search

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import refrigeration.components.selector.DefaultSubscriber
import refrigeration.components.selector.api.polynomials.PolynomialCoefficientRequest
import refrigeration.components.selector.config.polynomials.db.*
import refrigeration.components.selector.pools.WorkerPool
import refrigeration.components.selector.util.*

@Service
class PolynomialCoefficientsService(
    private val capacityRepos: CapacityPolynomialMappingRepository,
    private val compressorRepo: CompressorPolynomialMappingRepository,
    private val operationType: OperationTypePolynomialMappingRepository,
    private val refrigerantRepo: RefrigerantPolynomialMappingRepository,
    private val frequencyRepository: FrequencyPolynomialMappingRepository,
    private val polynomialSearchResult: PolynomialSearchResultRepository,
    private val polynomialTypeRepository: PolynomialTypeRepository,
    private val repo: PolynomialCoefficientsRepository,
    private val pool: WorkerPool
) {

    companion object {
        private final val logger = LoggerFactory.getLogger(PolynomialCoefficientsService::class.java)
    }

    fun findAllPolynomialMappings(): Flux<PolynomialCoefficientsEntity> {
        return repo.findAll()
    }

    fun findPolynomialsForCompressorAndRefrigerant(
        compressor: String,
        refrigerant: String,
        transCritical: Boolean,
        polynomialType: String
    ): Flux<PolynomialSearchResult> {
        val start = System.currentTimeMillis()
        val result = polynomialSearchResult.findByCompressorTypeAndRefrigerantTypeAndTransCriticalAndPolynomialType(
            compressor,
            refrigerant,
            transCritical,
            polynomialType
        )
            .subscribeOn(Schedulers.fromExecutor(pool))
        val end = System.currentTimeMillis()
        logger.info("search duration ${(end - start)}")
        return result
    }

    fun findPolynomialMappingByID(id: Long): Mono<PolynomialCoefficientsEntity> {
        return repo.findById(id)
    }

    fun findPolynomialMappingByIdIn(ids: List<Long>): Flux<PolynomialCoefficientsEntity> {
        return repo.findByIdIn(ids)
    }

    fun findAllRefrigerantMappings(): Flux<RefrigerantPolynomialMappingEntity> {
        return refrigerantRepo.findAll()
    }

    fun findRefrigerantMappings(name: String): Flux<RefrigerantPolynomialMappingEntity> {
        return refrigerantRepo.findByRefrigerantType(name)
    }

    fun findRefrigerantMappingById(id: Long): Mono<RefrigerantPolynomialMappingEntity> {
        return refrigerantRepo.findById(id)
    }

    fun findAllOperationType(): Flux<OperationTypePolynomialMappingEntity> {
        return operationType.findAll()
    }

    fun findAllCapacityMappings(): Flux<CapacityPolynomialMappingEntity> {
        return capacityRepos.findAll()
    }

    fun findCapacityMappingById(id: Long): Mono<CapacityPolynomialMappingEntity> {
        return capacityRepos.findById(id)
    }

    fun findOperationTypeMappingById(id: Long): Mono<OperationTypePolynomialMappingEntity> {
        return operationType.findById(id)
    }

    fun findAllCompressorMappings(): Flux<CompressorPolynomialMappingEntity> {
        return compressorRepo.findAll()
    }

    fun findCompressorMappingsById(id: Long): Mono<CompressorPolynomialMappingEntity> {
        return compressorRepo.findById(id)
    }

    fun save(request: List<PolynomialCoefficientRequest>): Flux<Pair<PolynomialCoefficientsEntity, PolynomialCoefficientRequest>> {
        val result = request.map { saveAll(it) }
        return Flux.merge(result).doOnNext { saveMappings(it.first, it.second) }
    }

    fun saveAll(request: PolynomialCoefficientRequest): Mono<Pair<PolynomialCoefficientsEntity, PolynomialCoefficientRequest>> {
        val entity = PolynomialCoefficientsEntity.convert(request)
        return savePolynomial(entity).log().map { Pair(it, request) }.subscribeOn(Schedulers.boundedElastic())
    }

    fun saveMappings(polynomials: PolynomialCoefficientsEntity, request: PolynomialCoefficientRequest) {
        val id = polynomials.id
        val refrigerantMapping = getRefrigerantMapping(request, id)
        val operationType = getOperationTypeMapping(request, id)
        val compressorMapping = getCompressorMapping(request, id)
        val capacityMapping = getCapacityMapping(request, id)
        val frequencyMapping = getFrequencyMapping(request, id)
        val polynomialType = getPolynomialTypeMapping(request, id)
        saveCapacityMapping(CapacityPolynomialMappingEntity.convert(capacityMapping))
            .subscribe(
                DefaultSubscriber(
                    "capacityMapping",
                    logger
                )
            )
        saveRefrigerantMapping(RefrigerantPolynomialMappingEntity.convert(refrigerantMapping))
            .subscribe(
                DefaultSubscriber(
                    "refrigerantMapping",
                    logger
                )
            )
        saveOperationType(OperationTypePolynomialMappingEntity.convert(operationType))
            .subscribe(
                DefaultSubscriber(
                    "operationType",
                    logger
                )
            )
        saveCompressorMapping(CompressorPolynomialMappingEntity.convert(compressorMapping))
            .subscribe(
                DefaultSubscriber(
                    "compressorMapping",
                    logger
                )
            )

        saveFrequencyMapping(FrequencyPolynomialMappingEntity.convert(frequencyMapping))
            .subscribe(
                DefaultSubscriber(
                    "frequencyMapping",
                    logger
                )
            )
        savePolynomialTypeMapping(PolynomialTypeEntity.convert(polynomialType))
            .subscribe(
                DefaultSubscriber(
                    "polynomialType",
                    logger
                )
            )
    }

    fun savePolynomial(data: PolynomialCoefficientsEntity): Mono<PolynomialCoefficientsEntity> {
        return repo.save(data)
    }

    fun getPolynomialById(id: Long): Mono<PolynomialCoefficientsEntity> {
        return repo.findById(id)
    }

    fun saveRefrigerantMapping(data: RefrigerantPolynomialMappingEntity): Mono<RefrigerantPolynomialMappingEntity> {
        return refrigerantRepo.save(data)
    }

    fun saveFrequencyMapping(data: FrequencyPolynomialMappingEntity): Mono<FrequencyPolynomialMappingEntity> {
        return frequencyRepository.save(data)
    }

    fun saveOperationType(data: OperationTypePolynomialMappingEntity): Mono<OperationTypePolynomialMappingEntity> {
        return operationType.save(data)
    }

    fun saveCompressorMapping(data: CompressorPolynomialMappingEntity): Mono<CompressorPolynomialMappingEntity> {
        return compressorRepo.save(data)
    }

    fun saveCapacityMapping(data: CapacityPolynomialMappingEntity): Mono<CapacityPolynomialMappingEntity> {
        return capacityRepos.save(data)
    }

    fun savePolynomialTypeMapping(data: PolynomialTypeEntity): Mono<PolynomialTypeEntity> {
        return polynomialTypeRepository.save(data)
    }
}
