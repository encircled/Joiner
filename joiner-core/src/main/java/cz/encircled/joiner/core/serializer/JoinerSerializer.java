package cz.encircled.joiner.core.serializer;

import cz.encircled.joiner.query.JoinerQuery;

import java.util.List;

public interface JoinerSerializer {
    String serialize(JoinerQuery<?, ?> joinerQuery);

    List<Object> getConstants();
}
