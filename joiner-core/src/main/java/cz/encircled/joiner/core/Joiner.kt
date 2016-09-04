package cz.encircled.joiner.core

import com.google.common.collect.ArrayListMultimap
import com.mysema.query.JoinType
import com.mysema.query.jpa.impl.AbstractJPAQuery
import com.mysema.query.jpa.impl.JPAQuery
import com.mysema.query.types.EntityPath
import com.mysema.query.types.Expression
import com.mysema.query.types.Operation
import com.mysema.query.types.Path
import cz.encircled.joiner.core.vendor.EclipselinkRepository
import cz.encircled.joiner.core.vendor.HibernateRepository
import cz.encircled.joiner.core.vendor.JoinerVendorRepository
import cz.encircled.joiner.exception.AliasMissingException
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.QueryFeature
import cz.encircled.joiner.query.join.JoinDescription
import cz.encircled.joiner.query.join.JoinGraphRegistry
import cz.encircled.joiner.util.Assert
import cz.encircled.joiner.util.ReflectionUtils
import java.util.*
import javax.persistence.EntityManager

/**
 * @author Kisel on 26.01.2016.
 */
class Joiner(private val entityManager: EntityManager) {

    private var joinerVendorRepository: JoinerVendorRepository? = null

    private var joinGraphRegistry: JoinGraphRegistry? = null

    private val aliasResolver = DefaultAliasResolver()

    init {
        val implName = entityManager.delegate.javaClass.name
        if (implName.startsWith("org.hibernate")) {
            this.joinerVendorRepository = HibernateRepository()
        } else if (implName.startsWith("org.eclipse")) {
            this.joinerVendorRepository = EclipselinkRepository()
        }
    }

    fun <T> findOne(request: Q<T>): T? {
        return findOne(request, request.from)
    }

    fun <T, P> findOne(request: Q<T>, projection: Expression<P>): P? {
        val list = find(request, projection)
        if (list.isEmpty()) {
            return null
        } else if (list.size == 1) {
            return list[0]
        } else {
            throw JoinerException("FindOne returned multiple records!")
        }
    }

    fun <T> find(request: Q<T>): List<T> {
        Assert.notNull(request)
        return find(request, request.from)
    }

    fun <T, P> find(request: Q<T>, projection: Expression<P>): List<P> {
        var request = request
        Assert.notNull(request)
        Assert.notNull(projection)
        // TODO extract validation
        Assert.notNull(request.from)

        setJoinsFromJoinsGraphs(request)

        for (feature in request.features) {
            request = doPreProcess(request, feature)
        }

        var query = joinerVendorRepository!!.createQuery(entityManager)
        makeInsertionOrderHints(query)

        query.from(request.from)
        if (request.isDistinct) {
            query.distinct()
        }

        val usedAliases = HashSet<Path<*>>()
        usedAliases.add(request.from)

        val joins = unrollChildren(request.joins)
        for (join in joins) {
            aliasResolver.resolveJoinAlias(join, request.from)
            usedAliases.add(join.alias)
        }
        addJoins(joins, query, request.from, request.from == projection)

        addHints(request, query)

        checkAliasesArePresent(request.where, usedAliases)
        checkAliasesArePresent(request.having, usedAliases)
        checkAliasesArePresent(request.groupBy, usedAliases)

        query.where(request.where)

        if (request.groupBy != null) {
            query.groupBy(request.groupBy)
        }

        if (request.having != null) {
            query.having(request.having)
        }

        for (feature in request.features) {
            query = doPostProcess(request, query, feature)
        }

        return query.list(projection)
    }

    private fun <T> setJoinsFromJoinsGraphs(request: Q<T>) {
        if (!request.joinGraphs.isEmpty()) {
            if (joinGraphRegistry == null) {
                throw JoinerException("Join graph are set, but joinGraphRegistry is null!")
            }

            val queryRootClass = request.from.type

            for (name in request.joinGraphs) {
                val joins = joinGraphRegistry!!.getJoinGraph(queryRootClass, name)
                if (joins.isEmpty()) {
                    throw JoinerException(String.format("JoinGraph with name [%s] is not defined for class [%s]", name, queryRootClass))
                } else {
                    request.joins(joins)
                }
            }
        }
    }

    private fun doPostProcess(request: Q<*>, query: JPAQuery, feature: QueryFeature): JPAQuery {
        return feature.after(request, query)
    }

    private fun <T> doPreProcess(request: Q<T>, feature: QueryFeature): Q<T> {
        return feature.before(request)
    }

    private fun makeInsertionOrderHints(sourceQuery: AbstractJPAQuery<JPAQuery>) {
        val f = ReflectionUtils.findField(AbstractJPAQuery::class.java, "hints")
        ReflectionUtils.setField(f!!, sourceQuery, ArrayListMultimap.create<Any, Any>())
    }

    private fun addJoins(joins: List<JoinDescription>, query: JPAQuery, rootPath: EntityPath<*>, doFetch: Boolean) {
        for (join in joins) {
            joinerVendorRepository!!.addJoin(query, join)
            if (doFetch && join.isFetch) {
                if (join.joinType == JoinType.RIGHTJOIN) {
                    throw JoinerException("Fetch is not supported for right join!")
                }
                joinerVendorRepository!!.addFetch(query, join, joins, rootPath)
            }
        }
    }

    private fun unrollChildren(joins: Set<JoinDescription>): List<JoinDescription> {
        val collection = LinkedList<JoinDescription>()

        for (joinDescription in joins) {
            unrollChildrenInternal(joinDescription, collection)
        }

        return collection
    }

    private fun unrollChildrenInternal(join: JoinDescription, collection: MutableList<JoinDescription>) {
        collection.add(join)
        for (child in join.children) {
            unrollChildrenInternal(child, collection)
        }
    }

    private fun addHints(request: Q<*>, query: JPAQuery) {
        for ((key, hintValues) in request.hints) {
            for (value in hintValues) {
                query.setHint(key, value)
            }
        }
    }

    private fun checkAliasesArePresent(expression: Expression<*>?, usedAliases: Set<Path<*>>) {
        for (path in collectPredicatePaths(expression)) {
            val predicatePath = path.root
            if (!predicatePath.toString().startsWith("any(")) {
                if (!usedAliases.contains(predicatePath)) {
                    throw AliasMissingException("Alias $predicatePath is not present in joins!")
                }
            }
        }
    }

    private fun collectPredicatePaths(expression: Expression<*>?): List<Path<*>> {
        val result = ArrayList<Path<*>>()
        if (expression != null) {
            collectPredicatePathsInternal(expression, result)
        }
        return result
    }

    private fun collectPredicatePathsInternal(expression: Expression<*>, paths: MutableList<Path<*>>) {
        if (expression is Path<*>) {
            paths.add(expression)
        } else if (expression is Operation<*>) {
            for (exp in expression.args) {
                collectPredicatePathsInternal(exp, paths)
            }
        }
    }

    fun setJoinGraphRegistry(joinGraphRegistry: JoinGraphRegistry) {
        this.joinGraphRegistry = joinGraphRegistry
    }

}
