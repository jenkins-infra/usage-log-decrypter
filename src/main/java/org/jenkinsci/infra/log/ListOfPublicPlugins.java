package org.jenkinsci.infra.log;

import hudson.util.IOUtils;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class ListOfPublicPlugins {
    private final Scrambler scrambler;
    private final Set<String> publicPluginNames;

    public ListOfPublicPlugins(Scrambler scrambler) throws IOException {
        this.scrambler = scrambler;

        // use the experimental update center to include plugins that are considered alpha/beta only
        String s = IOUtils.toString(new URL("http://updates.jenkins-ci.org/experimental/update-center.actual.json").openStream(), "UTF-8");

        JSONObject o = JSONObject.fromObject(s);
        publicPluginNames = new HashSet<String>(o.getJSONObject("plugins").keySet());

        // TODO since this isn't _that_ important, should we wrap this in try/catch and discard exceptions?
        Properties p = new Properties();
        p.load(new URL("https://raw.githubusercontent.com/jenkins-infra/backend-update-center2/master/src/main/resources/artifact-ignores.properties").openStream());
        for (String key : p.stringPropertyNames()) {
            // this will add useless entries of the form id-version for plugins that aren't actually blacklisted, but that's not a problem -- they'll never be checked.
            publicPluginNames.add(key);
        }
    }

    public String escape(String pluginName) throws IOException, GeneralSecurityException {
        if (publicPluginNames.contains(pluginName))
            return pluginName;
        return "privateplugin-"+scrambler.string(pluginName);
    }
}
