package refrigeration.components.selector.config.valves.crud

import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import refrigeration.components.selector.api.valves.Valve
import refrigeration.components.selector.config.InterpolationGroup
import refrigeration.components.selector.config.polynomials.eval.LinearInterpolation
import refrigeration.components.selector.config.valves.ValveSelectionResult
import refrigeration.components.selector.config.valves.db.ValveEntity
import refrigeration.components.selector.config.valves.db.ValveRepository
import refrigeration.components.selector.util.gte
import refrigeration.components.selector.util.lte
import java.io.File

@Service
class ValveService(private val repo: ValveRepository) {

    private val interpolationService = LinearInterpolation()

    fun saveAll(entities: List<Valve>): Flux<Valve> {
        val converted = entities.map { ValveEntity.convert(it) }
        val monoEntities = Flux.fromIterable(converted)
        return repo.saveAll(monoEntities).map { it.convert() }
    }

    fun findAll(): Flux<Valve> {
        return repo.findAll().map { it.convert() }
    }
    fun deleteAll(): Mono<Void> {
        return repo.deleteAll()
    }

    fun getValveBestMatchRaw(
        refrigerant: String,
        capacity: Double,
        condensingTemperature: Double,
        evaporatingTemperature: Double
    ): Mono<MutableList<Valve>> {
        val minCapacity = capacity - (capacity * 0.1)
        val maxCapacity = capacity + (capacity * 3)
        val minCondensing = condensingTemperature - (10.0)
        val minEvaporating = evaporatingTemperature - (10.0)
        return repo.findByRefrigerantAndRefrigerationCapacityBetweenAndCondensingTemperatureGreaterThanEqualAndEvaporatingTemperatureGreaterThanEqual(
            refrigerant,
            minCapacity,
            maxCapacity,
            minCondensing,
            minEvaporating
        ).cache()
            .map { it.convert() }.collectList()

    }

    fun getValveBestMatchCandidate(
        refrigerant: String,
        capacity: Double,
        condensingTemperature: Double,
        evaporatingTemperature: Double
    ): Mono<ValveSelectionResult> {

        return getValveBestMatchRaw(refrigerant, capacity, condensingTemperature, evaporatingTemperature)
            .flatMap { findValues(it, condensingTemperature, evaporatingTemperature) }
            .map { it.minByOrNull { et -> et.valveCalculatedCapacity }!! }
            .onErrorResume { Mono.empty() }
    }


    fun findValues(
        valve: List<Valve>?,
        condensingTemperature: Double,
        evaporatingTemperature: Double
    ): Mono<List<ValveSelectionResult>> {
        valve ?: return Mono.just(listOf())
        val grouped = valve.groupBy { it.valveName }
            .map { createInterpolationGroups(it.value, condensingTemperature, evaporatingTemperature) }
            .map { getValue(it, evaporatingTemperature, condensingTemperature) }
        return Mono.just(grouped)
    }

    fun getValue(
        interpolationGroup: InterpolationGroup<Valve>,
        evaporatingTemperature: Double,
        condensingTemperature: Double
    ): ValveSelectionResult {

        val result = interpolationService.interpolate(interpolationGroup, evaporatingTemperature, condensingTemperature)
        val valveName = interpolationGroup.highXLowY?.valveName ?: ""
        return ValveSelectionResult(result.toDouble(), valveName)

    }

    fun createInterpolationGroups(
        valve: List<Valve?>?,
        condensingTemperature: Double,
        evaporatingTemperature: Double
    ): InterpolationGroup<Valve> {
        valve ?: return getDefaultInterpolationGroup()
        //TODO log and return
        val valveNames = valve.mapNotNull { it!!.valveName }.distinctBy { it }.size
        if (valveNames > 1) return getDefaultInterpolationGroup()
        val filteredValves = valve
            .filterNotNull()
        val lowEvaporation =
            filteredValves.sortedBy { it.evaporatingTemperature }
                .filter { it.evaporatingTemperature lte evaporatingTemperature }.lastOrNull()
        val highEvaporation =
            filteredValves.sortedBy { it.evaporatingTemperature }
                .firstOrNull { it.evaporatingTemperature gte evaporatingTemperature }

        val lowCondensing =
            filteredValves.sortedBy { it.condensingTemperature }
                .lastOrNull { it.condensingTemperature lte condensingTemperature }
        val highCondensing =
            filteredValves.sortedBy { it.condensingTemperature }
                .firstOrNull { it.condensingTemperature gte condensingTemperature }

        val lowEvaporationLowCondensation = filteredValves
            .asSequence()
            .sortedBy { it.evaporatingTemperature }
            .filter { it.evaporatingTemperature == lowEvaporation?.evaporatingTemperature }
            .sortedBy { it.condensingTemperature }
            .filter { it.condensingTemperature == lowCondensing?.condensingTemperature }.firstOrNull()

        val lowEvaporationHighCondensation = filteredValves
            .asSequence()
            .sortedBy { it.evaporatingTemperature }
            .filter { it.evaporatingTemperature == lowEvaporation?.evaporatingTemperature }
            .sortedBy { it.condensingTemperature }
            .filter { it.condensingTemperature == highCondensing?.condensingTemperature }.firstOrNull()

        val highEvaporationLowCondensation = filteredValves
            .asSequence()
            .sortedBy { it.evaporatingTemperature }
            .filter { it.evaporatingTemperature == highEvaporation?.evaporatingTemperature }
            .sortedBy { it.condensingTemperature }
            .filter { it.condensingTemperature == lowCondensing?.condensingTemperature }.firstOrNull()

        val highEvaporationHighCondensation = filteredValves
            .asSequence()
            .sortedBy { it.evaporatingTemperature }
            .filter { it.evaporatingTemperature == highEvaporation?.evaporatingTemperature }
            .sortedBy { it.condensingTemperature }
            .filter { it.condensingTemperature == lowCondensing?.condensingTemperature }.firstOrNull()

        return InterpolationGroup(
            false,
            "evaporationTemperature",
            "condensingTemperature",
            lowEvaporation?.evaporatingTemperature,
            highEvaporation?.evaporatingTemperature,
            lowCondensing?.condensingTemperature,
            highCondensing?.condensingTemperature,
            lowEvaporationLowCondensation,
            lowEvaporationHighCondensation,
            highEvaporationLowCondensation,
            highEvaporationHighCondensation
        )
    }

    fun getDefaultInterpolationGroup(): InterpolationGroup<Valve> {
        return InterpolationGroup(
            true,
            "",
            "",
            null, null, null, null, null, null, null, null
        )
    }

    fun findByRefrigerantAndCapacity(
        refrigerant: String,
        minimalCapacity: Double,
        maximalCapacity: Double
    ): Flux<Valve> {
        return repo
            .findByRefrigerantAndRefrigerationCapacityBetween(refrigerant, minimalCapacity, maximalCapacity)
            .mapNotNull { it.convert() }
    }

    fun findByRefrigerantAndRefrigerationCapacityBetweenAndValveName(
        refrigerant: String,
        minimalCapacity: Double,
        maximalCapacity: Double, valveName: String
    ): Flux<Valve> {
        return repo.findByRefrigerantAndRefrigerationCapacityBetweenAndValveName(
            refrigerant,
            minimalCapacity,
            maximalCapacity,
            valveName
        ).mapNotNull { it.convert() }
    }


    fun saveValvesFromFile(file: Mono<FilePart>) {
        val newFile = File("tmp")
        newFile.createNewFile()
        file
            .map { it.transferTo(newFile) }
            .flatMap { Mono.just(1) }
            .map { newFile.inputStream() }
    }
}
