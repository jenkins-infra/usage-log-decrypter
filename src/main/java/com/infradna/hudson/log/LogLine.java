package com.infradna.hudson.log;

import net.sf.json.JSONObject;
import org.apache.commons.lang.time.FastDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class LogLine {
    public String remoteIp;
    public Date timestamp;
    public String request;
    public int status;
    public String referer;
    public String userAgent;

    public JSONObject usage;

    public String getRequestUrl() {
        // skip off the HTTP verb
        String s = request.substring(request.indexOf(' ')+1);
        // skip off the HTTP version
        s = s.substring(0,s.lastIndexOf(' '));
        return s;
    }

    public static LogLine parse(String line) throws ParseException {
        List<String> tokens = new ArrayList<String>(16);
        for (int i=0; i<line.length();) {
            char head = line.charAt(i);
            int end;
            if (head=='\"')
                end = line.indexOf('"',i+1)+1;
            else if (head=='[')
                end = line.indexOf(']',i+1)+1;
            else
                end = line.indexOf(' ',i);
            if (end<=0)
                end = line.length();
            tokens.add(line.substring(i,end));
            i = end+1;
        }

        LogLine ll = new LogLine();
        ll.remoteIp = tokens.get(0);
        synchronized (fdf) {
            ll.timestamp = (Date)fdf.parseObject(trimQuote(tokens.get(3)));
        }
        ll.request = trimQuote(tokens.get(4));
        ll.status = Integer.parseInt(tokens.get(5));
        ll.referer = trimQuote(tokens.get(7));
        ll.userAgent = trimQuote(tokens.get(8));

        return ll;
    }

    private static String trimQuote(String s) {
        if (s.length()==0)  return s;
        switch (s.charAt(0)) {
        case '"':
        case '[':
            return s.substring(1,s.length()-1);
        default:
            return s;
        }
    }

    private static final SimpleDateFormat fdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
}
