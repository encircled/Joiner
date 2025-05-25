package cz.encircled.joiner.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

/**
 * KSP processor for generating Querydsl metamodel classes from JPA entities.
 * This processor replicates the functionality of the Querydsl apt-maven-plugin.
 */
class QuerydslProcessor(
    private val codeGenerator: CodeGenerator? = null,
    private val logger: KSPLogger? = null,
    private val options: Map<String, String> = mapOf()
) : SymbolProcessor {

    fun Resolver.getSymbolsWithAnnotations(vararg annotations: String) =
        annotations.flatMap { getSymbolsWithAnnotation(it) }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger?.info("Starting Querydsl processor2")

        // Find all classes annotated with @Entity\@MappedSuperclass
        val entitySymbols =
            resolver.getSymbolsWithAnnotations("jakarta.persistence.Entity", "jakarta.persistence.MappedSuperclass")
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.validate() }

        if (entitySymbols.isEmpty()) {
            logger?.info("No jakarta entity classes found")
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

        logger?.info("Generated Querydsl metamodel class: $packageName.$qClassName")
        return content
    }

    private fun generateMetamodelClass(
        packageName: String,
        className: String,
        qClassName: String,
        entityClass: KSClassDeclaration
    ): String {
        val ctx = ClassCtx()
        ctx.append(
            """
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

            private static final long serialVersionUID = ${className.hashCode()}L;
            private static final PathInits INITS = PathInits.DIRECT2;

    """.trimIndent()
        )

        // Process superclass if any
        val superClass = entityClass.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
        if (superClass != null && superClass.qualifiedName?.asString() != "java.lang.Object" && superClass.qualifiedName?.asString() != "kotlin.Any") {
            val superClassName = superClass.simpleName.asString()
            val qSuperClassName = "Q$superClassName"
            ctx.append("    public final $qSuperClassName _super = new $qSuperClassName(this);\n")
        }

        // Process properties
        entityClass.getAllProperties().forEach { property ->
            processProperty(ctx, property, entityClass)
        }

        // Default static accessor
        val staticAccessor = getStaticAccessor(ctx, className)
        ctx.append("    public static final $qClassName $staticAccessor = new $qClassName(\"$staticAccessor\");")

        // Constructors
        generateConstructors(ctx, className, qClassName)

        // Close class
        ctx.append("}\n")

        return ctx.toString()
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

    private fun processProperty(ctx: ClassCtx, property: KSPropertyDeclaration, entityClass: KSClassDeclaration) {
        val propertyName = property.simpleName.asString()
        val propertyType = property.type.resolve()
        val propertyTypeDeclaration = propertyType.declaration

        logger?.info("Processing property $propertyName of type ${propertyType.declaration.qualifiedName?.asString()}")

        // Skip transient properties
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

        // Handle different property types
        when {
            // Collection types
            propertyType.isCollectionType() -> {
                logger?.info("Property $propertyName is a collection type")
                val elementType = propertyType.arguments.firstOrNull()?.type?.resolve()
                if (elementType != null) {
                    val elementTypeName = elementType.declaration.simpleName.asString()
                    val qElementTypeName = "Q$elementTypeName"
                    val collectionType = when {
                        propertyType.isListType() -> "List"
                        propertyType.isSetType() -> "Set"
                        else -> "Collection"
                    }
                    val value = if (isInherited) " = _super.$propertyName;"
                    else "this.<$elementTypeName, $qElementTypeName>create$collectionType(\"$propertyName\", $elementTypeName.class, $qElementTypeName.class, PathInits.DIRECT2)"

                    ctx.append("    public final ${collectionType}Path<$elementTypeName, $qElementTypeName> $propertyName = $value;\n")
                }
            }
            // Basic types
            propertyType.isBasicType() -> {
                logger?.info("Property $propertyName is a basic type: ${propertyType.declaration.simpleName.asString()} (${propertyType.declaration.qualifiedName?.asString()}) - adding as a simple path")
                val pathType = getPathTypeForBasicType(propertyType)
                val value = if (isInherited) "= _super.$propertyName;"
                else "createSimple(\"$propertyName\", ${propertyType.declaration.qualifiedName?.asString()}.class);"
                ctx.append("    public final $pathType $propertyName $value\n")
            }
            // Entity references
            propertyTypeDeclaration is KSClassDeclaration && propertyTypeDeclaration.annotations.any { it.shortName.asString() == "Entity" } -> {
                val referencedEntityName = propertyTypeDeclaration.simpleName.asString()
                val qReferencedEntityName = "Q$referencedEntityName"
                val value = if (isInherited) " = _super.$propertyName;" else ""

                ctx.append("    public final $qReferencedEntityName ${propertyName}${value};\n")

                if (!isInherited) {
                    ctx.singularReferences.add(propertyName to propertyType)
                }
            }
            // Other types
            else -> {
                // Handle as a simple path
                ctx.append("    public final SimplePath<${propertyTypeDeclaration.simpleName.asString()}> $propertyName = createSimple(\"$propertyName\", ${propertyTypeDeclaration.simpleName.asString()}.class);\n")
            }
        }
    }

    fun KSPropertyDeclaration.isInCompanionObject(): Boolean {
        return (parentDeclaration as? KSClassDeclaration)?.isCompanionObject == true
    }

    private fun generateConstructors(ctx: ClassCtx, className: String, qClassName: String) {
        val referenceInit = ctx.singularReferences.joinToString("\n") {
            "this.${it.first} = inits.isInitialized(\"${it.first}\") ? new Q${it.second.declaration.simpleName.asString()}(forProperty(\"${it.first}\"), inits.get(\"${it.first}\")) : null;"
        }
        ctx.append(
            """
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
        )
    }

    private fun KSType.isBasicType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName.startsWith("java.lang.") ||
                typeName.startsWith("kotlin.") ||
                typeName == "java.util.Date" ||
                typeName == "java.time.LocalDate" ||
                typeName == "java.time.LocalDateTime" ||
                typeName == "java.math.BigDecimal" ||
                typeName == "java.math.BigInteger" ||
                declaration.modifiers.contains(Modifier.ENUM)
    }

    private fun KSType.isCollectionType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName.startsWith("java.util.Collection") ||
                typeName.startsWith("java.util.List") ||
                typeName.startsWith("java.util.Set") ||
                typeName.startsWith("kotlin.collections") ||
                typeName == "kotlin.Array"
    }

    private fun KSType.isListType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName.startsWith("java.util.List") ||
                typeName == "kotlin.collections.List" ||
                typeName == "kotlin.collections.MutableList"
    }

    private fun KSType.isSetType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName.startsWith("java.util.Set") ||
                typeName == "kotlin.collections.Set" ||
                typeName == "kotlin.collections.MutableSet"
    }

    private fun getPathTypeForBasicType(type: KSType): String {
        val typeName = type.declaration.qualifiedName?.asString() ?: return "SimplePath<Object>"
        return when {
            typeName == "java.lang.String" || typeName == "kotlin.String" -> "StringPath"
            typeName == "java.lang.Integer" || typeName == "int" || typeName == "kotlin.Int" -> "NumberPath<Integer>"
            typeName == "java.lang.Long" || typeName == "long" || typeName == "kotlin.Long" -> "NumberPath<Long>"
            typeName == "java.lang.Boolean" || typeName == "boolean" || typeName == "kotlin.Boolean" -> "BooleanPath"
            typeName == "java.lang.Float" || typeName == "float" || typeName == "kotlin.Float" -> "NumberPath<Float>"
            typeName == "java.lang.Double" || typeName == "double" || typeName == "kotlin.Double" -> "NumberPath<Double>"
            typeName == "java.lang.Short" || typeName == "short" || typeName == "kotlin.Short" -> "NumberPath<Short>"
            typeName == "java.lang.Byte" || typeName == "byte" || typeName == "kotlin.Byte" -> "NumberPath<Byte>"
            typeName == "java.util.Date" -> "DatePath<java.util.Date>"
            typeName == "java.time.LocalDate" -> "DatePath<java.time.LocalDate>"
            typeName == "java.time.LocalDateTime" -> "DateTimePath<java.time.LocalDateTime>"
            typeName == "java.math.BigDecimal" -> "NumberPath<java.math.BigDecimal>"
            typeName == "java.math.BigInteger" -> "NumberPath<java.math.BigInteger>"
            type.declaration.modifiers.contains(Modifier.ENUM) -> "EnumPath<${type.declaration.simpleName.asString()}>"
            else -> "SimplePath<${type.declaration.simpleName.asString()}>"
        }
    }

    private fun String.decapitalize(): String {
        if (isEmpty() || !first().isUpperCase()) return this
        return first().lowercase() + substring(1)
    }

    class ClassCtx(
        val sb: java.lang.StringBuilder = StringBuilder(),
        val fieldNames: MutableSet<String> = mutableSetOf(),
        val singularReferences: MutableSet<Pair<String, KSType>> = mutableSetOf()
    ) {
        fun append(lines: String) {
            sb.append(lines + "\n")
        }

        override fun toString(): String = sb.toString()
    }

}

/**
 * Provider for the Querydsl processor.
 */
class QuerydslProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return QuerydslProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
    }
}
