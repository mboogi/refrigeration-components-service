package refrigeration.components.selector.api

interface Context {
    fun bind(key: String, value: Any)
    fun <T>lookup(key: String): T?
    fun unbind(key: String): Boolean
    fun keys():Set<String>
}
