package cz.encircled.joiner.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

typealias PropertyType = String
typealias ResultType = String

val mapping: Map<Set<PropertyType>, ResultType> = mapOf()

/**
 * KSP processor for generating Querydsl metamodel classes from JPA entities.
 * This processor replicates the functionality of the Querydsl apt-maven-plugin.
 */
class QuerydslProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator? = null,
) : SymbolProcessor {

    fun Resolver.getSymbolsWithAnnotations(vararg annotations: String) =
        annotations.flatMap { getSymbolsWithAnnotation(it) }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Starting Joiner Querydsl processor")

        // Find all classes annotated with @Entity\@MappedSuperclass
        val entitySymbols =
            resolver.getSymbolsWithAnnotations("jakarta.persistence.Entity", "jakarta.persistence.MappedSuperclass")
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.validate() }

        if (entitySymbols.isEmpty()) {
            logger.info("No jakarta entity classes found")
            return emptyList()
        }

        entitySymbols.forEach { entityClass ->
            processEntity(entityClass)
        }

        // Return any symbols that couldn't be processed in this round
        return entitySymbols.filterNot { it.validate() }
    }

    fun processEntity(entityClass: KSClassDeclaration): String {
        val packageName = entityClass.packageName.asString()
        val className = entityClass.simpleName.asString()
        val qClassName = "Q$className"

        // Generate the metamodel class
        val fileSpec = codeGenerator?.createNewFile(
            dependencies = Dependencies(false, entityClass.containingFile!!),
            packageName = packageName,
            fileName = qClassName,
            extensionName = "java"
        )

        val content = generateMetamodelClass(packageName, className, qClassName, entityClass)
        fileSpec.use { outputStream ->
            // Generate the metamodel class content
            outputStream?.write(content.toByteArray())
        }

        logger.info("Generated Querydsl metamodel class: $packageName.$qClassName")
        return content
    }

    private fun generateMetamodelClass(
        packageName: String,
        className: String,
        qClassName: String,
        entityClass: KSClassDeclaration
    ): String {
        val ctx = ClassCtx(
            mutableListOf(
                Field("long", "serialVersionUID", "${className.hashCode()}L", true),
                Field("PathInits", "INITS", "PathInits.DIRECT2", true, false)
            )
        )

        getSupertype(entityClass)?.let {
            ctx.addField(it, "_super", "new $it(this)")
        }

        // Process properties
        entityClass.getAllProperties().forEach {
            processProperty(ctx, it, entityClass)
        }

        // Default static accessor
        getStaticAccessor(ctx, className).let {
            ctx.addField(qClassName, it, "new $qClassName(\"$it\")", true)
        }

        return """
package $packageName;

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

${ctx.fields.joinToString("\n\n") { "    $it" }}

${generateConstructors(ctx, className, qClassName)}

}
"""
    }

    class Field(
        val type: String,
        val name: String,
        val value: String,
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

    private fun getSupertype(entityClass: KSClassDeclaration): String? {
        val superClass = entityClass.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
        val superclassName = superClass?.qualifiedName?.asString() ?: ""
        return if (superClass != null && superclassName != "java.lang.Object" && superclassName != "kotlin.Any") {
            "Q${superClass.simpleName.asString()}"
        } else null
    }

    private fun getStaticAccessor(ctx: ClassCtx, className: String): String {
        val candidate = className.decapitalize()
        if (ctx.fieldNames.contains(candidate)) {
            var i = 1
            while (ctx.fieldNames.contains(candidate + i)) {
                i++
            }
            return candidate + i
        }
        return candidate
    }

    private fun processProperty(
        ctx: ClassCtx,
        property: KSPropertyDeclaration,
        entityClass: KSClassDeclaration,
    ) {
        val propertyName = property.simpleName.asString()
        val propertyType = property.type.resolve()
        val propertyTypeDeclaration = propertyType.declaration

        if (propertyName.startsWith("_") ||
            property.modifiers.contains(Modifier.JAVA_TRANSIENT) ||
            property.annotations.any { it.shortName.asString() == "Transient" } ||
            property.isInCompanionObject()
        ) {
            return
        }

        ctx.fieldNames.add(propertyName)

        val isInherited =
            property.parentDeclaration?.qualifiedName?.asString() != entityClass.qualifiedName!!.asString()

        when {
            propertyType.isCollectionType() -> {
                val elementType = propertyType.arguments.firstOrNull()?.type?.resolve()
                if (elementType != null) {
                    val elementTypeName = elementType.shortName()
                    val qElementTypeName = "Q$elementTypeName"
                    val collectionType = when {
                        propertyType.isListType() -> "List"
                        propertyType.isSetType() -> "Set"
                        else -> "Collection"
                    }
                    val value = if (isInherited) "_super.$propertyName;"
                    else "this.<$elementTypeName, $qElementTypeName>create$collectionType(\"$propertyName\", $elementTypeName.class, $qElementTypeName.class, PathInits.DIRECT2)"

                    ctx.addField(
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
                    else -> "${getCreateMethodForPathType(pathType)}(\"$propertyName\", ${getJavaClassName(propertyType)}.class)"
                }
                ctx.addField(pathType, propertyName, value)
            }

            // Entity references
            propertyTypeDeclaration is KSClassDeclaration && propertyTypeDeclaration.annotations.any { it.shortName.asString() == "Entity" } -> {
                val referencedEntityName = propertyTypeDeclaration.simpleName.asString()
                val qReferencedEntityName = "Q$referencedEntityName"
                val value = if (isInherited) " = _super.$propertyName;" else ""

                ctx.addField(qReferencedEntityName, propertyName, value)

                if (!isInherited) {
                    ctx.singularReferences.add(propertyName to propertyType)
                }
            }

            else -> {
                ctx.addField(
                    "SimplePath<${propertyTypeDeclaration.simpleName.asString()}>",
                    propertyName,
                    "createSimple(\"$propertyName\", ${propertyTypeDeclaration.simpleName.asString()}.class)"
                )
            }
        }
    }

    fun KSPropertyDeclaration.isInCompanionObject(): Boolean {
        return (parentDeclaration as? KSClassDeclaration)?.isCompanionObject == true
    }

    private fun KSType.isBasicType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName in basicTypeToPathType.keys || declaration.modifiers.contains(Modifier.ENUM)
    }

    private fun KSType.isCollectionType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName in collectionTypeMapping.keys
    }

    private fun KSType.isListType() = listTypeMapping.contains(name())

    private fun KSType.isSetType() = setTypeMapping.contains(name())

    private fun getPathTypeForBasicType(type: KSType): String {
        val typeName = type.name() ?: return "SimplePath<Object>"
        return basicTypeToPathType[typeName]
            ?: if (type.declaration.modifiers.contains(Modifier.ENUM))
                "EnumPath<${type.declaration.simpleName.asString()}>"
            else
                "SimplePath<${type.declaration.simpleName.asString()}>"
    }

    private fun getCreateMethodForPathType(pathType: String): String {
        return when {
            pathType.startsWith("StringPath") -> "createString"
            pathType.startsWith("NumberPath") -> "createNumber"
            pathType.startsWith("BooleanPath") -> "createBoolean"
            pathType.startsWith("DatePath") -> "createDate"
            pathType.startsWith("DateTimePath") -> "createDateTime"
            pathType.startsWith("EnumPath") -> "createEnum"
            else -> "createSimple"
        }
    }

    private fun getJavaClassName(type: KSType): String {
        val typeName = type.name() ?: return "Object"
        if (typeName == "kotlin.Int") {
            return "Integer"
        }
        return typeName.replace("kotlin.", "")
    }

    private fun KSType.name(): String? = declaration.qualifiedName?.asString()

    private fun KSType.shortName(): String = declaration.simpleName.asString()

    private fun generateConstructors(ctx: ClassCtx, className: String, qClassName: String): String {
        val referenceInit = ctx.singularReferences.joinToString("\n") {
            "this.${it.first} = inits.isInitialized(\"${it.first}\") ? new Q${it.second.declaration.simpleName.asString()}(forProperty(\"${it.first}\"), inits.get(\"${it.first}\")) : null;"
        }

        val constructors = """
    public $qClassName(String variable) {
        this($className.class, forVariable(variable), INITS);
    }

    public $qClassName(Path<? extends $className> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public $qClassName(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public $qClassName(PathMetadata metadata, PathInits inits) {
        this($className.class, metadata, inits);
    }

    public $qClassName(Class<? extends $className> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        $referenceInit
    }
        """
        return constructors
    }

    private fun String.decapitalize(): String {
        if (isEmpty() || !first().isUpperCase()) return this
        return first().lowercase() + substring(1)
    }

    class ClassCtx(
        val fields: MutableList<Field>,
        val fieldNames: MutableSet<String> = mutableSetOf(),
        val singularReferences: MutableSet<Pair<String, KSType>> = mutableSetOf()
    ) {
        fun addField(
            type: String,
            name: String,
            value: String,
            isStatic: Boolean = false,
            isPublic: Boolean = true
        ) {
            fields.add(Field(type, name, value, isStatic, isPublic))
        }
    }

}

/**
 * Provider for the Querydsl processor.
 */
class QuerydslProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return QuerydslProcessor(environment.logger, environment.codeGenerator)
    }
}
