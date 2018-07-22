package cz.encircled.joiner.kotlin

import com.querydsl.core.JoinType
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Predicate
import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.query.join.JoinDescription

//class KtJoinerQuery<R, T : EntityPath<R>>(private val entityPath: T) {
class KtJoinerQuery<F, R, T : EntityPath<F>>(private val p: EntityPath<R>) {

    lateinit var entityPath: T

    val delegate: JoinerQuery<R, R> = Q.from(p)

    fun from(from: () -> EntityPath<R>) {
        delegate
    }

    private fun checkState() {
        if(entityPath == null) throw IllegalStateException("From not initialized")
    }

    fun where(where: (e: T) -> Predicate) {
        delegate.where(where.invoke(entityPath!!))
    }

    fun asc(asc: (e: T) -> Expression<*>) {
        checkState()

        delegate.asc(asc.invoke(entityPath!!))
    }

    fun desc(desc: (e: T) -> Expression<*>) {
        delegate.desc(desc.invoke(entityPath))
    }

    fun leftJoin(path: EntityPath<*>, init: KtJoinerJoin.() -> Unit): KtJoinerJoin =
            join(path, true, init)

    fun leftJoin(vararg paths: EntityPath<*>) {
        paths.forEach { delegate.joins(J.left(it)) }
    }

    fun innerJoin(vararg paths: EntityPath<*>) {
        paths.forEach { delegate.joins(J.inner(it)) }
    }

    fun innerJoin(path: EntityPath<*>, init: KtJoinerJoin.() -> Unit): KtJoinerJoin =
            join(path, false, init)

    private fun join(path: EntityPath<*>, isLeft: Boolean, init: KtJoinerJoin.() -> Unit): KtJoinerJoin {
        val builder = KtJoinerJoin(path)
        builder.init()

        val join = if (isLeft) J.left(path) else J.inner(path)
        addNestedJoins(builder, join)

        delegate.joins(join)

        return builder
    }

    private fun addNestedJoins(builder: KtJoinerJoin, parentJoin: JoinDescription) {
        val children = mutableListOf<Pair<KtJoinerJoin, JoinDescription>>()
        builder.children.forEach {
            val nested = J.left(it.second.entityPath)
            parentJoin.nested(nested)
            children.add(Pair(it.second, nested))
        }
        children.forEach {
            addNestedJoins(it.first, it.second)
        }
    }

}

data class KtJoinerJoin(var entityPath: EntityPath<*>) {

    val children: MutableList<Pair<JoinType, KtJoinerJoin>> = mutableListOf()

    operator fun EntityPath<*>.unaryPlus() {
        entityPath = this
    }

    fun leftJoin(path: EntityPath<*>, init: KtJoinerJoin.() -> Unit): KtJoinerJoin =
            join(path, true, init)

    fun leftJoin(vararg paths: EntityPath<*>) {
        paths.forEach { children.add(Pair(JoinType.LEFTJOIN, KtJoinerJoin(it))) }
    }

    fun innerJoin(vararg paths: EntityPath<*>) {
        paths.forEach { children.add(Pair(JoinType.INNERJOIN, KtJoinerJoin(it))) }
    }

    fun innerJoin(path: EntityPath<*>, init: KtJoinerJoin.() -> Unit): KtJoinerJoin =
            join(path, false, init)

    private fun join(path: EntityPath<*>, isLeft: Boolean, init: KtJoinerJoin.() -> Unit): KtJoinerJoin {
        val j = KtJoinerJoin(path)
        j.init()
        val joinType = if (isLeft) JoinType.LEFTJOIN else JoinType.RIGHTJOIN
        children.add(Pair(joinType, j))
        return j
    }

}

/**
 * @author Vlad on 05-Jun-18.
 */
object QueryBuilder {

    fun <R, T : EntityPath<R>> select(from: T, init: KtJoinerQuery<R, R, T>.() -> Unit): JoinerQuery<R, R> {
        val query = KtJoinerQuery<R, R, T>(from)
        query.init()

        return query.delegate
    }


}