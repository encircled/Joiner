package cz.encircled.joiner.ksp.mock

import com.google.devtools.ksp.symbol.AnnotationUseSiteTarget
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Origin

class MockKSAnnotation(val annotation: Annotation) : KSAnnotation {
    override val annotationType: KSTypeReference
        get() = TODO("Not yet implemented")
    override val arguments: List<KSValueArgument>
        get() = TODO("Not yet implemented")
    override val defaultArguments: List<KSValueArgument>
        get() = TODO("Not yet implemented")
    override val shortName: KSName
        get() = KSNameImpl(annotation.annotationClass.simpleName!!)
    override val useSiteTarget: AnnotationUseSiteTarget?
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
}