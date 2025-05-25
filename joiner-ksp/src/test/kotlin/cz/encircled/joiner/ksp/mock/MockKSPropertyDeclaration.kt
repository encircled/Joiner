package cz.encircled.joiner.ksp.mock

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyGetter
import com.google.devtools.ksp.symbol.KSPropertySetter
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.NonExistLocation
import com.google.devtools.ksp.symbol.Origin
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

class MockKSPropertyDeclaration(private val delegate: KProperty1<out Any, *>) : KSPropertyDeclaration {
    override val simpleName: KSName = KSNameImpl(delegate.name)
    override val annotations: Sequence<KSAnnotation> = delegate.javaField!!.annotations.map { MockKSAnnotation(it) }.asSequence()
    override val modifiers: Set<Modifier> = emptySet()
    override val origin: Origin = Origin.KOTLIN
    override val location: Location = NonExistLocation
    override val parentDeclaration: KSDeclaration? = MockKSClassDeclaration(delegate.javaField!!.declaringClass.kotlin)
    override val containingFile: KSFile? = null
    override val type: KSTypeReference = object : KSTypeReference {
        override val annotations: Sequence<KSAnnotation> = emptySequence()
        override val element: KSReferenceElement? = null
        override val location: Location = NonExistLocation
        override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R = visitor.visitTypeReference(this, data)
        override fun resolve(): KSType {
            return MockKSType(delegate.name, delegate)
        }

        override val origin: Origin
            get() = TODO("Not yet implemented")
        override val parent: KSNode?
            get() = TODO("Not yet implemented")
        override val modifiers: Set<Modifier>
            get() = TODO("Not yet implemented")
    }

    override val extensionReceiver: KSTypeReference? = null
    override val isMutable: Boolean = false

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R = visitor.visitPropertyDeclaration(this, data)

    override fun asMemberOf(containing: KSType): KSType {
        TODO("Not yet implemented")
    }

    override fun findOverridee(): KSPropertyDeclaration? {
        TODO("Not yet implemented")
    }

    override fun isDelegated(): Boolean {
        TODO("Not yet implemented")
    }

    override val getter: KSPropertyGetter?
        get() = TODO("Not yet implemented")
    override val hasBackingField: Boolean
        get() = TODO("Not yet implemented")
    override val setter: KSPropertySetter?
        get() = TODO("Not yet implemented")
    override val docString: String?
        get() = TODO("Not yet implemented")
    override val packageName: KSName
        get() = TODO("Not yet implemented")
    override val qualifiedName: KSName?
        get() = TODO("Not yet implemented")
    override val typeParameters: List<KSTypeParameter>
        get() = TODO("Not yet implemented")
    override val parent: KSNode?
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