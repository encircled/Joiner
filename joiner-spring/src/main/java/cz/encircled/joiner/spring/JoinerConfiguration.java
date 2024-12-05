package cz.encircled.joiner.spring;

import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.kotlin.JoinerKt;
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ClassUtils;

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
    @Primary
    public Joiner joiner(JoinGraphRegistry joinGraphRegistry) {
        Joiner joiner = new Joiner(entityManager);
        joiner.setJoinGraphRegistry(joinGraphRegistry);
        return joiner;
    }

    @Bean
    public JoinerKt joinerKt(JoinGraphRegistry joinGraphRegistry) {
        if (ClassUtils.isPresent("cz.encircled.joiner.kotlin.JoinerKt", JoinerConfiguration.class.getClassLoader())) {
            JoinerKt joiner = new JoinerKt(entityManager);
            joiner.setJoinGraphRegistry(joinGraphRegistry);
            return joiner;
        }
        return null;
    }

}
