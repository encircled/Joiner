package cz.encircled.joiner.test.core.resolver;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.core.AbstractTest;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Vlad on 04-Aug-16.
 */
@ContextConfiguration(classes = {TestConfig.class})
public class PredicateConverterTest extends AbstractTest {

    @Test
    public void basicTest() {
        joiner.find(Q.from(QGroup.group)
                .joins(J.left(QStatus.status), J.left(QUser.user1).nested(J.left(QStatus.status)))
                .where(QGroup.group.statuses.any().id.eq(2L)));
    }

}
