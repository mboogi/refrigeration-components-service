package refrigeration.components.selector.api

class EvaluationContext : Context {
    // limit the size of the map
    private val registry = mutableMapOf<String, Any>()

    override fun bind(key: String, value: Any) {
        registry[key] = value
    }

    override fun <T> lookup(key: String): T? {
        return registry[key] as? T?
    }

    override fun unbind(key: String): Boolean {
        registry.remove(key)
        return true
    }

    override fun keys(): Set<String> {
        return registry.keys.toSet()
    }
}
