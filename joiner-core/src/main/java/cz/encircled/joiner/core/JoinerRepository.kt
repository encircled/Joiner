package cz.encircled.joiner.core

import com.mysema.query.types.Expression
import cz.encircled.joiner.query.Q

/**
 * @author Kisel on 11.01.2016.
 */
interface JoinerRepository<T> {

    fun find(request: Q<T>): List<T>

    fun <P> find(request: Q<T>, projection: Expression<P>): List<P>

    fun findOne(request: Q<T>): T

    fun <P> findOne(request: Q<T>, projection: Expression<P>): P

}
