package cz.encircled.joiner.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kisel on 01.02.2016.
 */
@Configuration
public class TestConfigWithPostProcessor {

    @Bean
    public TestPostProcessor testPostProcessor() {
        return new TestPostProcessor();
    }

}
