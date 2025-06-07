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
    fun testJavaEntityWithAllFieldTypes() {
        QuerydslProcessor(TestLogger()).processEntity(MockKSClassDeclaration(JavaCustomer::class)).apply {
            println(this)

            assertContains(
                "public final ArrayPath<String[], String> arrayOfStrings = createArray(\"arrayOfStrings\", String[].class);",
                "public final MapPath<String, Integer, SimplePath<Integer>> mapStrToInt = this.<String, Integer, SimplePath<Integer>>createMap(\"mapStrToInt\", String.class, Integer.class, SimplePath.class);",
                "NumberPath<Integer> intValue = createNumber(\"intValue\", Integer.class)",
            )
        }
    }

    @Test
    fun testEntityWithAllFieldTypes() {
        QuerydslProcessor(TestLogger()).processEntity(MockKSClassDeclaration(Customer::class)).apply {
            println(this)
            assertContains(
                "NumberPath<Double> doubleValue = createNumber(\"doubleValue\", Double.class)",
                "BooleanPath booleanValue = createBoolean(\"booleanValue\")",
                "DatePath<java.time.LocalDate> localDateValue = createDate(\"localDateValue\", java.time.LocalDate.class)",
                "NumberPath<Float> floatValue = createNumber(\"floatValue\", Float.class)",
                "NumberPath<Integer> intValue = createNumber(\"intValue\", Integer.class)",
                "StringPath stringValue = createString(\"stringValue\")",
                "DateTimePath<java.time.LocalDateTime> localDateTimeValue = createDateTime(\"localDateTimeValue\", java.time.LocalDateTime.class)",
                "NumberPath<java.math.BigDecimal> bigDecimalValue = createNumber(\"bigDecimalValue\", java.math.BigDecimal.class)",
                "NumberPath<Byte> byteValue = createNumber(\"byteValue\", Byte.class)",
                "NumberPath<Short> shortValue = createNumber(\"shortValue\", Short.class)",

                "EnumPath<TestEnum> enumValue = createEnum(\"enumValue\", TestEnum.class)",
                "ListPath<TestEnum, SimplePath<TestEnum>> listOfSimpleValues = this.<TestEnum, SimplePath<TestEnum>>createList(\"listOfSimpleValues\", TestEnum.class, SimplePath.class, PathInits.DIRECT2)",
                "ListPath<TestEnum, SimplePath<TestEnum>> mutableListOfSimpleValues = this.<TestEnum, SimplePath<TestEnum>>createList(\"mutableListOfSimpleValues\", TestEnum.class, SimplePath.class, PathInits.DIRECT2)",

                "public final ArrayPath<Byte[], Byte> byteArrayValue = createArray(\"byteArrayValue\", Byte[].class);",
                "public final ArrayPath<String[], String> stringArrayValue = createArray(\"stringArrayValue\", String[].class);",

                "public final MapPath<String, Integer, SimplePath<Integer>> mapStrToInt = this.<String, Integer, SimplePath<Integer>>createMap(\"mapStrToInt\", String.class, Integer.class, SimplePath.class);"
            )
        }


    }

    private fun String.assertContains(vararg expected: String) {
        expected.forEach {
            assertTrue(this.contains(it), "Generated class must contain:\n $it")
        }
    }

    // Compares the output with querydsl-apt
    @Test
    fun testEntityWithInheritance() {
        val sourceCode =
            File("../joiner-test-support/target/generated-sources/annotations/cz/encircled/joiner/model/QUser.java").readText()
        val actual =
            QuerydslProcessor(TestLogger()).processEntity(MockKSClassDeclaration(User::class) as KSClassDeclaration)

        sourceCode.lines()
            .filter { !it.contains("@Generated") && !it.contains("serialVersionUID") && !it.trim().startsWith("//") }
            .forEach { line ->
                assertTrue(actual.contains(line.trim()), "$line not present!")
            }

        actual.lines()
            .filter { !it.contains("@Generated") && !it.contains("serialVersionUID") && !it.trim().startsWith("//") }
            .forEach { line ->
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
        assertTrue("public final QTestEntity testEntity;" in content)
        assertTrue("this.parent = inits.isInitialized(\"parent\") ? new QTestEntity(forProperty(\"parent\"), inits.get(\"parent\")) : null;" in content)
        assertTrue("this.testEntity = inits.isInitialized(\"testEntity\") ? new QTestEntity(forProperty(\"testEntity\"), inits.get(\"testEntity\")) : null;" in content)
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