package com.infradna.hudson.log;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class ListOfJobTypesTest {
    @Test
    public void sanity() throws IOException {
        assertTrue(new ListOfJobTypes().isPublic("hudson-model-FreeStyleProject"));
    }
}