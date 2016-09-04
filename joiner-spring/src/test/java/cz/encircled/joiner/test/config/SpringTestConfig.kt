package cz.encircled.joiner.test.config

import cz.encircled.joiner.spring.JoinerConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@Import(EntityManagerConfig::class, JoinerConfiguration::class)
@ComponentScan("cz.encircled.joiner.test.repository")
class SpringTestConfig
