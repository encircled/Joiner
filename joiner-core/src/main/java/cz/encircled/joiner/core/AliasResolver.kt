package cz.encircled.joiner.core

import com.mysema.query.types.EntityPath
import cz.encircled.joiner.query.join.JoinDescription

/**
 * @author Vlad on 16-Aug-16.
 */
interface AliasResolver {

    fun resolveJoinAlias(join: JoinDescription, root: EntityPath<*>)

}
