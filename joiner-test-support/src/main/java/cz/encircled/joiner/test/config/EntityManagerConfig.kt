package cz.encircled.joiner.test.config

import org.eclipse.persistence.config.PersistenceUnitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@EnableTransactionManagement
class EntityManagerConfig {

    @Bean
    fun dataSource(): DataSource {
        val builder = EmbeddedDatabaseBuilder()
        return builder.setType(EmbeddedDatabaseType.H2).build()
    }

    @Bean
    fun entityManagerFactory(dataSource: DataSource, environment: Environment): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan("cz.encircled")

        val fresh = environment.acceptsProfiles("fresh")

        if (environment.acceptsProfiles("eclipse")) {
            val vendorAdapter = EclipseLinkJpaVendorAdapter()
            em.jpaVendorAdapter = vendorAdapter
            em.setJpaProperties(eclipseProperties(fresh))
        } else {
            val vendorAdapter = HibernateJpaVendorAdapter()
            em.jpaVendorAdapter = vendorAdapter
            em.setJpaProperties(hibernateProperties(fresh))
        }
        return em
    }

    @Bean(name = arrayOf("transactionManager"))
    fun transactionManager(emf: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = emf

        return transactionManager
    }

    @Bean
    fun exceptionTranslation(): PersistenceExceptionTranslationPostProcessor {
        return PersistenceExceptionTranslationPostProcessor()
    }

    private fun eclipseProperties(fresh: Boolean): Properties {
        val properties = Properties()
        properties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false")
        properties.put(PersistenceUnitProperties.WEAVING, "false")
        if (fresh) {
            properties.put(PersistenceUnitProperties.DDL_GENERATION, "drop-and-create-tables")
        }
        return properties
    }

    private fun hibernateProperties(fresh: Boolean): Properties {
        val properties = Properties()
        properties.setProperty("hibernate.hbm2ddl.auto", if (fresh) "create" else "update")
        properties.setProperty("hibernate.show_sql", "true")
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect")
        return properties
    }

}
