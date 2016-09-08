package cz.encircled.joiner.test.config;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@EnableTransactionManagement
public class EntityManagerConfig {

    @Bean
    public DataSource dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Environment environment) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("cz.encircled");

        boolean fresh = environment.acceptsProfiles("fresh");

        if (environment.acceptsProfiles("eclipse")) {
            AbstractJpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            em.setJpaProperties(eclipseProperties(fresh));
        } else {
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            em.setJpaProperties(hibernateProperties(fresh));
        }
        return em;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    private Properties eclipseProperties(boolean fresh) {
        Properties properties = new Properties();
        properties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");
        properties.put(PersistenceUnitProperties.WEAVING, "static");
        properties.put("eclipselink.logging.level.sql", "FINE");
        properties.put("eclipselink.session.customizer", "cz.encircled.joiner.eclipse.InheritanceJoiningSessionCustomizer");
        if (fresh) {
            properties.put(PersistenceUnitProperties.DDL_GENERATION, "drop-and-create-tables");
        }
        return properties;
    }

    private Properties hibernateProperties(boolean fresh) {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", fresh ? "create" : "update");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        return properties;
    }

}
