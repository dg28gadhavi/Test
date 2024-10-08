package com.sec.internal.ims.cmstore.syncschedulers;

import android.os.Looper;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;

public class MultiLineScheduler extends BaseMessagingScheduler {
    private String TAG = MultiLineScheduler.class.getSimpleName();
    private final MultiLineStatusBuilder mBufferDbQuery;

    public MultiLineScheduler(MessageStoreClient messageStoreClient, CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule, SummaryQueryBuilder summaryQueryBuilder, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, Looper looper) {
        super(messageStoreClient, cloudMessageBufferDBEventSchedulingRule, iDeviceDataChangeListener, iBufferDBEventListener, looper, summaryQueryBuilder);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mBufferDbQuery = new MultiLineStatusBuilder(messageStoreClient, iBufferDBEventListener);
    }

    public void resetImsi() {
        Log.i(this.TAG, "resetImsi");
        this.mBufferDbQuery.resetImsi();
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x002c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void insertNewLine(java.lang.String r6, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r7) {
        /*
            r5 = this;
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r0 = r5.mBufferDbQuery
            android.database.Cursor r0 = r0.queryUsingLineAndSyncMsgType(r6, r7)
            if (r0 == 0) goto L_0x0025
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0030 }
            if (r1 == 0) goto L_0x0025
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r1 = r5.mBufferDbQuery     // Catch:{ all -> 0x0030 }
            java.lang.String r2 = ""
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DEFAULT     // Catch:{ all -> 0x0030 }
            int r4 = r3.getId()     // Catch:{ all -> 0x0030 }
            r1.updateLineInitsyncStatus(r6, r7, r2, r4)     // Catch:{ all -> 0x0030 }
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r5 = r5.mBufferDbQuery     // Catch:{ all -> 0x0030 }
            int r1 = r3.getId()     // Catch:{ all -> 0x0030 }
            r5.updateLineUploadStatus(r6, r7, r1)     // Catch:{ all -> 0x0030 }
            goto L_0x002a
        L_0x0025:
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r5 = r5.mBufferDbQuery     // Catch:{ all -> 0x0030 }
            r5.insertNewLine(r6, r7)     // Catch:{ all -> 0x0030 }
        L_0x002a:
            if (r0 == 0) goto L_0x002f
            r0.close()
        L_0x002f:
            return
        L_0x0030:
            r5 = move-exception
            if (r0 == 0) goto L_0x003b
            r0.close()     // Catch:{ all -> 0x0037 }
            goto L_0x003b
        L_0x0037:
            r6 = move-exception
            r5.addSuppressed(r6)
        L_0x003b:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler.insertNewLine(java.lang.String, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType):void");
    }

    public void updateLineUploadStatus(String str, SyncMsgType syncMsgType, int i) {
        this.mBufferDbQuery.updateLineUploadStatus(str, syncMsgType, i);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getLineUploadStatus(java.lang.String r3, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r4) {
        /*
            r2 = this;
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DEFAULT
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r1 = r2.mBufferDbQuery
            android.database.Cursor r3 = r1.queryUsingLineAndSyncMsgType(r3, r4)
            if (r3 == 0) goto L_0x0029
            boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x001f }
            if (r4 == 0) goto L_0x0029
            java.lang.String r4 = "initupload_status"
            int r4 = r3.getColumnIndex(r4)     // Catch:{ all -> 0x001f }
            int r4 = r3.getInt(r4)     // Catch:{ all -> 0x001f }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.valueOf((int) r4)     // Catch:{ all -> 0x001f }
            goto L_0x002a
        L_0x001f:
            r2 = move-exception
            r3.close()     // Catch:{ all -> 0x0024 }
            goto L_0x0028
        L_0x0024:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x0028:
            throw r2
        L_0x0029:
            r4 = r0
        L_0x002a:
            if (r3 == 0) goto L_0x002f
            r3.close()
        L_0x002f:
            java.lang.String r2 = r2.TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r1 = "getLineInitSyncStatus(): "
            r3.append(r1)
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            if (r4 != 0) goto L_0x004c
            int r2 = r0.getId()
            goto L_0x0050
        L_0x004c:
            int r2 = r4.getId()
        L_0x0050:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler.getLineUploadStatus(java.lang.String, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType):int");
    }

    public void updateLineInitsyncStatus(String str, SyncMsgType syncMsgType, String str2, int i) {
        this.mBufferDbQuery.updateLineInitsyncStatus(str, syncMsgType, str2, i);
    }

    public void deleteLine(String str, SyncMsgType syncMsgType) {
        this.mBufferDbQuery.deleteLine(str, syncMsgType);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x004c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getLineInitSyncStatus(java.lang.String r3, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r4) {
        /*
            r2 = this;
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DEFAULT
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r1 = r2.mBufferDbQuery
            android.database.Cursor r3 = r1.queryUsingLineAndSyncMsgType(r3, r4)
            if (r3 == 0) goto L_0x0029
            boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x001f }
            if (r4 == 0) goto L_0x0029
            java.lang.String r4 = "initsync_status"
            int r4 = r3.getColumnIndex(r4)     // Catch:{ all -> 0x001f }
            int r4 = r3.getInt(r4)     // Catch:{ all -> 0x001f }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.valueOf((int) r4)     // Catch:{ all -> 0x001f }
            goto L_0x002a
        L_0x001f:
            r2 = move-exception
            r3.close()     // Catch:{ all -> 0x0024 }
            goto L_0x0028
        L_0x0024:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x0028:
            throw r2
        L_0x0029:
            r4 = r0
        L_0x002a:
            if (r3 == 0) goto L_0x002f
            r3.close()
        L_0x002f:
            java.lang.String r2 = r2.TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r1 = "getLineInitSyncStatus(): "
            r3.append(r1)
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            if (r4 != 0) goto L_0x004c
            int r2 = r0.getId()
            goto L_0x0050
        L_0x004c:
            int r2 = r4.getId()
        L_0x0050:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler.getLineInitSyncStatus(java.lang.String, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType):int");
    }
}
