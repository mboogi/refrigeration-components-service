package refrigeration.components.selector

import java.util.function.Supplier

class UrlSupplier(private val url: String) : Supplier<String> {
    override fun get(): String {
        return url.replace("jdbc", "r2dbc")
    }
}
