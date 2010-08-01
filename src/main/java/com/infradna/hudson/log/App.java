package com.infradna.hudson.log;

import hudson.Util;
import org.bouncycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class App
{
    public static void main( String[] _args ) throws Exception  {
        final File keyFile = new File(_args[0]);
        final File outDir = new File(_args[2]);
        final byte[] secret = Hex.decode(Util.getDigestOf(_args[1]));

        ExecutorService es = Executors.newFixedThreadPool(8);

        List<Future> futures = new ArrayList<Future>();
        List<String> a = Arrays.asList(_args);
        for (final String log : a.subList(3,a.size())) {
            futures.add(es.submit(new Runnable() {
                public void run() {
                    try {
                        new Decrypter(keyFile,new Scrambler(secret)).process(
                                new File(log),
                                new File(outDir, new File(log).getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }

        for (Future f : futures)
            f.get();
        es.shutdown();
    }
}
