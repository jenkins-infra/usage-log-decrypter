package org.jenkinsci.infra.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class ListOfJobTypes {
    private final Set<String> fqcns = new HashSet<String>();

    public ListOfJobTypes() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("job-types.txt"),"UTF-8"));
        String line;
        while ((line=r.readLine())!=null) {
            line = line.trim();
            fqcns.add(line);
        }
    }

    /**
     * Is this job type public?
     *
     * @param jsonKey
     *      The job type in JSON key-safe format, which replaces '.' with '-'.
     */
    public boolean isPublic(String jsonKey)
        return fqcns.contains(jsonKey.replace('-','.'));
    }
}
