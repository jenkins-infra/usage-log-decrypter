package org.jenkinsci.infra.log;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class ListOfJobTypesTest {
    @Test
    public void sanity() throws IOException {
        ListOfJobTypes l = new ListOfJobTypes();
        assertTrue(l.isPublic("hudson-model-FreeStyleProject"));
        assertTrue(l.isPublic("org-cloudbees-literate-jenkins-LiterateBranchProject"));

        // not going to be a valid type (unless someone reads this and decides to go trolling)
        assertFalse(l.isPublic("whatever"));
    }
}