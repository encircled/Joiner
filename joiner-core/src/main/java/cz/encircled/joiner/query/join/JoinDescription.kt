package cz.encircled.joiner.query.join

import com.mysema.query.JoinType
import com.mysema.query.types.EntityPath
import com.mysema.query.types.Predicate
import com.mysema.query.types.path.CollectionPathBase
import cz.encircled.joiner.util.Assert
import java.util.*

/**
 * Represents query join.
 * For collection joins - [collectionPath][JoinDescription.collectionPath] is used, for single entity joins - [singlePath][JoinDescription.singlePath].
 *
 *
 * By default, all joins are **left fetch** joins
 *

 * @author Kisel on 21.01.2016.
 */
class JoinDescription internal constructor(var alias: EntityPath<*>) {

    var collectionPath: CollectionPathBase<*, *, *>? = null
        private set

    var singlePath: EntityPath<*>? = null
        private set

    var originalAlias: EntityPath<*>
        private set

    var joinType = JoinType.LEFTJOIN
        private set

    var isFetch = true
        private set

    var on: Predicate? = null
        private set

    var parent: JoinDescription? = null
        private set

    val children = ArrayList<JoinDescription>()
        get

    init {
        this.originalAlias = alias
    }

    fun fetch(fetch: Boolean): JoinDescription {
        this.isFetch = fetch
        return this
    }

    fun on(on: Predicate): JoinDescription {
        this.on = on
        return this
    }

    private fun joinType(joinType: JoinType): JoinDescription {
        this.joinType = joinType
        return this
    }

    fun alias(alias: EntityPath<*>): JoinDescription {
        this.alias = alias
        this.originalAlias = alias
        return this
    }

    fun singlePath(path: EntityPath<*>): JoinDescription {
        Assert.notNull(path)

        singlePath = path
        collectionPath = null
        return this
    }

    fun collectionPath(path: CollectionPathBase<*, *, *>): JoinDescription {
        Assert.notNull(path)

        collectionPath = path
        singlePath = null
        return this
    }

    val isCollectionPath: Boolean
        get() = collectionPath != null

    fun inner(): JoinDescription {
        return joinType(JoinType.INNERJOIN)
    }

    fun left(): JoinDescription {
        return joinType(JoinType.LEFTJOIN)
    }

    fun right(): JoinDescription {
        return joinType(JoinType.RIGHTJOIN)
    }

    fun nested(vararg joins: JoinDescription): JoinDescription {
        for (join in joins) {
            join.parent = this
            children.add(join)
        }
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JoinDescription) return false

        if (alias != other.alias) return false
        return if (parent != null) parent == other.parent else other.parent == null

    }

    override fun hashCode(): Int {
        var result = alias.hashCode()
        result = 31 * result + if (parent != null) parent!!.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "JoinDescription{" +
                "collectionPath=" + collectionPath +
                ", singlePath=" + singlePath +
                ", alias=" + alias +
                '}'
    }

}
