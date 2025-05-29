package cz.encircled.joiner.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

/**
 * KSP processor for generating Querydsl metamodel classes from JPA entities.
 * This processor replicates the functionality of the Querydsl apt-maven-plugin.
 */
class QuerydslProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator? = null,
) : SymbolProcessor {

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
        val qClassName = "Q${entityClass.simpleName.asString()}"

        val fileSpec = codeGenerator?.createNewFile(
            dependencies = Dependencies(false, entityClass.containingFile!!),
            packageName = packageName,
            fileName = qClassName,
            extensionName = "java"
        )

        val content = JoinerClassProcessor(entityClass).generateMetamodelClass()
        fileSpec.use { it?.write(content.toByteArray()) }

        logger.info("Generated Querydsl metamodel class: $packageName.$qClassName")
        return content
    }

    private fun Resolver.getSymbolsWithAnnotations(vararg annotations: String) =
        annotations.flatMap { getSymbolsWithAnnotation(it) }

}

/**
 * Provider for the Querydsl processor.
 */
class QuerydslProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return QuerydslProcessor(environment.logger, environment.codeGenerator)
    }
}
