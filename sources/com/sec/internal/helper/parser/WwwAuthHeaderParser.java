package com.sec.internal.helper.parser;

import android.util.Log;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.log.IMSLog;

public class WwwAuthHeaderParser extends HttpHeaderParser {
    private static final String TAG = "WwwAuthHeaderParser";
    private String paramSplitHeader = null;

    public WwwAuthenticateHeader wwwAuthHeaderParse(String str) {
        if (str == null) {
            return null;
        }
        WwwAuthenticateHeader wwwAuthenticateHeader = new WwwAuthenticateHeader();
        parse(wwwAuthenticateHeader, str);
        Log.d(TAG, "WwwAuthenticateHeader - wwwAuthHeaderParse : " + wwwAuthenticateHeader.toString());
        return wwwAuthenticateHeader;
    }

    public WwwAuthenticateHeader parseHeaderValue(String str) {
        WwwAuthenticateHeader wwwAuthenticateHeader = new WwwAuthenticateHeader();
        parse(wwwAuthenticateHeader, str);
        IMSLog.d(TAG, "WwwAuthenticateHeader - parseHeaderValue : " + wwwAuthenticateHeader.toString());
        return wwwAuthenticateHeader;
    }

    private void parse(WwwAuthenticateHeader wwwAuthenticateHeader, String str) {
        if (wwwAuthenticateHeader != null && str != null) {
            setScheme(wwwAuthenticateHeader, str);
            setRealm(wwwAuthenticateHeader, str);
            setNonce(wwwAuthenticateHeader, str);
            setAlgorithm(wwwAuthenticateHeader, str);
            setQop(wwwAuthenticateHeader, str);
            setStale(wwwAuthenticateHeader, str);
            setOpaque(wwwAuthenticateHeader, str);
            wwwAuthenticateHeader.toString();
        }
    }

    private void setScheme(WwwAuthenticateHeader wwwAuthenticateHeader, String str) {
        if (str.startsWith(WwwAuthenticateHeader.HEADER_PARAM_DIGEST_SCHEME)) {
            wwwAuthenticateHeader.setScheme(WwwAuthenticateHeader.HEADER_PARAM_DIGEST_SCHEME);
        } else if (str.startsWith(WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME)) {
            wwwAuthenticateHeader.setScheme(WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME);
        } else {
            wwwAuthenticateHeader.setScheme(WwwAuthenticateHeader.HEADER_PARAM_UNKNOWN_SCHEME);
        }
    }

    private void setRealm(WwwAuthenticateHeader wwwAuthenticateHeader, String str) {
        String splitHeader = getSplitHeader("realm[\\s]*=", str);
        this.paramSplitHeader = splitHeader;
        wwwAuthenticateHeader.setRealm(splitHeader != null ? getParamValue(splitHeader) : null);
    }

    private void setNonce(WwwAuthenticateHeader wwwAuthenticateHeader, String str) {
        String str2;
        String splitHeader = getSplitHeader("nonce[\\s]*=", str);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            str2 = getParamValue(splitHeader);
        } else {
            str2 = str.substring(str.indexOf(",") + 1).trim();
        }
        wwwAuthenticateHeader.setNonce(str2);
    }

    private void setAlgorithm(WwwAuthenticateHeader wwwAuthenticateHeader, String str) {
        String splitHeader = getSplitHeader("algorithm[\\s]*=", str);
        this.paramSplitHeader = splitHeader;
        wwwAuthenticateHeader.setAlgorithm(splitHeader != null ? getParamValue(splitHeader) : null);
    }

    private void setQop(WwwAuthenticateHeader wwwAuthenticateHeader, String str) {
        String str2;
        String splitHeader = getSplitHeader("qop[\\s]*=", str);
        this.paramSplitHeader = splitHeader;
        if (splitHeader == null) {
            Log.d(TAG, "setQop - no qop");
            str2 = "";
        } else {
            String paramValue = getParamValue(splitHeader);
            Log.d(TAG, "setQop - paramSplitHeader: " + this.paramSplitHeader + ", qopVal : " + paramValue);
            str2 = paramValue;
        }
        wwwAuthenticateHeader.setQop(str2);
    }

    private void setOpaque(WwwAuthenticateHeader wwwAuthenticateHeader, String str) {
        String splitHeader = getSplitHeader("opaque[\\s]*=", str);
        this.paramSplitHeader = splitHeader;
        wwwAuthenticateHeader.setOpaque(splitHeader != null ? getParamValue(splitHeader) : null);
    }

    private void setStale(WwwAuthenticateHeader wwwAuthenticateHeader, String str) {
        String splitHeader = getSplitHeader("stale[\\s]*=", str);
        this.paramSplitHeader = splitHeader;
        wwwAuthenticateHeader.setStale(Boolean.parseBoolean(splitHeader != null ? getParamValue(splitHeader) : null));
    }
}
