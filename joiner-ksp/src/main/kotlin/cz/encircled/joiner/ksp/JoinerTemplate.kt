package cz.encircled.joiner.ksp

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType

interface JoinerTemplate {

    /**
     * Like com.my.SampleClass
     */
    fun classReference(target: KSDeclaration?, namePrefix: String = ""): String

    /**
     * Like com.my.SampleClass
     */
    fun classReference(target: KSType, namePrefix: String = ""): String

    /**
     * Like com.my.SampleClass.class
     */
    fun classLiteral(target: KSType, namePrefix: String = ""): String
    fun classLiteralSuffix(): String
}

class JavaJoinerTemplate(packageName: String) : BaseJoinerTemplate(packageName) {

    override fun classLiteralSuffix() : String = ".class"
}

class KtJoinerTemplate(packageName: String) : BaseJoinerTemplate(packageName) {
    override fun classLiteralSuffix() : String = "::class"
}

abstract class BaseJoinerTemplate(val packageName: String) : JoinerTemplate {

    /**
     * com.my.SampleClass
     */
    override fun classReference(target: KSDeclaration?, namePrefix: String): String {
        var pkg = target?.packageName?.asString() ?: return ""
        val simpleName = if (pkg.startsWith("kotlin")) {
            val (p, n) = getJavaClassName(target)
            pkg = p
            n
        } else {
            target.simpleName()
        }

        val isQuerydslDefault = pkg.startsWith("com.querydsl")
        val isJavaDefault = pkg.startsWith("java.lang")

        return if (isJavaDefault || isQuerydslDefault || pkg == packageName) {
            namePrefix + simpleName
        } else "$pkg.$namePrefix$simpleName"
    }

    override fun classReference(target: KSType, namePrefix: String): String {
        return classReference(target.declaration, namePrefix)
    }

    override fun classLiteral(target: KSType, namePrefix: String): String {
        return classReference(target, namePrefix) + classLiteralSuffix()
    }

    // TODO mapping
    private fun getJavaClassName(type: KSDeclaration): Pair<String, String> {
        val typeName = type.name() ?: return "java.lang" to "Object"
        return when (typeName) {
            "kotlin.Int" -> "java.lang" to "Integer"
            "kotlin.Char" -> "java.lang" to "Character"
            "kotlin.Any" -> "java.lang" to "Object"

            "kotlin.collections.MutableList", "kotlin.collections.List" -> {
                "java.util" to "List"
            }

            "kotlin.collections.MutableSet", "kotlin.collections.Set" -> {
                "java.util" to "Set"
            }

            "kotlin.collections.MutableMap", "kotlin.collections.Map" -> {
                "java.util" to "Map"
            }

            else -> "java.lang" to type.simpleName()
        }
    }

}