package cz.encircled.joiner.spring

import cz.encircled.joiner.AbstractSpringJoinerTest
import cz.encircled.joiner.kotlin.JoinerKtOps.contains
import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.model.QGroup
import cz.encircled.joiner.model.QStatus
import cz.encircled.joiner.model.QUser.user1
import cz.encircled.joiner.model.User
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.query.join.JoinGraphRegistry
import cz.encircled.joiner.spring.config.JoinerKtJpaRepositoryFactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

open class SpringJoinerKtRepositoryTest : AbstractSpringJoinerTest() {

    @Autowired
    lateinit var userRepository: KtUserRepository

    @Autowired
    lateinit var joinGraphRegistry: JoinGraphRegistry

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

    @Test
    fun `find tuple list`() {
        val find = userRepository.findTuple(listOf(user1.name)) { distinct(false) }
        assertNotNull(find.find { it.get(user1.name) == "user1" })
        assertEquals(find.size.toLong(), userRepository.count {})
    }

    @Test
    fun `find tuple`() {
        val find = userRepository.findTuple(user1.name) { distinct(false) }
        assertNotNull(find.find { it == "user1" })
        assertEquals(find.size.toLong(), userRepository.count {})
    }

    @Test
    fun `stream many`() {
        val find = userRepository.findStream { }.toList()
        assertNotNull(find.find { it.name == "user1" })
        assertEquals(find.size.toLong(), userRepository.count {})
    }

    @Test
    fun `find page`() {
        val graph = this::class.simpleName + "-find page"
        joinGraphRegistry.registerJoinGraph(graph, listOf(J.left(QStatus.status)), User::class.java)

        val page = userRepository.findPage(PageRequest.of(0, 2, Sort.by(Sort.Order.desc("name")))) {
            where { it.name contains "user" } leftJoin QGroup.group joinGraph graph
        }
        assertEquals(2, page.content.size)
        assertEquals("user3", page.content[0].name)
        assertEquals("user2", page.content[1].name)

        val dtoPage = userRepository.findPage(TestUserDto::class, PageRequest.of(0, 2)) {}
        assertEquals(listOf("user1", "user2"), dtoPage.content.map { it.name })
    }

    @Configuration
    @EnableJpaRepositories(repositoryFactoryBeanClass = JoinerKtJpaRepositoryFactoryBean::class)
    open class SpringTestConfig {
    }

    data class TestUserDto(
        val name: String,
    ) {
        constructor(user: User) : this(user.name)
    }

}