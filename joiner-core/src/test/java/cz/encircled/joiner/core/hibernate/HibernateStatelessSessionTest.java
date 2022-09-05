package cz.encircled.joiner.core.hibernate;

import cz.encircled.joiner.core.AbstractTest;
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
            joiner.setUseStatelessSessions(true);

            String testName = "SHOULD_BE_IGNORED";
            List<Password> passwords = joiner.find(Q.from(QPassword.password));
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
            joiner.setUseStatelessSessions(false);
        }
    }

    @Test
    public void testHintsInStatelessSession() {
        try {
            joiner.setUseStatelessSessions(true);

            assertThrows(NumberFormatException.class, () -> {
                joiner.find(Q.from(QAddress.address)
                        .addHint("org.hibernate.timeout", "wrong_val")
                );
            });
        } finally {
            joiner.setUseStatelessSessions(false);
        }
    }

}
