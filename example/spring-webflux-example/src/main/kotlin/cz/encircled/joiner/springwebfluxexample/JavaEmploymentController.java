package cz.encircled.joiner.springwebfluxexample;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.reactive.ReactorJoiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/java/employment")
public class JavaEmploymentController {

    @Autowired
    ReactorJoiner joiner;

    @GetMapping("{id}")
    Mono<Employment> getOne(@PathVariable Long id) {
        return joiner.findOne(Q.from(QEmployment.employment).where(QEmployment.employment.id.eq(id)));
    }

    @GetMapping
    Flux<Employment> getAll() {
        return joiner.find(Q.from(QEmployment.employment));
    }

}
