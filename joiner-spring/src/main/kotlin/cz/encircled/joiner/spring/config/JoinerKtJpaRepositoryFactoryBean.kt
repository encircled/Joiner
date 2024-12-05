package cz.encircled.joiner.spring.config

import cz.encircled.joiner.kotlin.JoinerKt
import cz.encircled.joiner.spring.SpringJoinerKtRepositoryBaseImpl
import jakarta.persistence.EntityManager
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.querydsl.SimpleEntityPathResolver
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryComposition.RepositoryFragments
import org.springframework.data.repository.core.support.RepositoryFactorySupport

open class JoinerKtJpaRepositoryFactoryBean<T : Repository<S, ID>, S, ID>(
    repositoryInterface: Class<out T>,
) : JpaRepositoryFactoryBean<T, S, ID>(repositoryInterface), ApplicationContextAware {

    lateinit var ctx: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.ctx = applicationContext
    }

    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {
        return JoinerRepositoryFactory(ctx, entityManager)
    }

    class JoinerRepositoryFactory(
        val applicationContext: ApplicationContext,
        entityManager: EntityManager,
    ) : JpaRepositoryFactory(entityManager) {

        override fun getRepositoryFragments(metadata: RepositoryMetadata): RepositoryFragments {
            val fragmentImplementation: Any = getTargetRepositoryViaReflection(
                SpringJoinerKtRepositoryBaseImpl::class.java,
                applicationContext.getBean(JoinerKt::class.java),
                SimpleEntityPathResolver.INSTANCE.createPath(metadata.domainType),
            )

            return RepositoryFragments.just(fragmentImplementation).append(super.getRepositoryFragments(metadata))
        }
    }

}