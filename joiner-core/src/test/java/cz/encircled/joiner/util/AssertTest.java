package cz.encircled.joiner.util;

import org.junit.Test;

public class AssertTest {

    @Test
    public void testAssertNotNull() {
        Assert.notNull("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssertWhenNull() {
        Assert.notNull(null);
    }

    @Test
    public void testAssertThat() {
        Assert.assertThat(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssertThatFail() {
        Assert.assertThat(false);
    }

}
