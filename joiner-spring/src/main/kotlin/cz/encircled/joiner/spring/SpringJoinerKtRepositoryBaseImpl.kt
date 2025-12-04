package cz.encircled.joiner.spring

import com.querydsl.core.JoinType
import com.querydsl.core.Tuple
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.SimpleExpression
import cz.encircled.joiner.kotlin.JoinerKt
import cz.encircled.joiner.kotlin.JoinerKtOps.isIn
import cz.encircled.joiner.kotlin.JoinerKtQuery
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.mappingTo
import cz.encircled.joiner.query.JoinerQueryBase
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.query.join.JoinDescription
import cz.encircled.joiner.util.ReflectionUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.function.Consumer
import kotlin.reflect.KClass

class SpringJoinerKtRepositoryBaseImpl<T, E : EntityPath<T>>(val joiner: JoinerKt, val entityPath: E) :
    SpringJoinerKtRepository<T, E> {

    override fun count(query: JoinerKtQuery<T, Long, E>.() -> Any): Long {
        val q = entityPath.countOf()
        query.invoke(q)
        return joiner.getOne(q)
    }

    override fun findPage(pageable: Pageable, query: JoinerKtQuery<T, T, E>.() -> Any): Page<T> {
        val q = entityPath.all()
        query.invoke(q)

        val totalCount = getTotalCount(q)

        val (idField, ids) = findMatchingIds(q, pageable)

        val contentQuery = q.copy().where(idField.isIn(ids)).addFeatures(PageableFeature(pageable, true))
        val content = joiner.find(contentQuery)

        return PageImpl(content, pageable, totalCount)
    }

    override fun <R : Any> findPage(
        mappingTo: KClass<R>,
        pageable: Pageable,
        query: JoinerKtQuery<T, R, E>.() -> Any
    ): Page<R> {
        val q = entityPath mappingTo mappingTo
        query.invoke(q)

        val totalCount = getTotalCount(q)

        val (idField, ids) = findMatchingIds(q, pageable)

        val contentQuery = q.copy().where(idField.isIn(ids)).addFeatures(PageableFeature(pageable, true))
        val content = joiner.find(contentQuery)

        return PageImpl(content, pageable, totalCount)
    }

    override fun find(query: JoinerKtQuery<T, T, E>.() -> Any): List<T> {
        val q = entityPath.all()
        query.invoke(q)
        return joiner.find(q)
    }

    override fun findTuple(projection: List<Expression<*>>, query: JoinerKtQuery<T, Tuple, E>.() -> Any): List<Tuple> {
        val q = projection from entityPath
        query.invoke(q)
        return joiner.find(q)
    }

    override fun <EXP : Expression<EXP_R>, EXP_R>findTuple(projection: EXP, query: JoinerKtQuery<T, EXP_R, E>.() -> Any): List<EXP_R> {
        val q = projection from entityPath
        query.invoke(q)
        return joiner.find(q)
    }

    override fun findStream(query: JoinerKtQuery<T, T, E>.() -> Any): Sequence<T> {
        val q = entityPath.all()
        query.invoke(q)
        return joiner.findStream(q).iterator().asSequence()
    }

    override fun findOne(query: JoinerKtQuery<T, T, E>.() -> Any): T? {
        val q = entityPath.all()
        query.invoke(q)
        return joiner.findOne(q)
    }

    override fun getOne(query: JoinerKtQuery<T, T, E>.() -> Any): T {
        val q = entityPath.all()
        query.invoke(q)
        return joiner.getOne(q)
    }

    private fun findMatchingIds(
        query: JoinerKtQuery<*, *, *>,
        pageable: Pageable
    ): Pair<SimpleExpression<Any>, List<Any>> {
        val idField = ReflectionUtils.getField("id", query.from) as SimpleExpression<Any>
        val orderExpressions = listOf(idField) + PageableFeature.getExpressionsForSortParam(query, pageable.sort)

        val idsQuery = query.copy(orderExpressions.toTypedArray())
        val ids = joiner.find(idsQuery.addFeatures(PageableFeature(pageable))).map { it.get(idField)!! }
        return Pair(idField, ids)
    }

    private fun getTotalCount(request: JoinerKtQuery<T, *, E>): Long {
        val countRequest = request.copy() as JoinerQueryBase<T, Long>
        countRequest.count()

        // Left joins does not affect the count
        countRequest.joins
            .filter { it.joinType == JoinType.LEFTJOIN }
            .forEach { countRequest.removeJoin(it) }

        countRequest.joinGraphs.clear()

        // Fetch is not allowed for count queries
        J.unrollChildrenJoins(countRequest.joins).forEach(Consumer { j: JoinDescription -> j.fetch(false) })

        return joiner.getOne(countRequest)
    }

}