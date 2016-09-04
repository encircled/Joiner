package cz.encircled.joiner.query

import com.mysema.query.types.EntityPath
import com.mysema.query.types.Expression
import com.mysema.query.types.Predicate
import cz.encircled.joiner.query.join.JoinDescription
import cz.encircled.joiner.util.Assert

import java.util.*

/**
 * TODO create JoinerQuery class
 * This class is a transfer object for repository queries.
 *
 *
 * T - root entity type
 *

 * @author Kisel on 11.01.2016.
 * *
 * @see JoinDescription
 */
class Q<T> internal constructor(val from: EntityPath<T>) {

    var where: Predicate? = null
        private set

    val joins = LinkedHashSet<JoinDescription>()
        get

    val joinGraphs: MutableCollection<String> = ArrayList()

    var isDistinct = true
        private set

    var groupBy: Expression<*>? = null
        private set

    var having: Predicate? = null
        private set

    val hints = LinkedHashMap<String, MutableCollection<Any>>(2)

    val features: MutableCollection<QueryFeature> = ArrayList(2)

    fun distinct(isDistinct: Boolean): Q<T> {
        this.isDistinct = isDistinct
        return this
    }

    fun groupBy(groupBy: Expression<*>): Q<T> {
        this.groupBy = groupBy
        return this
    }

    fun where(where: Predicate): Q<T> {
        this.where = where
        return this
    }

    fun having(having: Predicate): Q<T> {
        this.having = having
        return this
    }

    fun joinGraphs(vararg names: String): Q<T> {
        Collections.addAll(joinGraphs, *names)

        return this
    }

    fun joins(vararg joins: JoinDescription): Q<T> {
        return joins(Arrays.asList(*joins))
    }

    fun joins(joins: Collection<JoinDescription>): Q<T> {
        Assert.notNull(joins)

        for (join in joins) {
            if (!this.joins.add(join)) {
                joins(join.children)
            }
        }

        return this
    }

    fun addHint(hint: String, value: Any): Q<T> {
        hints.computeIfAbsent(hint) { h -> ArrayList<Any>(2) }
        hints[hint]!!.add(value)

        return this
    }

    fun addFeatures(vararg features: QueryFeature): Q<T> {
        Collections.addAll(this.features, *features)
        return this
    }

    companion object {

        fun <T> from(from: EntityPath<T>): Q<T> {
            return Q(from)
        }
    }

}
