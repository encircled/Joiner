package cz.encircled.joiner.query.join

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.util.Assert

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * ConcurrentHashMap-based implementation of [JoinGraphRegistry]

 * @author Vlad on 15-Aug-16.
 */
class DefaultJoinGraphRegistry : JoinGraphRegistry {

    private val registry = ConcurrentHashMap<Class<*>, MutableMap<String, List<JoinDescription>>>()

    override fun registerJoinGraph(graphName: String, joins: Collection<JoinDescription>, vararg rootClasses: Class<*>) {
        Assert.notNull(graphName)
        Assert.notNull(joins)
        Assert.notNull(rootClasses)
        Assert.assertThat(rootClasses.size > 0)

        for (clazz in rootClasses) {
            registry.computeIfAbsent(clazz) { c -> ConcurrentHashMap<String, List<JoinDescription>>() }
            val joinsOfClass = registry[clazz]
            if (joinsOfClass == null) {
                throw JoinerException(String.format("JoinGraph with name [%s] is already defined for the class [%s]", graphName, clazz.name))
            } else {
                joinsOfClass.put(graphName, ArrayList(joins))
            }
        }
    }

    override fun getJoinGraph(clazz: Class<*>, name: String): List<JoinDescription> {
        val joinsOfClass = registry[clazz]
        if (joinsOfClass != null) {
            val joins = joinsOfClass[name]
            if (joins != null) {
                return joins
            }
        }

        return emptyList()
    }

}
