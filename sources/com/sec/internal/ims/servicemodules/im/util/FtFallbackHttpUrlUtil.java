package com.sec.internal.ims.servicemodules.im.util;

import android.util.Log;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.core.cmc.CmcConstants;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class FtFallbackHttpUrlUtil {
    private static final String AMPERSAND = "&";
    private static final Pattern FALLBACK_PARAMS_PATTERN = Pattern.compile("\\?[tse]=.*&[tse]=.*&[tse]=.*");
    private static final String LOG_TAG = "FtFallbackHttpUrlUtil";
    private static final String QUERY = "?";

    public static boolean areFallbackParamsPresent(String str) throws NullPointerException {
        Preconditions.checkNotNull(str);
        return FALLBACK_PARAMS_PATTERN.matcher(str).find();
    }

    public static String addFtFallbackParams(String str, long j, String str2, String str3) throws NullPointerException, IllegalArgumentException {
        Preconditions.checkNotNull(str, "fileDataUrl");
        Preconditions.checkArgument(!FALLBACK_PARAMS_PATTERN.matcher(str).find(), "Invalid fileDataUrl format!");
        Preconditions.checkNotNull(str2, "fileContentType");
        Preconditions.checkNotNull(str3, "fileDataUntil");
        return str + QUERY + "t=" + encodeRFC3986(str2) + AMPERSAND + "s=" + j + AMPERSAND + "e=" + str3.replace(CmcConstants.E_NUM_SLOT_SPLIT, "").replace(":", "");
    }

    public static String addDurationFtFallbackParam(String str, int i) throws NullPointerException, IllegalArgumentException {
        Preconditions.checkNotNull(str);
        Preconditions.checkArgument(FALLBACK_PARAMS_PATTERN.matcher(str).find(), "Invalid fileDataUrl format!");
        return str + AMPERSAND + "d=" + i;
    }

    private static String encodeRFC3986(String str) {
        try {
            return URLEncoder.encode(str, "utf-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, e.toString());
            e.printStackTrace();
            return str;
        }
    }
}
