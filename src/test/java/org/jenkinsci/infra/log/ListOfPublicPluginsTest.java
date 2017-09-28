package org.jenkinsci.infra.log;

import org.junit.Assert;
import org.junit.Test;

public class ListOfPublicPluginsTest {
    @Test
    public void testInclusions() throws Exception {
        Scrambler scrambler = new Scrambler(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        ListOfPublicPlugins list = new ListOfPublicPlugins(scrambler);
        Assert.assertEquals("junit", list.escape("junit")); // sample for plugin being published
        Assert.assertEquals("girls", list.escape("girls")); // safe to assume we won't restore distribution of this

        // Ensure plugins that are neither blacklisted nor available show as 'privateplugin-' followed by not the plugin name
        Assert.assertTrue(list.escape("i-don-t-exist").startsWith("privateplugin-"));
        Assert.assertFalse(list.escape("i-don-t-exist").contains("exist"));
    }
}
