package cz.encircled.joiner.spring

import com.querydsl.core.JoinType
import com.querydsl.core.types.EntityPath
import cz.encircled.joiner.kotlin.JoinerKt
import cz.encircled.joiner.kotlin.JoinerKtQuery
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.mappingTo
import cz.encircled.joiner.query.JoinerQueryBase
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.query.join.JoinDescription
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

        val count = getTotalCount(q)
        val content: List<T> = joiner.find(q.copy().addFeatures(PageableFeature(pageable)))

        return PageImpl(content, pageable, count)
    }

    override fun <R : Any> findPage(
        mappingTo: KClass<R>,
        pageable: Pageable,
        query: JoinerKtQuery<T, R, E>.() -> Any
    ): Page<R> {
        val q = entityPath mappingTo mappingTo
        query.invoke(q)

        val count = getTotalCount(q)
        val content: List<R> = joiner.find(q.copy().addFeatures(PageableFeature(pageable)))

        return PageImpl(content, pageable, count)
    }

    override fun find(query: JoinerKtQuery<T, T, E>.() -> Any): List<T> {
        val q = entityPath.all()
        query.invoke(q)
        return joiner.find(q)
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