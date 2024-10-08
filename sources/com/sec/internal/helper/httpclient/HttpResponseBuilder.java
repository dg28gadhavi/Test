package com.sec.internal.helper.httpclient;

import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import okhttp3.Response;

public class HttpResponseBuilder {
    private static final String TAG = "HttpResponseBuilder";

    public static HttpResponseParams buildResponse(Response response) {
        String str;
        String str2 = null;
        if (response == null) {
            Log.e(TAG, "buildResponse: okhttp response is null");
            return null;
        }
        HttpResponseParams httpResponseParams = new HttpResponseParams();
        HashMap hashMap = new HashMap();
        for (String next : response.headers().names()) {
            hashMap.put(next, response.headers(next));
        }
        httpResponseParams.setStatusCode(response.code());
        httpResponseParams.setHeaders(hashMap);
        try {
            httpResponseParams.setDataBinary(response.body().bytes());
            response.body().close();
            if (isGzipSupported(httpResponseParams)) {
                str = GzipCompressionUtil.decompress(httpResponseParams.getDataBinary());
            } else {
                str = new String(httpResponseParams.getDataBinary());
            }
            str2 = str;
        } catch (IOException unused) {
            Log.e(TAG, "buildResponse: decompression failed, revoke");
        }
        httpResponseParams.setDataString(str2);
        return httpResponseParams;
    }

    private static boolean isGzipSupported(HttpResponseParams httpResponseParams) {
        List<String> contentEncoding = getContentEncoding(httpResponseParams);
        return contentEncoding != null && containsIgnoreCase("gzip", contentEncoding);
    }

    private static List<String> getContentEncoding(HttpResponseParams httpResponseParams) {
        List<String> list = httpResponseParams.getHeaders().get("Content-Encoding");
        if (list != null && !list.isEmpty()) {
            return list;
        }
        Log.d(TAG, "getContentEncoding: no content encoding, set to null");
        return null;
    }

    private static boolean containsIgnoreCase(String str, List<String> list) {
        for (String equalsIgnoreCase : list) {
            if (equalsIgnoreCase.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }
}
