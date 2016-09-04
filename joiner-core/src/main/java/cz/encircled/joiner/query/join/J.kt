package cz.encircled.joiner.query.join

import com.mysema.query.types.EntityPath
import cz.encircled.joiner.util.JoinerUtil

/**
 * Util class, which helps to build new [joins][JoinDescription]

 * @author Kisel on 26.01.2016.
 */
object J {

    @SuppressWarnings("unchcecked")
    fun <T : EntityPath<*>> path(parent: EntityPath<*>?, path: T): T {
        // TODO check
        if (parent != null) {
            return JoinerUtil.getAliasForChild(parent, path)
        }
        return path
    }

    fun left(path: EntityPath<*>): JoinDescription {
        return getBasicJoin(path).left()
    }

    fun inner(path: EntityPath<*>): JoinDescription {
        return getBasicJoin(path).inner()
    }

    private fun getBasicJoin(path: EntityPath<*>): JoinDescription {
        return JoinDescription(path)
    }

}
