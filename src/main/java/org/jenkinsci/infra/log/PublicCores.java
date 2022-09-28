package org.jenkinsci.infra.log;

import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import org.apache.commons.io.IOUtils;

/**
 * @author Daniel Beck
 */
public class PublicCores implements Predicate<LogLine> {

    private final HashSet<String> publicCores;

    public PublicCores() throws IOException {
        publicCores = new HashSet<>();
        
        String s = IOUtils.toString(new URL("https://repo.jenkins-ci.org/api/search/versions?g=org.jenkins-ci.main&a=jenkins-core&repos=releases").openStream(), StandardCharsets.UTF_8);
        JSONArray o = JSONObject.fromObject(s).getJSONArray("results");

        for (int i = 0; i < o.size(); i++) {
            JSONObject entry = o.getJSONObject(i);
            publicCores.add(entry.getString("version"));
        }
    }

    @Override
    public boolean test(LogLine ll) {
        return !isPublic(ll.usage.getString("version"));
    }

    boolean isPublic(String vs) {
        return publicCores.contains(vs);
    }
}
