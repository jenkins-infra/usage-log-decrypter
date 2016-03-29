package org.jenkinsci.infra.log;

import com.google.common.base.Predicate;
import hudson.util.VersionNumber;

/**
 * @author Kohsuke Kawaguchi
 */
public class INFRA621 implements Predicate<LogLine> {
    @Override
    public boolean apply(LogLine ll) {
        return isAffected(ll.usage.getString("version"));
    }

    boolean isAffected(String vs) {
        vs = stripAfter(vs," ");
        vs = stripAfter(vs,"-SNAPSHOT");

        VersionNumber v = null;
        try {
            v = new VersionNumber(vs);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // 1.651.x LTS is OK
        if (vs.startsWith("1.651."))    return false;

        // main line: 1.645 - 1.652
        // LTS:  1.642.2 & 1.642.3
        return between(v645,v,v652) || between(v642_2,v,v642_3);
    }

    /**
     * Checks if the given version number is in range between
     */
    private boolean between(VersionNumber lo, VersionNumber v, VersionNumber hi) {
        return lo.compareTo(v)<=0 && v.compareTo(hi)<=0;
    }

    private String stripAfter(String vs, String s) {
        int idx = vs.indexOf(s);
        if (idx>0)
            vs = vs.substring(0,idx);
        return vs;
    }


    private static final VersionNumber v645 = new VersionNumber("1.645");
    private static final VersionNumber v652 = new VersionNumber("1.652");
    private static final VersionNumber v642_2 = new VersionNumber("1.642.2");
    private static final VersionNumber v642_3 = new VersionNumber("1.642.3");
}
