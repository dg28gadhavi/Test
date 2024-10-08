package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Network;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.HttpRequest;
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
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;

public class DownloadFileTask extends AsyncFileTask<Long> {
    private static final long FT_SIZE_MARGIN = 10240;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "DownloadFileTask";
    private static final int MAX_PROGRESS_COUNT = 50;
    protected Context mContext;
    /* access modifiers changed from: private */
    public long mDownloadProgressElapsed;
    private HttpRequest mHttpRequest = null;
    protected final IMnoStrategy mMnoStrategy;
    private int mPhoneId;
    /* access modifiers changed from: private */
    public DownloadRequest mRequest;
    /* access modifiers changed from: private */
    public long mTotal;
    /* access modifiers changed from: private */
    public long mTransferred;
    /* access modifiers changed from: private */
    public long mWritten;

    public interface DownloadTaskCallback {
        void onCanceled(CancelReason cancelReason, int i, int i2);

        void onCompleted(long j);

        void onProgressUpdate(long j);
    }

    public DownloadFileTask(int i, Context context, Looper looper, DownloadRequest downloadRequest) {
        super(looper);
        String str = LOG_TAG;
        Log.i(str, "phoneId: " + i);
        this.mPhoneId = i;
        this.mContext = context;
        this.mMnoStrategy = RcsPolicyManager.getRcsStrategy(i);
        this.mRequest = downloadRequest;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0122 A[Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1, all -> 0x019f }] */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0197 A[SYNTHETIC, Splitter:B:62:0x0197] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Long doInBackground() {
        /*
            r14 = this;
            int r0 = android.os.Process.myTid()
            android.net.TrafficStats.setThreadStatsTag(r0)
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "doInBackground: "
            r1.append(r2)
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r2 = r14.mRequest
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r1 = r14.mRequest
            long r2 = r1.mTransferredBytes
            r14.mTransferred = r2
            long r4 = r1.mTotalBytes
            r14.mTotal = r4
            r4 = 0
            int r1 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r1 == 0) goto L_0x006a
            long r1 = r14.getFileLength()
            long r6 = r14.mTransferred
            int r3 = (r1 > r6 ? 1 : (r1 == r6 ? 0 : -1))
            if (r3 == 0) goto L_0x006b
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r6 = "Adjust mTransferred to "
            r3.append(r6)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r0, r3)
            r14.mTransferred = r1
            long r6 = r14.mTotal
            int r3 = (r1 > r6 ? 1 : (r1 == r6 ? 0 : -1))
            if (r3 < 0) goto L_0x006b
            java.lang.String r1 = "Already the download was completed."
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r0 = r14.mRequest
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadTaskCallback r0 = r0.mCallback
            long r1 = r14.mTransferred
            r0.onCompleted(r1)
            long r0 = r14.mTransferred
            java.lang.Long r14 = java.lang.Long.valueOf(r0)
            return r14
        L_0x006a:
            r1 = r4
        L_0x006b:
            r14.mWritten = r1
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r1 = r14.mRequest
            long r1 = r1.mTotalBytes
            r6 = 50
            long r1 = r1 / r6
            r6 = 61440(0xf000, double:3.03554E-319)
            long r1 = java.lang.Math.max(r1, r6)
            r6 = 512000(0x7d000, double:2.529616E-318)
            long r1 = java.lang.Math.min(r6, r1)
            int r1 = (int) r1
            r2 = 0
            r3 = -1
            int r6 = r14.sendGetRequest(r1)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r7 = 200(0xc8, float:2.8E-43)
            r8 = 206(0xce, float:2.89E-43)
            if (r6 == r7) goto L_0x00c2
            if (r6 != r8) goto L_0x0098
            long r9 = r14.mTransferred     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            int r7 = (r9 > r4 ? 1 : (r9 == r4 ? 0 : -1))
            if (r7 <= 0) goto L_0x0098
            goto L_0x00c2
        L_0x0098:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r1.<init>()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.String r7 = "Download failed, response: "
            r1.append(r7)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r1.append(r6)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.String r1 = r1.toString()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            android.util.Log.i(r0, r1)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r14.cancelRequest(r0)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r0 = r14.mTransferred     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.Long r0 = java.lang.Long.valueOf(r0)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            throw r2     // Catch:{ NullPointerException -> 0x00b8 }
        L_0x00b8:
            r1 = move-exception
        L_0x00b9:
            r1.printStackTrace()
        L_0x00bc:
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            r14.disconnect()
            return r0
        L_0x00c2:
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r6 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.String r6 = r6.mFilePath     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            boolean r6 = android.text.TextUtils.isEmpty(r6)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r7 = 1
            r9 = 0
            if (r6 != 0) goto L_0x00e7
            java.io.BufferedOutputStream r6 = new java.io.BufferedOutputStream     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.io.FileOutputStream r10 = new java.io.FileOutputStream     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r11 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.String r11 = r11.mFilePath     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r12 = r14.mTransferred     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            int r12 = (r12 > r4 ? 1 : (r12 == r4 ? 0 : -1))
            if (r12 <= 0) goto L_0x00de
            r12 = r7
            goto L_0x00df
        L_0x00de:
            r12 = r9
        L_0x00df:
            r10.<init>(r11, r12)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r6.<init>(r10, r1)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
        L_0x00e5:
            r2 = r6
            goto L_0x010e
        L_0x00e7:
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r1 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            android.net.Uri r1 = r1.mContentUri     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            if (r1 == 0) goto L_0x010e
            long r10 = r14.mTransferred     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            int r1 = (r10 > r4 ? 1 : (r10 == r4 ? 0 : -1))
            if (r1 <= 0) goto L_0x00f7
            java.lang.String r1 = "wa"
            goto L_0x00fa
        L_0x00f7:
            java.lang.String r1 = "w"
        L_0x00fa:
            java.io.BufferedOutputStream r6 = new java.io.BufferedOutputStream     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            android.content.Context r10 = r14.mContext     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            android.content.ContentResolver r10 = r10.getContentResolver()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r11 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            android.net.Uri r11 = r11.mContentUri     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.io.OutputStream r1 = r10.openOutputStream(r11, r1)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r6.<init>(r1)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            goto L_0x00e5
        L_0x010e:
            com.sec.internal.helper.HttpRequest r1 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$1 r6 = new com.sec.internal.ims.servicemodules.im.DownloadFileTask$1     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r6.<init>()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.helper.HttpRequest r1 = r1.progress(r6)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r1.receive(r2)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            boolean r1 = r14.isCancelled()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            if (r1 != 0) goto L_0x0197
            com.sec.internal.helper.HttpRequest r1 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            boolean r1 = r1.ok()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            if (r1 == 0) goto L_0x013c
            java.lang.String r1 = "Download success, handle response message."
            android.util.Log.i(r0, r1)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r0 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadTaskCallback r0 = r0.mCallback     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r6 = r14.mTransferred     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r8 = r14.mWritten     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r6 = r6 + r8
            r0.onCompleted(r6)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            goto L_0x0183
        L_0x013c:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r1.<init>()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.String r6 = "Download failed, "
            r1.append(r6)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.helper.HttpRequest r6 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.String r6 = r6.message()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r1.append(r6)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.String r1 = r1.toString()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            android.util.Log.e(r0, r1)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r0 = r14.mWritten     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r10 = r14.mTotal     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            int r6 = (r0 > r10 ? 1 : (r0 == r10 ? 0 : -1))
            if (r6 == 0) goto L_0x0167
            long r12 = r14.mTransferred     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r12 = r12 + r0
            int r0 = (r12 > r10 ? 1 : (r12 == r10 ? 0 : -1))
            if (r0 != 0) goto L_0x0166
            goto L_0x0167
        L_0x0166:
            r7 = r9
        L_0x0167:
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            int r0 = r0.code()     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            if (r0 != r8) goto L_0x017e
            if (r7 == 0) goto L_0x017e
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r0 = r14.mRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadTaskCallback r0 = r0.mCallback     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r6 = r14.mTransferred     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r8 = r14.mWritten     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            long r6 = r6 + r8
            r0.onCompleted(r6)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            goto L_0x0183
        L_0x017e:
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            r14.cancelRequest(r0)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
        L_0x0183:
            r2.close()     // Catch:{ IOException | NullPointerException -> 0x0187 }
            goto L_0x018b
        L_0x0187:
            r0 = move-exception
            r0.printStackTrace()
        L_0x018b:
            com.sec.internal.helper.HttpRequest r0 = r14.mHttpRequest
            r0.disconnect()
            long r0 = r14.mTransferred
            java.lang.Long r14 = java.lang.Long.valueOf(r0)
            return r14
        L_0x0197:
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadFileTaskException r0 = new com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadFileTaskException     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            java.lang.String r1 = "Download Task Failed. isCancelled() is called."
            r0.<init>(r1)     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
            throw r0     // Catch:{ HttpRequestException -> 0x01cc, DownloadFileTaskException -> 0x01ba, FileNotFoundException -> 0x01a1 }
        L_0x019f:
            r0 = move-exception
            goto L_0x0214
        L_0x01a1:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x019f }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r0 = r14.mRequest     // Catch:{ all -> 0x019f }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadTaskCallback r0 = r0.mCallback     // Catch:{ all -> 0x019f }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x019f }
            r0.onCanceled(r1, r3, r3)     // Catch:{ all -> 0x019f }
            java.lang.Long r0 = java.lang.Long.valueOf(r4)     // Catch:{ all -> 0x019f }
            r2.close()     // Catch:{ IOException | NullPointerException -> 0x01b7 }
            goto L_0x00bc
        L_0x01b7:
            r1 = move-exception
            goto L_0x00b9
        L_0x01ba:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x019f }
            long r0 = r14.mTransferred     // Catch:{ all -> 0x019f }
            java.lang.Long r0 = java.lang.Long.valueOf(r0)     // Catch:{ all -> 0x019f }
            r2.close()     // Catch:{ IOException | NullPointerException -> 0x01c9 }
            goto L_0x00bc
        L_0x01c9:
            r1 = move-exception
            goto L_0x00b9
        L_0x01cc:
            r0 = move-exception
            boolean r1 = r14.isPermanentFailCause(r0)     // Catch:{ all -> 0x019f }
            if (r1 == 0) goto L_0x01e2
            r0.printStackTrace()     // Catch:{ all -> 0x019f }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r1 = r14.mRequest     // Catch:{ all -> 0x019f }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadTaskCallback r1 = r1.mCallback     // Catch:{ all -> 0x019f }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r4 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x019f }
            r5 = 30
            r1.onCanceled(r4, r5, r3)     // Catch:{ all -> 0x019f }
            goto L_0x01ec
        L_0x01e2:
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadRequest r1 = r14.mRequest     // Catch:{ all -> 0x019f }
            com.sec.internal.ims.servicemodules.im.DownloadFileTask$DownloadTaskCallback r1 = r1.mCallback     // Catch:{ all -> 0x019f }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r4 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ all -> 0x019f }
            r5 = 3
            r1.onCanceled(r4, r5, r3)     // Catch:{ all -> 0x019f }
        L_0x01ec:
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x019f }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x019f }
            r3.<init>()     // Catch:{ all -> 0x019f }
            java.io.IOException r0 = r0.getCause()     // Catch:{ all -> 0x019f }
            r3.append(r0)     // Catch:{ all -> 0x019f }
            java.lang.String r0 = " happened. Retry download Task."
            r3.append(r0)     // Catch:{ all -> 0x019f }
            java.lang.String r0 = r3.toString()     // Catch:{ all -> 0x019f }
            android.util.Log.e(r1, r0)     // Catch:{ all -> 0x019f }
            long r0 = r14.mTransferred     // Catch:{ all -> 0x019f }
            java.lang.Long r0 = java.lang.Long.valueOf(r0)     // Catch:{ all -> 0x019f }
            r2.close()     // Catch:{ IOException | NullPointerException -> 0x0211 }
            goto L_0x00bc
        L_0x0211:
            r1 = move-exception
            goto L_0x00b9
        L_0x0214:
            r2.close()     // Catch:{ IOException | NullPointerException -> 0x0218 }
            goto L_0x021c
        L_0x0218:
            r1 = move-exception
            r1.printStackTrace()
        L_0x021c:
            com.sec.internal.helper.HttpRequest r14 = r14.mHttpRequest
            r14.disconnect()
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.DownloadFileTask.doInBackground():java.lang.Long");
    }

    private long getFileLength() {
        if (!TextUtils.isEmpty(this.mRequest.mFilePath)) {
            File file = new File(this.mRequest.mFilePath);
            if (file.exists()) {
                return file.length();
            }
            return -1;
        }
        Uri uri = this.mRequest.mContentUri;
        if (uri != null) {
            return FileUtils.getSizeFromUri(this.mContext, uri);
        }
        return -1;
    }

    private boolean isPermanentFailCause(HttpRequest.HttpRequestException httpRequestException) {
        return httpRequestException.getCause() instanceof UnknownHostException;
    }

    private void cancelRequest(HttpRequest httpRequest) {
        IMnoStrategy iMnoStrategy = this.mMnoStrategy;
        IMnoStrategy.HttpStrategyResponse handleFtHttpDownloadError = iMnoStrategy != null ? iMnoStrategy.handleFtHttpDownloadError(httpRequest) : new IMnoStrategy.HttpStrategyResponse(CancelReason.ERROR, 3);
        this.mRequest.mCallback.onCanceled(handleFtHttpDownloadError.getCancelReason(), handleFtHttpDownloadError.getDelay(), httpRequest.code());
    }

    public static class DownloadRequest {
        public DownloadTaskCallback mCallback;
        public Uri mContentUri;
        public String mFilePath;
        public Network mNetwork;
        public long mTotalBytes;
        public long mTransferredBytes;
        public boolean mTrustAllCerts;
        public String mUrl;
        public String mUserAgent;

        public DownloadRequest(String str, long j, long j2, String str2, Uri uri, String str3, Network network, boolean z, String str4, Map<String, String> map, DownloadTaskCallback downloadTaskCallback) {
            this.mTotalBytes = j;
            this.mTransferredBytes = j2;
            this.mFilePath = str2;
            this.mContentUri = uri;
            this.mUserAgent = str3;
            this.mCallback = downloadTaskCallback;
            this.mNetwork = network;
            this.mTrustAllCerts = z;
            this.mUrl = !TextUtils.isEmpty(str4) ? str4 : str;
            if (map != null && map.size() > 0) {
                this.mUrl = FileTaskUtil.createRequestUrl(this.mUrl, map);
            }
        }

        public String toString() {
            return "DownloadRequest{mUrl=" + IMSLog.checker(this.mUrl) + ", mTotalBytes=" + this.mTotalBytes + ", mTransferredBytes=" + this.mTransferredBytes + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mContentUir=" + IMSLog.checker(this.mContentUri) + ", mUserAgent=" + this.mUserAgent + ", mCallback=" + this.mCallback + ", mNetwork=" + this.mNetwork + ", mTrustAllCerts=" + this.mTrustAllCerts + "}";
        }

        public boolean isValid() {
            return this.mCallback != null && !TextUtils.isEmpty(this.mUrl) && !TextUtils.isEmpty(this.mUserAgent);
        }
    }

    public static class DownloadFileTaskException extends Exception {
        public DownloadFileTaskException(String str) {
            super(str);
        }
    }

    private int sendGetRequest(int i) {
        int i2;
        String eAPAkaChallengeResponse;
        this.mHttpRequest = null;
        try {
            i2 = sendEmptyGetRequest(i);
            if (i2 == 200) {
                String str = LOG_TAG;
                Log.i(str, "Receive 200 OK");
                if (this.mHttpRequest.header("Content-Type") != null && this.mHttpRequest.header("Content-Type").contains("application/vnd.gsma.eap-relay.v1.0+json")) {
                    this.mHttpRequest.disconnect();
                    String body = this.mHttpRequest.body();
                    if (body == null || (eAPAkaChallengeResponse = HttpAuthGenerator.getEAPAkaChallengeResponse(this.mPhoneId, body)) == null) {
                        Log.e(str, "EAP AKA authentication failed, code: " + this.mHttpRequest.code());
                        this.mRequest.mCallback.onCanceled(CancelReason.ERROR, -1, -1);
                        return -1;
                    }
                    this.mHttpRequest = HttpRequest.post(this.mRequest.mUrl);
                    setDefaultHeaders(i);
                    this.mHttpRequest.contentType("application/vnd.gsma.eap-relay.v1.0+json");
                    this.mHttpRequest.send((CharSequence) eAPAkaChallengeResponse);
                    return this.mHttpRequest.code();
                }
                return i2;
            } else if (i2 == 206) {
                Log.i(LOG_TAG, "Receive 206 Partial");
                return i2;
            } else if (i2 == 302) {
                String header = this.mHttpRequest.header("Location");
                if (!TextUtils.isEmpty(header)) {
                    int i3 = this.mPhoneId;
                    DownloadRequest downloadRequest = this.mRequest;
                    String sendAuthRequest = OpenIdAuth.sendAuthRequest(new OpenIdAuth.OpenIdRequest(i3, header, downloadRequest.mNetwork, downloadRequest.mUserAgent, downloadRequest.mTrustAllCerts));
                    if (sendAuthRequest != null) {
                        this.mHttpRequest.disconnect();
                        this.mRequest.mUrl = sendAuthRequest;
                        return sendEmptyGetRequest(i);
                    }
                }
                Log.e(LOG_TAG, "sendGetRequest: OpenId Process failed!");
                this.mRequest.mCallback.onCanceled(CancelReason.ERROR, -1, -1);
                return -1;
            } else if (i2 != 401) {
                try {
                    IMSLog.e(LOG_TAG, "Receive HTTP response " + this.mHttpRequest.message() + " neither OK nor UNAUTHORIZED");
                    cancelRequest(this.mHttpRequest);
                } catch (HttpRequest.HttpRequestException e) {
                    e = e;
                    if (e.getCause() != null) {
                        e.getCause().printStackTrace();
                    }
                    if (isPermanentFailCause(e)) {
                        this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 30, -1);
                    } else {
                        this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 3, -1);
                    }
                    Log.e(LOG_TAG, e.getCause() + " happened. Retry download Task.");
                    return i2;
                } catch (OutOfMemoryError e2) {
                    e = e2;
                    e.printStackTrace();
                    this.mRequest.mCallback.onCanceled(CancelReason.ERROR, -1, -1);
                    return i2;
                } catch (IllegalArgumentException e3) {
                    e = e3;
                    e.printStackTrace();
                    this.mRequest.mCallback.onCanceled(CancelReason.DEVICE_UNREGISTERED, -1, -1);
                    return i2;
                } catch (RuntimeException e4) {
                    e = e4;
                    e.printStackTrace();
                    this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 3, -1);
                    return i2;
                }
                return i2;
            } else {
                String str2 = LOG_TAG;
                Log.i(str2, "Receive 401 Unauthorized, attempt to generate response");
                this.mHttpRequest.disconnect();
                String wwwAuthenticate = this.mHttpRequest.wwwAuthenticate();
                IMSLog.s(str2, "challenge: " + wwwAuthenticate);
                String authorizationHeader = HttpAuthGenerator.getAuthorizationHeader(this.mPhoneId, this.mRequest.mUrl, wwwAuthenticate, "GET", this.mHttpRequest.getCipherSuite());
                this.mHttpRequest = HttpRequest.get(this.mRequest.mUrl);
                setDefaultHeaders(i);
                this.mHttpRequest.authorization(authorizationHeader);
                return this.mHttpRequest.code();
            }
        } catch (HttpRequest.HttpRequestException e5) {
            e = e5;
            i2 = -1;
        } catch (OutOfMemoryError e6) {
            e = e6;
            i2 = -1;
            e.printStackTrace();
            this.mRequest.mCallback.onCanceled(CancelReason.ERROR, -1, -1);
            return i2;
        } catch (IllegalArgumentException e7) {
            e = e7;
            i2 = -1;
            e.printStackTrace();
            this.mRequest.mCallback.onCanceled(CancelReason.DEVICE_UNREGISTERED, -1, -1);
            return i2;
        } catch (RuntimeException e8) {
            e = e8;
            i2 = -1;
            e.printStackTrace();
            this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 3, -1);
            return i2;
        }
    }

    private int sendEmptyGetRequest(int i) {
        this.mHttpRequest = HttpRequest.get(this.mRequest.mUrl);
        setDefaultHeaders(i);
        return this.mHttpRequest.code();
    }

    private void setDefaultHeaders(int i) {
        HttpRequest httpRequest = this.mHttpRequest;
        DownloadRequest downloadRequest = this.mRequest;
        httpRequest.setParams(downloadRequest.mNetwork, false, 10000, FileTaskUtil.READ_DATA_TIMEOUT, downloadRequest.mUserAgent).bufferSize(i);
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.IS_EAP_SUPPORTED)) {
            this.mHttpRequest.header("Accept", "application/vnd.gsma.eap-relay.v1.0+json");
        }
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FT_WITH_GBA) && this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.USE_USERIDENTITY_FOR_FTHTTP)) {
            String str = (String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId)).map(new DownloadFileTask$$ExternalSyntheticLambda0()).orElse("");
            if (TextUtils.isEmpty(str)) {
                str = ImsUtil.getPublicId(this.mPhoneId);
            }
            HttpRequest httpRequest2 = this.mHttpRequest;
            httpRequest2.header("X-3GPP-Intended-Identity", CmcConstants.E_NUM_STR_QUOTE + str + CmcConstants.E_NUM_STR_QUOTE);
        }
        long j = this.mTransferred;
        if (j > 0) {
            this.mHttpRequest.range(j, -1);
        }
        if (this.mRequest.mTrustAllCerts) {
            this.mHttpRequest.trustAllCerts().trustAllHosts();
        }
    }
}
