package cz.encircled.joiner.spring;

import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Default configuration of joiner for Spring environment. Instance of shared spring-manager EntityManager must be available
 *
 * @author Vlad on 14-Aug-16.
 */
@Configuration
public class JoinerConfiguration {

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
