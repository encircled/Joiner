package cz.encircled.joiner.ksp.property

import com.google.devtools.ksp.symbol.Modifier
import cz.encircled.joiner.ksp.OutProperty
import cz.encircled.joiner.ksp.basicTypeToPathType

object BasicPropProcessor {

    fun process(propInfo: PropertyInfo): OutProperty {
        val propertyType = propInfo.propertyType
        val propertyName = propInfo.propertyName
        val pathType = getPathTypeForBasicType(propInfo)

        val value = when {
            propInfo.isInherited -> "_super.$propertyName"
            pathType == "StringPath" -> "createString(\"$propertyName\")"
            pathType == "BooleanPath" -> "createBoolean(\"$propertyName\")"
            else -> "${getCreateMethodForPathType(pathType)}(\"$propertyName\", ${propInfo.classLiteral(propertyType)})"
        }
        return OutProperty(pathType, propertyName, value)
    }


    private fun getPathTypeForBasicType(propInfo: PropertyInfo): String {
        val typeName = propInfo.classReference(propInfo.propertyType)

        return basicTypeToPathType[typeName]
            ?: if (propInfo.propertyType.declaration.modifiers.contains(Modifier.ENUM)) "EnumPath<$typeName>"
            else "SimplePath<$typeName>"
    }

    private fun getCreateMethodForPathType(pathType: String): String {
        return when {
            pathType.startsWith("NumberPath") -> "createNumber"
            pathType.startsWith("DatePath") -> "createDate"
            pathType.startsWith("DateTimePath") -> "createDateTime"
            pathType.startsWith("EnumPath") -> "createEnum"
            else -> "createSimple"
        }
    }

}