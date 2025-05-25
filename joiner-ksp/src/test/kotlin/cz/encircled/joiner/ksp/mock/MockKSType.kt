package cz.encircled.joiner.ksp.mock

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Nullability
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField


class MockKSType(
    val simpleName: String,
    val delegate: KProperty1<out Any, *>?,
    val type: Type = delegate!!.javaField!!.genericType
) : KSType {
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
    override val declaration: KSDeclaration
//    override val declaration: KSDeclaration = MockKSDeclaration(type, simpleName)
//        get() = MockKSDeclaration(type, simpleName)
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


    init {
        if (type is ParameterizedType) {
            val kClass = (type.rawType as Class<*>).kotlin
            declaration = MockKSClassDeclaration(kClass)
            arguments = type.actualTypeArguments.map { MockKSTypeArgument(it) }
        } else {
            declaration = MockKSClassDeclaration((type as Class<*>).kotlin)
            arguments = listOf()
        }
    }
}