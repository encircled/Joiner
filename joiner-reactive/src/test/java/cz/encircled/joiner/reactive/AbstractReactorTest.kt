package cz.encircled.joiner.reactive

import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.model.QUser
import kotlin.test.assertTrue

abstract class AbstractReactorTest : WithInMemMySql() {

    protected fun assertUsersAreEmpty() {
        assertTrue(reactorJoiner.find(QUser.user1.all()).collectList().block()!!.isEmpty())
    }

}