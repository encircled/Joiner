package cz.encircled.joiner.ksp.mock

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Origin
import kotlin.reflect.KClass

class MockKSClassReference(val klass: KClass<*>) : KSTypeReference {
    override fun resolve(): KSType {
        return MockKSTypeForClass(klass)
    }

    override val element: KSReferenceElement?
        get() = TODO("Not yet implemented")
    override val annotations: Sequence<KSAnnotation>
        get() = TODO("Not yet implemented")

    override fun <D, R> accept(visitor: KSVisitor<D, R>, data: D): R {
        TODO("Not yet implemented")
    }

    override val location: Location
        get() = TODO("Not yet implemented")
    override val origin: Origin
        get() = TODO("Not yet implemented")
    override val parent: KSNode?
        get() = TODO("Not yet implemented")
    override val modifiers: Set<Modifier>
        get() = TODO("Not yet implemented")

    private class MockKSTypeForClass(val klass: KClass<*>) : KSType {
        override fun isAssignableFrom(that: KSType): Boolean {
            TODO("Not yet implemented")
        }

        override fun isCovarianceFlexible(): Boolean {
            TODO("Not yet implemented")
        }

        override fun isMutabilityFlexible(): Boolean {
            TODO("Not yet implemented")
        }

        override fun makeNotNullable(): KSType {
            TODO("Not yet implemented")
        }

        override fun makeNullable(): KSType {
            TODO("Not yet implemented")
        }

        override fun replace(arguments: List<KSTypeArgument>): KSType {
            TODO("Not yet implemented")
        }

        override fun starProjection(): KSType {
            TODO("Not yet implemented")
        }

        override val annotations: Sequence<KSAnnotation>
            get() = TODO("Not yet implemented")
        override val arguments: List<KSTypeArgument>
            get() = TODO("Not yet implemented")
        override val declaration: KSDeclaration
            get() = MockKSClassDeclaration(klass)
        override val isError: Boolean
            get() = TODO("Not yet implemented")
        override val isFunctionType: Boolean
            get() = TODO("Not yet implemented")
        override val isMarkedNullable: Boolean
            get() = TODO("Not yet implemented")
        override val isSuspendFunctionType: Boolean
            get() = TODO("Not yet implemented")
        override val nullability: Nullability
            get() = TODO("Not yet implemented")
    }

}
