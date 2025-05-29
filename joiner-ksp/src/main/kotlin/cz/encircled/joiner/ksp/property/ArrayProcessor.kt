package cz.encircled.joiner.ksp.property

import com.google.devtools.ksp.symbol.KSType
import cz.encircled.joiner.ksp.Field
import cz.encircled.joiner.ksp.name
import cz.encircled.joiner.ksp.simpleName

class ArrayProcessor(val propertyName : String, val propertyType: KSType) {

    // TODO must use template
    fun process() : Field {
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

        return Field("ArrayPath<$elementType[], $elementType>", propertyName, "createArray(\"$propertyName\", $elementType[].class)")
    }

}