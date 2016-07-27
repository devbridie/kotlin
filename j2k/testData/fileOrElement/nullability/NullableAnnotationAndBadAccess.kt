// ERROR: Type mismatch: inferred type is String? but String was expected
class NuLib {
    fun getJbValue(provider: NullValueProvider): String {
        return provider.provide()!!
    }

    class NullValueProvider {
        fun provide(): String? {
            return "aaa"
        }
    }
}
