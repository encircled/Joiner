package cz.encircled.joiner.ksp.property

import com.google.devtools.ksp.symbol.KSType
import cz.encircled.joiner.ksp.OutProperty
import cz.encircled.joiner.ksp.hasAnnotation
import cz.encircled.joiner.ksp.isArrayType
import cz.encircled.joiner.ksp.isListType
import cz.encircled.joiner.ksp.isMapType
import cz.encircled.joiner.ksp.isSetType

object CollectionProcessor {

    fun process(propInfo: PropertyInfo) : OutProperty? {
        val propertyType = propInfo.propertyType
        val propertyName = propInfo.propertyName

        val elementTypes = propertyType.arguments.mapNotNull { it.type?.resolve() }

        if (elementTypes.isNotEmpty()) {
            val resolved = elementTypes.map { resolveQTypeInfo(propInfo, it) }

            val allTypeNames = resolved.joinToString { it.typeName }
            val allTypeLiterals = resolved.joinToString { it.literal }

            val collectionType = when {
                propertyType.isListType() -> "List"
                propertyType.isSetType() -> "Set"
                propertyType.isMapType() -> "Map"
                propertyType.isArrayType() -> "Array"
                else -> "Collection"
            }

            val pathInits = if (collectionType == "Map") "" else ", PathInits.DIRECT2"
            val value = if (propInfo.isInherited) "_super.$propertyName"
            else "this.<$allTypeNames, ${resolved.last().qTypeName}>" +
                    "create$collectionType(\"$propertyName\", $allTypeLiterals, ${resolved.last().qTypeLiteral}$pathInits)"

            return OutProperty(
                "${collectionType}Path<$allTypeNames, ${resolved.last().qTypeName}>",
                propertyName,
                value
            )
        }
        return null
    }

    private fun resolveQTypeInfo(info: PropertyInfo, type: KSType): QTypeInfo {
        val typeName = info.classReference(type)
        val literal = info.classLiteral(type)

        val qTypeName: String
        val qTypeLiteral: String

        if (type.declaration.hasAnnotation("Entity")) {
            qTypeName = "Q$typeName"
            qTypeLiteral = info.classLiteral(type, "Q")
        } else {
            qTypeName = "SimplePath<$typeName>"
            qTypeLiteral = "SimplePath${info.classLiteralSuffix()}"
        }

        return QTypeInfo(typeName, literal, qTypeName, qTypeLiteral)
    }

    data class QTypeInfo(
        val typeName: String,
        val literal: String,
        val qTypeName: String,
        val qTypeLiteral: String
    )

}