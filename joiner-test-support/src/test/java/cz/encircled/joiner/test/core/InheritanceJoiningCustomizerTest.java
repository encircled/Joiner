package cz.encircled.joiner.test.core;

import cz.encircled.joiner.test.model.Contact;
import cz.encircled.joiner.test.model.Password;
import cz.encircled.joiner.test.model.User;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.internal.queries.JoinedAttributeManager;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.ObjectBuildingQuery;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Persistence;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Vlad on 06-Sep-16.
 */
public class InheritanceJoiningCustomizerTest extends AbstractTest {

    @Test
    public void test() {
        Query query = entityManager.createQuery("select p from Password p");
        query.setHint(QueryHints.LEFT_FETCH, "p.normalUser.employmentContacts");

        Field f = ReflectionUtils.findField(((EJBQueryImpl) query).getDatabaseQuery().getClass(), "joinedAttributeManager");
        f.setAccessible(true);
        JoinedAttributeManager old = (JoinedAttributeManager) ReflectionUtils.getField(f, ((EJBQueryImpl) query).getDatabaseQuery());
        My newManager = new My(old.getDescriptor(), old.getBaseExpressionBuilder(), old.getBaseQuery());
        newManager.copyFrom(old);

        ReflectionUtils.setField(f, ((EJBQueryImpl) query).getDatabaseQuery(), newManager);

        query.setMaxResults(1);

        List<Password> passwords = query.getResultList();

        Assert.assertFalse(passwords.isEmpty());

        for (Password password : passwords) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(password, "normalUser"));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(password.getNormalUser(), "employmentContacts"));
        }
    }

    @Test
    public void testNestedAssociationOnChildFetched() {
        Query query = entityManager.createQuery("select u from User u left join treat (u.contacts as Phone) p left join fetch p.statuses s");
        query.setHint(QueryHints.LEFT_FETCH, "u.contacts.statuses");
        List<User> users = query.getResultList();

        Assert.assertFalse(users.isEmpty());
        for (User user : users) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"));
            for (Contact contact : user.getContacts()) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "statuses"));
            }
        }
    }

    private class My extends JoinedAttributeManager {

        public My(ClassDescriptor descriptor, ExpressionBuilder baseBuilder, ObjectBuildingQuery baseQuery) {
            super(descriptor, baseBuilder, baseQuery);
        }

        @Override
        protected void processDataResults(AbstractSession session) {
            int originalMax = baseQuery.getMaxRows();
            int originalFirst = baseQuery.getFirstResult();

            try {
                baseQuery.setMaxRows(0);
                baseQuery.setFirstResult(0);
                super.processDataResults(session);
            } finally {
                baseQuery.setMaxRows(originalMax);
                baseQuery.setFirstResult(originalFirst);
            }

        }
    }

}
