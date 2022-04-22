package cz.encircled.joiner.reactive

import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.model.QUser
import reactor.test.StepVerifier

abstract class AbstractReactorTest : WithInMemMySql() {

    protected fun assertUsersAreEmpty() {
        StepVerifier.create(reactorJoiner.find(QUser.user1.all()))
            .expectComplete()
            .verify()
    }

}