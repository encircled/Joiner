package cz.encircled.joiner.config;

import jakarta.persistence.EntityManagerFactory;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@EnableTransactionManagement
public class EntityManagerConfig {

    static EmbeddedDatabase db;

    @Bean
    public DataSource dataSource() {
        if (db != null) {
            db.shutdown();
        }
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        EmbeddedDatabase newDb = builder
                .setType(EmbeddedDatabaseType.H2)
                .build();
        db = newDb;
        return newDb;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, @Value("${orm:hibernate}") String orm) {
        System.out.println("Creating LocalContainerEntityManagerFactoryBean for " + orm);
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("cz.encircled");

        if (orm.equals("hibernate")) {
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            em.setJpaProperties(hibernateProperties());
        } else {
            AbstractJpaVendorAdapter vendorAdapter = new EclipseLinkJpaVendorAdapter();
            em.setJpaVendorAdapter(vendorAdapter);
            em.setJpaProperties(eclipseProperties());
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

    private Properties eclipseProperties() {
        Properties properties = new Properties();
        properties.put(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");
        properties.put(PersistenceUnitProperties.WEAVING, "static");
        properties.put("eclipselink.logging.level.sql", "FINE");
        try {
            Class.forName("cz.encircled.joiner.eclipse.InheritanceJoiningSessionCustomizer");
            properties.put("eclipselink.session.customizer", "cz.encircled.joiner.eclipse.InheritanceJoiningSessionCustomizer");
        } catch (Exception e) {
            // ignore
        }
        properties.put(PersistenceUnitProperties.DDL_GENERATION, "drop-and-create-tables");
        return properties;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "create");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        return properties;
    }

}
