package org.jenkinsci.infra.log;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PublicCoresTest {
    private final PublicCores i = new PublicCores();
    
    @Test
    public void basics() {
        y("1.642");
        y("2.0");
        y("2.0-beta-2");
        y("2.0-rc-1");
        y("2.0");
        y("2.7.4");
        y("2.19.4");
        y("1.509.3.JENKINS-14362-jzlib");
        y("1.509.2.JENKINS-8856-diag");
        y("1.565.1.JENKINS-22395-dropLinks");
        y("1.396");

        n("1.395");
        n("2.19.5");
        n("2.32.2.1");
        n("2.32.2-whatever");
    }

    private void y(String... args) {
        for (String s : args) {
            assertTrue(s, i.isPublic(s));
        }
    }

    private void n(String... args) {
        for (String s : args) {
            assertFalse(s, i.isPublic(s));
        }
    }
}
