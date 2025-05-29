package cz.encircled.joiner.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import cz.encircled.joiner.ksp.property.ArrayProcessor

class QuerydslClassProcessor(
    private val entityClass: KSClassDeclaration,
    private val classPackage : String = entityClass.packageName.asString(),
    template : JoinerTemplate = JavaJoinerTemplate(classPackage)
) : JoinerTemplate by template {

    private val fields: MutableList<Field>
    private val fieldNames: MutableSet<String> = mutableSetOf()
    private val singularReferences: MutableSet<Pair<String, KSType>> = mutableSetOf()

    private val className = entityClass.simpleName()
    private val qClassName = "Q$className"

    init {
        fields = mutableListOf(
            Field("long", "serialVersionUID", "${className.hashCode()}L", true),
            Field("PathInits", "INITS", "PathInits.DIRECT2", true, false)
        )
    }

    fun generateMetamodelClass(): String {
        getSupertype(entityClass)?.let {
            addField(it, "_super", "new $it(this)")
        }

        entityClass.getAllProperties().forEach {
            processProperty(it, entityClass)
        }

        getStaticAccessor(className).let {
            addField(qClassName, it, "new $qClassName(\"$it\")", true)
        }

        val sb = StringBuilder(
            """
                package $classPackage;
                
                import static com.querydsl.core.types.PathMetadataFactory.*;
                import com.querydsl.core.types.dsl.*;
                import com.querydsl.core.types.PathMetadata;
                import javax.annotation.processing.Generated;
                import com.querydsl.core.types.Path;
                import com.querydsl.core.types.dsl.PathInits;
                
                /**
                 * $qClassName is a Querydsl query type for $className
                 */
                @Generated("cz.encircled.joiner.ksp.QuerydslProcessor")
                public class $qClassName extends EntityPathBase<$className> {
                
                
                """.trimIndent()
        )
        sb.append(fields.joinToString("\n\n") { "    $it" })
        sb.append("\n")
        sb.append(generateConstructors(className).joinToString("") { it.stringify(qClassName) })
        sb.append("\n}")

        return sb.toString()
    }

    private fun getSupertype(entityClass: KSClassDeclaration): String? {
        val parent = entityClass.superTypes
            .map { it.resolve().declaration }
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull { it.classKind == ClassKind.CLASS && it.simpleName() != "Any" && it.simpleName() != "Object" }

        return classReference(parent, "Q")
    }

    private fun getStaticAccessor(className: String): String {
        val base = className.decapitalize()
        if (base !in fieldNames) return base

        return generateSequence(1) { it + 1 }
            .map { "$base$it" }
            .first { it !in fieldNames }
    }

    private fun processProperty(
        property: KSPropertyDeclaration,
        entityClass: KSClassDeclaration,
    ) {
        val propertyName = property.simpleName()
        val propertyType = property.type.resolve()
        val typeDeclaration = propertyType.declaration

        if (propertyName.startsWith("_") ||
            property.modifiers.contains(Modifier.JAVA_TRANSIENT) ||
            property.hasAnnotation("Transient") ||
            property.isInCompanionObject()
        ) {
            return
        }

        fieldNames.add(propertyName)

        val isInherited = property.parentDeclaration?.name() != entityClass.name()

        when {
            propertyType.isArrayType() -> {
                fields.add(ArrayProcessor(propertyName, propertyType).process())
            }

            propertyType.isCollectionType() -> {
                val elementType = propertyType.arguments.firstOrNull()?.type?.resolve()
                if (elementType != null) {
                    val elementTypeName = classReference(elementType)
                    val elementTypeLiteral = classLiteral(elementType)

                    val qElementTypeName: String
                    val qElementRawTypeName: String

                    if (elementType.declaration.hasAnnotation("Entity")) {
                        qElementTypeName = "Q$elementTypeName"
                        qElementRawTypeName = classLiteral(elementType, "Q")
                    } else {
                        // Collection of non-entities, like a list of strings
                        qElementTypeName = "SimplePath<$elementTypeName>"
                        qElementRawTypeName = "SimplePath${classLiteralSuffix()}"
                    }

                    val collectionType = when {
                        propertyType.isListType() -> "List"
                        propertyType.isSetType() -> "Set"
                        propertyType.isArrayType() -> "Array"
                        else -> "Collection"
                    }
                    val value = if (isInherited) "_super.$propertyName"
                    else "this.<$elementTypeName, $qElementTypeName>create$collectionType(\"$propertyName\", $elementTypeLiteral, $qElementRawTypeName, PathInits.DIRECT2)"

                    addField(
                        "${collectionType}Path<$elementTypeName, $qElementTypeName>",
                        propertyName,
                        value
                    )
                }
            }

            propertyType.isBasicType() -> {
                val pathType = getPathTypeForBasicType(propertyType)
                val value = when {
                    isInherited -> "_super.$propertyName"
                    pathType == "StringPath" -> "createString(\"$propertyName\")"
                    pathType == "BooleanPath" -> "createBoolean(\"$propertyName\")"
                    else -> "${getCreateMethodForPathType(pathType)}(\"$propertyName\", ${classLiteral(propertyType)})"
                }
                addField(pathType, propertyName, value)
            }

            // Entity references
            typeDeclaration is KSClassDeclaration && typeDeclaration.hasAnnotation("Entity") -> {
                val qReferencedEntityName = "Q${typeDeclaration.simpleName()}"
                val value = if (isInherited) "= _super.$propertyName" else ""

                val qualifiedName =
                    if (entityClass.name() == typeDeclaration.name()) qReferencedEntityName
                    else "${typeDeclaration.packageName.asString()}.$qReferencedEntityName"

                addField(qualifiedName, propertyName, value)

                if (!isInherited) {
                    singularReferences.add(propertyName to propertyType)
                }
            }

            else -> {
                addField(
                    "SimplePath<${classReference(typeDeclaration)}>",
                    propertyName,
                    "createSimple(\"$propertyName\", ${classReference(typeDeclaration)}.class)"
                )
            }
        }
    }

    private fun getPathTypeForBasicType(type: KSType): String {
        val typeName = classReference(type)

        return basicTypeToPathType[typeName]
            ?: if (type.declaration.modifiers.contains(Modifier.ENUM)) "EnumPath<$typeName>"
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

    private fun generateConstructors(className: String): List<Constructor> {
        val referenceInit = singularReferences.joinToString("\n") {
            val ifInitialized = "new ${classReference(it.second, "Q")}(forProperty(\"${it.first}\"), inits.get(\"${it.first}\"))"

            "this.${it.first} = inits.isInitialized(\"${it.first}\") ? $ifInitialized : null"
        }

        return listOf(
            Constructor(listOf("this($className.class, forVariable(variable), INITS)"), "String" to "variable"),

            Constructor(
                listOf("this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS))"),
                "Path<? extends $className>" to "path"
            ),

            Constructor(listOf("this(metadata, PathInits.getFor(metadata, INITS))"), "PathMetadata" to "metadata"),

            Constructor(
                listOf("this($className.class, metadata, inits)"),
                "PathMetadata" to "metadata",
                "PathInits" to "inits"
            ),

            Constructor(
                listOf("super(type, metadata, inits)", referenceInit),
                "Class<? extends $className>" to "type",
                "PathMetadata" to "metadata",
                "PathInits" to "inits"
            ),
        )
    }

    private fun addField(
        type: String,
        name: String,
        value: String,
        isStatic: Boolean = false,
        isPublic: Boolean = true
    ) {
        fields.add(Field(type, name, value, isStatic, isPublic))
    }

}

class Constructor(val body: List<String>, vararg val fields: Pair<String, String>) {
    fun stringify(qClassName: String): String {
        val fieldsStr = fields.joinToString { it.first + " " + it.second }
        return """
    public $qClassName($fieldsStr) {
    ${body.filter { it.isNotBlank() }.joinToString("\n") { "    $it;" }}
    }
    """
    }
}

class Field(
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