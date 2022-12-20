package refrigeration.components.selector.config

data class InterpolationValuesGroup<T>(
     val ids: List<Long>,
     val first: T?,
     val second: T?,
     val third: T?,
     val fourth: T?
)