package cz.encircled.joiner.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

/**
 * KSP processor for generating Querydsl metamodel classes from JPA entities.
 * This processor replicates the functionality of the Querydsl apt-maven-plugin.
 */
class QuerydslProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Starting Querydsl processor")
        // Find all classes annotated with @Entity
        val entitySymbols = resolver.getSymbolsWithAnnotation("jakarta.persistence.Entity")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .toList()

        if (entitySymbols.isEmpty()) {
            logger.info("No jakarta @Entity classes found")
            return emptyList()
        }

        // Process each entity
        entitySymbols.forEach { entityClass ->
            processEntity(entityClass)
        }

        // Return any symbols that couldn't be processed in this round
        return entitySymbols.filterNot { it.validate() }
    }

    private fun processEntity(entityClass: KSClassDeclaration) {
        val packageName = entityClass.packageName.asString()
        val className = entityClass.simpleName.asString()
        val qClassName = "Q$className"

        // Generate the metamodel class
        val fileSpec = codeGenerator.createNewFile(
            dependencies = Dependencies(false, entityClass.containingFile!!),
            packageName = packageName,
            fileName = qClassName,
            extensionName = "java"
        )

        fileSpec.use { outputStream ->
            // Generate the metamodel class content
            val content = generateMetamodelClass(packageName, className, qClassName, entityClass)
            outputStream.write(content.toByteArray())
        }

        logger.info("Generated Querydsl metamodel class: $packageName.$qClassName")
    }

    private fun generateMetamodelClass(
        packageName: String,
        className: String,
        qClassName: String,
        entityClass: KSClassDeclaration
    ): String {
        // Build the metamodel class content
        val sb = StringBuilder()

        // Package declaration
        sb.append("package $packageName;\n\n")

        // Imports
        sb.append("import static com.querydsl.core.types.PathMetadataFactory.*;\n\n")
        sb.append("import com.querydsl.core.types.dsl.*;\n\n")
        sb.append("import com.querydsl.core.types.PathMetadata;\n")
        sb.append("import javax.annotation.processing.Generated;\n")
        sb.append("import com.querydsl.core.types.Path;\n")
        sb.append("import com.querydsl.core.types.dsl.PathInits;\n\n")

        // Class declaration
        sb.append("/**\n")
        sb.append(" * $qClassName is a Querydsl query type for $className\n")
        sb.append(" */\n")
        sb.append("@Generated(\"cz.encircled.joiner.ksp.QuerydslProcessor\")\n")
        sb.append("public class $qClassName extends EntityPathBase<$className> {\n\n")

        // Serial version UID
        sb.append("    private static final long serialVersionUID = ${className.hashCode()}L;\n\n")

        // Path inits
        sb.append("    private static final PathInits INITS = PathInits.DIRECT2;\n\n")

        // Static instance
        sb.append("    public static final $qClassName ${className.decapitalize()} = new $qClassName(\"${className.decapitalize()}\");\n\n")

        // Process superclass if any
        val superClass = entityClass.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
        if (superClass != null && superClass.qualifiedName?.asString() != "java.lang.Object" && superClass.qualifiedName?.asString() != "kotlin.Any") {
            val superClassName = superClass.simpleName.asString()
            val qSuperClassName = "Q$superClassName"
            sb.append("    public final $qSuperClassName _super = new $qSuperClassName(this);\n\n")
        }

        // Process properties
        entityClass.getAllProperties().forEach { property ->
            processProperty(sb, property, entityClass)
        }

        // Constructors
        generateConstructors(sb, className, qClassName)

        // Close class
        sb.append("}\n")

        return sb.toString()
    }

    private fun processProperty(sb: StringBuilder, property: KSPropertyDeclaration, entityClass: KSClassDeclaration) {
        val propertyName = property.simpleName.asString()
        val propertyType = property.type.resolve()
        val propertyTypeDeclaration = propertyType.declaration

        // Skip transient properties
        if (property.annotations.any { it.shortName.asString() == "Transient" }) {
            return
        }

        // Handle different property types
        when {
            // Basic types
            propertyType.isBasicType() -> {
                val pathType = getPathTypeForBasicType(propertyType)
                sb.append("    public final $pathType $propertyName = createSimple(\"$propertyName\", ${propertyType.declaration.qualifiedName?.asString()}.class);\n\n")
            }
            // Collection types
            propertyType.isCollectionType() -> {
                val elementType = propertyType.arguments.firstOrNull()?.type?.resolve()
                if (elementType != null) {
                    val elementTypeName = elementType.declaration.simpleName.asString()
                    val qElementTypeName = "Q$elementTypeName"
                    val collectionType = when {
                        propertyType.isListType() -> "ListPath"
                        propertyType.isSetType() -> "SetPath"
                        else -> "CollectionPath"
                    }
                    sb.append("    public final $collectionType<$elementTypeName, $qElementTypeName> $propertyName = this.<$elementTypeName, $qElementTypeName>create$collectionType(\"$propertyName\", $elementTypeName.class, $qElementTypeName.class, PathInits.DIRECT2);\n\n")
                }
            }
            // Entity references
            propertyTypeDeclaration is KSClassDeclaration && propertyTypeDeclaration.annotations.any { it.shortName.asString() == "Entity" } -> {
                val referencedEntityName = propertyTypeDeclaration.simpleName.asString()
                val qReferencedEntityName = "Q$referencedEntityName"
                sb.append("    public final $qReferencedEntityName $propertyName;\n\n")
            }
            // Other types
            else -> {
                // Handle as simple path
                sb.append("    public final SimplePath<${propertyTypeDeclaration.simpleName.asString()}> $propertyName = createSimple(\"$propertyName\", ${propertyTypeDeclaration.simpleName.asString()}.class);\n\n")
            }
        }
    }

    private fun generateConstructors(sb: StringBuilder, className: String, qClassName: String) {
        // Variable constructor
        sb.append("    public $qClassName(String variable) {\n")
        sb.append("        this($className.class, forVariable(variable), INITS);\n")
        sb.append("    }\n\n")

        // Path constructor
        sb.append("    public $qClassName(Path<? extends $className> path) {\n")
        sb.append("        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));\n")
        sb.append("    }\n\n")

        // Metadata constructor
        sb.append("    public $qClassName(PathMetadata metadata) {\n")
        sb.append("        this(metadata, PathInits.getFor(metadata, INITS));\n")
        sb.append("    }\n\n")

        // Metadata with inits constructor
        sb.append("    public $qClassName(PathMetadata metadata, PathInits inits) {\n")
        sb.append("        this($className.class, metadata, inits);\n")
        sb.append("    }\n\n")

        // Full constructor
        sb.append("    public $qClassName(Class<? extends $className> type, PathMetadata metadata, PathInits inits) {\n")
        sb.append("        super(type, metadata, inits);\n")
        sb.append("        // Initialize entity references here\n")
        sb.append("    }\n\n")
    }

    private fun KSType.isBasicType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName.startsWith("java.lang.") ||
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
                typeName.startsWith("java.util.Set")
    }

    private fun KSType.isListType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName.startsWith("java.util.List")
    }

    private fun KSType.isSetType(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName.startsWith("java.util.Set")
    }

    private fun getPathTypeForBasicType(type: KSType): String {
        val typeName = type.declaration.qualifiedName?.asString() ?: return "SimplePath<Object>"
        return when {
            typeName == "java.lang.String" -> "StringPath"
            typeName == "java.lang.Integer" || typeName == "int" -> "NumberPath<Integer>"
            typeName == "java.lang.Long" || typeName == "long" -> "NumberPath<Long>"
            typeName == "java.lang.Boolean" || typeName == "boolean" -> "BooleanPath"
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