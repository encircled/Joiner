package cz.encircled.joiner.spring

import cz.encircled.joiner.AbstractSpringJoinerTest
import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.spring.config.JoinerKtJpaRepositoryFactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

open class SpringJoinerKtRepositoryTest : AbstractSpringJoinerTest() {

    @Autowired
    lateinit var userRepository: KtUserRepository

    @Test
    fun `find one`() {
        val user = userRepository.findOne {
            where { it.name eq "user1" }
        }
        assertNotNull(user)
        assertEquals("user1", user.name)
    }

    @Test
    fun `get one`() {
        assertEquals("user1", userRepository.getOne { where { it.name eq "user1" } }.name)
    }

    @Test
    fun `find many`() {
        val find = userRepository.find { }
        assertNotNull(find.find { it.name == "user1" })
        assertEquals(find.size.toLong(), userRepository.count {})
    }

    @Configuration
    @EnableJpaRepositories(repositoryFactoryBeanClass = JoinerKtJpaRepositoryFactoryBean::class)
    open class SpringTestConfig {
    }

}