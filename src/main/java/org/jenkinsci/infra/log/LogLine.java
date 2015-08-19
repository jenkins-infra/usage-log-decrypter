package org.jenkinsci.infra.log;

import net.sf.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public class LogLine {
    public String remoteIp;
    public String timestampString;
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
}
