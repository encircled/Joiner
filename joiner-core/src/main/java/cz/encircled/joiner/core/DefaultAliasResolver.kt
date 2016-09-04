package cz.encircled.joiner.core

import com.mysema.query.types.EntityPath
import com.mysema.query.types.Path
import com.mysema.query.types.path.BooleanPath
import com.mysema.query.types.path.CollectionPathBase
import com.mysema.query.types.path.EntityPathBase
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.join.JoinDescription
import cz.encircled.joiner.util.JoinerUtil
import cz.encircled.joiner.util.ReflectionUtils
import cz.encircled.joiner.util.ReflectionUtils.getField
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Vlad on 16-Aug-16.
 */
class DefaultAliasResolver : AliasResolver {

    private val aliasCache = ConcurrentHashMap<String, Path<*>>()

    @SuppressWarnings("unchecked")
    override fun resolveJoinAlias(join: JoinDescription, root: EntityPath<*>) {
        val parent = join.parent?.alias ?: root
        val targetType = join.alias.type

        val fieldOnParent = findPathOnParent(parent, targetType, join)
        if (fieldOnParent is CollectionPathBase<*, *, *>) {
            join.collectionPath(fieldOnParent)
        } else if (fieldOnParent is EntityPath<*>) {
            join.singlePath(fieldOnParent)
        }
        if (join.parent != null) {
            join.alias(JoinerUtil.getAliasForChild(join.parent!!.alias, join.alias))
        }
    }

    private fun findPathOnParent(parent: Any, targetType: Class<*>, joinDescription: JoinDescription): Path<*>? {
        var currentTargetType = targetType
        while (currentTargetType != Any::class.java) {
            val cacheKey = parent.javaClass.name + currentTargetType.simpleName + joinDescription.alias.toString()
            val cached = aliasCache[cacheKey]
            if (cached != null && cached != nullPath) {
                // TODO test
                // TODO optimize inheritance cases
                return cached
            }

            val candidatePaths = ArrayList<Path<*>>()

            for (field in parent.javaClass.fields) {
                val candidate = getField(field, parent)

                if (candidate is CollectionPathBase<*, *, *>) {
                    val elementTypeField = ReflectionUtils.findField(candidate.javaClass, "elementType")
                    val elementType = getField(elementTypeField!!, candidate) as Class<*>

                    if (elementType == currentTargetType) {
                        candidatePaths.add(candidate as Path<*>)
                    }
                } else if (candidate is EntityPathBase<*>) {
                    val type = candidate.type
                    if (type == currentTargetType) {
                        candidatePaths.add(candidate as Path<*>)
                    }
                }
            }

            if (candidatePaths.isEmpty()) {
                // TODO may be exception?
                joinDescription.fetch(false)
                for (child in joinDescription.children) {
                    child.fetch(false)
                }
                aliasCache.put(cacheKey, nullPath)
                currentTargetType = currentTargetType.superclass
            } else if (candidatePaths.size == 1) {
                aliasCache.put(cacheKey, candidatePaths[0])
                return candidatePaths[0]
            } else {
                // Multiple associations on parent, try find by specified alias
                val targetFieldName = joinDescription.originalAlias.toString()
                for (candidatePath in candidatePaths) {
                    if (targetFieldName == candidatePath.metadata.element) {
                        aliasCache.put(cacheKey, candidatePath)
                        return candidatePath
                    }
                }
                throw JoinerException("Join with ambiguous alias : $joinDescription. Multiple mappings found")
            }
        }

        return null
    }

    companion object {

        private val nullPath = BooleanPath("")
    }

}
