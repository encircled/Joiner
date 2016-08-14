package cz.encircled.joiner.test.config;

import cz.encircled.joiner.repository.Joiner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Vlad on 14-Aug-16.
 */
@Configuration
public class JoinerConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public Joiner joiner() {
        return new Joiner(entityManager);
    }

}
