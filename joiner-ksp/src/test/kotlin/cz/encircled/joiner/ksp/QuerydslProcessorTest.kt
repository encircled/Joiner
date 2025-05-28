@file:OptIn(ExperimentalCompilerApi::class)

package cz.encircled.joiner.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import cz.encircled.joiner.ksp.mock.MockKSClassDeclaration
import cz.encircled.joiner.model.User
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class QuerydslProcessorTest {

    @Test
    fun testEntityWithAllFieldTypes() {
        val actual = QuerydslProcessor(TestLogger()).processEntity(MockKSClassDeclaration(Customer::class) as KSClassDeclaration)
        assertTrue(actual.contains("NumberPath<Double> doubleValue = createNumber(\"doubleValue\", Double.class)"))
        assertTrue(actual.contains("BooleanPath booleanValue = createBoolean(\"booleanValue\", Boolean.class)"))
        assertTrue(actual.contains("DatePath<java.time.LocalDate> localDateValue = createDate(\"localDateValue\", java.time.LocalDate.class)"))
        assertTrue(actual.contains("NumberPath<Float> floatValue = createNumber(\"floatValue\", Float.class)"))
        assertTrue(actual.contains("NumberPath<Integer> intValue = createNumber(\"intValue\", Integer.class)"))
        assertTrue(actual.contains("StringPath stringValue = createString(\"stringValue\")"))
        assertTrue(actual.contains("DateTimePath<java.time.LocalDateTime> localDateTimeValue = createDateTime(\"localDateTimeValue\", java.time.LocalDateTime.class)"))
        assertTrue(actual.contains("NumberPath<java.math.BigDecimal> bigDecimalValue = createNumber(\"bigDecimalValue\", java.math.BigDecimal.class)"))
        assertTrue(actual.contains("NumberPath<Byte> byteValue = createNumber(\"byteValue\", Byte.class)"))
        assertTrue(actual.contains("NumberPath<Short> shortValue = createNumber(\"shortValue\", Short.class)"))
        assertTrue(actual.contains("EnumPath<TestEnum> enumValue = createEnum(\"enumValue\", cz.encircled.joiner.ksp.TestEnum.class)"))
    }

    @Test
    fun testEntityWithInheritance() {
        val sourceCode =
            File("../joiner-test-support/target/generated-sources/annotations/cz/encircled/joiner/model/QUser.java").readText()
        val actual = QuerydslProcessor(TestLogger()).processEntity(MockKSClassDeclaration(User::class) as KSClassDeclaration)

        sourceCode.lines().filter { !it.contains("@Generated") && !it.contains("serialVersionUID") && !it.trim().startsWith("//") }.forEach { line ->
            assertTrue(actual.contains(line.trim()), "$line not present!")
        }

        actual.lines().filter { !it.contains("@Generated") && !it.contains("serialVersionUID") && !it.trim().startsWith("//") }.forEach { line ->
            assertTrue(sourceCode.contains(line.trim()), "$line was not expected!")
        }
    }

    @Test
    fun `Q class should be generated for entity`() {
        val source = SourceFile.kotlin(
            "TestEntity.kt", """
            package test

            import jakarta.persistence.Entity
            import jakarta.persistence.Id
            import jakarta.persistence.OneToMany
            import jakarta.persistence.ManyToOne

            @Entity
            class TestEntity(
                @Id
                val id: Long,
                val name: String,

                @OneToMany(mappedBy = "parent")
                val children: List<TestEntity> = listOf(),

                @ManyToOne
                val parent: TestEntity? = null,

                @ManyToOne
                val testEntity: TestEntity? = null // self ref with the same name
            )
        """.trimIndent()
        )

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            symbolProcessorProviders = mutableListOf(QuerydslProcessorProvider())
            inheritClassPath = true
            messageOutputStream = System.out
            languageVersion = "1.9"
            kotlincArguments += "-Xuse-k2-kapt=false"
        }

        val result = compilation.compile()

        // Generated file path is build/generated/ksp.../test/QTestEntity.java
        val generatedFile = compilation.kspSourcesDir.walkTopDown().firstOrNull { it.name == "QTestEntity.java" }
            ?: error("QTestEntity.java was not generated")

        val content = generatedFile.readText()

        assertTrue("public class QTestEntity extends EntityPathBase<TestEntity> {" in content)
        assertTrue("public static final QTestEntity testEntity1 = new QTestEntity(\"testEntity1\");" in content)
        assertTrue("public final NumberPath<Long> id" in content)
        assertTrue("public final StringPath name" in content)
    }

    class TestLogger : KSPLogger {
        override fun error(message: String, symbol: KSNode?) {
            println(message)
        }

        override fun exception(e: Throwable) {
            println(e.message)
        }

        override fun info(message: String, symbol: KSNode?) {
            println(message)
        }

        override fun logging(message: String, symbol: KSNode?) {
            println(message)
        }

        override fun warn(message: String, symbol: KSNode?) {
            println(message)
        }
    }

}