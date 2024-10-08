package com.sec.internal.helper.httpclient;

import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Map;

public class HttpResponseParams {
    private byte[] mCipherSuite = {0, 47};
    private byte[] mDataBinary = null;
    private String mDataString = null;
    private Map<String, List<String>> mHeaders = null;
    private int mStatusCode = -1;
    private String mStatusReason = null;

    public int getStatusCode() {
        return this.mStatusCode;
    }

    public void setStatusCode(int i) {
        this.mStatusCode = i;
    }

    public Map<String, List<String>> getHeaders() {
        return this.mHeaders;
    }

    public void setHeaders(Map<String, List<String>> map) {
        this.mHeaders = map;
    }

    public String getDataString() {
        return this.mDataString;
    }

    public void setDataString(String str) {
        this.mDataString = str;
    }

    public byte[] getDataBinary() {
        return this.mDataBinary;
    }

    public void setStatusReason(String str) {
        this.mStatusReason = str;
    }

    public String getStatusReason() {
        return this.mStatusReason;
    }

    public void setDataBinary(byte[] bArr) {
        this.mDataBinary = bArr;
    }

    public void setCipherSuite(byte[] bArr) {
        this.mCipherSuite = bArr;
    }

    public byte[] getCipherSuite() {
        return this.mCipherSuite;
    }

    public StringBuffer headerToString(Map<String, List<String>> map) {
        StringBuffer stringBuffer = new StringBuffer();
        if (map != null) {
            for (Map.Entry next : map.entrySet()) {
                stringBuffer.append("\r\n        " + ((String) next.getKey()) + " : " + next.getValue());
            }
        }
        return stringBuffer;
    }

    public boolean isDebugLog() {
        if (!IMSLog.isShipBuild()) {
            return true;
        }
        int i = this.mStatusCode;
        return i >= 300 && i != 401;
    }

    public String toStringWoPayload() {
        String str;
        StringBuffer headerToString = headerToString(this.mHeaders);
        StringBuilder sb = new StringBuilder();
        sb.append("HttpResponseParams[\r\n    mStatusCode=");
        sb.append(this.mStatusCode);
        sb.append("\r\n");
        if (isDebugLog()) {
            str = "    mHeaders=" + headerToString;
        } else {
            str = "";
        }
        sb.append(str);
        sb.append("]");
        return sb.toString();
    }

    public String toString() {
        String str;
        String str2 = "";
        StringBuffer headerToString = headerToString(this.mHeaders);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("HttpResponseParams[\r\n    mStatusCode=");
            sb.append(this.mStatusCode);
            sb.append("\r\n");
            if (isDebugLog()) {
                str = "    mHeaders=" + headerToString + "\r\n    mDataString=" + IMSLog.numberChecker(this.mDataString);
            } else {
                str = str2;
            }
            sb.append(str);
            sb.append("]");
            return sb.toString();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("HttpResponseParams[\r\n    mStatusCode=");
            sb2.append(this.mStatusCode);
            sb2.append("\r\n");
            if (isDebugLog()) {
                str2 = "    mHeaders=" + headerToString + "\r\n    mDataString length=" + this.mDataString.length();
            }
            sb2.append(str2);
            sb2.append("]");
            return sb2.toString();
        }
    }
}
