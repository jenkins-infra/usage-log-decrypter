package org.jenkinsci.infra.log;

import com.trilead.ssh2.crypto.Base64;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Scrambler {
    private final SecretKey key;
    private final Cipher cipher;
    private final ListOfPublicPlugins listOfPublicPlugins;
    private final ListOfJobTypes listOfJobTypes;

    public Scrambler(byte[] secret) throws GeneralSecurityException, IOException {
        key = new SecretKeySpec(secret,0,128/8, "AES");
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        listOfPublicPlugins = new ListOfPublicPlugins(this);
        listOfJobTypes = new ListOfJobTypes();
    }

    /**
     * Scrambles a base64 string and set it back to 64bit again.
     */
    public String base64(String s) throws IOException, GeneralSecurityException {
        // add the magic suffix which works like a check sum.
        return new String(Base64.encode(cipher.doFinal(Base64.decode(s.toCharArray()))));
    }

    public String hex(String s) throws IOException, GeneralSecurityException {
        // add the magic suffix which works like a check sum.
        return new String(Hex.encode(cipher.doFinal(Hex.decode(s))));
    }

    public String string(String pluginName) throws IOException, GeneralSecurityException {
        return new String(Hex.encode(cipher.doFinal(pluginName.getBytes("UTF-8"))));
    }

    public String version(String v) throws IOException, GeneralSecurityException {
        int idx = v.indexOf("(private");
        if (idx>=0)      return v.substring(0,idx)+"(private)";

        // mask out other (...)
        idx = v.indexOf('(');
        if (idx>=0) {
            int e = v.indexOf(')',idx);
            if (e>=0)
                v = v.substring(0,idx)+"(***)"+v.substring(e+1);
        }
        
        return v;
    }

    public String jobType(String v) throws IOException, GeneralSecurityException {
        if (listOfJobTypes.isPublic(v))
            return v;

        return "private-"+string(v);
    }

    public void handleJSONObject(JSONObject o) throws IOException, GeneralSecurityException {
        o.put("install",hex(o.getString("install")));
        JSONArray plugins = o.optJSONArray("plugins");
        if (plugins!=null) {
            for (JSONObject item : (List<JSONObject>) (List) plugins) {
                item.put("name", listOfPublicPlugins.escape(item.getString("name")));
                item.put("version", version(item.getString("version")));
            }
        }

        JSONObject jobs = o.optJSONObject("jobs");
        if (jobs!=null) {
            Map<String, Object> copy = new HashMap<String, Object>(jobs);
            jobs.clear();
            for (Map.Entry<String, Object> e : copy.entrySet()) {
                jobs.put(jobType(e.getKey()), e.getValue());
            }
        }
        o.put("version",version(o.getString("version")));

    }
}
