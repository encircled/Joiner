package cz.encircled.joiner.model;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;

public class MyGenerator
        implements IdentifierGenerator, Configurable {

    static long ids = 1;

    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        return ids++;
    }

}