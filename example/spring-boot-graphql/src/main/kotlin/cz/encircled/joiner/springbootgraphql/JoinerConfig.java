package cz.encircled.joiner.springbootgraphql;

import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.springbootgraphql.model.Author;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static cz.encircled.joiner.springbootgraphql.model.QPost.post;
import static cz.encircled.joiner.springbootgraphql.model.QStatus.status;

@Configuration
public class JoinerConfig {

    @PersistenceContext
    EntityManager entityManager;

    @Bean
    Joiner joiner() {
        Joiner joiner = new Joiner(entityManager);

        DefaultJoinGraphRegistry joinRegistry = new DefaultJoinGraphRegistry();
        joiner.setJoinGraphRegistry(joinRegistry);
        joinRegistry.registerJoinGraph("posts", List.of(J.left(post)), Author.class);
        joinRegistry.registerJoinGraph("posts/status", List.of(J.left(post).nested(status)), Author.class);
        joinRegistry.registerJoinGraph("status", List.of(J.inner(status)), Author.class);

        return joiner;
    }

}
