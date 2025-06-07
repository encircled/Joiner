package cz.encircled.joiner.ksp

class OutConstructor(val body: List<String>, vararg val fields: Pair<String, String>) {
    fun stringify(qClassName: String): String {
        val fieldsStr = fields.joinToString { it.first + " " + it.second }
        return """
    public $qClassName($fieldsStr) {
    ${body.filter { it.isNotBlank() }.joinToString("\n") { "$it;".prependIndent() }}
    }
    """
    }
}

class OutProperty(
    val type: String,
    val name: String,
    val value: String = "",
    val isStatic: Boolean = false,
    val isPublic: Boolean = true
) {
    override fun toString(): String {
        val visibility = if (isPublic) "public" else "private"
        val static = if (isStatic) "static " else ""
        val modifiers = "$visibility ${static}final"
        val initializer = if (value.isNotBlank()) " = $value" else ""

        return "$modifiers $type $name$initializer;"
    }
}