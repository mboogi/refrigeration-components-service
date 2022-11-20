package refrigeration.components.selector.config.pipes.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import refrigeration.components.selector.api.pipes.Pipe

@Table(name = "hydraulic_pipe")
data class PipeEntity(
    @Id
    val name: String,
    val standard: String,
    val outerDiameter: Double,
    val innerDiameter: Double,
    val material: String,
    val maxPressure: Double
) {
    fun convert() = Pipe(name, standard, outerDiameter, innerDiameter, material, maxPressure)

    companion object {
        fun convert(request: Pipe): PipeEntity {
            return PipeEntity(

                request.name,
                request.standard,
                request.outerDiameter,
                request.innerDiameter,
                request.material,
                request.maxPressure
            )
        }
    }
}
