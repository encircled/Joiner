package cz.encircled.joiner.test.config;

import java.util.Collections;

import javax.annotation.PostConstruct;

import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import cz.encircled.joiner.spring.JoinerConfiguration;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@Import({ EntityManagerConfig.class, JoinerConfiguration.class })
@ComponentScan(basePackages = { "cz.encircled.joiner.test" })
public class SpringTestConfig {

    @Autowired
    private JoinGraphRegistry graphRegistry;

    @PostConstruct
    public void init() {
        graphRegistry.registerJoinGraph("userGroups", Collections.singleton(J.left(QGroup.group)), User.class);
    }

}
