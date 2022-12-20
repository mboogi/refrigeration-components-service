package refrigeration.components.selector

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import refrigeration.components.selector.api.polynomials.PolynomialCoefficientRequest
import refrigeration.components.selector.api.valves.Valve
import refrigeration.components.selector.config.pipes.db.PipeEntity
import java.io.File

class TestDataProvider {
    private val mapper = ObjectMapper().registerModule(
        KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, true)
            .build()
    )

    init {

        mapper.findAndRegisterModules()
        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
    }

    fun getPolynomialCoefficientRequests(name: String): List<PolynomialCoefficientRequest> {
        return mapper.readValue(readFile(name), object : TypeReference<List<PolynomialCoefficientRequest>>() {})
    }

    fun getPipes(name: String): List<PipeEntity> {
        return mapper.readValue(readFile(name), object : TypeReference<List<PipeEntity>>() {})
    }
    fun getValves(name:String):List<Valve>{
        return mapper.readValue(readFile(name), object : TypeReference<List<Valve>>() {})
    }



    private fun readFile(name: String): File {
        val resourceLocation = TestDataProvider::class.java.classLoader.getResource("data/$name")
        if (resourceLocation == null) {
            val dir = File(TestDataProvider::class.java.classLoader.getResource("data/$name")!!.toURI())
            val result = File(dir, name)
            result.createNewFile()
            return result
        }
        return File(resourceLocation.toURI())
    }
}
