package cz.encircled.joiner.config;

import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import cz.encircled.joiner.spring.JoinerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.Collections;

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@Import({EntityManagerConfig.class, JoinerConfiguration.class})
@ComponentScan(basePackages = {"cz.encircled.joiner"})
public class SpringTestConfig {

    @Autowired
    private JoinGraphRegistry graphRegistry;

    @PostConstruct
    public void init() {
        graphRegistry.registerJoinGraph("userGroups", Collections.singleton(J.left(QGroup.group)), User.class);
    }

}
