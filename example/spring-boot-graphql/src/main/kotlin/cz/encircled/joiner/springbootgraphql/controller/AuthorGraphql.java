package cz.encircled.joiner.springbootgraphql.controller;

import cz.encircled.joiner.springbootgraphql.model.Author;
import cz.encircled.joiner.springbootgraphql.service.AuthorService;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/graphql")
public class AuthorGraphql {

    final
    AuthorService authorService;

    public AuthorGraphql(AuthorService authorService) {
        this.authorService = authorService;
    }

    @QueryMapping
    public Author getAuthor(@Argument Long id, DataFetchingEnvironment env) {
        return authorService.findById(id, getSelectedFields(env));
    }

    Set<String> getSelectedFields(DataFetchingEnvironment env) {
        return env.getSelectionSet().getFields().stream().map(SelectedField::getQualifiedName).collect(Collectors.toSet());
    }

}
