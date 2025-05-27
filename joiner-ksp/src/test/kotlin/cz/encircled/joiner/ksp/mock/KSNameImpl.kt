package cz.encircled.joiner.ksp.mock

import com.google.devtools.ksp.symbol.KSName

class KSNameImpl(private val name: String) : KSName {
    override fun asString(): String = name
    override fun getQualifier(): String = name.substringBeforeLast('.', "")
    override fun getShortName(): String = name.substringAfterLast('.')
}