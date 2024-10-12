package cz.encircled.joiner.springbootexample.controller;

import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.springbootexample.Employment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cz.encircled.joiner.springbootexample.QEmployment.employment;

@RestController
@RequestMapping("/java/employment")
public class JavaEmploymentController {

    @Autowired
    Joiner joiner;

    @GetMapping("{id}")
    Employment getOne(@PathVariable Long id) {
        return joiner.findOne(Q.from(employment).where(employment.id.eq(id)));
    }

    @GetMapping
    List<Employment> getAll() {
        return joiner.find(Q.from(employment));
    }

    @GetMapping("/names")
    List<String> getAllNames() {
        return joiner.find(Q.select(employment.name).from(employment));
    }

}
