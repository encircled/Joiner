package cz.encircled.joiner.springbootgraphql;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringBootExampleApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringBootExampleApplication.class).run(args);
    }

}
