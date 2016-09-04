package cz.encircled.joiner.query

import com.mysema.query.jpa.impl.JPAQuery

/**
 * @author Vlad on 27-Jul-16.
 */
interface QueryFeature {

    fun <T> before(request: Q<T>): Q<T>

    fun after(request: Q<*>, query: JPAQuery): JPAQuery

}
