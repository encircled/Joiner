package cz.encircled.joiner.ksp.mock

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Origin
import java.lang.reflect.Type

class MockKSDeclaration(val type: Type, val name: String) : KSDeclaration {
    override val containingFile: KSFile?
        get() = TODO("Not yet implemented")
    override val docString: String?
        get() = TODO("Not yet implemented")
    override val packageName: KSName
        get() = TODO("Not yet implemented")
    override val parentDeclaration: KSDeclaration?
        get() = TODO("Not yet implemented")
    override val qualifiedName: KSName?
        get() = KSNameImpl(type.typeName)
    override val simpleName: KSName
        get() = KSNameImpl(name)
    override val typeParameters: List<KSTypeParameter>
        get() = TODO("Not yet implemented")
    override val modifiers: Set<Modifier>
        get() = setOf(Modifier.PUBLIC)

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        TODO("Not yet implemented")
    }

    override val location: Location
        get() = TODO("Not yet implemented")
    override val origin: Origin
        get() = TODO("Not yet implemented")
    override val parent: KSNode?
        get() = TODO("Not yet implemented")
    override val annotations: Sequence<KSAnnotation>
        get() = TODO("Not yet implemented")

    override fun findActuals(): Sequence<KSDeclaration> {
        TODO("Not yet implemented")
    }

    override fun findExpects(): Sequence<KSDeclaration> {
        TODO("Not yet implemented")
    }

    override val isActual: Boolean
        get() = TODO("Not yet implemented")
    override val isExpect: Boolean
        get() = TODO("Not yet implemented")
}