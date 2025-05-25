@file:OptIn(ExperimentalCompilerApi::class)

package cz.encircled.joiner.ksp

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
    fun test2() {
        val actual = QuerydslProcessor().processEntity(MockKSClassDeclaration(Customer::class) as KSClassDeclaration)
        println(actual)
    }

    @Test
    fun test() {
        val sourceCode =
            File("../joiner-test-support/target/generated-sources/annotations/cz/encircled/joiner/model/QUser.java").readText()
        val actual = QuerydslProcessor().processEntity(MockKSClassDeclaration(User::class) as KSClassDeclaration)
        println(actual)

        sourceCode.lines().filter { !it.contains("@Generated") && !it.contains("serialVersionUID") && !it.trim().startsWith("//") }.forEach { line ->
            assertTrue(actual.contains(line.trim()))
        }

        actual.lines().filter { !it.contains("@Generated") && !it.contains("serialVersionUID") && !it.trim().startsWith("//") }.forEach { line ->
            assertTrue(sourceCode.contains(line.trim()))
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
                val parent: TestEntity? = null
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

        assertTrue("public class QTestEntity" in content)
        assertTrue("public final NumberPath<Long> id" in content)
        assertTrue("public final StringPath name" in content)
    }
}
