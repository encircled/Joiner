package cz.encircled.joiner.springbootgraphql.service;

import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.springbootgraphql.model.Author;
import cz.encircled.joiner.springbootgraphql.model.QAuthor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthorServiceImpl implements AuthorService {

    final
    Joiner joiner;

    public AuthorServiceImpl(Joiner joiner) {
        this.joiner = joiner;
    }

    @Override
    public Author findById(Long id, Set<String> selectedFields) {
        return joiner.findOne(Q.from(QAuthor.author)
                .joinGraphs(selectedFields)
                .where(QAuthor.author.id.eq(id)));
    }

}
