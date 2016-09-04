package cz.encircled.joiner.util

import java.lang.reflect.Field

/**
 * @author Vlad on 14-Aug-16.
 */
object ReflectionUtils {

    fun findField(clazz: Class<*>, name: String): Field? {
        Assert.notNull(clazz)
        Assert.notNull(name)

        try {
            return clazz.getDeclaredField(name)
        } catch (e: NoSuchFieldException) {
            return null
        }
    }

    fun setField(field: Field, targetObject: Any, value: Any) {
        Assert.notNull(field)

        makeAccessible(field)
        try {
            field.set(targetObject, value)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }

    }

    fun getField(field: Field, instance: Any): Any {
        Assert.notNull(field)
        makeAccessible(field)

        try {
            return field.get(instance)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }

    }

    fun makeAccessible(field: Field) {
        Assert.notNull(field)

        if (!field.isAccessible) {
            field.isAccessible = true
        }
    }

}
