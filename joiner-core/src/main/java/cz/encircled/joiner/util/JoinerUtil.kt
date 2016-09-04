package cz.encircled.joiner.util

import com.mysema.query.types.EntityPath
import cz.encircled.joiner.exception.JoinerException

/**
 * @author Kisel on 25.01.2016.
 */
object JoinerUtil {

    @SuppressWarnings("unchecked")
    fun <T : EntityPath<*>> instantiate(generatedClass: Class<out EntityPath<*>>, alias: String): T {
        Assert.notNull(alias)

        try {
            val constructor = generatedClass.getConstructor(String::class.java)
            return constructor.newInstance(alias) as T
        } catch (e: NoSuchMethodException) {
            throw JoinerException("EntityPath String constructor is missing on " + generatedClass)
        } catch (e: Exception) {
            throw JoinerException("Failed to create new instance of " + generatedClass, e)
        }

    }

    fun <T : EntityPath<*>> getAliasForChild(parent: EntityPath<*>, childPath: T): T {
        return JoinerUtil.instantiate<T>(childPath.javaClass, childPath.toString() + "_on_" + parent.toString())
    }

}
