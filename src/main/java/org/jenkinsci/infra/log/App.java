package org.jenkinsci.infra.log;

import hudson.Util;
import hudson.util.DaemonThreadFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class App
{
    public static void main( String[] _args ) throws Exception  {
        final File keyFile = new File(_args[0]);
        final File outDir = new File(_args[2]);
        final byte[] secret = Util.fromHexString(Util.getDigestOf(_args[1]));

        ExecutorService es = Executors.newFixedThreadPool(8,new DaemonThreadFactory());

        List<Future> futures = new ArrayList<>();
        List<String> a = Arrays.asList(_args);
        for (final String log : a.subList(3,a.size())) {
            futures.add(es.submit(() -> {
                try {
                    File in = new File(log);
                    File out = new File(outDir, new File(log).getName());

                    if (out.exists() && out.lastModified()>in.lastModified()) {
                        System.out.println("Skipping "+in);
                        return;
                    }
                    System.out.println("Handling "+in);

                    new Decrypter(keyFile,new Scrambler(secret)).process(in,out);
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        es.shutdown();
    }
}
