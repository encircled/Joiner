package cz.encircled.joiner.test.core.resolver;

import cz.encircled.joiner.core.AliasResolver;
import cz.encircled.joiner.core.DefaultAliasResolver;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.test.core.AbstractTest;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QNormalUser;
import cz.encircled.joiner.test.model.QPassword;
import cz.encircled.joiner.test.model.QSuperUser;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vlad on 06-Sep-16.
 */
public class AliasResolverUnitTest extends AbstractTest {

    @Test
    public void testCollectionAssociationFoundOnParent() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);
        JoinDescription left = J.left(QPassword.password);
        J.left(QUser.user1).nested(left);

        resolver.resolveJoinAlias(left, QUser.user1);

        Assert.assertEquals(new QNormalUser("user1").passwords, left.getCollectionPath());
    }

    @Test
    public void testFieldOnParentFound() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);

        JoinDescription left = J.left(QAddress.address);
        resolver.resolveJoinAlias(left, QSuperUser.superUser);

        Assert.assertEquals(new QUser(QSuperUser.superUser.toString()).addresses, left.getCollectionPath());
    }

    @Test(expected = JoinerException.class)
    public void testFieldNotFound() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);

        resolver.resolveJoinAlias(J.left(QPassword.password), QGroup.group);
    }

}
