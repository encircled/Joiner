package cz.encircled.joiner.ksp.property

import com.google.devtools.ksp.symbol.KSType
import cz.encircled.joiner.ksp.JoinerTemplate

class PropertyInfo(
    val propertyType: KSType,
    val propertyName: String,
    val isInherited: Boolean,
    template: JoinerTemplate
) : JoinerTemplate by template