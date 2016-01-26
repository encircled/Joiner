package cz.encircled.joiner.test.config;

import cz.encircled.joiner.alias.JoinerAliasResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kisel on 26.01.2016.
 */
@Configuration
public class TestConfigWithResolver {

    @Bean
    public JoinerAliasResolver joinerAliasResolver() {
        return new TestAliasResolver();
    }

}
