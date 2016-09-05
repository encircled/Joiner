package cz.encircled.joiner.spring

import com.mysema.query.types.Expression
import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.core.JoinerRepository
import cz.encircled.joiner.query.Q
import org.springframework.beans.factory.annotation.Autowired

/**
 * Parent class for repositories with Joiner support within spring context.

 * @author Vlad on 14-Aug-16.
 */
abstract class SpringJoinerRepository<T> : JoinerRepository<T> {

    @Autowired
    protected var delegate: Joiner? = null

    override fun find(request: Q<T>): List<T> {
        return delegate!!.find(request)
    }

    override fun <P> find(request: Q<T>, projection: Expression<P>): List<P> {
        return delegate!!.find(request, projection)
    }

    override fun findOne(request: Q<T>): T? {
        return delegate!!.findOne(request)
    }

    override fun <P> findOne(request: Q<T>, projection: Expression<P>): P? {
        return delegate!!.findOne(request, projection)
    }

}