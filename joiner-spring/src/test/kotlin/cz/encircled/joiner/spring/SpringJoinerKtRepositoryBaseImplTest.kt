package cz.encircled.joiner.spring

import cz.encircled.joiner.AbstractSpringJoinerTest
import cz.encircled.joiner.kotlin.JoinerKt
import cz.encircled.joiner.model.QUser
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class SpringJoinerKtRepositoryBaseImplTest : AbstractSpringJoinerTest() {

    @Autowired
    lateinit var joinerKt: JoinerKt

    @Test
    fun test() {
        var repo : SpringJoinerKtRepositoryBaseImpl<*, *> = SpringJoinerKtRepositoryBaseImpl(joinerKt, QUser.user1)
    }

}