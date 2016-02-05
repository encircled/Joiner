package cz.encircled.joiner.test.config.hint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kisel on 04.02.2016.
 */
@Configuration
public class TestConfigWithHintPostProcessor {

    @Bean
    public HintPostProcessor hintPostProcessor() {
        return new HintPostProcessor();
    }

}
