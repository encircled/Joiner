package cz.encircled.joiner.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

fun KSType.name(): String? = declaration.name()

fun KSType.simpleName(): String = declaration.simpleName()

fun KSDeclaration.name() = qualifiedName?.asString()

fun KSDeclaration.simpleName() = simpleName.asString()

fun String.decapitalize(): String = replaceFirstChar { it.lowercase() }

fun KSDeclaration.hasAnnotation(name: String): Boolean = annotations.any { it.shortName.asString() == name }

fun KSPropertyDeclaration.isInCompanionObject(): Boolean {
    return (parentDeclaration as? KSClassDeclaration)?.isCompanionObject == true
}

fun KSType.isBasicType(): Boolean = name() in basicTypeToPathType || declaration.modifiers.contains(Modifier.ENUM)

fun KSType.isCollectionType(): Boolean = name() in collectionTypeMapping

fun KSType.isListType() = name() in listTypeMapping

fun KSType.isMapType() = name() in mapTypeMapping

fun KSType.isSetType() = name() in setTypeMapping

fun KSType.isArrayType() = name() in arrayTypeMapping