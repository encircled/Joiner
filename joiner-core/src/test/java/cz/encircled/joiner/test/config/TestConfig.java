package cz.encircled.joiner.test.config;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"cz.encircled.joiner.test.core", "cz.encircled.joiner.test.repository"})
public class TestConfig {

    /*@Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:postgresql://localhost:5432/joiner", "dev", "dev");
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }*/

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

        if (environment.acceptsProfiles("eclipse")) {
            AbstractJpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            em.setJpaProperties(eclipseProperties());
        } else {
            AbstractJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            em.setJpaProperties(hibernateProperties(environment.acceptsProfiles("fresh")));
        }
        return em;
    }

    @Bean
    public EntityManager entityManager(EntityManagerFactory emf) {
        return emf.createEntityManager();
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

    Properties eclipseProperties() {
        Properties properties = new Properties();
        properties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");
        properties.put(PersistenceUnitProperties.WEAVING, "false");
        return properties;
    }

    Properties hibernateProperties(boolean fresh) {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", fresh ? "create" : "update");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        return properties;
    }

}
