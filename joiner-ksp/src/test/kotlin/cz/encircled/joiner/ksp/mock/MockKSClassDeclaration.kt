package cz.encircled.joiner.ksp.mock

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.NonExistLocation
import com.google.devtools.ksp.symbol.Origin
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses

class MockKSClassDeclaration(kClass: KClass<*>) : KSClassDeclaration {
    override val simpleName: KSName = KSNameImpl(kClass.simpleName ?: "")
    override val qualifiedName: KSName? = KSNameImpl(kClass.qualifiedName ?: "")
    override val packageName: KSName = KSNameImpl(kClass.qualifiedName?.substringBeforeLast('.') ?: "")
    override val classKind: ClassKind = if (kClass.java.isInterface) ClassKind.INTERFACE else ClassKind.CLASS
    override val origin: Origin = Origin.KOTLIN
    override val location: Location = NonExistLocation
    override val modifiers: Set<Modifier> = if(kClass.java.isEnum) setOf(Modifier.ENUM) else setOf()
    override val annotations: Sequence<KSAnnotation> = kClass.java.annotations.asSequence().map { MockKSAnnotation(it) }
    override val containingFile: KSFile? = null
    override val parentDeclaration: KSDeclaration? = null
    override val typeParameters: List<KSTypeParameter> = emptyList()
    override val primaryConstructor: KSFunctionDeclaration? = null
    override val superTypes: Sequence<KSTypeReference> = kClass.superclasses.asSequence().map { MockKSClassReference(it) }
    override val declarations: Sequence<KSDeclaration> = kClass.memberProperties.asSequence().map { MockKSPropertyDeclaration(it) }

    override fun getAllProperties(): Sequence<KSPropertyDeclaration> = declarations.filterIsInstance<KSPropertyDeclaration>().asSequence()

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R = visitor.visitClassDeclaration(this, data)

    override fun asStarProjectedType(): KSType {
        TODO("Not yet implemented")
    }

    override fun asType(typeArguments: List<KSTypeArgument>): KSType {
        TODO("Not yet implemented")
    }

    override fun getAllFunctions(): Sequence<KSFunctionDeclaration> {
        TODO("Not yet implemented")
    }

    override fun getSealedSubclasses(): Sequence<KSClassDeclaration> {
        TODO("Not yet implemented")
    }

    override val isCompanionObject: Boolean
        get() = false
    override val docString: String?
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