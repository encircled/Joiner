package cz.encircled.joiner.core.hibernate;

import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.model.Password;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QPassword;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HibernateStatelessSessionTest extends AbstractTest {

    @Test
    public void testStatelessSessionIsUsedViaGlobalProperty() {
        try {
            joiner.getJoinerProperties().setUseStatelessSessions(true);

            List<Password> passwords = joiner.find(Q.from(QPassword.password).where(QPassword.password.id.gt(0)));
            assertIsStateless(passwords);
        } finally {
            joiner.getJoinerProperties().setUseStatelessSessions(false);
        }
    }

    @Test
    public void testStatelessSessionIsUsed() {
        joiner.getJoinerProperties().setUseStatelessSessions(false);

        List<Password> passwords = joiner.find(Q.from(QPassword.password)
                .setStatelessSession(true)
                .where(QPassword.password.id.gt(0)));
        assertIsStateless(passwords);
    }

    private void assertIsStateless(List<Password> passwords) {
        assertFalse(passwords.isEmpty());

        String testName = "SHOULD_BE_IGNORED";
        for (Password password : passwords) {
            password.setName(testName);
        }

        // Flush should have no effect in stateless session
        entityManager.flush();
        entityManager.clear();

        passwords = joiner.find(Q.from(QPassword.password));
        assertFalse(passwords.isEmpty());

        for (Password password : passwords) {
            assertNotEquals(testName, password.getName());
        }
    }

    @Test
    public void testHintsInStatelessSession() {
        try {
            joiner.setJoinerProperties(new JoinerProperties().setUseStatelessSessions(true));

            joiner.find(Q.from(QAddress.address).addHint("org.hibernate.timeout", "30"));

            assertThrows(NumberFormatException.class, () -> joiner.find(Q.from(QAddress.address)
                    .addHint("org.hibernate.timeout", "wrong_val")
            ));
        } finally {
            joiner.setJoinerProperties(null);
        }
    }

    @Test
    @Disabled("To be fixed in querydsl, count+groupBy+having generates a jpa query w/o 'group by' clause")
    public void testCountInStatelessSession() {
        try {
            joiner.setJoinerProperties(new JoinerProperties().setUseStatelessSessions(true));

            joiner.find(Q.count(QAddress.address)
                    .groupBy(QAddress.address.id)
                    .having(QAddress.address.id.gt(0))
                    .where(QAddress.address.name.eq("normalUser1street1").and(QAddress.address.id.gt(0))));
        } finally {
            joiner.setJoinerProperties(null);
        }
    }

    @Test
    public void testDefaultHintsInStatelessSession() {
        try {
            joiner.setJoinerProperties(new JoinerProperties()
                    .addDefaultHint("org.hibernate.timeout", "wrong_val")
                    .setUseStatelessSessions(true));

            assertThrows(NumberFormatException.class, () -> joiner.find(Q.from(QAddress.address)));
        } finally {
            joiner.setJoinerProperties(null);
        }
    }

    @Test
    public void testLimit() {
        try {
            joiner.getJoinerProperties().setUseStatelessSessions(true);

            assertEquals(1, joiner.find(Q.from(QAddress.address).limit(1L)).size());
        } finally {
            joiner.getJoinerProperties().setUseStatelessSessions(false);
        }
    }

}
