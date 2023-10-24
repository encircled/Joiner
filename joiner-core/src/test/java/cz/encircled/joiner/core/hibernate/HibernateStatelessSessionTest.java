package cz.encircled.joiner.core.hibernate;

import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.model.Password;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QPassword;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HibernateStatelessSessionTest extends AbstractTest {

    @Test
    public void testStatelessSessionIsUsed() {
        try {
            joiner.setJoinerProperties(new JoinerProperties().setUseStatelessSessions(true));

            String testName = "SHOULD_BE_IGNORED";
            List<Password> passwords = joiner.find(Q.from(QPassword.password).where(QPassword.password.id.gt(0)));
            assertFalse(passwords.isEmpty());

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
        } finally {
            joiner.setJoinerProperties(null);
        }
    }

    @Test
    public void testHintsInStatelessSession() {
        try {
            joiner.setJoinerProperties(new JoinerProperties().setUseStatelessSessions(true));

            assertThrows(NumberFormatException.class, () -> {
                joiner.find(Q.from(QAddress.address)
                        .addHint("org.hibernate.timeout", "wrong_val")
                );
            });
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


            assertThrows(NumberFormatException.class, () -> {
                joiner.find(Q.from(QAddress.address));
            });
        } finally {
            joiner.setJoinerProperties(null);
        }
    }

}
