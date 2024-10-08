package com.sec.internal.helper.header;

public class WwwAuthenticateHeader extends AuthenticationHeaders {
    public static final String HEADER_NAME = "WWW-Authenticate";
    public static final String HEADER_PARAM_ALGORITHM = "algorithm";
    public static final String HEADER_PARAM_BASIC_SCHEME = "Basic";
    public static final String HEADER_PARAM_DIGEST_SCHEME = "Digest";
    public static final String HEADER_PARAM_NONCE = "nonce";
    public static final String HEADER_PARAM_OPAQUE = "opaque";
    public static final String HEADER_PARAM_REALM = "realm";
    public static final String HEADER_PARAM_STALE = "stale";
    public static final String HEADER_PARAM_UNKNOWN_SCHEME = "Unknown";
    private String algorithm = null;
    private String nonce = null;
    private String opaque = null;
    private String qop = null;
    private String realm = null;
    private String scheme = null;
    private boolean stale = false;

    public void setScheme(String str) {
        this.scheme = str;
    }

    public String getRealm() {
        return this.realm;
    }

    public void setRealm(String str) {
        this.realm = str;
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String str) {
        this.nonce = str;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(String str) {
        this.algorithm = str;
    }

    public String getQop() {
        return this.qop;
    }

    public void setQop(String str) {
        this.qop = str;
    }

    public String getOpaque() {
        return this.opaque;
    }

    public void setOpaque(String str) {
        this.opaque = str;
    }

    public boolean isStale() {
        return this.stale;
    }

    public void setStale(boolean z) {
        this.stale = z;
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        StringBuilder sb = new StringBuilder();
        sb.append("WwwAuthenticateHeader [");
        String str6 = "";
        if (this.scheme != null) {
            str = "scheme=" + this.scheme + ", ";
        } else {
            str = str6;
        }
        sb.append(str);
        if (this.realm != null) {
            str2 = "realm=" + this.realm + ", ";
        } else {
            str2 = str6;
        }
        sb.append(str2);
        if (this.nonce != null) {
            str3 = "nonce=" + this.nonce + ", ";
        } else {
            str3 = str6;
        }
        sb.append(str3);
        if (this.algorithm != null) {
            str4 = "algorithm=" + this.algorithm + ", ";
        } else {
            str4 = str6;
        }
        sb.append(str4);
        if (this.qop != null) {
            str5 = "qop=" + this.qop + ", ";
        } else {
            str5 = str6;
        }
        sb.append(str5);
        if (this.opaque != null) {
            str6 = "opaque=" + this.opaque + ", ";
        }
        sb.append(str6);
        sb.append("stale=");
        sb.append(this.stale);
        sb.append("]");
        return sb.toString();
    }
}
