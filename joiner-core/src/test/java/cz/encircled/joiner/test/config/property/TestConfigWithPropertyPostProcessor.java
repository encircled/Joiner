package cz.encircled.joiner.test.config.property;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kisel on 05.02.2016.
 */
@Configuration
public class TestConfigWithPropertyPostProcessor {

    @Bean
    public PropertyPostProcessor propertyPostProcessor() {
        return new PropertyPostProcessor();
    }

}
