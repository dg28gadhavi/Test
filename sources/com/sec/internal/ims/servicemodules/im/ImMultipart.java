package com.sec.internal.ims.servicemodules.im;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.ims.core.cmc.CmcConstants;
import java.util.Locale;

public class ImMultipart {
    private static final String LOG_TAG = "ImMultipart";
    private String mBody;
    private String mContentType;
    private String mSuggestion;

    public ImMultipart(String str, String str2) {
        String boundary = getBoundary(str2);
        if (boundary == null) {
            this.mBody = str;
            this.mContentType = str2;
            return;
        }
        parseParts(str, boundary);
        Log.i(LOG_TAG, "boundary = " + boundary + ", ContentType = " + this.mContentType);
    }

    public static boolean isMultipart(String str) {
        if (str != null) {
            Locale locale = Locale.US;
            if (str.toLowerCase(locale).contains("multipart/mixed".toLowerCase(locale))) {
                return true;
            }
        }
        return false;
    }

    public String getBody() {
        return this.mBody;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public String getSuggestion() {
        return this.mSuggestion;
    }

    private String getBoundary(String str) {
        int i;
        int indexOf = str.indexOf("boundary=");
        if (indexOf == -1 || (i = indexOf + 9) > str.length()) {
            Log.e(LOG_TAG, "no boundary");
            return null;
        }
        int indexOf2 = str.indexOf(59, i);
        if (indexOf2 == -1) {
            return str.substring(i).replace(CmcConstants.E_NUM_STR_QUOTE, "");
        }
        return str.substring(i, indexOf2).replace(CmcConstants.E_NUM_STR_QUOTE, "");
    }

    private void parseParts(String str, String str2) {
        for (String parsePart : str.split("\r?\n?--" + str2 + "(--)?\r?\n?")) {
            parsePart(parsePart);
        }
    }

    private void parsePart(String str) {
        if (!TextUtils.isEmpty(str)) {
            if (str.startsWith("\n") || str.startsWith("\r\n")) {
                parsePartWithoutHeaders(str);
            } else {
                parsePartWithHeaders(str);
            }
        }
    }

    private void parsePartWithoutHeaders(String str) {
        int indexOf = str.indexOf("\n") + 1;
        if (str.length() > indexOf) {
            this.mBody = str.substring(indexOf);
            this.mContentType = MIMEContentType.PLAIN_TEXT;
        }
    }

    private void parsePartWithHeaders(String str) {
        String[] split = str.split("\r?\n\r?\n", 2);
        String contentType = split.length == 2 ? getContentType(split[0]) : null;
        if (contentType == null) {
            this.mBody = str;
            this.mContentType = MIMEContentType.PLAIN_TEXT;
        } else if (contentType.contains(MIMEContentType.BOT_SUGGESTION)) {
            this.mSuggestion = split[1];
        } else {
            this.mBody = split[1];
            this.mContentType = contentType;
        }
    }

    private String getContentType(String str) {
        for (String split : str.split("\r?\n")) {
            String[] split2 = split.split(": |:", 2);
            if (split2.length == 2 && "content-type".equalsIgnoreCase(split2[0])) {
                return split2[1].trim();
            }
        }
        return null;
    }
}
