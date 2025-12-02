package cz.encircled.joiner.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import cz.encircled.joiner.ksp.property.ArrayProcessor
import cz.encircled.joiner.ksp.property.BasicPropProcessor
import cz.encircled.joiner.ksp.property.CollectionProcessor
import cz.encircled.joiner.ksp.property.PropertyInfo

class JoinerClassProcessor(
    private val entityClass: KSClassDeclaration,
    private val classPackage: String = entityClass.packageName.asString(),
    template: JoinerTemplate = JavaJoinerTemplate(classPackage)
) : JoinerTemplate by template {

    private val outProperties: MutableList<OutProperty>
    private val propertyNames: MutableSet<String> = mutableSetOf()
    private val singularReferences: MutableSet<Pair<String, KSType>> = mutableSetOf()

    private val className = entityClass.simpleName()
    private val qClassName = "Q$className"

    init {
        outProperties = mutableListOf(
            OutProperty("long", "serialVersionUID", "${className.hashCode()}L", true),
            OutProperty("PathInits", "INITS", "PathInits.DIRECT2", true, false)
        )
    }

    fun generateMetamodelClass(): String {
        getSupertype(entityClass).let {
            if (it.isNotBlank()) outProperties.add(OutProperty(it, "_super", "new $it(this)"))
        }

        outProperties.addAll(
            entityClass.getAllProperties().map { processProperty(it) }.filterNotNull()
        )

        getStaticAccessor(className).let {
            outProperties.add(OutProperty(qClassName, it, "new $qClassName(\"$it\")", true))
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
                @Generated("cz.encircled.joiner.ksp.JoinerProcessor")
                public class $qClassName extends EntityPathBase<$className> {
                
                
                """.trimIndent()
        )
        sb.append(outProperties.joinToString("\n\n") { it.toString().prependIndent() })
        sb.append("\n")
        sb.append(generateConstructors(className).joinToString("") { it.stringify(qClassName) })
        sb.append("\n}")

        return sb.toString()
    }

    private fun getSupertype(entityClass: KSClassDeclaration): String {
        val parent = entityClass.superTypes
            .map { it.resolve().declaration }
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull { it.classKind == ClassKind.CLASS && it.simpleName() != "Any" && it.simpleName() != "Object" }

        return classReference(parent, "Q")
    }

    private fun getStaticAccessor(className: String): String {
        val base = className.decapitalize()
        if (base !in propertyNames) return base

        return generateSequence(1) { it + 1 }
            .map { "$base$it" }
            .first { it !in propertyNames }
    }

    private fun processProperty(prop: KSPropertyDeclaration): OutProperty? {
        val propName = prop.simpleName()
        val propType = prop.type.resolve()
        val typeDeclaration = propType.declaration

        if (propName.startsWith("_") ||
            prop.modifiers.contains(Modifier.JAVA_TRANSIENT) ||
            prop.hasAnnotation("Transient") ||
            prop.isInCompanionObject()
        ) {
            return null
        }

        propertyNames.add(propName)

        val isInherited = prop.parentDeclaration?.name() != entityClass.name()
        val info = PropertyInfo(propType, propName, isInherited, this)

        return when {
            propType.isArrayType() -> ArrayProcessor.process(info)
            propType.isCollectionType() -> CollectionProcessor.process(info)
            propType.isBasicType() -> BasicPropProcessor.process(info)

            typeDeclaration is KSClassDeclaration && (typeDeclaration.hasAnnotation("Entity") || typeDeclaration.hasAnnotation("Embeddable")) -> {
                processEntityReference(info, typeDeclaration)
            }

            else -> OutProperty(
                "SimplePath<${classReference(typeDeclaration)}>",
                propName,
                "createSimple(\"$propName\", ${classReference(typeDeclaration)}.class)"
            )
        }
    }

    private fun processEntityReference(
        info: PropertyInfo,
        typeDeclaration: KSDeclaration,
    ): OutProperty {
        val propertyName = info.propertyName
        val qReferencedEntityName = "Q${typeDeclaration.simpleName()}"
        val value = if (info.isInherited) "_super.$propertyName" else ""

        val qualifiedName =
            if (entityClass.name() == typeDeclaration.name()) qReferencedEntityName
            else "${typeDeclaration.packageName.asString()}.$qReferencedEntityName"

        if (!info.isInherited) {
            singularReferences.add(propertyName to info.propertyType)
        }

        return OutProperty(qualifiedName, propertyName, value)
    }

    // TODO template
    private fun generateConstructors(className: String): List<OutConstructor> {
        val referenceInit = singularReferences.map {
            val ifInitialized =
                "new ${classReference(it.second, "Q")}(forProperty(\"${it.first}\"), inits.get(\"${it.first}\"))"

            "this.${it.first} = inits.isInitialized(\"${it.first}\") ? $ifInitialized : null".prependIndent()
        }

        return listOf(
            OutConstructor(listOf("this($className.class, forVariable(variable), INITS)"), "String" to "variable"),

            OutConstructor(
                listOf("this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS))"),
                "Path<? extends $className>" to "path"
            ),

            OutConstructor(listOf("this(metadata, PathInits.getFor(metadata, INITS))"), "PathMetadata" to "metadata"),

            OutConstructor(
                listOf("this($className.class, metadata, inits)"),
                "PathMetadata" to "metadata",
                "PathInits" to "inits"
            ),

            OutConstructor(
                listOf("super(type, metadata, inits)") + referenceInit,
                "Class<? extends $className>" to "type",
                "PathMetadata" to "metadata",
                "PathInits" to "inits"
            ),
        )
    }

}