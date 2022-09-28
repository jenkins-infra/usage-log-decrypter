package org.jenkinsci.infra.log;

import hudson.model.UsageStatistics.CombinedCipherInputStream;
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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.crypto.Cipher;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author Kohsuke Kawaguchi
 */
public class Decrypter {
    private final Cipher cipher;
    private final Scrambler scrambler;
    private final LogLineFactory llf = new LogLineFactory();
    private final Predicate<LogLine> infra621 = new INFRA621();
    private final Predicate<LogLine> publicCores = new PublicCores();

    public Decrypter(File keyFile, Scrambler scrambler) throws IOException, GeneralSecurityException {
        this.cipher = createCipher(keyFile);
        this.scrambler = scrambler;
    }

    private Cipher createCipher(File key) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, loadKey(key));
        return cipher;
    }

    private PrivateKey loadKey(File key) throws IOException, GeneralSecurityException {
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

    public void process(File logFile, File outFile) throws IOException, GeneralSecurityException {
        File tmpFile = File.createTempFile("log","tmp",outFile.getParentFile());

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(logFile))));
                PrintWriter w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(tmpFile)))))) {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    if (!line.contains("usage-stats.js")) continue;   // throw away unrelated lines quickly

                    LogLine ll = llf.parse(line);
                    String url = ll.getRequestUrl();
                    if (!url.startsWith("/usage-stats.js?"))
                        continue;
                    try {
                        String data = url.substring(url.indexOf('?') + 1);
                        if (data.length() == 0) continue;   // there seems to be many of those
                        ll.usage = decrypt(cipher, data);
                    } catch (IOException | GeneralSecurityException | NumberFormatException e) {
                        System.err.println("Failed to handle " + line);
                        e.printStackTrace();
                        continue;
                    }
                    if (ll.usage.isNullObject()) {
                        System.err.println("Failed to handle " + line);
                        continue;
                    }
                    if (infra621.test(ll)) {
                        continue;
                    }
                    if (publicCores.test(ll)) {
                        continue;
                    }
                    scrambler.handleJSONObject(ll.usage);
                    ll.usage.put("timestamp", ll.timestampString);

                    w.println(ll.usage);
                } catch (ParseException e) {
                    System.err.println("Failed to handle " + line);
                    e.printStackTrace();
                }
            }
        }
        tmpFile.renameTo(outFile);
    }

    private JSONObject decrypt(Cipher cipher, String data) throws IOException, GeneralSecurityException {
        byte[] cipherText = Base64.getDecoder().decode(data);
        InputStreamReader r = new InputStreamReader(new GZIPInputStream(
                new CombinedCipherInputStream(new ByteArrayInputStream(cipherText),cipher,"AES",1024)), StandardCharsets.UTF_8);
        return JSONObject.fromObject(IOUtils.toString(r));
    }
}
