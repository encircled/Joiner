package cz.encircled.joiner.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertTest {

    @Test
    public void testAssertNotNull() {
        Assert.notNull("");
    }

    @Test
    public void testAssertWhenNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Assert.notNull(null));
    }

    @Test
    public void testAssertThat() {
        Assert.assertThat(true);
    }

    @Test
    public void testAssertThatFail() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Assert.assertThat(false));
    }

}
