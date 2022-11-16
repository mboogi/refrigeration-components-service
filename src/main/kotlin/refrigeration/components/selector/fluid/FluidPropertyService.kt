package refrigeration.components.selector.fluid

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import refrigeration.components.selector.fluid.api.FluidPropertiesApi
import refrigeration.components.selector.fluid.api.FluidPropertyRequest
import refrigeration.components.selector.fluid.external.FluidPropertyClient
import refrigeration.components.selector.pools.WorkerPool

@Service
class FluidPropertyService(
    private val client: FluidPropertyClient,
    private val pool: WorkerPool
) : FluidPropertiesApi {
    companion object {
        private val logger = LoggerFactory.getLogger(FluidPropertyService::class.java)
    }

    override fun getFluids(): Flux<String> {
        return client.getFluids().subscribeOn(Schedulers.fromExecutor(pool))
    }

    override fun getNumericProperty(request: FluidPropertyRequest): Mono<Double> {
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    override fun getNumericProperties(request: List<FluidPropertyRequest>): Flux<Double> {
        return client.getNumericProperties(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getDryVapourEnthalpy(temperature: Double, fluid: String): Mono<Double> {
        val input = FluidPropertyRequest("H", "T", temperature, "Q", 1.0, fluid)
        return client.getNumericProperty(input).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getWetVapourEnthalpy(temperature: Double, fluid: String): Mono<Double> {
        val input = FluidPropertyRequest("H", "T", temperature, "Q", 1.0, fluid)
        return client.getNumericProperty(input).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getSuperHeatedVapourEnthalpy(temperature: Double, pressure: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("H", "T", temperature, "P|gas", pressure, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getTemperatureForSuperheatedEnthalpy(enthalpy: Double, pressure: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("T", "P", pressure, "H|gas", enthalpy, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getSubCooledVapourEnthalpy(temperature: Double, pressure: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("H", "T", temperature, "P|liq", pressure, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getSuperHeatedVapourDensity(temperature: Double, pressure: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("D", "T", temperature, "P|gas", pressure, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getDryVapourDensity(temperature: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("D", "T", temperature, "Q", 1.0, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }
    fun getWetVapourDensity(temperature: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("D", "T", temperature, "Q", 0.0, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getSubCooledVapourDensity(temperature: Double, pressure: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("D", "T", temperature, "P|liq", pressure, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getDryVapourPressure(temperature: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("P", "T", temperature, "Q", 1.0, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }

    fun getWetVapourPressure(temperature: Double, fluid: String): Mono<Double> {
        val request = FluidPropertyRequest("P", "T", temperature, "Q", 0.0, fluid)
        return client.getNumericProperty(request).subscribeOn(Schedulers.fromExecutor(pool))
    }
}
