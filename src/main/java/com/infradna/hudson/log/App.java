package com.infradna.hudson.log;

import com.trilead.ssh2.crypto.Base64;
import hudson.model.UsageStatistics.CombinedCipherInputStream;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.openssl.PEMReader;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] _args ) throws Exception  {
        Cipher cipher = createCipher(new File(_args[0]));

        List<String> a = Arrays.asList(_args);
        for (String log : a.subList(1,a.size())) {
            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(log))));
            try {
                String line;
                while ((line=in.readLine())!=null) {
                    LogLine ll = LogLine.parse(line);
                    String url = ll.getRequestUrl();
                    if (!url.startsWith("/usage-stats.js?"))
                        continue;
                    ll.usage = decrypt(cipher, url.substring(url.indexOf('?') + 1));
                    System.out.println(ll.usage);
                }
            } finally {
                in.close();
            }
        }
    }

    private static JSONObject decrypt(Cipher cipher, String data) throws IOException, GeneralSecurityException {
        byte[] cipherText = Base64.decode(data.toCharArray());
        InputStreamReader r = new InputStreamReader(new GZIPInputStream(
                new CombinedCipherInputStream(new ByteArrayInputStream(cipherText),cipher,"AES",1024)), "UTF-8");
        return JSONObject.fromObject(IOUtils.toString(r));
    }

    private static PrivateKey loadKey(File key) throws IOException, GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        // decode private key
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(FileUtils.readFileToByteArray(key));
        return keyFactory.generatePrivate(privSpec);
        

//        FileReader in = new FileReader(key);
//        try {
//            return ((KeyPair)new PEMReader(in).readObject()).getPrivate();
//        } finally {
//            in.close();
//        }
    }

    private static Cipher createCipher(File key) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, loadKey(key));
        return cipher;
    }
}