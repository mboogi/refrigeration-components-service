package refrigeration.components.selector.config.polynomials.search

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono
import refrigeration.components.selector.config.polynomials.db.PolynomialSearchResult
import refrigeration.components.selector.pools.WorkerPool
import refrigeration.components.selector.util.eq
import refrigeration.components.selector.util.gte
import refrigeration.components.selector.util.lte

@Service
class PolynomialSearchService(private val service: PolynomialCoefficientsService, private val pool: WorkerPool) {
    private val lowCapLowFreqGroupName = "lowCapacityLowFrequencyGroup"
    private val lowCapHighFreqGroupName = "lowCapacityHighFrequencyGroup"
    private val highCapLowFreGroupName = "highCapacityLowFrequencyGroup"
    private val highCapHighFreqGroupName = "highCapacityHighFrequencyGroup"

    companion object {
        private val logger = LoggerFactory.getLogger(PolynomialSearchService::class.java)
    }

    fun defaultPolynomialGroup(): PolynomialGroups {
        return PolynomialGroups(
            true,
            null,
            null,
            null,
            null,
            PolynomialSearchGroup(lowCapLowFreqGroupName, null),
            PolynomialSearchGroup(lowCapHighFreqGroupName, null),
            PolynomialSearchGroup(highCapLowFreGroupName, null),
            PolynomialSearchGroup(highCapHighFreqGroupName, null)
        )
    }

    fun getPolynomialGroups(
        compressor: String,
        refrigerant: String,
        capacity: Double,
        frequency: Double,
        transCritical: Boolean,
        polynomialType: String
    ): Mono<PolynomialGroups> {
        logger.debug("$compressor $refrigerant $transCritical $polynomialType")
        val result =
            service
                .findPolynomialsForCompressorAndRefrigerant(
                    compressor,
                    refrigerant,
                    transCritical,
                    polynomialType
                )
                .cache()
                .subscribeOn(Schedulers.fromExecutor(pool), false)
                .collectList()
                .toMono()

        return result.flatMap { selectNearestPolynomials(capacity, frequency, it) }
    }

    private fun selectNearestPolynomials(
        capacity: Double,
        frequency: Double,
        polynomials: List<PolynomialSearchResult>
    ): Mono<PolynomialGroups> {
        val lowCapacity =
            polynomials.sortedBy { it.capacity }.filter { it.capacity lte capacity }.map { it.capacity }.lastOrNull()
        val highCapacity =
            polynomials.sortedBy { it.capacity }.filter { it.capacity gte capacity }.map { it.capacity }.firstOrNull()

        val lowFrequency =
            polynomials.sortedBy { it.frequency }.filter { it.frequency lte frequency }.map { it.frequency }
                .lastOrNull()
        val highFrequency =
            polynomials.sortedBy { it.frequency }.filter { it.frequency gte frequency }.map { it.frequency }
                .firstOrNull()

        val lowCapacityLowFrequency =
            polynomials.filter { (it.capacity eq lowCapacity) and (it.frequency eq lowFrequency) }.firstOrNull()
        val lowCapacityHighFrequency =
            polynomials.filter { (it.capacity eq lowCapacity) and (it.frequency eq highFrequency) }.firstOrNull()

        val highCapacityLowFrequency =
            polynomials.filter { (it.capacity eq highCapacity) and (it.frequency eq lowFrequency) }.firstOrNull()
        val highCapacityHighFrequency =
            polynomials.filter { (it.capacity eq highCapacity) and (it.frequency eq highFrequency) }.firstOrNull()

        val polynomialsFound = !(lowCapacityLowFrequency == null).and(lowCapacityHighFrequency == null)
            .and(highCapacityLowFrequency == null).and(highCapacityHighFrequency == null)

        if (!polynomialsFound) return Mono.just(defaultPolynomialGroup())

        val lowCapacityLowFrequencyGroup =
            PolynomialSearchGroup(lowCapLowFreqGroupName, lowCapacityLowFrequency)
        val lowCapacityHighFrequencyGroup =
            PolynomialSearchGroup(lowCapHighFreqGroupName, lowCapacityHighFrequency)
        val highCapacityLowFrequencyGroup = PolynomialSearchGroup(highCapLowFreGroupName, highCapacityLowFrequency)
        val highCapacityHighFrequencyGroup =
            PolynomialSearchGroup(highCapHighFreqGroupName, highCapacityHighFrequency)

        val result = PolynomialGroups(
            false,
            lowCapacity,
            highCapacity,
            lowFrequency,
            highFrequency,
            lowCapacityLowFrequencyGroup,
            lowCapacityHighFrequencyGroup,
            highCapacityLowFrequencyGroup,
            highCapacityHighFrequencyGroup
        )
        return Mono.just(result)
    }
}
