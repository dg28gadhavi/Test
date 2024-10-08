package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.ims.servicemodules.im.UploadFileTask;
import com.sec.internal.ims.servicemodules.im.data.info.FtHttpResumeInfo;
import com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.OpenIdAuth;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.net.URL;
import org.xmlpull.v1.XmlPullParserException;

public class UploadResumeFileTask extends UploadFileTask {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "UploadResumeFileTask";

    public UploadResumeFileTask(int i, Context context, Looper looper, UploadFileTask.UploadRequest uploadRequest) {
        super(i, context, looper, uploadRequest);
        String str = LOG_TAG;
        Log.i(str, "phoneId: " + i);
    }

    private String getRequestUrl() {
        Uri parse = Uri.parse(this.mRequest.mUrl);
        if (parse.getPath() == null || parse.getPath().equals("/")) {
            if (this.mRequest.mUrl.endsWith("/")) {
                return this.mRequest.mUrl;
            }
            return this.mRequest.mUrl + "/";
        } else if (!this.mRequest.mUrl.endsWith("/")) {
            return this.mRequest.mUrl;
        } else {
            String str = this.mRequest.mUrl;
            return str.substring(0, str.length() - 1);
        }
    }

    private FtHttpResumeInfo getUploadInfo() {
        HttpRequest httpRequest;
        String str = "?tid=" + this.mRequest.mTid + "&get_upload_info";
        this.mRequest.mUrl = getRequestUrl();
        String str2 = LOG_TAG;
        Log.i(str2, "getUploadInfo: params=" + str);
        this.mHttpRequest = null;
        try {
            this.mHttpRequest = HttpRequest.get(this.mRequest.mUrl + str);
            setDefaultHeaders();
            HttpRequest httpRequest2 = this.mHttpRequest;
            if (httpRequest2 == null) {
                Log.e(str2, "mHttpRequest is null");
                cancelRequest(CancelReason.ERROR, 3, -1, true);
                HttpRequest httpRequest3 = this.mHttpRequest;
                if (httpRequest3 != null) {
                    httpRequest3.disconnect();
                }
                return null;
            }
            int code = httpRequest2.code();
            if (code == 200) {
                Log.i(str2, "Receive 200 OK");
            } else if (code == 302) {
                String header = this.mHttpRequest.header("Location");
                if (!TextUtils.isEmpty(header)) {
                    int i = this.mPhoneId;
                    UploadFileTask.UploadRequest uploadRequest = this.mRequest;
                    String sendAuthRequest = OpenIdAuth.sendAuthRequest(new OpenIdAuth.OpenIdRequest(i, header, uploadRequest.mNetwork, uploadRequest.mUserAgent, uploadRequest.mTrustAllCerts));
                    if (sendAuthRequest != null) {
                        this.mHttpRequest.disconnect();
                        this.mHttpRequest = HttpRequest.get(sendAuthRequest);
                        setDefaultHeaders();
                        this.mHttpRequest.chunk(0);
                        code = this.mHttpRequest.code();
                    }
                }
                Log.e(str2, "getUploadInfo: openId process failed");
                cancelRequest(this.mHttpRequest, true);
                HttpRequest httpRequest4 = this.mHttpRequest;
                if (httpRequest4 == null) {
                    return null;
                }
                httpRequest4.disconnect();
                return null;
            } else if (code == 401) {
                String authorizationHeader = getAuthorizationHeader(this.mPhoneId, this.mHttpRequest, this.mRequest.mUrl + str, "GET");
                if (!TextUtils.isEmpty(authorizationHeader)) {
                    this.mHttpRequest.disconnect();
                    this.mHttpRequest = HttpRequest.get(this.mRequest.mUrl + str);
                    setDefaultHeaders();
                    this.mHttpRequest.authorization(authorizationHeader).chunk(0);
                    HttpRequest httpRequest5 = this.mHttpRequest;
                    if (httpRequest5 == null) {
                        Log.e(str2, "mHttpRequest is null");
                        cancelRequest(CancelReason.ERROR, 3, -1, true);
                        HttpRequest httpRequest6 = this.mHttpRequest;
                        if (httpRequest6 == null) {
                            return null;
                        }
                        httpRequest6.disconnect();
                        return null;
                    }
                    code = httpRequest5.code();
                }
            } else if (code != 503) {
                Log.e(str2, "getUploadInfo: Receive " + this.mHttpRequest.code() + " " + this.mHttpRequest.message());
                cancelRequest(this.mHttpRequest, true);
                HttpRequest httpRequest7 = this.mHttpRequest;
                if (httpRequest7 != null) {
                    httpRequest7.disconnect();
                }
                return null;
            } else {
                Log.e(str2, "Receive 503 Unavailable");
                if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FTHTTP_UPLOAD_RESUME_FROM_THE_START)) {
                    cancelRequest(CancelReason.ERROR, 3, code, true);
                } else {
                    cancelRequest(this.mHttpRequest, false);
                }
                HttpRequest httpRequest8 = this.mHttpRequest;
                if (httpRequest8 != null) {
                    httpRequest8.disconnect();
                }
                return null;
            }
            if (200 == code) {
                Log.i(str2, "getUploadInfo: Success");
                String body = this.mHttpRequest.body();
                this.mHttpRequest.disconnect();
                IMSLog.s(str2, "getUploadInfo: Received. body=" + body);
                FtHttpResumeInfo parseResume = FtHttpXmlParser.parseResume(body);
                HttpRequest httpRequest9 = this.mHttpRequest;
                if (httpRequest9 != null) {
                    httpRequest9.disconnect();
                }
                return parseResume;
            }
            Log.e(str2, "getUploadInfo: Failed, Receive " + this.mHttpRequest.code() + " " + this.mHttpRequest.message());
            if (503 == code) {
                if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FTHTTP_UPLOAD_RESUME_FROM_THE_START)) {
                    cancelRequest(CancelReason.ERROR, 3, code, true);
                } else {
                    cancelRequest(this.mHttpRequest, false);
                }
                HttpRequest httpRequest10 = this.mHttpRequest;
                if (httpRequest10 == null) {
                    return null;
                }
                httpRequest10.disconnect();
                return null;
            }
            cancelRequest(this.mHttpRequest, true);
            httpRequest = this.mHttpRequest;
            if (httpRequest == null) {
                return null;
            }
            httpRequest.disconnect();
            return null;
        } catch (HttpRequest.HttpRequestException e) {
            HttpRequest.HttpRequestException httpRequestException = e;
            httpRequestException.printStackTrace();
            if (isPermanentFailCause(httpRequestException)) {
                cancelRequest(CancelReason.ERROR, 30, -1, false);
            } else {
                Log.e(LOG_TAG, httpRequestException.getCause() + " happened. Retry Upload Task.");
                cancelRequest(CancelReason.ERROR, 3, -1, false);
            }
            httpRequest = this.mHttpRequest;
            if (httpRequest == null) {
                return null;
            }
        } catch (IllegalArgumentException | NullPointerException | OutOfMemoryError e2) {
            e2.printStackTrace();
            cancelRequest(CancelReason.ERROR, -1, -1, false);
            httpRequest = this.mHttpRequest;
            if (httpRequest == null) {
                return null;
            }
        } catch (IOException | XmlPullParserException e3) {
            e3.printStackTrace();
            cancelRequest(CancelReason.ERROR, 3, -1, true);
            httpRequest = this.mHttpRequest;
            if (httpRequest == null) {
                return null;
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            HttpRequest httpRequest11 = this.mHttpRequest;
            if (httpRequest11 != null) {
                httpRequest11.disconnect();
            }
            throw th2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:120:0x0275, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x0276, code lost:
        r11 = r8;
        r15 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x0279, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x027a, code lost:
        r14 = " happened. Retry Upload Task.";
        r11 = r8;
        r15 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x027e, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x027f, code lost:
        r11 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0299, code lost:
        if (r0 == null) goto L_0x02e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x029b, code lost:
        r0.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x02ac, code lost:
        cancelRequest(com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR, 30, -1, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x02b5, code lost:
        android.util.Log.e(LOG_TAG, r0.getCause() + r14);
        cancelRequest(com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR, 3, -1, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x02df, code lost:
        if (r0 == null) goto L_0x02e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x02e2, code lost:
        return r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x02f1, code lost:
        r1.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x02fa, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x02fb, code lost:
        r15 = false;
        r2 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x02fe, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x02ff, code lost:
        r15 = false;
        r7 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x0314, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x0315, code lost:
        r2 = -1;
        r15 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x0320, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x0321, code lost:
        r14 = " happened. Retry Upload Task.";
        r15 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:179:0x0325, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x0326, code lost:
        r15 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x0355, code lost:
        cancelRequest(com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR, 30, r2, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x035d, code lost:
        android.util.Log.e(LOG_TAG, r0.getCause() + r14);
        cancelRequest(com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR, 3, r2, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01a4, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01a5, code lost:
        r7 = r8;
        r15 = false;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:26:0x00e8, B:46:0x0132] */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x027e A[ExcHandler: all (th java.lang.Throwable), Splitter:B:74:0x01ab] */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x02ac A[Catch:{ all -> 0x02e3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:143:0x02b5 A[Catch:{ all -> 0x02e3 }] */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x02f1  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x02fa A[ExcHandler: FileNotFoundException | NullPointerException (e java.lang.Throwable), Splitter:B:51:0x014e] */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x02fe A[ExcHandler: IOException (e java.io.IOException), Splitter:B:49:0x014c] */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x0314 A[ExcHandler: FileNotFoundException | NullPointerException (e java.lang.Throwable), Splitter:B:49:0x014c] */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x0325 A[ExcHandler: IllegalArgumentException (e java.lang.IllegalArgumentException), Splitter:B:26:0x00e8] */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0355  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x035d  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:139:0x02a3=Splitter:B:139:0x02a3, B:128:0x0284=Splitter:B:128:0x0284} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean doResumeFile(java.net.URL r25, long r26, long r28, long r30) {
        /*
            r24 = this;
            r1 = r24
            r9 = r26
            r11 = r28
            java.lang.String r13 = " IOException happened."
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "doResumeFile: "
            r2.append(r3)
            r2.append(r9)
            java.lang.String r3 = " - "
            r2.append(r3)
            r2.append(r11)
            java.lang.String r3 = " / "
            r2.append(r3)
            r14 = r30
            r2.append(r14)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r0, r2)
            r7 = 0
            r1.mHttpRequest = r7
            com.sec.internal.helper.HttpRequest r2 = com.sec.internal.helper.HttpRequest.put((java.net.URL) r25)
            r1.mHttpRequest = r2
            r24.setDefaultHeaders()
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest
            r3 = r26
            r5 = r28
            r16 = r7
            r7 = r30
            r2.contentRange(r3, r5, r7)
            r8 = 0
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ Exception -> 0x037c }
            int r2 = r2.code()     // Catch:{ Exception -> 0x037c }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Receive "
            r3.append(r4)
            r3.append(r2)
            java.lang.String r5 = " "
            r3.append(r5)
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest
            java.lang.String r4 = r4.message()
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r0, r3)
            r3 = 200(0xc8, float:2.8E-43)
            java.lang.String r7 = " happened. Retry Upload Task."
            if (r2 == r3) goto L_0x012e
            r3 = 302(0x12e, float:4.23E-43)
            if (r2 == r3) goto L_0x00d2
            r3 = 401(0x191, float:5.62E-43)
            if (r2 == r3) goto L_0x009f
            r0 = 404(0x194, float:5.66E-43)
            if (r2 == r0) goto L_0x0093
            r0 = 410(0x19a, float:5.75E-43)
            if (r2 == r0) goto L_0x0093
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            r0.disconnect()     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            r1.cancelRequest(r0, r8)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            return r8
        L_0x0093:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            r0.disconnect()     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            r3 = 1
            r1.cancelRequest(r0, r3)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            return r8
        L_0x009f:
            r3 = 1
            int r2 = r1.mPhoneId     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            com.sec.internal.helper.HttpRequest r3 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            java.lang.String r4 = r25.toString()     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            java.lang.String r6 = "PUT"
            java.lang.String r2 = r1.getAuthorizationHeader(r2, r3, r4, r6)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            com.sec.internal.helper.HttpRequest r3 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            r3.disconnect()     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            boolean r3 = android.text.TextUtils.isEmpty(r2)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            if (r3 == 0) goto L_0x00bf
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            r1.cancelRequest(r0, r8)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            return r8
        L_0x00bf:
            com.sec.internal.helper.HttpRequest r3 = com.sec.internal.helper.HttpRequest.put((java.net.URL) r25)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r4 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            android.net.Network r4 = r4.mNetwork     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            com.sec.internal.helper.HttpRequest r3 = r3.useNetwork(r4)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            com.sec.internal.helper.HttpRequest r2 = r3.authorization(r2)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            r1.mHttpRequest = r2     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            goto L_0x0134
        L_0x00d2:
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            java.lang.String r3 = "Location"
            java.lang.String r20 = r2.header(r3)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            boolean r2 = android.text.TextUtils.isEmpty(r20)     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            if (r2 != 0) goto L_0x010b
            com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest r2 = new com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            int r3 = r1.mPhoneId     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r4 = r1.mRequest     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            android.net.Network r6 = r4.mNetwork     // Catch:{ HttpRequestException -> 0x0128, IllegalArgumentException -> 0x0124 }
            java.lang.String r8 = r4.mUserAgent     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            boolean r4 = r4.mTrustAllCerts     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            r18 = r2
            r19 = r3
            r21 = r6
            r22 = r8
            r23 = r4
            r18.<init>(r19, r20, r21, r22, r23)     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            java.lang.String r2 = com.sec.internal.ims.util.OpenIdAuth.sendAuthRequest(r2)     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            if (r2 == 0) goto L_0x010b
            com.sec.internal.helper.HttpRequest r3 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            r3.disconnect()     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            com.sec.internal.helper.HttpRequest r2 = com.sec.internal.helper.HttpRequest.put((java.lang.CharSequence) r2)     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            r1.mHttpRequest = r2     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            goto L_0x0134
        L_0x010b:
            java.lang.String r2 = "doResumeFile: OpenId process failed!"
            android.util.Log.e(r0, r2)     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            r0.disconnect()     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            r2 = 0
            r1.cancelRequest(r0, r2)     // Catch:{ HttpRequestException -> 0x0120, IllegalArgumentException -> 0x011c }
            return r2
        L_0x011c:
            r0 = move-exception
            r15 = r2
            goto L_0x0327
        L_0x0120:
            r0 = move-exception
            r15 = r2
            r14 = r7
            goto L_0x012b
        L_0x0124:
            r0 = move-exception
            r15 = r8
            goto L_0x0327
        L_0x0128:
            r0 = move-exception
            r14 = r7
            r15 = r8
        L_0x012b:
            r2 = -1
            goto L_0x034c
        L_0x012e:
            com.sec.internal.helper.HttpRequest r2 = com.sec.internal.helper.HttpRequest.put((java.net.URL) r25)     // Catch:{ HttpRequestException -> 0x0348, IllegalArgumentException -> 0x0325 }
            r1.mHttpRequest = r2     // Catch:{ HttpRequestException -> 0x0320, IllegalArgumentException -> 0x0325 }
        L_0x0134:
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r2 = r1.mRequest
            long r2 = r2.mTotalBytes
            r18 = 20
            long r2 = r2 / r18
            r6 = r5
            r4 = 61440(0xf000, double:3.03554E-319)
            long r2 = java.lang.Math.max(r2, r4)
            r4 = 512000(0x7d000, double:2.529616E-318)
            long r2 = java.lang.Math.min(r4, r2)
            int r2 = (int) r2
            java.io.BufferedInputStream r8 = new java.io.BufferedInputStream     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x02fe }
            android.content.Context r3 = r1.mContext     // Catch:{ FileNotFoundException | NullPointerException -> 0x02fa, IOException -> 0x02fe }
            android.content.ContentResolver r3 = r3.getContentResolver()     // Catch:{ FileNotFoundException | NullPointerException -> 0x02fa, IOException -> 0x02fe }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r4 = r1.mRequest     // Catch:{ FileNotFoundException | NullPointerException -> 0x02fa, IOException -> 0x02fe }
            android.net.Uri r4 = r4.mContentUri     // Catch:{ FileNotFoundException | NullPointerException -> 0x02fa, IOException -> 0x02fe }
            java.io.InputStream r3 = r3.openInputStream(r4)     // Catch:{ FileNotFoundException | NullPointerException -> 0x02fa, IOException -> 0x02fe }
            r8.<init>(r3, r2)     // Catch:{ FileNotFoundException | NullPointerException -> 0x02fa, IOException -> 0x02fe }
            long r3 = r8.skip(r9)     // Catch:{ FileNotFoundException | NullPointerException -> 0x02fa, FileNotFoundException | NullPointerException -> 0x02fa, IOException -> 0x02f5 }
            int r5 = (r3 > r9 ? 1 : (r3 == r9 ? 0 : -1))
            if (r5 >= 0) goto L_0x01a9
            r8.close()     // Catch:{ IOException | NullPointerException -> 0x016b }
            goto L_0x016f
        L_0x016b:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
        L_0x016f:
            java.lang.String r0 = LOG_TAG     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            r2.<init>()     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            java.lang.String r5 = "Try to skip "
            r2.append(r5)     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            r2.append(r9)     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            java.lang.String r5 = " bytes. "
            r2.append(r5)     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            r2.append(r3)     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            java.lang.String r3 = " bytes actually skipped"
            r2.append(r3)     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            java.lang.String r2 = r2.toString()     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            android.util.Log.i(r0, r2)     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ FileNotFoundException | NullPointerException -> 0x0314, FileNotFoundException | NullPointerException -> 0x0314, IOException -> 0x01a4 }
            r3 = 0
            r5 = -1
            r1.cancelRequest(r0, r5, r5, r3)     // Catch:{ FileNotFoundException | NullPointerException -> 0x019f, IOException -> 0x019a }
            return r3
        L_0x019a:
            r0 = move-exception
            r15 = r3
            r7 = r8
            goto L_0x0302
        L_0x019f:
            r0 = move-exception
            r15 = r3
            r2 = r5
            goto L_0x0317
        L_0x01a4:
            r0 = move-exception
            r7 = r8
            r15 = 0
            goto L_0x0302
        L_0x01a9:
            r3 = 0
            r5 = -1
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r4 = r1.mRequest     // Catch:{ HttpRequestException -> 0x029f, IllegalStateException -> 0x0281, all -> 0x027e }
            java.lang.String r3 = r4.mContentType     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            if (r3 == 0) goto L_0x01b2
            goto L_0x01bc
        L_0x01b2:
            android.content.Context r3 = r1.mContext     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            java.lang.String r5 = r4.mFileName     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            android.net.Uri r4 = r4.mContentUri     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            java.lang.String r3 = com.sec.internal.helper.FileUtils.getContentType(r3, r5, r4)     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
        L_0x01bc:
            r24.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            com.sec.internal.helper.HttpRequest r4 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            com.sec.internal.helper.HttpRequest r2 = r4.bufferSize(r2)     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            com.sec.internal.helper.HttpRequest r2 = r2.contentType(r3)     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            long r3 = r11 - r9
            r18 = 1
            long r3 = r3 + r18
            java.lang.String r3 = java.lang.Long.toString(r3)     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            com.sec.internal.helper.HttpRequest r2 = r2.contentLength((java.lang.String) r3)     // Catch:{ HttpRequestException -> 0x0279, IllegalStateException -> 0x0275, all -> 0x027e }
            r5 = 1
            r11 = 30
            r12 = 0
            r3 = r26
            r16 = r5
            r12 = r6
            r11 = 3
            r17 = -1
            r5 = r28
            r14 = r7
            r11 = r8
            r15 = 0
            r7 = r30
            com.sec.internal.helper.HttpRequest r2 = r2.contentRange(r3, r5, r7)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            com.sec.internal.ims.servicemodules.im.UploadResumeFileTask$1 r3 = new com.sec.internal.ims.servicemodules.im.UploadResumeFileTask$1     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r3.<init>(r9)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r2.progress(r3)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r2.send((java.io.InputStream) r11)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            boolean r2 = r24.isCancelled()     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            if (r2 == 0) goto L_0x0212
            r11.close()     // Catch:{ IOException -> 0x0205 }
            goto L_0x020a
        L_0x0205:
            java.lang.String r0 = LOG_TAG
            android.util.Log.e(r0, r13)
        L_0x020a:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x0211
            r0.disconnect()
        L_0x0211:
            return r15
        L_0x0212:
            java.lang.String r2 = "Upload file done. Read http response."
            android.util.Log.i(r0, r2)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            com.sec.internal.helper.HttpRequest r2 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            boolean r2 = r2.ok()     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            if (r2 != 0) goto L_0x025b
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r2.<init>()     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            java.lang.String r3 = "doResumeFile: Failed, "
            r2.append(r3)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            com.sec.internal.helper.HttpRequest r3 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            int r3 = r3.code()     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r2.append(r3)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r2.append(r12)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            com.sec.internal.helper.HttpRequest r3 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            java.lang.String r3 = r3.message()     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r2.append(r3)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            java.lang.String r2 = r2.toString()     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            android.util.Log.e(r0, r2)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r1.cancelRequest(r0, r15)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r11.close()     // Catch:{ IOException -> 0x024e }
            goto L_0x0253
        L_0x024e:
            java.lang.String r0 = LOG_TAG
            android.util.Log.e(r0, r13)
        L_0x0253:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x025a
            r0.disconnect()
        L_0x025a:
            return r15
        L_0x025b:
            java.lang.String r2 = "doResumeFile: Success"
            android.util.Log.i(r0, r2)     // Catch:{ HttpRequestException -> 0x0273, IllegalStateException -> 0x0271 }
            r11.close()     // Catch:{ IOException -> 0x0264 }
            goto L_0x0269
        L_0x0264:
            java.lang.String r0 = LOG_TAG
            android.util.Log.e(r0, r13)
        L_0x0269:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x0270
            r0.disconnect()
        L_0x0270:
            return r16
        L_0x0271:
            r0 = move-exception
            goto L_0x0284
        L_0x0273:
            r0 = move-exception
            goto L_0x02a3
        L_0x0275:
            r0 = move-exception
            r11 = r8
            r15 = 0
            goto L_0x0284
        L_0x0279:
            r0 = move-exception
            r14 = r7
            r11 = r8
            r15 = 0
            goto L_0x02a3
        L_0x027e:
            r0 = move-exception
            r11 = r8
            goto L_0x02e4
        L_0x0281:
            r0 = move-exception
            r15 = r3
            r11 = r8
        L_0x0284:
            r0.printStackTrace()     // Catch:{ all -> 0x02e3 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x02e3 }
            r2 = 3
            r3 = -1
            r1.cancelRequest(r0, r2, r3, r15)     // Catch:{ all -> 0x02e3 }
            r11.close()     // Catch:{ IOException -> 0x0292 }
            goto L_0x0297
        L_0x0292:
            java.lang.String r0 = LOG_TAG
            android.util.Log.e(r0, r13)
        L_0x0297:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x02e2
        L_0x029b:
            r0.disconnect()
            goto L_0x02e2
        L_0x029f:
            r0 = move-exception
            r15 = r3
            r14 = r7
            r11 = r8
        L_0x02a3:
            r0.printStackTrace()     // Catch:{ all -> 0x02e3 }
            boolean r2 = r1.isPermanentFailCause(r0)     // Catch:{ all -> 0x02e3 }
            if (r2 == 0) goto L_0x02b5
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x02e3 }
            r2 = 30
            r3 = -1
            r1.cancelRequest(r0, r2, r3, r15)     // Catch:{ all -> 0x02e3 }
            goto L_0x02d4
        L_0x02b5:
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x02e3 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x02e3 }
            r3.<init>()     // Catch:{ all -> 0x02e3 }
            java.io.IOException r0 = r0.getCause()     // Catch:{ all -> 0x02e3 }
            r3.append(r0)     // Catch:{ all -> 0x02e3 }
            r3.append(r14)     // Catch:{ all -> 0x02e3 }
            java.lang.String r0 = r3.toString()     // Catch:{ all -> 0x02e3 }
            android.util.Log.e(r2, r0)     // Catch:{ all -> 0x02e3 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x02e3 }
            r2 = 3
            r3 = -1
            r1.cancelRequest(r0, r2, r3, r15)     // Catch:{ all -> 0x02e3 }
        L_0x02d4:
            r11.close()     // Catch:{ IOException -> 0x02d8 }
            goto L_0x02dd
        L_0x02d8:
            java.lang.String r0 = LOG_TAG
            android.util.Log.e(r0, r13)
        L_0x02dd:
            com.sec.internal.helper.HttpRequest r0 = r1.mHttpRequest
            if (r0 == 0) goto L_0x02e2
            goto L_0x029b
        L_0x02e2:
            return r15
        L_0x02e3:
            r0 = move-exception
        L_0x02e4:
            r11.close()     // Catch:{ IOException -> 0x02e8 }
            goto L_0x02ed
        L_0x02e8:
            java.lang.String r2 = LOG_TAG
            android.util.Log.e(r2, r13)
        L_0x02ed:
            com.sec.internal.helper.HttpRequest r1 = r1.mHttpRequest
            if (r1 == 0) goto L_0x02f4
            r1.disconnect()
        L_0x02f4:
            throw r0
        L_0x02f5:
            r0 = move-exception
            r11 = r8
            r15 = 0
            r7 = r11
            goto L_0x0302
        L_0x02fa:
            r0 = move-exception
            r15 = 0
            r2 = -1
            goto L_0x0317
        L_0x02fe:
            r0 = move-exception
            r15 = 0
            r7 = r16
        L_0x0302:
            r0.printStackTrace()
            r7.close()     // Catch:{ IOException | NullPointerException -> 0x0309 }
            goto L_0x030d
        L_0x0309:
            r0 = move-exception
            r0.printStackTrace()
        L_0x030d:
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r2 = -1
            r1.cancelRequest(r0, r2, r2, r15)
            return r15
        L_0x0314:
            r0 = move-exception
            r2 = -1
            r15 = 0
        L_0x0317:
            r0.printStackTrace()
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r1.cancelRequest(r0, r2, r2, r15)
            return r15
        L_0x0320:
            r0 = move-exception
            r14 = r7
            r15 = 0
            goto L_0x012b
        L_0x0325:
            r0 = move-exception
            r15 = 0
        L_0x0327:
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.Throwable r0 = r0.getCause()
            r3.append(r0)
            java.lang.String r0 = " happened. Cancel resume."
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            android.util.Log.e(r2, r0)
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r2 = -1
            r1.cancelRequest(r0, r2, r2, r15)
            return r15
        L_0x0348:
            r0 = move-exception
            r14 = r7
            r2 = -1
            r15 = 0
        L_0x034c:
            r0.printStackTrace()
            boolean r3 = r1.isPermanentFailCause(r0)
            if (r3 == 0) goto L_0x035d
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r3 = 30
            r1.cancelRequest(r0, r3, r2, r15)
            goto L_0x037b
        L_0x035d:
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.io.IOException r0 = r0.getCause()
            r4.append(r0)
            r4.append(r14)
            java.lang.String r0 = r4.toString()
            android.util.Log.e(r3, r0)
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r3 = 3
            r1.cancelRequest(r0, r3, r2, r15)
        L_0x037b:
            return r15
        L_0x037c:
            r0 = move-exception
            r15 = r8
            r2 = -1
            r0.printStackTrace()
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r1.cancelRequest(r0, r2, r2, r15)
            return r15
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.UploadResumeFileTask.doResumeFile(java.net.URL, long, long, long):boolean");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0181, code lost:
        if (r14 != null) goto L_0x01e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x01ad, code lost:
        if (r14 == null) goto L_0x01e9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x01e4, code lost:
        if (r14 == null) goto L_0x01e9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01e6, code lost:
        r14.disconnect();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01e9, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getDownloadInfo() {
        /*
            r14 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "?tid="
            r0.append(r1)
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r1 = r14.mRequest
            java.lang.String r1 = r1.mTid
            r0.append(r1)
            java.lang.String r1 = "&get_download_info"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r1 = r14.mRequest
            java.lang.String r2 = r14.getRequestUrl()
            r1.mUrl = r2
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getDownloadInfo: params="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r3 = r14.mRequest
            java.lang.String r3 = r3.mUrl
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            com.sec.internal.helper.HttpRequest r2 = com.sec.internal.helper.HttpRequest.get(r2)
            r14.mHttpRequest = r2
            r14.setDefaultHeaders()
            r2 = 0
            r3 = -1
            r4 = 0
            com.sec.internal.helper.HttpRequest r5 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            int r5 = r5.code()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r6 = " "
            r7 = 200(0xc8, float:2.8E-43)
            if (r5 == r7) goto L_0x013a
            r8 = 302(0x12e, float:4.23E-43)
            if (r5 == r8) goto L_0x00ef
            r8 = 401(0x191, float:5.62E-43)
            if (r5 == r8) goto L_0x0099
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.<init>()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r5 = "Receive "
            r0.append(r5)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r5 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            int r5 = r5.code()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.append(r5)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.append(r6)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r5 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r5 = r5.message()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.append(r5)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r0 = r0.toString()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            android.util.Log.e(r1, r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            if (r14 == 0) goto L_0x0098
            r14.disconnect()
        L_0x0098:
            return r2
        L_0x0099:
            int r8 = r14.mPhoneId     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r9 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r10.<init>()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r11 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r11 = r11.mUrl     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r10.append(r11)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r10.append(r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r10 = r10.toString()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r11 = "GET"
            java.lang.String r8 = r14.getAuthorizationHeader(r8, r9, r10, r11)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            boolean r9 = android.text.TextUtils.isEmpty(r8)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            if (r9 == 0) goto L_0x00be
            goto L_0x013f
        L_0x00be:
            com.sec.internal.helper.HttpRequest r5 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r5.disconnect()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r5.<init>()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r9 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r9 = r9.mUrl     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r5.append(r9)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r5.append(r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r0 = r5.toString()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = com.sec.internal.helper.HttpRequest.get(r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r14.mHttpRequest = r0     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r14.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = r0.authorization(r8)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.chunk(r4)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            int r5 = r0.code()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            goto L_0x013f
        L_0x00ef:
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r5 = "Location"
            java.lang.String r10 = r0.header(r5)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            boolean r0 = android.text.TextUtils.isEmpty(r10)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            if (r0 != 0) goto L_0x012d
            com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest r0 = new com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            int r9 = r14.mPhoneId     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r5 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            android.net.Network r11 = r5.mNetwork     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r12 = r5.mUserAgent     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            boolean r13 = r5.mTrustAllCerts     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r8 = r0
            r8.<init>(r9, r10, r11, r12, r13)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r0 = com.sec.internal.ims.util.OpenIdAuth.sendAuthRequest(r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            if (r0 == 0) goto L_0x012d
            com.sec.internal.helper.HttpRequest r5 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r5.disconnect()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = com.sec.internal.helper.HttpRequest.get(r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r14.mHttpRequest = r0     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r14.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.chunk(r4)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            int r5 = r0.code()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            goto L_0x013f
        L_0x012d:
            java.lang.String r0 = "getDownloadInfo: OPenID Process failed!"
            android.util.Log.e(r1, r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            if (r14 == 0) goto L_0x0139
            r14.disconnect()
        L_0x0139:
            return r2
        L_0x013a:
            java.lang.String r0 = "Receive 200 OK"
            android.util.Log.i(r1, r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
        L_0x013f:
            if (r7 != r5) goto L_0x0154
            java.lang.String r0 = "getDownloadInfo: Success"
            android.util.Log.i(r1, r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r0 = r0.body()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            if (r14 == 0) goto L_0x0153
            r14.disconnect()
        L_0x0153:
            return r0
        L_0x0154:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.<init>()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r5 = "getDownloadInfo: Failed, "
            r0.append(r5)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r5 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            int r5 = r5.code()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.append(r5)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.append(r6)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r5 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r5 = r5.message()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r0.append(r5)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            java.lang.String r0 = r0.toString()     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            android.util.Log.e(r1, r0)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            r14.cancelRequest(r0, r4)     // Catch:{ HttpRequestException -> 0x01b0, IllegalArgumentException -> 0x0186 }
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            if (r14 == 0) goto L_0x01e9
            goto L_0x01e6
        L_0x0184:
            r0 = move-exception
            goto L_0x01ea
        L_0x0186:
            r0 = move-exception
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x0184 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0184 }
            r5.<init>()     // Catch:{ all -> 0x0184 }
            java.lang.String r6 = "getDownloadInfo: "
            r5.append(r6)     // Catch:{ all -> 0x0184 }
            java.lang.Throwable r0 = r0.getCause()     // Catch:{ all -> 0x0184 }
            r5.append(r0)     // Catch:{ all -> 0x0184 }
            java.lang.String r0 = " happened. Cancel."
            r5.append(r0)     // Catch:{ all -> 0x0184 }
            java.lang.String r0 = r5.toString()     // Catch:{ all -> 0x0184 }
            android.util.Log.e(r1, r0)     // Catch:{ all -> 0x0184 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0184 }
            r14.cancelRequest(r0, r3, r3, r4)     // Catch:{ all -> 0x0184 }
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            if (r14 == 0) goto L_0x01e9
            goto L_0x01e6
        L_0x01b0:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0184 }
            boolean r1 = r14.isPermanentFailCause(r0)     // Catch:{ all -> 0x0184 }
            if (r1 == 0) goto L_0x01c2
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0184 }
            r1 = 30
            r14.cancelRequest(r0, r1, r3, r4)     // Catch:{ all -> 0x0184 }
            goto L_0x01e2
        L_0x01c2:
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x0184 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0184 }
            r5.<init>()     // Catch:{ all -> 0x0184 }
            java.io.IOException r0 = r0.getCause()     // Catch:{ all -> 0x0184 }
            r5.append(r0)     // Catch:{ all -> 0x0184 }
            java.lang.String r0 = " happened. Retry Upload Task."
            r5.append(r0)     // Catch:{ all -> 0x0184 }
            java.lang.String r0 = r5.toString()     // Catch:{ all -> 0x0184 }
            android.util.Log.e(r1, r0)     // Catch:{ all -> 0x0184 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0184 }
            r1 = 3
            r14.cancelRequest(r0, r1, r3, r4)     // Catch:{ all -> 0x0184 }
        L_0x01e2:
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            if (r14 == 0) goto L_0x01e9
        L_0x01e6:
            r14.disconnect()
        L_0x01e9:
            return r2
        L_0x01ea:
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            if (r14 == 0) goto L_0x01f1
            r14.disconnect()
        L_0x01f1:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.UploadResumeFileTask.getDownloadInfo():java.lang.String");
    }

    /* access modifiers changed from: protected */
    public Long doInBackground() {
        String downloadInfo;
        boolean z = false;
        if (this.mMnoStrategy == null) {
            Log.e(LOG_TAG, "mMnoStrategy is null");
            cancelRequest(CancelReason.ERROR, -1, -1, false);
            return Long.valueOf(this.mTransferred);
        }
        TrafficStats.setThreadStatsTag(Process.myTid());
        String str = LOG_TAG;
        Log.i(str, "doInBackground: " + this.mRequest);
        this.mTotal = this.mRequest.mTotalBytes;
        FtHttpResumeInfo uploadInfo = getUploadInfo();
        if (uploadInfo == null) {
            Log.e(str, "Failed to get upload info.");
            return Long.valueOf(this.mTransferred);
        }
        if (uploadInfo.getEnd() + 1 > this.mRequest.mTotalBytes) {
            Log.i(str, "Uploaded over than requested size.  : " + (uploadInfo.getEnd() + 1));
        } else if (uploadInfo.getEnd() + 1 == this.mRequest.mTotalBytes) {
            Log.i(str, "Already uploaded.");
        } else {
            if (uploadInfo.getUrl() != null) {
                URL url = uploadInfo.getUrl();
                long end = uploadInfo.getEnd() + 1;
                long j = this.mRequest.mTotalBytes;
                z = doResumeFile(url, end, j - 1, j);
            }
            if (z && (downloadInfo = getDownloadInfo()) != null) {
                this.mRequest.mCallback.onCompleted(downloadInfo);
            }
            return Long.valueOf(this.mTransferred);
        }
        z = true;
        this.mRequest.mCallback.onCompleted(downloadInfo);
        return Long.valueOf(this.mTransferred);
    }
}
