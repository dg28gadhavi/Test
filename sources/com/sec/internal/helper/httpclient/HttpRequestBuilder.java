package com.sec.internal.helper.httpclient;

import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequestBuilder {
    private static final String TAG = "HttpRequestBuilder";

    public static Request buildRequest(HttpRequestParams httpRequestParams) {
        Request.Builder builder;
        HttpQueryParams queryParams = httpRequestParams.getQueryParams();
        HttpUrl parse = HttpUrl.parse(httpRequestParams.getUrl());
        if (queryParams == null || parse == null) {
            builder = new Request.Builder().url(httpRequestParams.getUrl());
        } else {
            try {
                HttpUrl.Builder newBuilder = parse.newBuilder();
                LinkedHashMap<String, String> params = queryParams.getParams();
                if (queryParams.isEncoded()) {
                    for (Map.Entry next : params.entrySet()) {
                        newBuilder.addEncodedQueryParameter((String) next.getKey(), (String) next.getValue());
                    }
                } else {
                    for (Map.Entry next2 : params.entrySet()) {
                        newBuilder.addQueryParameter((String) next2.getKey(), (String) next2.getValue());
                    }
                }
                builder = new Request.Builder().url(newBuilder.build());
            } catch (IllegalArgumentException unused) {
                Log.e(TAG, "URL is wrong");
                return null;
            }
        }
        buildRequestHeader(httpRequestParams, builder);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[httpRequestParams.getMethod().ordinal()];
        if (i == 1) {
            return builder.build();
        }
        if (i == 2) {
            return buildDeleteRequest(httpRequestParams, builder);
        }
        if (i == 3 || i == 4) {
            return buildPostOrPutRequest(httpRequestParams, builder);
        }
        if (i != 5) {
            return null;
        }
        builder.head();
        return builder.build();
    }

    /* renamed from: com.sec.internal.helper.httpclient.HttpRequestBuilder$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.helper.httpclient.HttpRequestParams$Method[] r0 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method = r0
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.GET     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.DELETE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.POST     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.PUT     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.helper.httpclient.HttpRequestParams$Method r1 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.HEAD     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.HttpRequestBuilder.AnonymousClass1.<clinit>():void");
        }
    }

    private static void buildRequestHeader(HttpRequestParams httpRequestParams, Request.Builder builder) {
        Map<String, String> headers = httpRequestParams.getHeaders();
        if (headers != null) {
            for (Map.Entry next : headers.entrySet()) {
                if (!(next == null || next.getKey() == null || next.getValue() == null)) {
                    builder.header((String) next.getKey(), (String) next.getValue());
                }
            }
        }
    }

    private static Request buildDeleteRequest(HttpRequestParams httpRequestParams, Request.Builder builder) {
        if (httpRequestParams.getPostBody() == null) {
            Log.d(TAG, "buildDeleteRequest: delete all");
            builder.delete();
            return builder.build();
        }
        builder.delete(buildRequestBody(httpRequestParams));
        return builder.build();
    }

    private static Request buildPostOrPutRequest(HttpRequestParams httpRequestParams, Request.Builder builder) {
        RequestBody requestBody;
        String str;
        String contentType = getContentType(httpRequestParams);
        String str2 = TAG;
        Log.d(str2, "buildPostOrPutRequest: " + contentType);
        if (httpRequestParams.getPostBody() == null) {
            builder.method(httpRequestParams.getMethod().name(), RequestBody.create("", MediaType.parse(contentType)));
        } else if (isContentMatching(contentType, HttpPostBody.CONTENT_TYPE_MULTIPART)) {
            int indexOf = contentType.indexOf("boundary=");
            int i = indexOf + 9;
            if (indexOf != -1) {
                str = contentType.substring(i).trim();
                Log.d(str2, "boundary:" + str);
            } else {
                str = null;
            }
            MultipartBody.Builder builder2 = new MultipartBody.Builder();
            if (str != null) {
                builder2 = new MultipartBody.Builder(str);
            }
            if (setMultipartType(builder2, contentType)) {
                buildMultipart(builder2, httpRequestParams.getPostBody().getMultiparts());
                builder.method(httpRequestParams.getMethod().name(), builder2.build());
            }
        } else if (isContentMatching(contentType, "application/x-www-form-urlencoded")) {
            if (httpRequestParams.getPostBody().getJSONBody() != null) {
                requestBody = buildFormEncodingBody(httpRequestParams);
            } else {
                requestBody = buildRequestBody(httpRequestParams);
            }
            builder.method(httpRequestParams.getMethod().name(), requestBody);
        } else {
            MediaType parse = MediaType.parse(contentType);
            if (httpRequestParams.getPostBody().getFile() != null) {
                builder.method(httpRequestParams.getMethod().name(), RequestBody.create(httpRequestParams.getPostBody().getFile(), parse));
            } else if (httpRequestParams.getPostBody().getBody() != null) {
                builder.method(httpRequestParams.getMethod().name(), buildRequestBody(httpRequestParams));
            } else {
                builder.method(httpRequestParams.getMethod().name(), RequestBody.create(httpRequestParams.getPostBody().getData(), parse));
            }
        }
        return builder.build();
    }

    private static RequestBody buildFormEncodingBody(HttpRequestParams httpRequestParams) {
        String str;
        JSONException e;
        FormBody.Builder builder = new FormBody.Builder();
        JSONObject jSONBody = httpRequestParams.getPostBody().getJSONBody();
        Iterator<String> keys = jSONBody.keys();
        String str2 = null;
        while (keys.hasNext()) {
            try {
                str = keys.next();
                try {
                    builder.add(str, jSONBody.getString(str));
                } catch (JSONException e2) {
                    e = e2;
                }
            } catch (JSONException e3) {
                JSONException jSONException = e3;
                str = str2;
                e = jSONException;
                String str3 = TAG;
                Log.e(str3, "buildFormEncodingBody: failed to load value " + str);
                e.printStackTrace();
                str2 = str;
            }
            str2 = str;
        }
        return builder.build();
    }

    private static void buildMultipart(MultipartBody.Builder builder, List<HttpPostBody> list) {
        if (list == null || list.size() <= 0) {
            Log.e(TAG, "buildMultipart: list is empty");
            return;
        }
        for (HttpPostBody next : list) {
            Headers.Builder builder2 = new Headers.Builder();
            if (next.getMultiparts() == null || next.getMultiparts().size() <= 0) {
                HashMap hashMap = new HashMap();
                hashMap.put(HttpController.HEADER_CONTENT_DISPOSITION, next.getContentDisposition());
                if (!TextUtils.isEmpty(next.getContentTransferEncoding())) {
                    hashMap.put(HttpController.HEADER_CONTENT_TRANSFER_ENCODING, next.getContentTransferEncoding());
                }
                if (!TextUtils.isEmpty(next.getFileIcon())) {
                    hashMap.put(HttpController.HEADER_FILE_ICON, next.getFileIcon());
                }
                if (!TextUtils.isEmpty(next.getContentId())) {
                    hashMap.put(HttpController.HEADER_CONTENT_ID, next.getContentId());
                }
                for (Map.Entry entry : hashMap.entrySet()) {
                    if (entry.getKey() == HttpController.HEADER_CONTENT_DISPOSITION || entry.getKey() == HttpController.HEADER_CONTENT_ID) {
                        builder2.addUnsafeNonAscii((String) entry.getKey(), (String) entry.getValue());
                    } else {
                        builder2.add((String) entry.getKey(), (String) entry.getValue());
                    }
                }
                Headers build = builder2.build();
                if (next.getFile() != null) {
                    builder.addPart(build, RequestBody.create(next.getFile(), MediaType.parse(next.getContentType())));
                } else if (next.getBody() != null) {
                    builder.addPart(build, RequestBody.create(next.getBody(), MediaType.parse(next.getContentType())));
                } else if (next.getData() != null) {
                    builder.addPart(build, RequestBody.create(next.getData(), MediaType.parse(next.getContentType())));
                } else {
                    Log.e(TAG, "buildMultipart: body, file and data are null.");
                }
            } else {
                MultipartBody.Builder builder3 = new MultipartBody.Builder();
                if (setMultipartType(builder3, next.getContentType())) {
                    buildMultipart(builder3, next.getMultiparts());
                    builder2.addUnsafeNonAscii(HttpController.HEADER_CONTENT_DISPOSITION, next.getContentDisposition());
                    builder.addPart(builder2.build(), builder3.build());
                }
            }
        }
    }

    private static RequestBody buildRequestBody(HttpRequestParams httpRequestParams) {
        RequestBody create;
        MediaType parse = MediaType.parse(getContentType(httpRequestParams));
        String body = httpRequestParams.getPostBody().getBody();
        byte[] data = httpRequestParams.getPostBody().getData();
        if (isGzipSupported(httpRequestParams)) {
            if (data != null) {
                try {
                    create = RequestBody.create(GzipCompressionUtil.compress(data), parse);
                } catch (IOException unused) {
                    Log.e(TAG, "buildRequestBody: body compression failed");
                    return null;
                }
            } else if (body != null) {
                create = RequestBody.create(GzipCompressionUtil.compress(body), parse);
            } else {
                Log.e(TAG, "buildRequestBody: body construction failed");
                return null;
            }
            return create;
        } else if (data != null) {
            return RequestBody.create(data, parse);
        } else {
            if (body != null) {
                return RequestBody.create(body, parse);
            }
            Log.e(TAG, "buildRequestBody: body compression failed");
            return null;
        }
    }

    private static boolean setMultipartType(MultipartBody.Builder builder, String str) {
        if (str.contains(HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA)) {
            builder.setType(MultipartBody.FORM);
        } else if (str.contains("multipart/mixed")) {
            builder.setType(MultipartBody.MIXED);
        } else {
            Log.e(TAG, "setMultipartType: wrong content-type, should be multipart.");
            return false;
        }
        return true;
    }

    private static String getContentType(HttpRequestParams httpRequestParams) {
        String str = httpRequestParams.getHeaders().get("Content-Type");
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        Log.d(TAG, "getContentType: no content type, set to default");
        return HttpPostBody.CONTENT_TYPE_DEFAULT;
    }

    private static String getContentEncoding(HttpRequestParams httpRequestParams) {
        String str = httpRequestParams.getHeaders().get("Content-Encoding");
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        Log.d(TAG, "getContentEncoding: no content encoding, set to null");
        return null;
    }

    private static boolean isContentMatching(String str, String str2) {
        return Pattern.compile(Pattern.quote(str2), 2).matcher(str).find();
    }

    private static boolean isGzipSupported(HttpRequestParams httpRequestParams) {
        String contentEncoding = getContentEncoding(httpRequestParams);
        return contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip");
    }
}
