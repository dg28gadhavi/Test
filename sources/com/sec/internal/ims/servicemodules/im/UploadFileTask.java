package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Network;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.RetryTimerUtil;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.AsyncFileTask;
import com.sec.internal.ims.servicemodules.im.util.FileTaskUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.HttpAuthGenerator;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.OpenIdAuth;
import com.sec.internal.ims.util.ThumbnailUtil;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class UploadFileTask extends AsyncFileTask<Long> {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "UploadFileTask";
    protected String mContentType;
    protected Context mContext;
    protected String mEncodedFileName;
    protected HttpRequest mHttpRequest;
    protected final IMnoStrategy mMnoStrategy;
    protected int mPhoneId;
    protected UploadRequest mRequest;
    protected long mTotal;
    protected long mTransferred;
    protected long mUploadProgressElapsed;
    protected String thumbFileName;
    protected byte[] thumbnailData;
    protected String thumbnailType;

    public interface UploadTaskCallback {
        void onCanceled(CancelReason cancelReason, int i, int i2, boolean z);

        void onCompleted(String str);

        void onFinished();

        void onProgressUpdate(long j);

        void onStarted();
    }

    public UploadFileTask(int i, Context context, Looper looper, UploadRequest uploadRequest) {
        super(looper);
        String str = LOG_TAG;
        Log.i(str, "phoneId: " + i);
        this.mPhoneId = i;
        this.mRequest = uploadRequest;
        this.mContext = context;
        this.mMnoStrategy = RcsPolicyManager.getRcsStrategy(i);
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Long l) {
        UploadTaskCallback uploadTaskCallback;
        super.lambda$handleResult$1(l);
        UploadRequest uploadRequest = this.mRequest;
        if (!(uploadRequest == null || (uploadTaskCallback = uploadRequest.mCallback) == null)) {
            uploadTaskCallback.onFinished();
        }
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Task");
        sb.append(isCancelled() ? "canceled " : "finished ");
        sb.append(l);
        Log.i(str, sb.toString());
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x014a A[Catch:{ all -> 0x0141 }] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0166 A[Catch:{ all -> 0x0141 }] */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0173 A[Catch:{ all -> 0x0141 }] */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x017b A[Catch:{ all -> 0x0141 }] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:53:0x0160=Splitter:B:53:0x0160, B:43:0x0144=Splitter:B:43:0x0144} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Long doInBackground() {
        /*
            r14 = this;
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = r14.mMnoStrategy
            r1 = 0
            r2 = -1
            if (r0 != 0) goto L_0x0019
            java.lang.String r0 = LOG_TAG
            java.lang.String r3 = "mMnoStrategy is null"
            android.util.Log.e(r0, r3)
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r14.cancelRequest(r0, r2, r2, r1)
            long r0 = r14.mTransferred
            java.lang.Long r14 = java.lang.Long.valueOf(r0)
            return r14
        L_0x0019:
            int r0 = android.os.Process.myTid()
            android.net.TrafficStats.setThreadStatsTag(r0)
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "doInBackground: "
            r3.append(r4)
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r4 = r14.mRequest
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r0, r3)
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r3 = r14.mRequest
            long r3 = r3.mTotalBytes
            r14.mTotal = r3
            r3 = 0
            r14.mHttpRequest = r3
            boolean r4 = r14.sendFirstPostRequest()
            if (r4 != 0) goto L_0x004e
            long r0 = r14.mTransferred
            java.lang.Long r14 = java.lang.Long.valueOf(r0)
            return r14
        L_0x004e:
            com.sec.internal.helper.HttpRequest r4 = r14.mHttpRequest
            if (r4 != 0) goto L_0x0063
            java.lang.String r3 = "mHttpRequest is null"
            android.util.Log.e(r0, r3)
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r14.cancelRequest(r0, r2, r2, r1)
            long r0 = r14.mTransferred
            java.lang.Long r14 = java.lang.Long.valueOf(r0)
            return r14
        L_0x0063:
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r4 = r14.mRequest
            long r4 = r4.mTotalBytes
            r6 = 50
            long r4 = r4 / r6
            r6 = 61440(0xf000, double:3.03554E-319)
            long r4 = java.lang.Math.max(r4, r6)
            r6 = 512000(0x7d000, double:2.529616E-318)
            long r4 = java.lang.Math.min(r6, r4)
            int r4 = (int) r4
            r5 = 3
            r14.setDefaultHeaders()     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            com.sec.internal.helper.HttpRequest r6 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r6.bufferSize(r4)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r14.generateFileInfo()     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r4 = r14.mMnoStrategy     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r6 = "support_fthttp_contentlength"
            boolean r4 = r4.boolSetting(r6)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            if (r4 == 0) goto L_0x00bf
            com.sec.internal.helper.HttpRequest r7 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            byte[] r8 = r14.thumbnailData     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r9 = r14.thumbnailType     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r10 = r14.thumbFileName     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r11 = r14.mContentType     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r12 = r14.mEncodedFileName     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r6 = r14
            long r6 = r6.getRequestContentLength(r7, r8, r9, r10, r11, r12)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r4.<init>()     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r8 = "Http request length:"
            r4.append(r8)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r4.append(r6)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r4 = r4.toString()     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            android.util.Log.i(r0, r4)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r4 = java.lang.Long.toString(r6)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r0.contentLength((java.lang.String) r4)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            goto L_0x00c4
        L_0x00bf:
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r0.chunk(r1)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
        L_0x00c4:
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r4 = "tid"
            java.lang.String r6 = "text/plain"
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r7 = r14.mRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r7 = r7.mTid     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r0.part((java.lang.String) r4, (java.lang.String) r3, (java.lang.String) r6, (java.lang.String) r7)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            byte[] r0 = r14.thumbnailData     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            if (r0 == 0) goto L_0x00e9
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r4 = "Thumbnail"
            java.lang.String r6 = r14.thumbFileName     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.lang.String r7 = r14.thumbnailType     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.io.ByteArrayInputStream r8 = new java.io.ByteArrayInputStream     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            byte[] r9 = r14.thumbnailData     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r8.<init>(r9)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r0.part((java.lang.String) r4, (java.lang.String) r6, (java.lang.String) r7, (java.io.InputStream) r8)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
        L_0x00e9:
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r0 = r14.mRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadTaskCallback r0 = r0.mCallback     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            r0.onStarted()     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            android.content.Context r0 = r14.mContext     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            android.content.ContentResolver r0 = r0.getContentResolver()     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$UploadRequest r4 = r14.mRequest     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            android.net.Uri r4 = r4.mContentUri     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            java.io.InputStream r0 = r0.openInputStream(r4)     // Catch:{ HttpRequestException -> 0x015f, IOException | IllegalStateException -> 0x0143 }
            com.sec.internal.helper.HttpRequest r4 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            com.sec.internal.ims.servicemodules.im.UploadFileTask$1 r6 = new com.sec.internal.ims.servicemodules.im.UploadFileTask$1     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            r6.<init>()     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            com.sec.internal.helper.HttpRequest r4 = r4.progress(r6)     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            java.lang.String r6 = "File"
            java.lang.String r7 = r14.mEncodedFileName     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            java.lang.String r8 = r14.mContentType     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            com.sec.internal.helper.HttpRequest r4 = r4.part((java.lang.String) r6, (java.lang.String) r7, (java.lang.String) r8, (java.io.InputStream) r0)     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            r4.progress(r3)     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            boolean r3 = r14.isCancelled()     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            if (r3 == 0) goto L_0x012b
            long r3 = r14.mTransferred     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            java.lang.Long r1 = java.lang.Long.valueOf(r3)     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            r0.close()     // Catch:{ IOException | NullPointerException -> 0x0125 }
        L_0x0125:
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            r14.disconnect()
            return r1
        L_0x012b:
            r14.onUploadFileDone()     // Catch:{ HttpRequestException -> 0x013c, IOException | IllegalStateException -> 0x0137, all -> 0x0132 }
            r0.close()     // Catch:{ IOException | NullPointerException -> 0x0159 }
            goto L_0x0159
        L_0x0132:
            r1 = move-exception
            r3 = r0
            r0 = r1
            goto L_0x01a2
        L_0x0137:
            r3 = move-exception
            r13 = r3
            r3 = r0
            r0 = r13
            goto L_0x0144
        L_0x013c:
            r3 = move-exception
            r13 = r3
            r3 = r0
            r0 = r13
            goto L_0x0160
        L_0x0141:
            r0 = move-exception
            goto L_0x01a2
        L_0x0143:
            r0 = move-exception
        L_0x0144:
            java.lang.Throwable r4 = r0.getCause()     // Catch:{ all -> 0x0141 }
            if (r4 == 0) goto L_0x0151
            java.lang.Throwable r0 = r0.getCause()     // Catch:{ all -> 0x0141 }
            r0.printStackTrace()     // Catch:{ all -> 0x0141 }
        L_0x0151:
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0141 }
            r14.cancelRequest(r0, r5, r2, r1)     // Catch:{ all -> 0x0141 }
        L_0x0156:
            r3.close()     // Catch:{ IOException | NullPointerException -> 0x0159 }
        L_0x0159:
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest
            r0.disconnect()
            goto L_0x019b
        L_0x015f:
            r0 = move-exception
        L_0x0160:
            java.io.IOException r4 = r0.getCause()     // Catch:{ all -> 0x0141 }
            if (r4 == 0) goto L_0x016d
            java.io.IOException r4 = r0.getCause()     // Catch:{ all -> 0x0141 }
            r4.printStackTrace()     // Catch:{ all -> 0x0141 }
        L_0x016d:
            boolean r4 = r14.isPermanentFailCause(r0)     // Catch:{ all -> 0x0141 }
            if (r4 == 0) goto L_0x017b
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0141 }
            r4 = 30
            r14.cancelRequest(r0, r4, r2, r1)     // Catch:{ all -> 0x0141 }
            goto L_0x0156
        L_0x017b:
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x0141 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0141 }
            r6.<init>()     // Catch:{ all -> 0x0141 }
            java.io.IOException r0 = r0.getCause()     // Catch:{ all -> 0x0141 }
            r6.append(r0)     // Catch:{ all -> 0x0141 }
            java.lang.String r0 = " happened. Retry Upload Task."
            r6.append(r0)     // Catch:{ all -> 0x0141 }
            java.lang.String r0 = r6.toString()     // Catch:{ all -> 0x0141 }
            android.util.Log.e(r4, r0)     // Catch:{ all -> 0x0141 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x0141 }
            r14.cancelRequest(r0, r5, r2, r1)     // Catch:{ all -> 0x0141 }
            goto L_0x0156
        L_0x019b:
            long r0 = r14.mTransferred
            java.lang.Long r14 = java.lang.Long.valueOf(r0)
            return r14
        L_0x01a2:
            r3.close()     // Catch:{ IOException | NullPointerException -> 0x01a5 }
        L_0x01a5:
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            r14.disconnect()
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.UploadFileTask.doInBackground():java.lang.Long");
    }

    /* access modifiers changed from: protected */
    public boolean isPermanentFailCause(HttpRequest.HttpRequestException httpRequestException) {
        return httpRequestException.getCause() instanceof UnknownHostException;
    }

    /* access modifiers changed from: protected */
    public void cancelRequest(HttpRequest httpRequest, boolean z) {
        CancelReason cancelReason = CancelReason.ERROR;
        int code = httpRequest.code();
        int i = 3;
        if (code == 401) {
            IMnoStrategy iMnoStrategy = this.mMnoStrategy;
            if (iMnoStrategy != null) {
                iMnoStrategy.handleFtHttpRequestFailure(CancelReason.UNAUTHORIZED, ImDirection.OUTGOING, false);
            }
        } else if (code == 403) {
            IMnoStrategy iMnoStrategy2 = this.mMnoStrategy;
            if (iMnoStrategy2 != null) {
                iMnoStrategy2.handleFtHttpRequestFailure(CancelReason.FORBIDDEN_FT_HTTP, ImDirection.OUTGOING, false);
            }
        } else if (code == 410) {
            i = 1;
        } else if (code == 500) {
            i = 5;
        } else if (code == 503) {
            i = RetryTimerUtil.getRetryAfter(httpRequest.header(HttpRequest.HEADER_RETRY_AFTER));
        }
        cancelRequest(cancelReason, i, httpRequest.code(), z);
    }

    /* access modifiers changed from: protected */
    public void cancelRequest(CancelReason cancelReason, int i, int i2, boolean z) {
        if (!isCancelled()) {
            this.mRequest.mCallback.onCanceled(cancelReason, i, i2, z);
        }
    }

    private long getRequestContentLength(HttpRequest httpRequest, byte[] bArr, String str, String str2, String str3, String str4) {
        long sizeFromUri = FileUtils.getSizeFromUri(this.mContext, this.mRequest.mContentUri);
        long partHeaderLength = httpRequest.getPartHeaderLength("tid", (String) null, MIMEContentType.PLAIN_TEXT, true) + ((long) this.mRequest.mTid.length()) + 0;
        if (bArr != null) {
            partHeaderLength += httpRequest.getPartHeaderLength("Thumbnail", str2, str, false) + ((long) bArr.length);
            IMnoStrategy iMnoStrategy = this.mMnoStrategy;
            if (iMnoStrategy != null && iMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.CONTENTLENGTH_IN_BYTE)) {
                partHeaderLength += (long) (str4.getBytes(Charset.defaultCharset()).length - str4.length());
            }
        }
        long partHeaderLength2 = partHeaderLength + httpRequest.getPartHeaderLength("File", str4, str3, false) + sizeFromUri;
        IMnoStrategy iMnoStrategy2 = this.mMnoStrategy;
        if (iMnoStrategy2 != null && iMnoStrategy2.boolSetting(RcsPolicySettings.RcsPolicy.CONTENTLENGTH_IN_BYTE)) {
            partHeaderLength2 += (long) (str4.getBytes().length - str4.length());
        }
        return partHeaderLength2 + ((long) ("\r\n" + "--" + HttpRequest.BOUNDARY + "--" + "\r\n").length());
    }

    private String getTrimmedFileName(String str, int i) {
        Log.i(LOG_TAG, "getTrimmedFileName() fileName=" + IMSLog.checker(str) + ", limitSize= " + i);
        try {
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            int length = URLEncoder.encode(str, "UTF-8").getBytes().length;
            int lastIndexOf = str.lastIndexOf(".");
            if (lastIndexOf == -1) {
                lastIndexOf = str.length();
            }
            String substring = str.substring(0, lastIndexOf);
            String substring2 = str.substring(lastIndexOf);
            int length2 = substring.length();
            int i2 = 0;
            while (length2 > 0 && length - i2 > i) {
                i2 += URLEncoder.encode(substring.substring(length2 - 1, length2), "UTF-8").getBytes().length;
                length2--;
            }
            String substring3 = substring.substring(0, length2);
            Log.i(LOG_TAG, "Trimmed fileName=" + IMSLog.checker(substring3) + substring2);
            return substring3 + substring2;
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            Log.e(LOG_TAG, "Exception: " + e);
            return str;
        }
    }

    private boolean sendFirstPostRequest() {
        try {
            sendEmptyPostRequest();
            setDefaultHeaders();
            int code = this.mHttpRequest.code();
            this.mHttpRequest.disconnect();
            if (!handleFirstRequestResponse(code)) {
                return false;
            }
            return true;
        } catch (HttpRequest.HttpRequestException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            if (isPermanentFailCause(e)) {
                cancelRequest(CancelReason.ERROR, 30, -1, false);
            } else {
                String str = LOG_TAG;
                Log.e(str, e.getCause() + " happened. Retry Upload Task.");
                cancelRequest(CancelReason.ERROR, 3, -1, false);
            }
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            cancelRequest(CancelReason.ERROR, -1, -1, false);
            return false;
        }
    }

    private void sendEmptyPostRequest() {
        String str = this.mRequest.mUrl;
        IMnoStrategy iMnoStrategy = this.mMnoStrategy;
        if (iMnoStrategy != null && iMnoStrategy.isHTTPUsedForEmptyFtHttpPOST()) {
            str = str.replaceFirst("https://", "http://");
        }
        this.mHttpRequest = HttpRequest.post(str);
    }

    private boolean handleFirstRequestResponse(int i) {
        if (i == 200 || i == 204) {
            String str = LOG_TAG;
            Log.i(str, "Receive: " + i);
            this.mHttpRequest = HttpRequest.post(this.mRequest.mUrl);
            return true;
        } else if (i == 302) {
            String header = this.mHttpRequest.header("Location");
            if (!TextUtils.isEmpty(header)) {
                int i2 = this.mPhoneId;
                UploadRequest uploadRequest = this.mRequest;
                String sendAuthRequest = OpenIdAuth.sendAuthRequest(new OpenIdAuth.OpenIdRequest(i2, header, uploadRequest.mNetwork, uploadRequest.mUserAgent, uploadRequest.mTrustAllCerts));
                if (sendAuthRequest != null) {
                    this.mHttpRequest.disconnect();
                    this.mRequest.mUrl = sendAuthRequest;
                    this.mHttpRequest = HttpRequest.post(sendAuthRequest);
                    return true;
                }
            }
            Log.e(LOG_TAG, "handleFirstRequestResponse: OpenId process failed!");
            cancelRequest(this.mHttpRequest, false);
            return false;
        } else if (i != 401) {
            String str2 = LOG_TAG;
            Log.e(str2, "Receive " + this.mHttpRequest.code() + " " + this.mHttpRequest.message());
            cancelRequest(this.mHttpRequest, false);
            return false;
        } else {
            String authorizationHeader = getAuthorizationHeader(this.mPhoneId, this.mHttpRequest, this.mRequest.mUrl, "POST");
            if (TextUtils.isEmpty(authorizationHeader)) {
                Log.e(LOG_TAG, "handleFirstRequestResponse: Authorization response is null!");
                cancelRequest(this.mHttpRequest, false);
                return false;
            }
            this.mHttpRequest = HttpRequest.post(this.mRequest.mUrl).useNetwork(this.mRequest.mNetwork).authorization(authorizationHeader);
            return true;
        }
    }

    private void generateFileInfo() {
        UploadRequest uploadRequest = this.mRequest;
        String str = uploadRequest.mContentType;
        if (str != null) {
            this.mContentType = str;
        } else {
            this.mContentType = FileUtils.getContentType(this.mContext, uploadRequest.mFileName, uploadRequest.mContentUri);
        }
        this.thumbFileName = "";
        this.thumbnailType = "image/jpeg";
        this.thumbnailData = null;
        UploadRequest uploadRequest2 = this.mRequest;
        if (uploadRequest2.bFileIcon) {
            generateThumbnailData(uploadRequest2.mContentUri, this.mContentType);
        }
        this.mEncodedFileName = this.mRequest.mFileName;
        try {
            if (!this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_ENCODING_FILENAME_BY_SERVER)) {
                int intSetting = this.mMnoStrategy.intSetting(RcsPolicySettings.RcsPolicy.FILE_NAME_LENGTH_LIMIT_IN_SERVER);
                String str2 = this.mEncodedFileName;
                if (intSetting > 0) {
                    if (URLEncoder.encode(str2, "UTF-8").getBytes().length > intSetting) {
                        str2 = getTrimmedFileName(str2, intSetting);
                    }
                }
                this.mEncodedFileName = URLEncoder.encode(str2, "UTF-8");
            }
        } catch (UnsupportedEncodingException | IllegalArgumentException unused) {
            Log.e(LOG_TAG, "UnsupportedEncodingException or IllegalArgumentException");
        }
        if (TextUtils.isEmpty(this.thumbFileName)) {
            IMSLog.s(LOG_TAG, "mEncodedFileName : " + this.mEncodedFileName);
            String[] split = this.mEncodedFileName.split("\\.");
            if (split.length > 0) {
                this.thumbFileName = split[0];
            } else {
                this.thumbFileName = "thumb";
            }
            if ("image/jpeg".equals(this.thumbnailType)) {
                this.thumbFileName += ".jpg";
            } else if ("image/bmp".equals(this.thumbnailType)) {
                this.thumbFileName += ".bmp";
            }
        }
    }

    private void generateThumbnailData(Uri uri, String str) {
        if (str != null && str.startsWith(SipMsg.FEATURE_TAG_MMTEL_VIDEO)) {
            this.thumbnailData = ThumbnailUtil.getVideoThumbnailByteArray(this.mContext, uri, this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_HIGHRESOLUTIONVIDEO_THUMBNAIL) ? ThumbnailUtil.MAX_BYTE_COUNT_HIGH : ThumbnailUtil.MAX_BYTE_COUNT);
        }
        if (this.thumbnailData == null) {
            this.thumbnailData = ThumbnailUtil.getThumbnailByteArray(this.mContext, uri);
        }
        if (this.thumbnailData == null) {
            String str2 = OmcCode.get();
            if ("DTM".equals(str2) || "DTR".equals(str2)) {
                this.thumbnailData = new byte[]{66, 77, 66, 0, 0, 0, 0, 0, 0, 0, 62, 0, 0, 0, 40, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, -64, 0, 0, 0};
                this.thumbnailType = "image/bmp";
                this.thumbFileName = "dummy.txt.txt.bmp";
            }
        }
    }

    private void onUploadFileDone() {
        String str = LOG_TAG;
        Log.i(str, "Upload File done. Read http response.");
        if (this.mHttpRequest.ok()) {
            Log.i(str, "Upload success, handle response message.");
            this.mRequest.mCallback.onCompleted(this.mHttpRequest.body());
            return;
        }
        Log.e(str, "Upload failed, " + this.mHttpRequest.code() + " " + this.mHttpRequest.message());
        if (this.mHttpRequest.code() == 500) {
            Log.e(str, "Retry uploading with deaccented mFile name.");
            cancelRequest(CancelReason.ERROR, 3, 500, false);
            return;
        }
        cancelRequest(this.mHttpRequest, false);
    }

    /* access modifiers changed from: protected */
    public String getAuthorizationHeader(int i, HttpRequest httpRequest, String str, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "Receive 401 Unauthorized, attempt to generate response");
        String wwwAuthenticate = httpRequest.wwwAuthenticate();
        IMSLog.s(str3, "challenge: " + wwwAuthenticate);
        if (wwwAuthenticate == null) {
            Log.i(str3, "Got 401 and challenge is NULL!");
            return "";
        } else if (wwwAuthenticate.trim().equals("SIT")) {
            Log.i(str3, "Got 401 for SIT. Skip GBA");
            return "";
        } else {
            String authorizationHeader = HttpAuthGenerator.getAuthorizationHeader(i, str, wwwAuthenticate, str2, httpRequest.getCipherSuite());
            IMSLog.s(str3, "response: " + authorizationHeader);
            return authorizationHeader;
        }
    }

    /* access modifiers changed from: protected */
    public void setDefaultHeaders() {
        this.mHttpRequest.useNetwork(this.mRequest.mNetwork).useCaches(false).connectTimeout(10000).readTimeout(FileTaskUtil.READ_DATA_TIMEOUT).userAgent(this.mRequest.mUserAgent);
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FT_WITH_GBA) && this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.USE_USERIDENTITY_FOR_FTHTTP)) {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
            String impuFromIsim = simManagerFromSimSlot != null ? simManagerFromSimSlot.getImpuFromIsim(0) : null;
            if (TextUtils.isEmpty(impuFromIsim)) {
                impuFromIsim = ImsUtil.getPublicId(this.mPhoneId);
            }
            HttpRequest httpRequest = this.mHttpRequest;
            httpRequest.header("X-3GPP-Intended-Identity", CmcConstants.E_NUM_STR_QUOTE + impuFromIsim + CmcConstants.E_NUM_STR_QUOTE);
        }
        if (this.mRequest.mTrustAllCerts) {
            this.mHttpRequest.trustAllCerts().trustAllHosts();
        }
    }

    public static class UploadRequest {
        public boolean bFileIcon;
        public UploadTaskCallback mCallback;
        public String mContentType;
        public Uri mContentUri;
        public String mFileName;
        public Network mNetwork;
        public String mTid;
        public long mTotalBytes;
        public boolean mTrustAllCerts;
        public String mUrl;
        public String mUserAgent;

        public UploadRequest(String str, long j, String str2, Uri uri, boolean z, String str3, String str4, Network network, boolean z2, UploadTaskCallback uploadTaskCallback, String str5) {
            this.mUrl = str;
            this.mTotalBytes = j;
            this.mFileName = str2;
            this.mContentUri = uri;
            this.mTid = str3;
            this.mUserAgent = str4;
            this.mCallback = uploadTaskCallback;
            this.bFileIcon = z;
            this.mNetwork = network;
            this.mTrustAllCerts = z2;
            this.mContentType = str5;
        }

        public String toString() {
            return "UploadRequest{mUrl=" + IMSLog.checker(this.mUrl) + ", mTotalBytes=" + this.mTotalBytes + ", mContentUri=" + IMSLog.checker(this.mContentUri) + ", bFileIcon=" + this.bFileIcon + ", mTid=" + this.mTid + ", mUserAgent=" + this.mUserAgent + ", mNetwork=" + this.mNetwork + ", mTrustAllCerts=" + this.mTrustAllCerts + ", mCallback=" + this.mCallback + ", mContentType=" + this.mContentType + "}";
        }

        public boolean isValid() {
            return this.mCallback != null && !TextUtils.isEmpty(this.mUrl) && !TextUtils.isEmpty(this.mTid) && this.mContentUri != null;
        }
    }
}
