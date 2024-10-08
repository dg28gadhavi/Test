package com.sec.internal.helper.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HttpHeaderParser {
    private static final String COMMA = ",";
    private static final String QUOTE = "\"";
    protected static final String REGEX_ALGORITHM = "algorithm[\\s]*=";
    protected static final String REGEX_NONCE = "nonce[\\s]*=";
    protected static final String REGEX_OPAQUE = "opaque[\\s]*=";
    protected static final String REGEX_QOP = "qop[\\s]*=";
    protected static final String REGEX_REALM = "realm[\\s]*=";
    protected static final String REGEX_STALE = "stale[\\s]*=";
    private static final String SPACE_REGEX = "[\\s]*";
    protected static final String SPACE_SEPERATOR_REGEX = "[\\s]*=";
    private Matcher paramMatcher = null;
    private Pattern paramPattern = null;

    /* access modifiers changed from: protected */
    public String getParamValue(String str) {
        if (str != null) {
            if (str.startsWith("\"")) {
                int indexOf = str.indexOf("\"", 1);
                if (indexOf >= 0) {
                    str = str.substring(1, indexOf);
                } else {
                    throw new IllegalArgumentException("HTTP Header Value is invalid - missing closing quote");
                }
            }
            if (str.contains(COMMA)) {
                str = str.substring(0, str.indexOf(COMMA));
            }
            if (!str.contains("\"")) {
                return str;
            }
            throw new IllegalArgumentException("HTTP Header Value is invalid - missing opening quote");
        }
        throw new IllegalArgumentException("splitHeader cannot be null");
    }

    /* access modifiers changed from: protected */
    public String getSplitHeader(String str, String str2) {
        Pattern compile = Pattern.compile(str, 2);
        this.paramPattern = compile;
        Matcher matcher = compile.matcher(str2);
        this.paramMatcher = matcher;
        if (matcher.find()) {
            return str2.substring(this.paramMatcher.end()).trim();
        }
        return null;
    }
}
