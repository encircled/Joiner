package cz.encircled.joiner.spring

import com.mysema.query.types.EntityPath
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
        setDefaultRootPath(request)
        return delegate!!.find(request)
    }

    override fun <P> find(request: Q<T>, projection: Expression<P>): List<P> {
        setDefaultRootPath(request)
        return delegate!!.find(request, projection)
    }

    override fun findOne(request: Q<T>): T {
        setDefaultRootPath(request)
        return delegate!!.findOne(request)
    }

    override fun <P> findOne(request: Q<T>, projection: Expression<P>): P {
        setDefaultRootPath(request)
        return delegate!!.findOne(request, projection)
    }

    private fun setDefaultRootPath(request: Q<T>?) {
        if (request != null && request.from == null) {
            request.rootEntityPath(rootEntityPath)
        }
    }

    /**
     * Implementations may override this method to specify default root path, which is used when `from` is not set in a request
     */
    protected val rootEntityPath: EntityPath<T>?
        get() = null

}