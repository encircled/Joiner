package cz.encircled.joiner.test.core.resolver;

import cz.encircled.joiner.core.AliasResolver;
import cz.encircled.joiner.core.DefaultAliasResolver;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.test.core.AbstractTest;
import cz.encircled.joiner.test.model.QNormalUser;
import cz.encircled.joiner.test.model.QPassword;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vlad on 06-Sep-16.
 */
public class AliasResolverUnitTest extends AbstractTest {

    @Test
    public void testCollectionAssociation() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);
        JoinDescription left = J.left(QPassword.password);
        J.left(QUser.user1).nested(left);

        resolver.resolveJoinAlias(left, QUser.user1);

        Assert.assertEquals(new QNormalUser("user1").passwords, left.getCollectionPath());
    }

}
