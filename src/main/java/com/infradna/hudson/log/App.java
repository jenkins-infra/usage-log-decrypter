package com.infradna.hudson.log;

import com.trilead.ssh2.crypto.Base64;
import hudson.Util;
import hudson.model.UsageStatistics.CombinedCipherInputStream;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] _args ) throws Exception  {
        File keyFile = new File(_args[0]);
        Scrambler sc = new Scrambler(Hex.decode(Util.getDigestOf(_args[1])));
        File outDir = new File(_args[2]);

        Cipher cipher = createCipher(keyFile);

        List<String> a = Arrays.asList(_args);
        for (String log : a.subList(3,a.size())) {
            File logFile = new File(log);
            File outFile = new File(outDir, logFile.getName());

            if (outFile.exists() && outFile.lastModified()>logFile.lastModified()) {
                System.out.println("Skipping "+log);
                continue;
            }
            System.out.println("Handling "+log);

            BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(logFile))));
            PrintWriter w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outFile)))));
            try {
                String line;
                while ((line=in.readLine())!=null) {
                    LogLine ll = LogLine.parse(line);
                    String url = ll.getRequestUrl();
                    if (!url.startsWith("/usage-stats.js?"))
                        continue;
                    try {
                        ll.usage = decrypt(cipher, url.substring(url.indexOf('?') + 1));
                    } catch (IOException e) {
                        System.err.println("Failed to handle "+line);
                        continue;
                    } catch (GeneralSecurityException e) {
                        System.err.println("Failed to handle "+line);
                        continue;
                    }
                    sc.handleJSONObject(ll.usage);
                    ll.usage.put("timestamp",ll.timestampString);

                    w.println(ll.usage);
                }
            } finally {
                in.close();
                w.close();
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
