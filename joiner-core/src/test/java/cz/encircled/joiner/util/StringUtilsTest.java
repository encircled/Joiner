package cz.encircled.joiner.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilsTest {

    @Test
    public void testUncapitalize() {
        assertEquals("tEST", StringUtils.uncapitalize("TEST"));
        assertEquals("tEsT", StringUtils.uncapitalize("tEsT"));
        assertEquals("test", StringUtils.uncapitalize("test"));
        assertEquals("tesT", StringUtils.uncapitalize("TesT"));
        assertEquals("t", StringUtils.uncapitalize("T"));
        assertEquals("", StringUtils.uncapitalize(""));
        assertNull(StringUtils.uncapitalize(null));
    }

}
