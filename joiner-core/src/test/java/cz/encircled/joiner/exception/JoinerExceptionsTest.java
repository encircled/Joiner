package cz.encircled.joiner.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JoinerExceptionsTest {

    @Test
    public void testExceptionThrown() {
        Assertions.assertThrows(JoinerException.class, () -> {
            throw JoinerExceptions.entityNotFound();
        });
        Assertions.assertThrows(JoinerException.class, () -> {
            throw JoinerExceptions.multipleEntitiesFound();
        });
    }

}
