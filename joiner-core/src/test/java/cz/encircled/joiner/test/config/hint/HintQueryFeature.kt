package cz.encircled.joiner.test.config.hint

import com.google.common.collect.Multimap
import com.mysema.query.jpa.impl.AbstractJPAQuery
import com.mysema.query.jpa.impl.JPAQuery
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.QueryFeature
import cz.encircled.joiner.test.core.TestException
import org.springframework.util.ReflectionUtils

/**
 * @author Kisel on 04.02.2016.
 */
class HintQueryFeature : QueryFeature {

    override fun <T> before(request: Q<T>): Q<T> {
        return request
    }

    override fun after(request: Q<*>, query: JPAQuery): JPAQuery {
        val f = ReflectionUtils.findField(AbstractJPAQuery<*>::class.java!!, "hints")
        ReflectionUtils.makeAccessible(f)
        val field = ReflectionUtils.getField(f, query) as Multimap<String, Any>

        val value = (field.get("testHint") as Collection<*>).iterator().next()
        if ("testHintValue" != value) {
            throw TestException()
        }

        return query
    }

}
