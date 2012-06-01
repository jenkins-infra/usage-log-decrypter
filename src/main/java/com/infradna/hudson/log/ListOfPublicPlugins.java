package com.infradna.hudson.log;

import hudson.util.IOUtils;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class ListOfPublicPlugins {
    private final Scrambler scrambler;
    private final Set<String> publicPluginNames;

    public ListOfPublicPlugins(Scrambler scrambler) throws IOException {
        this.scrambler = scrambler;

        String s = IOUtils.toString(new URL("http://updates.jenkins-ci.org/update-center.json").openStream(), "UTF-8");
        s = s.substring(s.indexOf('{'));
        s = s.substring(0, s.lastIndexOf('}')+1);

        JSONObject o = JSONObject.fromObject(s);
        publicPluginNames = o.getJSONObject("plugins").keySet();
    }

    public String escape(String pluginName) throws IOException, GeneralSecurityException {
        if (publicPluginNames.contains(pluginName))
            return pluginName;
        return "privateplugin-"+scrambler.string(pluginName);
    }
}
