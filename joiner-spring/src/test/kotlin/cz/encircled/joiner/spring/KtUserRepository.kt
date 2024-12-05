package cz.encircled.joiner.spring

import cz.encircled.joiner.model.QUser
import cz.encircled.joiner.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface KtUserRepository : JpaRepository<User, Long>, SpringJoinerKtRepository<User, QUser> {
}