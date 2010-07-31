package com.infradna.hudson.log;

import com.trilead.ssh2.crypto.Base64;
import net.sf.json.JSONObject;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class Scrambler {
    private final SecretKey key;
    private final Cipher cipher;
    private final ListOfPublicPlugins listOfPublicPlugins;

    public Scrambler(byte[] secret) throws GeneralSecurityException, IOException {
        key = new SecretKeySpec(secret,0,128/8, "AES");
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        listOfPublicPlugins = new ListOfPublicPlugins(this);
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

    public void handleJSONObject(JSONObject o) throws IOException, GeneralSecurityException {
        o.put("install",hex(o.getString("install")));
        for (JSONObject item : (List<JSONObject>)(List)o.getJSONArray("plugins")) {
            item.put("name",listOfPublicPlugins.escape(item.getString("name")));
        }
    }
}
