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
        if (typeName == "kotlin.Int") {
            return "java.lang" to "Integer"
        } else if (typeName == "kotlin.collections.MutableList" || typeName == "kotlin.collections.List") {
            return "java.util" to "List"
        } else if (typeName == "kotlin.collections.MutableSet" || typeName == "kotlin.collections.Set") {
            return "java.util" to "Set"
        } else if (typeName == "kotlin.collections.MutableMap" || typeName == "kotlin.collections.Map") {
            return "java.util" to "Map"
        }
        return "java.lang" to type.simpleName()
    }

}