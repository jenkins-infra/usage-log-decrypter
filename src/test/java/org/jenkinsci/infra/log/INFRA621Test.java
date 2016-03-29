package org.jenkinsci.infra.log;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class INFRA621Test {
    private final INFRA621 i = new INFRA621();

    @Test
    public void basics() {
        n("1.644");
        y("1.645");
        y("1.652");
        n("1.653");

        n("1.642");
        n("1.642.1");
        y("1.642.2");
        y("1.642.3");
        n("1.642.4");

        y("1.651");
        n("1.651.1");
        n("1.651.2");

        // SNAPSHOT and other private build handling. both positive & negative
        y("1.650");
        y("1.650-SNAPSHOT");
        y("1.650 (private-03/29/2016 00:56 GMT-kohsuke)");

        n("1.640");
        n("1.640-SNAPSHOT");
        n("1.640 (private-03/29/2016 00:56 GMT-kohsuke)");

        // edge case
        n("bogus");
    }

    private void y(String... args) {
        for (String s : args) {
            assertTrue(s, i.isAffected(s));
        }
    }

    private void n(String... args) {
        for (String s : args) {
            assertFalse(s, i.isAffected(s));
        }
    }
}