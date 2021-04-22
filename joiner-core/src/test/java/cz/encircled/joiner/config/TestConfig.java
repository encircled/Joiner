package cz.encircled.joiner.config;

import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@Import(EntityManagerConfig.class)
@ComponentScan(basePackages = {"cz.encircled.joiner"})
public class TestConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JoinGraphRegistry joinGraphRegistry() {
        return new DefaultJoinGraphRegistry();
    }

    @Bean
    public Joiner joiner(JoinGraphRegistry joinGraphRegistry) {
        Joiner joiner = new Joiner(entityManager);
        joiner.setJoinGraphRegistry(joinGraphRegistry);
        return joiner;
    }

}
