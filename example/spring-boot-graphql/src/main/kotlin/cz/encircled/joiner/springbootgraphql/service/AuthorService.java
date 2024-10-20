package cz.encircled.joiner.springbootgraphql.service;

import cz.encircled.joiner.springbootgraphql.model.Author;

import java.util.Set;

public interface AuthorService {
    Author findById(Long id, Set<String> selectedFields);
}
