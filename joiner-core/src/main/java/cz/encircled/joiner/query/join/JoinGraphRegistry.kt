package cz.encircled.joiner.query.join

/**
 * JoinGraph allows to predefine a set of joins for a specific class that can be added to a query using [cz.encircled.joiner.query.Q.joinGraphs]

 * @author Vlad on 15-Aug-16.
 */
interface JoinGraphRegistry {

    fun registerJoinGraph(graphName: String, joins: Collection<JoinDescription>, vararg rootClasses: Class<*>)

    fun getJoinGraph(clazz: Class<*>, name: String): List<JoinDescription>

}
