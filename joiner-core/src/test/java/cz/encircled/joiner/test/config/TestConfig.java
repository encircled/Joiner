package cz.encircled.joiner.test.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@Import(EntityManagerConfig.class)
public class TestConfig {

}
