package org.jenkinsci.infra.log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses {@link LogLine}.
 *
 * @author Kohsuke Kawaguchi
 */
public class LogLineFactory {
    public LogLine parse(String line) throws ParseException {
        try {
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

            // for a brief period, Java.net access log had session ID in the 4th field.
            int offset=0;
            if (isSessionId(tokens.get(3)))
                offset = 1;

            LogLine ll = new LogLine();
            ll.remoteIp = tokens.get(0);
            ll.timestampString = trimQuote(tokens.get(offset+3));
            ll.timestamp = (Date)fdf.parseObject(ll.timestampString);
            ll.request = trimQuote(tokens.get(offset+4));
            ll.status = Integer.parseInt(tokens.get(offset+5));
            ll.referer = trimQuote(tokens.get(offset+7));
            ll.userAgent = trimQuote(tokens.get(offset+8));

            return ll;
        } catch (NumberFormatException e) {
            throw new ParseException(e.getMessage(),0);
        }
    }

    private boolean isSessionId(String s) {
        return  SESSIONID_PATTERN.matcher(s).matches();
    }

    private String trimQuote(String s) {
        if (s.length()==0)  return s;
        switch (s.charAt(0)) {
        case '"':
        case '[':
            return s.substring(1,s.length()-1);
        default:
            return s;
        }
    }

    private static final Pattern SESSIONID_PATTERN = Pattern.compile("[0-9A-Fa-f\\-]+");
    private final SimpleDateFormat fdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
}
