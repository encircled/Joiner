package cz.encircled.joiner.ksp.property

import cz.encircled.joiner.ksp.OutProperty
import cz.encircled.joiner.ksp.name
import cz.encircled.joiner.ksp.simpleName

object ArrayProcessor {

    // TODO must use template
    fun process(info: PropertyInfo) : OutProperty {
        val propertyType = info.propertyType
        val propertyName = info.propertyName

        val elementType = when (propertyType.name()) {
            "kotlin.ByteArray" -> "Byte"
            "kotlin.IntArray" -> "Int"
            "kotlin.LongArray" -> "Long"
            "kotlin.FloatArray" -> "Float"
            "kotlin.DoubleArray" -> "Double"
            "kotlin.BooleanArray" -> "Boolean"
            "kotlin.ShortArray" -> "Short"
            "kotlin.CharArray" -> "Char"
            else -> propertyType.arguments.firstOrNull()?.type?.resolve()?.simpleName()
        }

        return OutProperty("ArrayPath<$elementType[], $elementType>", propertyName, "createArray(\"$propertyName\", $elementType[].class)")
    }

}