package cz.encircled.joiner.core.resolver;

import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.core.AliasResolver;
import cz.encircled.joiner.core.DefaultAliasResolver;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QContact;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QNormalUser;
import cz.encircled.joiner.model.QPassword;
import cz.encircled.joiner.model.QSuperUser;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Vlad on 06-Sep-16.
 */
public class AliasResolverUnitTest extends AbstractTest {

    @Test
    public void testCollectionAssociationFoundOnParent() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);
        JoinDescription left = J.left(QPassword.password);
        J.left(QUser.user1).nested(left);

        resolver.resolveFieldPathForJoinAlias(left, QUser.user1);

        assertEquals(new QNormalUser("user1").passwords, left.getCollectionPath());
    }

    @Test
    public void testFieldOnParentFound() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);

        JoinDescription left = J.left(QAddress.address);
        resolver.resolveFieldPathForJoinAlias(left, QSuperUser.superUser);

        assertEquals(new QUser(QSuperUser.superUser.toString()).addresses, left.getCollectionPath());
    }

    @Test
    public void testSubtypeFound() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);

        JoinDescription left = J.left(QSuperUser.superUser);
        resolver.resolveFieldPathForJoinAlias(left, QAddress.address);

        assertEquals(QAddress.address.user, left.getSinglePath());
    }

    @Test
    public void testFieldNotFound() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);

        assertThrows(JoinerException.class, () -> resolver.resolveFieldPathForJoinAlias(J.left(QPassword.password), QGroup.group));
    }

    @Test
    public void testAmbiguousAliasExceptionMessage() {
        AliasResolver resolver = new DefaultAliasResolver(entityManager);

        try {
            resolver.resolveFieldPathForJoinAlias(J.left(QUser.user1), QContact.contact);
        } catch (JoinerException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("Multiple mappings found: [contact.employmentUser, contact.user]"));
            return;
        }

        fail();
    }

}
