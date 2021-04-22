package cz.encircled.joiner.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testUncapitalize() {
        Assert.assertEquals("tEST", StringUtils.uncapitalize("TEST"));
        Assert.assertEquals("tEsT", StringUtils.uncapitalize("tEsT"));
        Assert.assertEquals("test", StringUtils.uncapitalize("test"));
        Assert.assertEquals("tesT", StringUtils.uncapitalize("TesT"));
        Assert.assertEquals("t", StringUtils.uncapitalize("T"));
        Assert.assertEquals("", StringUtils.uncapitalize(""));
        Assert.assertNull(StringUtils.uncapitalize(null));
    }

}
