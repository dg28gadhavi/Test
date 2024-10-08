package com.sec.internal.helper.parser;

import com.sec.internal.helper.header.AuthenticationInfoHeader;
import com.sec.internal.log.IMSLog;

public class AuthInfoHeaderParser extends HttpHeaderParser {
    private static final String REGEX_CNONCE = "cnonce[\\s]*=";
    private static final String REGEX_NEXTNONCE = "nextnonce[\\s]*=";
    private static final String REGEX_NONCECOUNT = "nc[\\s]*=";
    private static final String REGEX_RSPAUTH = "rspauth[\\s]*=";
    private static final String TAG = "AuthInfoHeaderParser";
    private String paramSplitHeader = null;

    public AuthenticationInfoHeader parseHeaderValue(String str) {
        AuthenticationInfoHeader authenticationInfoHeader = new AuthenticationInfoHeader();
        parse(authenticationInfoHeader, str);
        IMSLog.d(TAG, "AuthenticationInfoHeader - parseHeaderValue : " + authenticationInfoHeader.toString());
        return authenticationInfoHeader;
    }

    private void parse(AuthenticationInfoHeader authenticationInfoHeader, String str) {
        if (authenticationInfoHeader == null || str == null) {
            throw new IllegalArgumentException("Authentication-Info Header Value is Null");
        }
        setQop(authenticationInfoHeader, str);
        setRspAuth(authenticationInfoHeader, str);
        setCNonce(authenticationInfoHeader, str);
        setNonceCount(authenticationInfoHeader, str);
        setNextNonce(authenticationInfoHeader, str);
        authenticationInfoHeader.toString();
    }

    private void setQop(AuthenticationInfoHeader authenticationInfoHeader, String str) {
        String splitHeader = getSplitHeader("qop[\\s]*=", str);
        this.paramSplitHeader = splitHeader;
        authenticationInfoHeader.setQop(splitHeader != null ? getParamValue(splitHeader) : null);
    }

    private void setRspAuth(AuthenticationInfoHeader authenticationInfoHeader, String str) {
        String splitHeader = getSplitHeader(REGEX_RSPAUTH, str);
        this.paramSplitHeader = splitHeader;
        authenticationInfoHeader.setRspauth(splitHeader != null ? getParamValue(splitHeader) : null);
    }

    private void setCNonce(AuthenticationInfoHeader authenticationInfoHeader, String str) {
        String splitHeader = getSplitHeader(REGEX_CNONCE, str);
        this.paramSplitHeader = splitHeader;
        authenticationInfoHeader.setCnonce(splitHeader != null ? getParamValue(splitHeader) : null);
    }

    private void setNonceCount(AuthenticationInfoHeader authenticationInfoHeader, String str) {
        String splitHeader = getSplitHeader(REGEX_NONCECOUNT, str);
        this.paramSplitHeader = splitHeader;
        authenticationInfoHeader.setCnonce(splitHeader != null ? getParamValue(splitHeader) : null);
    }

    private void setNextNonce(AuthenticationInfoHeader authenticationInfoHeader, String str) {
        String splitHeader = getSplitHeader(REGEX_NEXTNONCE, str);
        this.paramSplitHeader = splitHeader;
        authenticationInfoHeader.setNextNonce(splitHeader != null ? getParamValue(splitHeader) : null);
    }
}
