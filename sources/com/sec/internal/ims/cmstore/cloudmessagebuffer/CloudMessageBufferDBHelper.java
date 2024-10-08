package com.sec.internal.ims.cmstore.cloudmessagebuffer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValueList;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import java.util.ArrayList;
import java.util.Iterator;

public class CloudMessageBufferDBHelper extends Handler {
    private String TAG = CloudMessageBufferDBHelper.class.getSimpleName();
    protected BufferDBChangeParamList mBufferDBChangeNetAPI = null;
    protected boolean mBufferDBloaded = false;
    protected final IBufferDBEventListener mCallbackMsgApp;
    protected final Context mContext;
    protected final IDeviceDataChangeListener mDeviceDataChangeListener;
    protected boolean mIsCmsEnabled = false;
    protected boolean mIsGoforwardSync = false;
    protected final MmsScheduler mMmsScheduler;
    protected final MultiLineScheduler mMultiLnScheduler;
    protected int mPhoneId = 0;
    protected boolean mProvisionSuccess = false;
    protected boolean mRCSDbReady = false;
    protected final RcsScheduler mRcsScheduler;
    protected final CloudMessageBufferDBEventSchedulingRule mScheduleRule;
    protected final SmsScheduler mSmsScheduler;
    protected MessageStoreClient mStoreClient;
    protected final SummaryQueryBuilder mSummaryQuery;
    protected final VVMScheduler mVVMScheduler;

    /* access modifiers changed from: protected */
    public void handleBufferDbReadMessageJson(String str) {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CloudMessageBufferDBHelper(Looper looper, MessageStoreClient messageStoreClient, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, boolean z) {
        super(looper);
        IBufferDBEventListener iBufferDBEventListener2 = iBufferDBEventListener;
        String str = this.TAG + "[" + messageStoreClient.getClientID() + "]";
        this.TAG = str;
        Log.d(str, "onCreate");
        this.mStoreClient = messageStoreClient;
        this.mPhoneId = messageStoreClient.getClientID();
        this.mContext = messageStoreClient.getContext();
        this.mIsCmsEnabled = z;
        this.mBufferDBloaded = this.mStoreClient.getPrerenceManager().getBufferDbLoaded();
        CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule = new CloudMessageBufferDBEventSchedulingRule();
        this.mScheduleRule = cloudMessageBufferDBEventSchedulingRule;
        this.mDeviceDataChangeListener = iDeviceDataChangeListener;
        this.mCallbackMsgApp = iBufferDBEventListener2;
        this.mBufferDBChangeNetAPI = new BufferDBChangeParamList();
        SummaryQueryBuilder summaryQueryBuilder = new SummaryQueryBuilder(this.mStoreClient, iBufferDBEventListener2);
        this.mSummaryQuery = summaryQueryBuilder;
        CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule2 = cloudMessageBufferDBEventSchedulingRule;
        SummaryQueryBuilder summaryQueryBuilder2 = summaryQueryBuilder;
        MultiLineScheduler multiLineScheduler = new MultiLineScheduler(this.mStoreClient, cloudMessageBufferDBEventSchedulingRule2, summaryQueryBuilder2, iDeviceDataChangeListener, iBufferDBEventListener, looper);
        this.mMultiLnScheduler = multiLineScheduler;
        MultiLineScheduler multiLineScheduler2 = multiLineScheduler;
        IDeviceDataChangeListener iDeviceDataChangeListener2 = iDeviceDataChangeListener;
        IBufferDBEventListener iBufferDBEventListener3 = iBufferDBEventListener;
        Looper looper2 = looper;
        SmsScheduler smsScheduler = new SmsScheduler(this.mStoreClient, cloudMessageBufferDBEventSchedulingRule2, summaryQueryBuilder2, multiLineScheduler2, iDeviceDataChangeListener2, iBufferDBEventListener3, looper2);
        this.mSmsScheduler = smsScheduler;
        MmsScheduler mmsScheduler = new MmsScheduler(this.mStoreClient, cloudMessageBufferDBEventSchedulingRule2, summaryQueryBuilder2, multiLineScheduler2, iDeviceDataChangeListener2, iBufferDBEventListener3, looper2);
        this.mMmsScheduler = mmsScheduler;
        IDeviceDataChangeListener iDeviceDataChangeListener3 = iDeviceDataChangeListener;
        IBufferDBEventListener iBufferDBEventListener4 = iBufferDBEventListener;
        RcsScheduler rcsScheduler = r1;
        RcsScheduler rcsScheduler2 = new RcsScheduler(this.mStoreClient, cloudMessageBufferDBEventSchedulingRule2, summaryQueryBuilder2, iDeviceDataChangeListener3, iBufferDBEventListener4, mmsScheduler, smsScheduler, looper);
        this.mRcsScheduler = rcsScheduler;
        this.mVVMScheduler = new VVMScheduler(this.mStoreClient, cloudMessageBufferDBEventSchedulingRule2, summaryQueryBuilder2, iDeviceDataChangeListener3, iBufferDBEventListener4, looper);
    }

    public void resetImsi() {
        this.mRcsScheduler.resetImsi();
        this.mMultiLnScheduler.resetImsi();
        this.mSummaryQuery.resetImsi();
        this.mVVMScheduler.resetImsi();
    }

    /* access modifiers changed from: protected */
    public void buildBufferList(BufferDBChangeParamList bufferDBChangeParamList, Cursor cursor, int i, boolean z, boolean z2) {
        if (z2) {
            if (cursor != null && cursor.moveToFirst()) {
                String str = this.TAG;
                Log.i(str, "bufferlist query count for isUpload : " + cursor.getCount());
                do {
                    int i2 = i;
                    boolean z3 = z;
                    bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(i2, (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)), z3, (String) null, this.mStoreClient));
                } while (cursor.moveToNext());
            }
        } else if (cursor != null && cursor.moveToFirst()) {
            String str2 = this.TAG;
            Log.i(str2, "bufferlist query count: " + cursor.getCount());
            do {
                int i3 = cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                int i4 = i;
                boolean z4 = z;
                bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(i4, (long) i3, z4, cursor.getString(cursor.getColumnIndexOrThrow("linenum")), this.mStoreClient));
            } while (cursor.moveToNext());
        }
    }

    /* access modifiers changed from: protected */
    public void onSendPayloadObject(String str, SyncMsgType syncMsgType) {
        Log.i(this.TAG, "onSendPayloadObject");
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        if (SyncMsgType.MESSAGE.equals(syncMsgType) || SyncMsgType.DEFAULT.equals(syncMsgType)) {
            MmsScheduler mmsScheduler = this.mMmsScheduler;
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri;
            Cursor queryToDeviceUnDownloadedMms = mmsScheduler.queryToDeviceUnDownloadedMms(str, actionStatusFlag.getId());
            try {
                buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedMms, 4, false, false);
                if (queryToDeviceUnDownloadedMms != null) {
                    queryToDeviceUnDownloadedMms.close();
                }
                Cursor queryToDeviceUnDownloadedMms2 = this.mMmsScheduler.queryToDeviceUnDownloadedMms(str, CloudMessageBufferDBConstants.ActionStatusFlag.FetchForce.getId());
                try {
                    buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedMms2, 4, false, false);
                    if (queryToDeviceUnDownloadedMms2 != null) {
                        queryToDeviceUnDownloadedMms2.close();
                    }
                    Cursor queryToDeviceUnDownloadedRcs = this.mRcsScheduler.queryToDeviceUnDownloadedRcs(str, actionStatusFlag.getId());
                    try {
                        buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedRcs, 1, false, false);
                        if (queryToDeviceUnDownloadedRcs != null) {
                            queryToDeviceUnDownloadedRcs.close();
                        }
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        }
        if (bufferDBChangeParamList.mChangelst.size() > 0) {
            notifyUriRequesttoApp(bufferDBChangeParamList);
            return;
        }
        return;
        throw th;
        throw th;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void onUnDownloadedMmsMessageForMcs(String str, SyncMsgType syncMsgType) {
        Log.i(this.TAG, "onUnDownloadedMmsMessageForMcs");
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        int messageType = getMessageType(syncMsgType);
        if (SyncMsgType.MESSAGE.equals(syncMsgType) || SyncMsgType.DEFAULT.equals(syncMsgType)) {
            Cursor queryToDeviceUnDownloadedMms = this.mMmsScheduler.queryToDeviceUnDownloadedMms(str, CloudMessageBufferDBConstants.ActionStatusFlag.FetchForce.getId());
            try {
                buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedMms, 4, false, false);
                if (queryToDeviceUnDownloadedMms != null) {
                    queryToDeviceUnDownloadedMms.close();
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (bufferDBChangeParamList.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceInitialSyncDownload(bufferDBChangeParamList);
            return;
        } else if (messageType >= 0) {
            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(messageType, 0, false, str, this.mStoreClient));
            this.mDeviceDataChangeListener.sendDeviceInitialSyncDownload(bufferDBChangeParamList);
            return;
        } else {
            return;
        }
        throw th;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public void onSendMCSUnDownloadedMessage(String str, SyncMsgType syncMsgType, boolean z) {
        Throwable th;
        Throwable th2;
        String str2 = str;
        SyncMsgType syncMsgType2 = syncMsgType;
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        int messageType = getMessageType(syncMsgType);
        if (this.mMmsScheduler.queryPendingFetchForce() > 0) {
            onUnDownloadedMmsMessageForMcs(str, SyncMsgType.MESSAGE);
            return;
        }
        if (SyncMsgType.MESSAGE.equals(syncMsgType) || SyncMsgType.DEFAULT.equals(syncMsgType)) {
            MmsScheduler mmsScheduler = this.mMmsScheduler;
            CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad;
            Cursor queryToDeviceUnDownloadedMms = mmsScheduler.queryToDeviceUnDownloadedMms(str, actionStatusFlag.getId());
            try {
                buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedMms, 4, z, false);
                if (queryToDeviceUnDownloadedMms != null) {
                    queryToDeviceUnDownloadedMms.close();
                }
                Cursor queryToDeviceUnDownloadedRcs = this.mRcsScheduler.queryToDeviceUnDownloadedRcs(str, actionStatusFlag.getId());
                try {
                    buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedRcs, 1, z, false);
                    if (queryToDeviceUnDownloadedRcs != null) {
                        queryToDeviceUnDownloadedRcs.close();
                    }
                } catch (Throwable th3) {
                    th2.addSuppressed(th3);
                }
            } catch (Throwable th4) {
                th.addSuppressed(th4);
            }
        }
        if (bufferDBChangeParamList.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceInitialSyncDownload(bufferDBChangeParamList);
            return;
        } else if (messageType >= 0) {
            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(messageType, 0, z, str, this.mStoreClient));
            this.mDeviceDataChangeListener.sendDeviceInitialSyncDownload(bufferDBChangeParamList);
            return;
        } else {
            return;
        }
        throw th;
        throw th2;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public void onSendUnDownloadedMessage(String str, SyncMsgType syncMsgType, boolean z, int i) {
        int i2;
        Throwable th;
        Throwable th2;
        Throwable th3;
        Throwable th4;
        String str2 = str;
        SyncMsgType syncMsgType2 = syncMsgType;
        int i3 = i;
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        int messageType = getMessageType(syncMsgType2);
        if (SyncMsgType.MESSAGE.equals(syncMsgType2) || SyncMsgType.DEFAULT.equals(syncMsgType2)) {
            int queryPendingUrlFetch = this.mRcsScheduler.queryPendingUrlFetch();
            int queryPendingFetchForce = this.mMmsScheduler.queryPendingFetchForce();
            String str3 = this.TAG;
            Log.i(str3, "onSendUnDownloadedMessage syncAction: " + i3 + ", pendingRCSCount: " + queryPendingUrlFetch + " pendingLegacyMMSCount" + queryPendingFetchForce);
            Cursor queryToDeviceUnDownloadedMms = this.mMmsScheduler.queryToDeviceUnDownloadedMms(str, i3);
            try {
                if (i3 == CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId()) {
                    buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedMms, 4, z, false);
                } else if (queryToDeviceUnDownloadedMms != null && queryToDeviceUnDownloadedMms.moveToFirst()) {
                    do {
                        int i4 = queryToDeviceUnDownloadedMms.getInt(queryToDeviceUnDownloadedMms.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        long j = (long) i4;
                        this.mMmsScheduler.addMmsPartDownloadList(bufferDBChangeParamList, j, queryToDeviceUnDownloadedMms.getString(queryToDeviceUnDownloadedMms.getColumnIndexOrThrow("linenum")), z);
                    } while (queryToDeviceUnDownloadedMms.moveToNext());
                }
                if (queryToDeviceUnDownloadedMms != null) {
                    queryToDeviceUnDownloadedMms.close();
                }
                Cursor queryToDeviceUnDownloadedRcs = this.mRcsScheduler.queryToDeviceUnDownloadedRcs(str, i3);
                try {
                    buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedRcs, 1, z, false);
                    if (queryToDeviceUnDownloadedRcs != null) {
                        queryToDeviceUnDownloadedRcs.close();
                    }
                    i2 = queryPendingFetchForce;
                } catch (Throwable th5) {
                    th2.addSuppressed(th5);
                }
            } catch (Throwable th6) {
                th.addSuppressed(th6);
            }
        } else {
            if (SyncMsgType.VM.equals(syncMsgType2)) {
                Cursor queryToDeviceUnDownloadedVvm = this.mVVMScheduler.queryToDeviceUnDownloadedVvm(str, i3);
                try {
                    buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedVvm, 17, z, false);
                    if (bufferDBChangeParamList.mChangelst.size() == 0) {
                        this.mDeviceDataChangeListener.setVVMSyncState(false);
                    }
                    if (queryToDeviceUnDownloadedVvm != null) {
                        queryToDeviceUnDownloadedVvm.close();
                    }
                } catch (Throwable th7) {
                    th4.addSuppressed(th7);
                }
            } else if (SyncMsgType.VM_GREETINGS.equals(syncMsgType2)) {
                Cursor queryToDeviceUnDownloadedGreeting = this.mVVMScheduler.queryToDeviceUnDownloadedGreeting(str, i3);
                try {
                    buildBufferList(bufferDBChangeParamList, queryToDeviceUnDownloadedGreeting, 18, z, false);
                    if (queryToDeviceUnDownloadedGreeting != null) {
                        queryToDeviceUnDownloadedGreeting.close();
                    }
                } catch (Throwable th8) {
                    th3.addSuppressed(th8);
                }
            }
            i2 = -1;
        }
        if (bufferDBChangeParamList.mChangelst.size() > 0) {
            if (this.mIsCmsEnabled) {
                if (i3 == CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId() && i2 > 0) {
                    onUnDownloadedMmsMessageForMcs(str, SyncMsgType.MESSAGE);
                    return;
                } else if (i3 == CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()) {
                    this.mDeviceDataChangeListener.sendDeviceInitialSyncDownload(bufferDBChangeParamList);
                    return;
                } else {
                    notifyUriRequesttoApp(bufferDBChangeParamList);
                    return;
                }
            } else if (i3 == CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()) {
                this.mDeviceDataChangeListener.sendDeviceInitialSyncDownload(bufferDBChangeParamList);
                return;
            } else {
                notifyUriRequesttoApp(bufferDBChangeParamList);
                return;
            }
        } else if (messageType >= 0 && this.mIsCmsEnabled && i3 == CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId()) {
            onSendMCSUnDownloadedMessage(str, SyncMsgType.MESSAGE, false);
            return;
        } else if (messageType >= 0) {
            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(messageType, 0, z, str, this.mStoreClient));
            this.mDeviceDataChangeListener.sendDeviceInitialSyncDownload(bufferDBChangeParamList);
            return;
        } else {
            return;
        }
        throw th3;
        throw th;
        throw th4;
        throw th2;
    }

    /* access modifiers changed from: protected */
    public void checkRCSNotifyUriRequestToApp(BufferDBChangeParamList bufferDBChangeParamList, int i) {
        ArrayList<BufferDBChangeParam> arrayList = bufferDBChangeParamList.mChangelst;
        Log.i(this.TAG, "checkRCSNotifyUriRequestToApp " + arrayList.size());
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(new JsonArray());
        if (i == CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId()) {
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                BufferDBChangeParam bufferDBChangeParam = arrayList.get(i2);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", String.valueOf(bufferDBChangeParam.mRowId));
                if (bufferDBChangeParam.mDBIndex == 1) {
                    ((JsonArray) arrayList2.get(0)).add(jsonObject);
                }
            }
            if (((JsonArray) arrayList2.get(0)).size() > 0) {
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FT", ((JsonArray) arrayList2.get(0)).toString(), false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyUriRequesttoApp(BufferDBChangeParamList bufferDBChangeParamList) {
        String str;
        ArrayList<BufferDBChangeParam> arrayList = bufferDBChangeParamList.mChangelst;
        Log.i(this.TAG, "notifyUriRequesttoApp " + arrayList.size());
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < 4; i++) {
            arrayList2.add(new JsonArray());
        }
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            BufferDBChangeParam bufferDBChangeParam = arrayList.get(i2);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", String.valueOf(bufferDBChangeParam.mRowId));
            int i3 = bufferDBChangeParam.mDBIndex;
            if (i3 == 4) {
                ((JsonArray) arrayList2.get(0)).add(jsonObject);
            } else if (i3 == 17) {
                ((JsonArray) arrayList2.get(2)).add(jsonObject);
            } else if (i3 == 18) {
                ((JsonArray) arrayList2.get(3)).add(jsonObject);
            } else {
                ((JsonArray) arrayList2.get(1)).add(jsonObject);
            }
        }
        Log.i(this.TAG, "notifyAppForFtUri notifyMMS " + ((JsonArray) arrayList2.get(0)).size() + " notifyRCS " + ((JsonArray) arrayList2.get(1)).size() + " notifyVVM count " + ((JsonArray) arrayList2.get(2)).size() + " notifyGreeting count " + ((JsonArray) arrayList2.get(3)).size());
        for (int i4 = 0; i4 < arrayList2.size(); i4++) {
            if (((JsonArray) arrayList2.get(i4)).size() > 0) {
                String str2 = CloudMessageProviderContract.ApplicationTypes.MSGDATA;
                if (i4 == 0) {
                    str = CloudMessageProviderContract.DataTypes.MMS;
                } else if (i4 != 1) {
                    str2 = "VVMDATA";
                    if (i4 != 2) {
                        if (i4 != 3) {
                            Log.d(this.TAG, "default apptype:" + "" + " datatype:" + "");
                            str2 = "";
                        } else {
                            str = CloudMessageProviderContract.DataTypes.VVMGREETING;
                        }
                    }
                    str = str2;
                } else {
                    str = "FT";
                }
                this.mCallbackMsgApp.notifyCloudMessageUpdate(str2, str, ((JsonArray) arrayList2.get(i4)).toString(), false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onSendCloudUnSyncedUpdate() {
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        Cursor queryToCloudUnsyncedSms = this.mSmsScheduler.queryToCloudUnsyncedSms();
        try {
            buildBufferList(bufferDBChangeParamList, queryToCloudUnsyncedSms, 3, false, false);
            if (queryToCloudUnsyncedSms != null) {
                queryToCloudUnsyncedSms.close();
            }
            Cursor queryToCloudUnsyncedMms = this.mMmsScheduler.queryToCloudUnsyncedMms();
            try {
                buildBufferList(bufferDBChangeParamList, queryToCloudUnsyncedMms, 4, false, false);
                if (queryToCloudUnsyncedMms != null) {
                    queryToCloudUnsyncedMms.close();
                }
                Cursor queryToCloudUnsyncedRcs = this.mRcsScheduler.queryToCloudUnsyncedRcs();
                try {
                    buildBufferList(bufferDBChangeParamList, queryToCloudUnsyncedRcs, 1, false, false);
                    if (queryToCloudUnsyncedRcs != null) {
                        queryToCloudUnsyncedRcs.close();
                    }
                    if (bufferDBChangeParamList.mChangelst.size() > 0) {
                        this.mDeviceDataChangeListener.sendDeviceUpdate(bufferDBChangeParamList);
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
            throw th;
            throw th;
        } catch (Throwable th3) {
            th.addSuppressed(th3);
        }
    }

    /* access modifiers changed from: protected */
    public void onSendDeviceUnSyncedUpdate() {
        Cursor queryToDeviceUnsyncedSms = this.mSmsScheduler.queryToDeviceUnsyncedSms();
        if (queryToDeviceUnsyncedSms != null) {
            try {
                if (queryToDeviceUnsyncedSms.moveToFirst()) {
                    do {
                        int i = queryToDeviceUnsyncedSms.getInt(queryToDeviceUnsyncedSms.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        SmsScheduler smsScheduler = this.mSmsScheduler;
                        smsScheduler.notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, smsScheduler.getMessageTypeString(3, false), (long) i, false);
                    } while (queryToDeviceUnsyncedSms.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryToDeviceUnsyncedSms != null) {
            queryToDeviceUnsyncedSms.close();
        }
        Cursor queryToDeviceUnsyncedMms = this.mMmsScheduler.queryToDeviceUnsyncedMms();
        if (queryToDeviceUnsyncedMms != null) {
            try {
                if (queryToDeviceUnsyncedMms.moveToFirst()) {
                    do {
                        int i2 = queryToDeviceUnsyncedMms.getInt(queryToDeviceUnsyncedMms.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        MmsScheduler mmsScheduler = this.mMmsScheduler;
                        mmsScheduler.notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, mmsScheduler.getMessageTypeString(4, false), (long) i2, false);
                    } while (queryToDeviceUnsyncedMms.moveToNext());
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryToDeviceUnsyncedMms != null) {
            queryToDeviceUnsyncedMms.close();
        }
        Cursor queryToDeviceUnsyncedRcs = this.mRcsScheduler.queryToDeviceUnsyncedRcs();
        if (queryToDeviceUnsyncedRcs != null) {
            try {
                if (queryToDeviceUnsyncedRcs.moveToFirst()) {
                    do {
                        int i3 = queryToDeviceUnsyncedRcs.getInt(queryToDeviceUnsyncedRcs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        boolean z = queryToDeviceUnsyncedRcs.getInt(queryToDeviceUnsyncedRcs.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1;
                        RcsScheduler rcsScheduler = this.mRcsScheduler;
                        rcsScheduler.notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, rcsScheduler.getMessageTypeString(1, z), (long) i3, false);
                    } while (queryToDeviceUnsyncedRcs.moveToNext());
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        }
        if (queryToDeviceUnsyncedRcs != null) {
            queryToDeviceUnsyncedRcs.close();
            return;
        }
        return;
        throw th;
        throw th;
        throw th;
    }

    private int getMessageType(SyncMsgType syncMsgType) {
        if (SyncMsgType.MESSAGE.equals(syncMsgType) || SyncMsgType.DEFAULT.equals(syncMsgType)) {
            return 1;
        }
        if (SyncMsgType.VM.equals(syncMsgType)) {
            return 17;
        }
        return SyncMsgType.VM_GREETINGS.equals(syncMsgType) ? 18 : -1;
    }

    /* access modifiers changed from: protected */
    public void notifyNetAPIUploadMessages(String str, SyncMsgType syncMsgType, boolean z) {
        IMSLog.i(this.TAG, "notifyNetAPIUploadMessages Message upload started");
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        if (!this.mStoreClient.getCloudMessageStrategyManager().getStrategy().requiresMsgUploadInInitSync()) {
            int messageType = getMessageType(syncMsgType);
            if (messageType >= 0) {
                bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(messageType, 0, z, str, this.mStoreClient));
            }
            this.mDeviceDataChangeListener.sendDeviceUpload(bufferDBChangeParamList);
            return;
        }
        if (this.mIsCmsEnabled) {
            Cursor queryGroupSession = this.mRcsScheduler.queryGroupSession();
            try {
                buildBufferList(bufferDBChangeParamList, queryGroupSession, 10, z, true);
                addSessionMessagesToList(queryGroupSession, bufferDBChangeParamList, z);
                if (queryGroupSession != null) {
                    queryGroupSession.close();
                }
                Cursor queryOneToOneSession = this.mRcsScheduler.queryOneToOneSession();
                try {
                    addSessionMessagesToList(queryOneToOneSession, bufferDBChangeParamList, z);
                    if (queryOneToOneSession != null) {
                        queryOneToOneSession.close();
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        Cursor querySMSMessagesToUpload = this.mSmsScheduler.querySMSMessagesToUpload();
        try {
            buildBufferList(bufferDBChangeParamList, querySMSMessagesToUpload, 3, z, true);
            if (querySMSMessagesToUpload != null) {
                querySMSMessagesToUpload.close();
            }
            Cursor queryMMSMessagesToUpload = this.mMmsScheduler.queryMMSMessagesToUpload();
            try {
                buildBufferList(bufferDBChangeParamList, queryMMSMessagesToUpload, 4, z, true);
                if (queryMMSMessagesToUpload != null) {
                    queryMMSMessagesToUpload.close();
                }
                if (!this.mIsCmsEnabled) {
                    Cursor queryRCSMessagesToUpload = this.mRcsScheduler.queryRCSMessagesToUpload();
                    try {
                        buildBufferList(bufferDBChangeParamList, queryRCSMessagesToUpload, 1, z, true);
                        if (queryRCSMessagesToUpload != null) {
                            queryRCSMessagesToUpload.close();
                        }
                        Cursor queryImdnMessagesToUpload = this.mRcsScheduler.queryImdnMessagesToUpload();
                        try {
                            buildBufferList(bufferDBChangeParamList, queryImdnMessagesToUpload, 13, z, true);
                            if (queryImdnMessagesToUpload != null) {
                                queryImdnMessagesToUpload.close();
                            }
                        } catch (Throwable th3) {
                            th.addSuppressed(th3);
                        }
                    } catch (Throwable th4) {
                        th.addSuppressed(th4);
                    }
                }
                if (bufferDBChangeParamList.mChangelst.size() > 0) {
                    this.mDeviceDataChangeListener.sendDeviceUpload(bufferDBChangeParamList);
                    return;
                }
                bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(1, 0, z, str, this.mStoreClient));
                this.mDeviceDataChangeListener.sendDeviceUpload(bufferDBChangeParamList);
                return;
            } catch (Throwable th5) {
                th.addSuppressed(th5);
            }
        } catch (Throwable th6) {
            th.addSuppressed(th6);
        }
        throw th;
        throw th;
        throw th;
        throw th;
        throw th;
        throw th;
    }

    private void addSessionMessagesToList(Cursor cursor, BufferDBChangeParamList bufferDBChangeParamList, boolean z) {
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String string = cursor.getString(cursor.getColumnIndexOrThrow("chat_id"));
                Cursor queryRCSMessagesToUploadByMessageType = this.mRcsScheduler.queryRCSMessagesToUploadByMessageType(string);
                try {
                    buildBufferList(bufferDBChangeParamList, queryRCSMessagesToUploadByMessageType, 1, z, true);
                    if (queryRCSMessagesToUploadByMessageType != null) {
                        queryRCSMessagesToUploadByMessageType.close();
                    }
                    Cursor queryRCSFtMessagesToUpload = this.mRcsScheduler.queryRCSFtMessagesToUpload(string);
                    try {
                        buildBufferList(bufferDBChangeParamList, queryRCSFtMessagesToUpload, 12, z, true);
                        if (queryRCSFtMessagesToUpload != null) {
                            queryRCSFtMessagesToUpload.close();
                        }
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            } while (cursor.moveToNext());
            return;
        }
        return;
        throw th;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void handleBulkOpSingleUrlSuccess(String str) {
        String str2 = this.TAG;
        Log.d(str2, "handleBulkDeleteSingleUrlSuccess: " + IMSLog.checker(str));
        if (str != null) {
            Cursor querySummaryDBwithResUrl = this.mSummaryQuery.querySummaryDBwithResUrl(str);
            if (querySummaryDBwithResUrl != null) {
                try {
                    if (querySummaryDBwithResUrl.moveToFirst()) {
                        onUpdateBufferDBBulkUpdateSuccess(querySummaryDBwithResUrl, str);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (querySummaryDBwithResUrl != null) {
                querySummaryDBwithResUrl.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:70:0x017b  */
    /* JADX WARNING: Removed duplicated region for block: B:72:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onUpdateBufferDBBulkUpdateSuccess(android.database.Cursor r11, java.lang.String r12) {
        /*
            r10 = this;
            java.lang.String r0 = "syncaction"
            int r0 = r11.getColumnIndexOrThrow(r0)
            int r0 = r11.getInt(r0)
            java.lang.String r1 = "messagetype"
            int r1 = r11.getColumnIndexOrThrow(r1)
            int r3 = r11.getInt(r1)
            java.lang.String r11 = r10.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onUpdateBufferDBBulkUpdateSuccess,  status: "
            r1.append(r2)
            r1.append(r0)
            java.lang.String r0 = " msgtype: "
            r1.append(r0)
            r1.append(r3)
            java.lang.String r0 = r1.toString()
            android.util.Log.d(r11, r0)
            java.lang.String r11 = "linenum"
            java.lang.String r0 = "_bufferdbid"
            r1 = 1
            r9 = 0
            if (r3 == r1) goto L_0x012d
            r2 = 17
            if (r3 == r2) goto L_0x00e1
            r2 = 3
            if (r3 == r2) goto L_0x0094
            r2 = 4
            if (r3 == r2) goto L_0x0047
            goto L_0x0179
        L_0x0047:
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r2 = r10.mMmsScheduler
            android.database.Cursor r12 = r2.queryMMSBufferDBwithResUrl(r12)
            if (r12 == 0) goto L_0x008d
            boolean r2 = r12.moveToFirst()     // Catch:{ all -> 0x0083 }
            if (r2 == 0) goto L_0x008d
            int r0 = r12.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0083 }
            int r0 = r12.getInt(r0)     // Catch:{ all -> 0x0083 }
            long r4 = (long) r0     // Catch:{ all -> 0x0083 }
            int r11 = r12.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x0083 }
            java.lang.String r7 = r12.getString(r11)     // Catch:{ all -> 0x0083 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r11 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x0083 }
            r11.<init>()     // Catch:{ all -> 0x0083 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0083 }
            r6 = 0
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r10.mStoreClient     // Catch:{ all -> 0x0083 }
            r2 = r0
            r2.<init>(r3, r4, r6, r7, r8)     // Catch:{ all -> 0x0083 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r11 = r11.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r0)     // Catch:{ all -> 0x0083 }
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r0 = r10.mMmsScheduler     // Catch:{ all -> 0x0083 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r11 = r11.build()     // Catch:{ all -> 0x0083 }
            r0.onCloudUpdateFlagSuccess(r11, r9)     // Catch:{ all -> 0x0083 }
            r1 = r9
            goto L_0x008d
        L_0x0083:
            r10 = move-exception
            r12.close()     // Catch:{ all -> 0x0088 }
            goto L_0x008c
        L_0x0088:
            r11 = move-exception
            r10.addSuppressed(r11)
        L_0x008c:
            throw r10
        L_0x008d:
            if (r12 == 0) goto L_0x0178
            r12.close()
            goto L_0x0178
        L_0x0094:
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r2 = r10.mSmsScheduler
            android.database.Cursor r12 = r2.querySMSBufferDBwithResUrl(r12)
            if (r12 == 0) goto L_0x00da
            boolean r2 = r12.moveToFirst()     // Catch:{ all -> 0x00d0 }
            if (r2 == 0) goto L_0x00da
            int r0 = r12.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x00d0 }
            int r0 = r12.getInt(r0)     // Catch:{ all -> 0x00d0 }
            long r4 = (long) r0     // Catch:{ all -> 0x00d0 }
            int r11 = r12.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x00d0 }
            java.lang.String r7 = r12.getString(r11)     // Catch:{ all -> 0x00d0 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r11 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x00d0 }
            r11.<init>()     // Catch:{ all -> 0x00d0 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x00d0 }
            r6 = 0
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r10.mStoreClient     // Catch:{ all -> 0x00d0 }
            r2 = r0
            r2.<init>(r3, r4, r6, r7, r8)     // Catch:{ all -> 0x00d0 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r11 = r11.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r0)     // Catch:{ all -> 0x00d0 }
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r0 = r10.mSmsScheduler     // Catch:{ all -> 0x00d0 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r11 = r11.build()     // Catch:{ all -> 0x00d0 }
            r0.onCloudUpdateFlagSuccess(r11, r9)     // Catch:{ all -> 0x00d0 }
            r1 = r9
            goto L_0x00da
        L_0x00d0:
            r10 = move-exception
            r12.close()     // Catch:{ all -> 0x00d5 }
            goto L_0x00d9
        L_0x00d5:
            r11 = move-exception
            r10.addSuppressed(r11)
        L_0x00d9:
            throw r10
        L_0x00da:
            if (r12 == 0) goto L_0x0178
            r12.close()
            goto L_0x0178
        L_0x00e1:
            com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler r2 = r10.mVVMScheduler
            android.database.Cursor r12 = r2.queryVVMwithResUrl(r12)
            if (r12 == 0) goto L_0x0127
            boolean r2 = r12.moveToFirst()     // Catch:{ all -> 0x011d }
            if (r2 == 0) goto L_0x0127
            int r0 = r12.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x011d }
            int r0 = r12.getInt(r0)     // Catch:{ all -> 0x011d }
            long r4 = (long) r0     // Catch:{ all -> 0x011d }
            int r11 = r12.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x011d }
            java.lang.String r7 = r12.getString(r11)     // Catch:{ all -> 0x011d }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r11 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x011d }
            r11.<init>()     // Catch:{ all -> 0x011d }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x011d }
            r6 = 0
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r10.mStoreClient     // Catch:{ all -> 0x011d }
            r2 = r0
            r2.<init>(r3, r4, r6, r7, r8)     // Catch:{ all -> 0x011d }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r11 = r11.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r0)     // Catch:{ all -> 0x011d }
            com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler r0 = r10.mVVMScheduler     // Catch:{ all -> 0x011d }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r11 = r11.build()     // Catch:{ all -> 0x011d }
            r0.onCloudUpdateFlagSuccess(r11, r9)     // Catch:{ all -> 0x011d }
            r1 = r9
            goto L_0x0127
        L_0x011d:
            r10 = move-exception
            r12.close()     // Catch:{ all -> 0x0122 }
            goto L_0x0126
        L_0x0122:
            r11 = move-exception
            r10.addSuppressed(r11)
        L_0x0126:
            throw r10
        L_0x0127:
            if (r12 == 0) goto L_0x0178
            r12.close()
            goto L_0x0178
        L_0x012d:
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r2 = r10.mRcsScheduler
            android.database.Cursor r12 = r2.queryRCSBufferDBwithResUrl(r12)
            if (r12 == 0) goto L_0x0173
            boolean r2 = r12.moveToFirst()     // Catch:{ all -> 0x0169 }
            if (r2 == 0) goto L_0x0173
            int r0 = r12.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0169 }
            int r0 = r12.getInt(r0)     // Catch:{ all -> 0x0169 }
            long r4 = (long) r0     // Catch:{ all -> 0x0169 }
            int r11 = r12.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x0169 }
            java.lang.String r7 = r12.getString(r11)     // Catch:{ all -> 0x0169 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r11 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x0169 }
            r11.<init>()     // Catch:{ all -> 0x0169 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0169 }
            r6 = 0
            com.sec.internal.ims.cmstore.MessageStoreClient r8 = r10.mStoreClient     // Catch:{ all -> 0x0169 }
            r2 = r0
            r2.<init>(r3, r4, r6, r7, r8)     // Catch:{ all -> 0x0169 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r11 = r11.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r0)     // Catch:{ all -> 0x0169 }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r0 = r10.mRcsScheduler     // Catch:{ all -> 0x0169 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r11 = r11.build()     // Catch:{ all -> 0x0169 }
            r0.onCloudUpdateFlagSuccess(r11, r9)     // Catch:{ all -> 0x0169 }
            r1 = r9
            goto L_0x0173
        L_0x0169:
            r10 = move-exception
            r12.close()     // Catch:{ all -> 0x016e }
            goto L_0x0172
        L_0x016e:
            r11 = move-exception
            r10.addSuppressed(r11)
        L_0x0172:
            throw r10
        L_0x0173:
            if (r12 == 0) goto L_0x0178
            r12.close()
        L_0x0178:
            r9 = r1
        L_0x0179:
            if (r9 == 0) goto L_0x0182
            java.lang.String r10 = r10.TAG
            java.lang.String r11 = "inconsistency between buffer or duplicated nms event"
            android.util.Log.e(r10, r11)
        L_0x0182:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.onUpdateBufferDBBulkUpdateSuccess(android.database.Cursor, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:59:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNmsEventChangedObjSummaryDbAvailableUsingUrl(android.database.Cursor r4, com.sec.internal.omanetapi.nms.data.ChangedObject r5, boolean r6) {
        /*
            r3 = this;
            java.lang.String r0 = "messagetype"
            int r0 = r4.getColumnIndexOrThrow(r0)
            int r4 = r4.getInt(r0)
            java.lang.String r0 = r3.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onNmsEventChangedObjSummaryDbAvailableUsingUrl(), type: "
            r1.append(r2)
            r1.append(r4)
            java.lang.String r2 = " isgoforwardSync: "
            r1.append(r2)
            r1.append(r6)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            r0 = 1
            if (r4 == r0) goto L_0x009c
            r0 = 14
            if (r4 == r0) goto L_0x009c
            r0 = 3
            if (r4 == r0) goto L_0x0069
            r0 = 4
            if (r4 == r0) goto L_0x003f
            r0 = 11
            if (r4 == r0) goto L_0x009c
            r0 = 12
            if (r4 == r0) goto L_0x009c
            goto L_0x00c5
        L_0x003f:
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r4 = r3.mMmsScheduler
            java.net.URL r0 = r5.resourceURL
            java.lang.String r0 = r0.toString()
            android.database.Cursor r4 = r4.queryMMSBufferDBwithResUrl(r0)
            if (r4 == 0) goto L_0x0063
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x0059 }
            if (r0 == 0) goto L_0x0063
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r3 = r3.mMmsScheduler     // Catch:{ all -> 0x0059 }
            r3.onNmsEventChangedObjBufferDbMmsAvailableUsingUrl(r4, r5, r6)     // Catch:{ all -> 0x0059 }
            goto L_0x0063
        L_0x0059:
            r3 = move-exception
            r4.close()     // Catch:{ all -> 0x005e }
            goto L_0x0062
        L_0x005e:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x0062:
            throw r3
        L_0x0063:
            if (r4 == 0) goto L_0x00c5
            r4.close()
            goto L_0x00c5
        L_0x0069:
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r4 = r3.mSmsScheduler
            java.net.URL r0 = r5.resourceURL
            java.lang.String r0 = r0.toString()
            android.database.Cursor r4 = r4.querySMSBufferDBwithResUrl(r0)
            if (r4 == 0) goto L_0x0083
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x0090 }
            if (r0 == 0) goto L_0x0083
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r3 = r3.mSmsScheduler     // Catch:{ all -> 0x0090 }
            r3.onNmsEventChangedObjBufferDbSmsAvailableUsingUrl(r4, r5, r6)     // Catch:{ all -> 0x0090 }
            goto L_0x008a
        L_0x0083:
            java.lang.String r3 = r3.TAG     // Catch:{ all -> 0x0090 }
            java.lang.String r5 = "inconsistency between buffer or duplicated nms event"
            android.util.Log.e(r3, r5)     // Catch:{ all -> 0x0090 }
        L_0x008a:
            if (r4 == 0) goto L_0x00c5
            r4.close()
            goto L_0x00c5
        L_0x0090:
            r3 = move-exception
            if (r4 == 0) goto L_0x009b
            r4.close()     // Catch:{ all -> 0x0097 }
            goto L_0x009b
        L_0x0097:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x009b:
            throw r3
        L_0x009c:
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r4 = r3.mRcsScheduler
            java.net.URL r0 = r5.resourceURL
            java.lang.String r0 = r0.toString()
            android.database.Cursor r4 = r4.queryRCSBufferDBwithResUrl(r0)
            if (r4 == 0) goto L_0x00c0
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x00b6 }
            if (r0 == 0) goto L_0x00c0
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r3 = r3.mRcsScheduler     // Catch:{ all -> 0x00b6 }
            r3.onNmsEventChangedObjBufferDbRcsAvailableUsingUrl(r4, r5, r6)     // Catch:{ all -> 0x00b6 }
            goto L_0x00c0
        L_0x00b6:
            r3 = move-exception
            r4.close()     // Catch:{ all -> 0x00bb }
            goto L_0x00bf
        L_0x00bb:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x00bf:
            throw r3
        L_0x00c0:
            if (r4 == 0) goto L_0x00c5
            r4.close()
        L_0x00c5:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.onNmsEventChangedObjSummaryDbAvailableUsingUrl(android.database.Cursor, com.sec.internal.omanetapi.nms.data.ChangedObject, boolean):void");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0189  */
    /* JADX WARNING: Removed duplicated region for block: B:98:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject r10, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r11, boolean r12) {
        /*
            r9 = this;
            java.net.URL r0 = r10.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            java.lang.String r0 = r10.protocol
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            java.lang.String r1 = "standard"
            if (r0 == 0) goto L_0x0017
            r0 = r1
            goto L_0x0019
        L_0x0017:
            java.lang.String r0 = r10.protocol
        L_0x0019:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r9.mSummaryQuery
            java.net.URL r3 = r10.resourceURL
            java.lang.String r3 = r3.toString()
            android.database.Cursor r8 = r2.querySummaryDBwithResUrl(r3)
            if (r8 == 0) goto L_0x005f
            boolean r2 = r8.moveToFirst()     // Catch:{ all -> 0x018d }
            if (r2 == 0) goto L_0x005f
            java.lang.String r11 = "syncaction"
            int r11 = r8.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x018d }
            int r11 = r8.getInt(r11)     // Catch:{ all -> 0x018d }
            java.lang.String r0 = r9.TAG     // Catch:{ all -> 0x018d }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x018d }
            r1.<init>()     // Catch:{ all -> 0x018d }
            java.lang.String r2 = "handleCloudNotifyChangedObj, Status: "
            r1.append(r2)     // Catch:{ all -> 0x018d }
            r1.append(r11)     // Catch:{ all -> 0x018d }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x018d }
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x018d }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x018d }
            int r0 = r0.getId()     // Catch:{ all -> 0x018d }
            if (r11 != r0) goto L_0x005a
            r8.close()
            return
        L_0x005a:
            r9.onNmsEventChangedObjSummaryDbAvailableUsingUrl(r8, r10, r12)     // Catch:{ all -> 0x018d }
            goto L_0x0187
        L_0x005f:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r9.mSummaryQuery     // Catch:{ all -> 0x018d }
            r3 = 0
            long r4 = r2.insertNmsEventChangedObjToSummaryDB(r10, r3)     // Catch:{ all -> 0x018d }
            com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler r2 = r9.mMultiLnScheduler     // Catch:{ all -> 0x018d }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.DEFAULT     // Catch:{ all -> 0x018d }
            int r2 = r2.getLineInitSyncStatus(r6, r7)     // Catch:{ all -> 0x018d }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r7 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x018d }
            int r7 = r7.getId()     // Catch:{ all -> 0x018d }
            if (r2 == r7) goto L_0x0083
            java.lang.String r9 = r9.TAG     // Catch:{ all -> 0x018d }
            java.lang.String r10 = "initial sync not complete yet, buffer the NMS events until initial sync is finished"
            android.util.Log.d(r9, r10)     // Catch:{ all -> 0x018d }
            if (r8 == 0) goto L_0x0082
            r8.close()
        L_0x0082:
            return
        L_0x0083:
            boolean r2 = r9.mIsCmsEnabled     // Catch:{ all -> 0x018d }
            if (r2 == 0) goto L_0x0097
            boolean r0 = r1.equals(r0)     // Catch:{ all -> 0x018d }
            if (r0 == 0) goto L_0x008e
            goto L_0x0097
        L_0x008e:
            java.lang.String r10 = r9.TAG     // Catch:{ all -> 0x018d }
            java.lang.String r0 = "SD outgoing message - needs to be inserted"
            com.sec.internal.log.IMSLog.i(r10, r0)     // Catch:{ all -> 0x018d }
            goto L_0x0173
        L_0x0097:
            java.lang.String r0 = r10.correlationId     // Catch:{ all -> 0x018d }
            r1 = 1
            if (r0 == 0) goto L_0x00df
            java.lang.String r0 = r9.TAG     // Catch:{ all -> 0x018d }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x018d }
            r2.<init>()     // Catch:{ all -> 0x018d }
            java.lang.String r7 = "handleCloudNotifyChangedObj RCS CloudUpdate: "
            r2.append(r7)     // Catch:{ all -> 0x018d }
            java.lang.String r7 = r10.correlationId     // Catch:{ all -> 0x018d }
            r2.append(r7)     // Catch:{ all -> 0x018d }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x018d }
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x018d }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r0 = r9.mRcsScheduler     // Catch:{ all -> 0x018d }
            java.lang.String r2 = r10.correlationId     // Catch:{ all -> 0x018d }
            android.database.Cursor r0 = r0.searchIMFTBufferUsingImdn(r2, r6)     // Catch:{ all -> 0x018d }
            if (r0 == 0) goto L_0x00da
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x00d0 }
            if (r2 == 0) goto L_0x00da
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r9.mSummaryQuery     // Catch:{ all -> 0x00d0 }
            r2.updateSummaryDbUsingMessageType(r4, r1)     // Catch:{ all -> 0x00d0 }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r2 = r9.mRcsScheduler     // Catch:{ all -> 0x00d0 }
            r2.onNmsEventChangedObjRcsBufferDbAvailableUsingImdnId(r0, r10, r12)     // Catch:{ all -> 0x00d0 }
            r3 = r1
            goto L_0x00da
        L_0x00d0:
            r9 = move-exception
            r0.close()     // Catch:{ all -> 0x00d5 }
            goto L_0x00d9
        L_0x00d5:
            r10 = move-exception
            r9.addSuppressed(r10)     // Catch:{ all -> 0x018d }
        L_0x00d9:
            throw r9     // Catch:{ all -> 0x018d }
        L_0x00da:
            if (r0 == 0) goto L_0x00df
            r0.close()     // Catch:{ all -> 0x018d }
        L_0x00df:
            if (r3 != 0) goto L_0x0129
            java.lang.String r0 = r10.correlationId     // Catch:{ all -> 0x018d }
            if (r0 == 0) goto L_0x0129
            java.lang.String r0 = r9.TAG     // Catch:{ all -> 0x018d }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x018d }
            r2.<init>()     // Catch:{ all -> 0x018d }
            java.lang.String r7 = "handleCloudNotifyChangedObj MMS CloudUpdate: "
            r2.append(r7)     // Catch:{ all -> 0x018d }
            java.lang.String r7 = r10.correlationId     // Catch:{ all -> 0x018d }
            r2.append(r7)     // Catch:{ all -> 0x018d }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x018d }
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x018d }
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r0 = r9.mMmsScheduler     // Catch:{ all -> 0x018d }
            java.lang.String r2 = r10.correlationId     // Catch:{ all -> 0x018d }
            android.database.Cursor r0 = r0.searchMMsPduBufferUsingCorrelationId(r2)     // Catch:{ all -> 0x018d }
            if (r0 == 0) goto L_0x0124
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x011a }
            if (r2 == 0) goto L_0x0124
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r9.mSummaryQuery     // Catch:{ all -> 0x011a }
            r3 = 4
            r2.updateSummaryDbUsingMessageType(r4, r3)     // Catch:{ all -> 0x011a }
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r2 = r9.mMmsScheduler     // Catch:{ all -> 0x011a }
            r2.onNmsEventChangedObjMmsBufferDbAvailableUsingCorrId(r0, r10, r12)     // Catch:{ all -> 0x011a }
            r3 = r1
            goto L_0x0124
        L_0x011a:
            r9 = move-exception
            r0.close()     // Catch:{ all -> 0x011f }
            goto L_0x0123
        L_0x011f:
            r10 = move-exception
            r9.addSuppressed(r10)     // Catch:{ all -> 0x018d }
        L_0x0123:
            throw r9     // Catch:{ all -> 0x018d }
        L_0x0124:
            if (r0 == 0) goto L_0x0129
            r0.close()     // Catch:{ all -> 0x018d }
        L_0x0129:
            if (r3 != 0) goto L_0x0173
            java.lang.String r0 = r10.correlationTag     // Catch:{ all -> 0x018d }
            if (r0 == 0) goto L_0x0173
            java.lang.String r0 = r9.TAG     // Catch:{ all -> 0x018d }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x018d }
            r2.<init>()     // Catch:{ all -> 0x018d }
            java.lang.String r7 = "handleCloudNotifyChangedObj: "
            r2.append(r7)     // Catch:{ all -> 0x018d }
            java.lang.String r7 = r10.correlationTag     // Catch:{ all -> 0x018d }
            r2.append(r7)     // Catch:{ all -> 0x018d }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x018d }
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x018d }
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r0 = r9.mSmsScheduler     // Catch:{ all -> 0x018d }
            java.lang.String r2 = r10.correlationTag     // Catch:{ all -> 0x018d }
            android.database.Cursor r0 = r0.searchUnSyncedSMSBufferUsingCorrelationTag(r2)     // Catch:{ all -> 0x018d }
            if (r0 == 0) goto L_0x016e
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x0164 }
            if (r2 == 0) goto L_0x016e
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r9.mSummaryQuery     // Catch:{ all -> 0x0164 }
            r3 = 3
            r2.updateSummaryDbUsingMessageType(r4, r3)     // Catch:{ all -> 0x0164 }
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r2 = r9.mSmsScheduler     // Catch:{ all -> 0x0164 }
            r2.onNmsEventChangedObjSmsBufferDbAvailableUsingCorrTag(r0, r10, r12)     // Catch:{ all -> 0x0164 }
            r3 = r1
            goto L_0x016e
        L_0x0164:
            r9 = move-exception
            r0.close()     // Catch:{ all -> 0x0169 }
            goto L_0x016d
        L_0x0169:
            r10 = move-exception
            r9.addSuppressed(r10)     // Catch:{ all -> 0x018d }
        L_0x016d:
            throw r9     // Catch:{ all -> 0x018d }
        L_0x016e:
            if (r0 == 0) goto L_0x0173
            r0.close()     // Catch:{ all -> 0x018d }
        L_0x0173:
            if (r12 != 0) goto L_0x0187
            if (r3 != 0) goto L_0x0187
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r10 = r11.mChangelst     // Catch:{ all -> 0x018d }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r11 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x018d }
            r2 = 7
            com.sec.internal.ims.cmstore.MessageStoreClient r7 = r9.mStoreClient     // Catch:{ all -> 0x018d }
            r1 = r11
            r3 = r4
            r5 = r12
            r1.<init>(r2, r3, r5, r6, r7)     // Catch:{ all -> 0x018d }
            r10.add(r11)     // Catch:{ all -> 0x018d }
        L_0x0187:
            if (r8 == 0) goto L_0x018c
            r8.close()
        L_0x018c:
            return
        L_0x018d:
            r9 = move-exception
            if (r8 == 0) goto L_0x0198
            r8.close()     // Catch:{ all -> 0x0194 }
            goto L_0x0198
        L_0x0194:
            r10 = move-exception
            r9.addSuppressed(r10)
        L_0x0198:
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList, boolean):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:59:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNmsEventDeletedObjSummaryDbAvailableUsingUrl(android.database.Cursor r4, com.sec.internal.omanetapi.nms.data.DeletedObject r5, boolean r6) {
        /*
            r3 = this;
            java.lang.String r0 = "messagetype"
            int r0 = r4.getColumnIndexOrThrow(r0)
            int r4 = r4.getInt(r0)
            java.lang.String r0 = r3.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onNmsEventDeletedObjSummaryDbAvailableUsingUrl(), type: "
            r1.append(r2)
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            r0 = 1
            if (r4 == r0) goto L_0x0094
            r0 = 14
            if (r4 == r0) goto L_0x0094
            r0 = 3
            if (r4 == r0) goto L_0x0061
            r0 = 4
            if (r4 == r0) goto L_0x0037
            r0 = 11
            if (r4 == r0) goto L_0x0094
            r0 = 12
            if (r4 == r0) goto L_0x0094
            goto L_0x00bd
        L_0x0037:
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r4 = r3.mMmsScheduler
            java.net.URL r0 = r5.resourceURL
            java.lang.String r0 = r0.toString()
            android.database.Cursor r4 = r4.queryMMSBufferDBwithResUrl(r0)
            if (r4 == 0) goto L_0x005b
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x0051 }
            if (r0 == 0) goto L_0x005b
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r3 = r3.mMmsScheduler     // Catch:{ all -> 0x0051 }
            r3.onNmsEventDeletedObjBufferDbMmsAvailableUsingUrl(r4, r5, r6)     // Catch:{ all -> 0x0051 }
            goto L_0x005b
        L_0x0051:
            r3 = move-exception
            r4.close()     // Catch:{ all -> 0x0056 }
            goto L_0x005a
        L_0x0056:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x005a:
            throw r3
        L_0x005b:
            if (r4 == 0) goto L_0x00bd
            r4.close()
            goto L_0x00bd
        L_0x0061:
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r4 = r3.mSmsScheduler
            java.net.URL r0 = r5.resourceURL
            java.lang.String r0 = r0.toString()
            android.database.Cursor r4 = r4.querySMSBufferDBwithResUrl(r0)
            if (r4 == 0) goto L_0x007b
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x0088 }
            if (r0 == 0) goto L_0x007b
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r3 = r3.mSmsScheduler     // Catch:{ all -> 0x0088 }
            r3.onNmsEventDeletedObjBufferDbSmsAvailableUsingUrl(r4, r5, r6)     // Catch:{ all -> 0x0088 }
            goto L_0x0082
        L_0x007b:
            java.lang.String r3 = r3.TAG     // Catch:{ all -> 0x0088 }
            java.lang.String r5 = "inconsistency between buffer or duplicated nms event"
            android.util.Log.e(r3, r5)     // Catch:{ all -> 0x0088 }
        L_0x0082:
            if (r4 == 0) goto L_0x00bd
            r4.close()
            goto L_0x00bd
        L_0x0088:
            r3 = move-exception
            if (r4 == 0) goto L_0x0093
            r4.close()     // Catch:{ all -> 0x008f }
            goto L_0x0093
        L_0x008f:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x0093:
            throw r3
        L_0x0094:
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r4 = r3.mRcsScheduler
            java.net.URL r0 = r5.resourceURL
            java.lang.String r0 = r0.toString()
            android.database.Cursor r4 = r4.queryRCSBufferDBwithResUrl(r0)
            if (r4 == 0) goto L_0x00b8
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x00ae }
            if (r0 == 0) goto L_0x00b8
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r3 = r3.mRcsScheduler     // Catch:{ all -> 0x00ae }
            r3.onNmsEventDeletedObjBufferDbRcsAvailableUsingUrl(r4, r5, r6)     // Catch:{ all -> 0x00ae }
            goto L_0x00b8
        L_0x00ae:
            r3 = move-exception
            r4.close()     // Catch:{ all -> 0x00b3 }
            goto L_0x00b7
        L_0x00b3:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x00b7:
            throw r3
        L_0x00b8:
            if (r4 == 0) goto L_0x00bd
            r4.close()
        L_0x00bd:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(android.database.Cursor, com.sec.internal.omanetapi.nms.data.DeletedObject, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void handleExpiredObject(DeletedObject deletedObject) {
        Cursor querySummaryDBwithResUrl = this.mSummaryQuery.querySummaryDBwithResUrl(deletedObject.resourceURL.toString());
        if (querySummaryDBwithResUrl != null) {
            try {
                if (querySummaryDBwithResUrl.moveToFirst()) {
                    int i = querySummaryDBwithResUrl.getInt(querySummaryDBwithResUrl.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                    String str = this.TAG;
                    Log.d(str, "handleExpiredObject, Status:" + i);
                    if (i == CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()) {
                        querySummaryDBwithResUrl.close();
                        return;
                    } else {
                        onNmsEventExpiredObjSummaryDbAvailableUsingUrl(querySummaryDBwithResUrl, deletedObject);
                        this.mSummaryQuery.deleteSummaryDBwithResUrl(deletedObject.resourceURL.toString());
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySummaryDBwithResUrl != null) {
            querySummaryDBwithResUrl.close();
            return;
        }
        return;
        throw th;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b7 A[SYNTHETIC, Splitter:B:29:0x00b7] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0109 A[SYNTHETIC, Splitter:B:51:0x0109] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x015b A[SYNTHETIC, Splitter:B:73:0x015b] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x016d  */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject r11, boolean r12) {
        /*
            r10 = this;
            java.net.URL r0 = r11.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r10.mSummaryQuery
            java.net.URL r2 = r11.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.querySummaryDBwithResUrl(r2)
            if (r1 == 0) goto L_0x0050
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x0171 }
            if (r2 == 0) goto L_0x0050
            java.lang.String r0 = "syncaction"
            int r0 = r1.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0171 }
            int r0 = r1.getInt(r0)     // Catch:{ all -> 0x0171 }
            java.lang.String r2 = r10.TAG     // Catch:{ all -> 0x0171 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0171 }
            r3.<init>()     // Catch:{ all -> 0x0171 }
            java.lang.String r4 = "handleCloudNotifyDeletedObj, Status:"
            r3.append(r4)     // Catch:{ all -> 0x0171 }
            r3.append(r0)     // Catch:{ all -> 0x0171 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0171 }
            android.util.Log.d(r2, r3)     // Catch:{ all -> 0x0171 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x0171 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0171 }
            if (r0 != r2) goto L_0x004b
            r1.close()
            return
        L_0x004b:
            r10.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(r1, r11, r12)     // Catch:{ all -> 0x0171 }
            goto L_0x016b
        L_0x0050:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r10.mSummaryQuery     // Catch:{ all -> 0x0171 }
            r3 = 0
            r2.insertNmsEventDeletedObjToSummaryDB(r11, r3)     // Catch:{ all -> 0x0171 }
            com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler r2 = r10.mMultiLnScheduler     // Catch:{ all -> 0x0171 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.DEFAULT     // Catch:{ all -> 0x0171 }
            int r2 = r2.getLineInitSyncStatus(r0, r4)     // Catch:{ all -> 0x0171 }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x0171 }
            int r4 = r4.getId()     // Catch:{ all -> 0x0171 }
            if (r2 == r4) goto L_0x0073
            java.lang.String r10 = r10.TAG     // Catch:{ all -> 0x0171 }
            java.lang.String r11 = "initial sync not complete yet, buffer the NMS events until initial sync is finished"
            android.util.Log.d(r10, r11)     // Catch:{ all -> 0x0171 }
            if (r1 == 0) goto L_0x0072
            r1.close()
        L_0x0072:
            return
        L_0x0073:
            java.lang.String r2 = r11.correlationId     // Catch:{ all -> 0x0171 }
            r4 = 1
            java.lang.String r5 = "did not find buffer item to delete"
            r6 = -1
            if (r2 == 0) goto L_0x00c7
            java.lang.String r2 = r10.TAG     // Catch:{ all -> 0x0171 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0171 }
            r8.<init>()     // Catch:{ all -> 0x0171 }
            java.lang.String r9 = "handleCloudNotifyDeletedObj RCS CloudUpdate: "
            r8.append(r9)     // Catch:{ all -> 0x0171 }
            java.lang.String r9 = r11.correlationId     // Catch:{ all -> 0x0171 }
            r8.append(r9)     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0171 }
            android.util.Log.d(r2, r8)     // Catch:{ all -> 0x0171 }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r2 = r10.mRcsScheduler     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = r11.correlationId     // Catch:{ all -> 0x0171 }
            android.database.Cursor r0 = r2.searchIMFTBufferUsingImdn(r8, r0)     // Catch:{ all -> 0x0171 }
            if (r0 == 0) goto L_0x00b0
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x00bb }
            if (r2 == 0) goto L_0x00b0
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r10.mSummaryQuery     // Catch:{ all -> 0x00bb }
            r2.updateSummaryDbUsingMessageType(r6, r4)     // Catch:{ all -> 0x00bb }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r2 = r10.mRcsScheduler     // Catch:{ all -> 0x00bb }
            r2.onNmsEventDeletedObjBufferDbRcsAvailableUsingImdnId(r0, r11, r12)     // Catch:{ all -> 0x00bb }
            r3 = r4
            goto L_0x00b5
        L_0x00b0:
            java.lang.String r2 = r10.TAG     // Catch:{ all -> 0x00bb }
            android.util.Log.d(r2, r5)     // Catch:{ all -> 0x00bb }
        L_0x00b5:
            if (r0 == 0) goto L_0x00c7
            r0.close()     // Catch:{ all -> 0x0171 }
            goto L_0x00c7
        L_0x00bb:
            r10 = move-exception
            if (r0 == 0) goto L_0x00c6
            r0.close()     // Catch:{ all -> 0x00c2 }
            goto L_0x00c6
        L_0x00c2:
            r11 = move-exception
            r10.addSuppressed(r11)     // Catch:{ all -> 0x0171 }
        L_0x00c6:
            throw r10     // Catch:{ all -> 0x0171 }
        L_0x00c7:
            if (r3 != 0) goto L_0x011a
            java.lang.String r0 = r11.correlationId     // Catch:{ all -> 0x0171 }
            if (r0 == 0) goto L_0x011a
            java.lang.String r0 = r10.TAG     // Catch:{ all -> 0x0171 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0171 }
            r2.<init>()     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = "handleCloudNotifyDeletedObj MMS CloudUpdate: "
            r2.append(r8)     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = r11.correlationId     // Catch:{ all -> 0x0171 }
            r2.append(r8)     // Catch:{ all -> 0x0171 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0171 }
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x0171 }
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r0 = r10.mMmsScheduler     // Catch:{ all -> 0x0171 }
            java.lang.String r2 = r11.correlationId     // Catch:{ all -> 0x0171 }
            android.database.Cursor r0 = r0.searchMMsPduBufferUsingCorrelationId(r2)     // Catch:{ all -> 0x0171 }
            if (r0 == 0) goto L_0x0101
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x010e }
            if (r2 == 0) goto L_0x0101
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r10.mSummaryQuery     // Catch:{ all -> 0x010e }
            r3 = 4
            r2.updateSummaryDbUsingMessageType(r6, r3)     // Catch:{ all -> 0x010e }
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r2 = r10.mMmsScheduler     // Catch:{ all -> 0x010e }
            r2.onNmsEventDeletedObjMmsBufferDbAvailableUsingCorrId(r0, r11, r12)     // Catch:{ all -> 0x010e }
            goto L_0x0107
        L_0x0101:
            java.lang.String r2 = r10.TAG     // Catch:{ all -> 0x010e }
            android.util.Log.d(r2, r5)     // Catch:{ all -> 0x010e }
            r4 = r3
        L_0x0107:
            if (r0 == 0) goto L_0x010c
            r0.close()     // Catch:{ all -> 0x0171 }
        L_0x010c:
            r3 = r4
            goto L_0x011a
        L_0x010e:
            r10 = move-exception
            if (r0 == 0) goto L_0x0119
            r0.close()     // Catch:{ all -> 0x0115 }
            goto L_0x0119
        L_0x0115:
            r11 = move-exception
            r10.addSuppressed(r11)     // Catch:{ all -> 0x0171 }
        L_0x0119:
            throw r10     // Catch:{ all -> 0x0171 }
        L_0x011a:
            if (r3 != 0) goto L_0x016b
            java.lang.String r0 = r11.correlationTag     // Catch:{ all -> 0x0171 }
            if (r0 == 0) goto L_0x016b
            java.lang.String r0 = r10.TAG     // Catch:{ all -> 0x0171 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0171 }
            r2.<init>()     // Catch:{ all -> 0x0171 }
            java.lang.String r3 = "handleCloudNotifyChangedObj: "
            r2.append(r3)     // Catch:{ all -> 0x0171 }
            java.lang.String r3 = r11.correlationTag     // Catch:{ all -> 0x0171 }
            r2.append(r3)     // Catch:{ all -> 0x0171 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0171 }
            android.util.Log.d(r0, r2)     // Catch:{ all -> 0x0171 }
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r0 = r10.mSmsScheduler     // Catch:{ all -> 0x0171 }
            java.lang.String r2 = r11.correlationTag     // Catch:{ all -> 0x0171 }
            android.database.Cursor r0 = r0.searchUnSyncedSMSBufferUsingCorrelationTag(r2)     // Catch:{ all -> 0x0171 }
            if (r0 == 0) goto L_0x0154
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x015f }
            if (r2 == 0) goto L_0x0154
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r10.mSummaryQuery     // Catch:{ all -> 0x015f }
            r3 = 3
            r2.updateSummaryDbUsingMessageType(r6, r3)     // Catch:{ all -> 0x015f }
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r10 = r10.mSmsScheduler     // Catch:{ all -> 0x015f }
            r10.onNmsEventDeletedObjSmsBufferDbAvailableUsingCorrTag(r0, r11, r12)     // Catch:{ all -> 0x015f }
            goto L_0x0159
        L_0x0154:
            java.lang.String r10 = r10.TAG     // Catch:{ all -> 0x015f }
            android.util.Log.d(r10, r5)     // Catch:{ all -> 0x015f }
        L_0x0159:
            if (r0 == 0) goto L_0x016b
            r0.close()     // Catch:{ all -> 0x0171 }
            goto L_0x016b
        L_0x015f:
            r10 = move-exception
            if (r0 == 0) goto L_0x016a
            r0.close()     // Catch:{ all -> 0x0166 }
            goto L_0x016a
        L_0x0166:
            r11 = move-exception
            r10.addSuppressed(r11)     // Catch:{ all -> 0x0171 }
        L_0x016a:
            throw r10     // Catch:{ all -> 0x0171 }
        L_0x016b:
            if (r1 == 0) goto L_0x0170
            r1.close()
        L_0x0170:
            return
        L_0x0171:
            r10 = move-exception
            if (r1 == 0) goto L_0x017c
            r1.close()     // Catch:{ all -> 0x0178 }
            goto L_0x017c
        L_0x0178:
            r11 = move-exception
            r10.addSuppressed(r11)
        L_0x017c:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void onHandlePendingNmsEvent() {
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        Cursor queryAllPendingNmsEventInSummaryDB = this.mSummaryQuery.queryAllPendingNmsEventInSummaryDB();
        if (queryAllPendingNmsEventInSummaryDB != null) {
            try {
                if (queryAllPendingNmsEventInSummaryDB.moveToFirst()) {
                    Log.d(this.TAG, "NmsEvent sync");
                    do {
                        bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(7, (long) queryAllPendingNmsEventInSummaryDB.getInt(queryAllPendingNmsEventInSummaryDB.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)), false, (String) null, this.mStoreClient));
                    } while (queryAllPendingNmsEventInSummaryDB.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryAllPendingNmsEventInSummaryDB != null) {
            queryAllPendingNmsEventInSummaryDB.close();
        }
        if (bufferDBChangeParamList.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(bufferDBChangeParamList);
            return;
        }
        return;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void startGoForwardSyncDbCopyTask() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        Cursor queryDeltaSMSfromTelephony = this.mSmsScheduler.queryDeltaSMSfromTelephony();
        if (queryDeltaSMSfromTelephony != null) {
            try {
                if (queryDeltaSMSfromTelephony.moveToFirst()) {
                    Log.d(this.TAG, "SMS DB loading");
                    this.mSmsScheduler.insertToSMSBufferDB(queryDeltaSMSfromTelephony, contentValues, true);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryDeltaSMSfromTelephony != null) {
            queryDeltaSMSfromTelephony.close();
        }
        Cursor queryDeltaMMSPduFromTelephonyDb = this.mMmsScheduler.queryDeltaMMSPduFromTelephonyDb();
        if (queryDeltaMMSPduFromTelephonyDb != null) {
            try {
                if (queryDeltaMMSPduFromTelephonyDb.moveToFirst()) {
                    Log.d(this.TAG, "MMS DB loading");
                    this.mMmsScheduler.insertToMMSPDUBufferDB(queryDeltaMMSPduFromTelephonyDb, contentValues, true);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryDeltaMMSPduFromTelephonyDb != null) {
            queryDeltaMMSPduFromTelephonyDb.close();
        }
        if (!this.mIsCmsEnabled) {
            Cursor queryDeltaSMSfromTelephonyWoImsi = this.mSmsScheduler.queryDeltaSMSfromTelephonyWoImsi();
            if (queryDeltaSMSfromTelephonyWoImsi != null) {
                try {
                    if (queryDeltaSMSfromTelephonyWoImsi.moveToFirst()) {
                        Log.d(this.TAG, "Null Imsi SMS DB loading");
                        this.mSmsScheduler.insertToSMSBufferDB(queryDeltaSMSfromTelephonyWoImsi, contentValues, true);
                    }
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
            }
            if (queryDeltaSMSfromTelephonyWoImsi != null) {
                queryDeltaSMSfromTelephonyWoImsi.close();
            }
            Cursor queryDeltaMMSPduFromTelephonyDbWoImsi = this.mMmsScheduler.queryDeltaMMSPduFromTelephonyDbWoImsi();
            if (queryDeltaMMSPduFromTelephonyDbWoImsi != null) {
                try {
                    if (queryDeltaMMSPduFromTelephonyDbWoImsi.moveToFirst()) {
                        Log.d(this.TAG, "Null Imsi MMS DB loading");
                        this.mMmsScheduler.insertToMMSPDUBufferDB(queryDeltaMMSPduFromTelephonyDbWoImsi, contentValues, true);
                    }
                } catch (Throwable th4) {
                    th.addSuppressed(th4);
                }
            }
            if (queryDeltaMMSPduFromTelephonyDbWoImsi != null) {
                queryDeltaMMSPduFromTelephonyDbWoImsi.close();
            }
        }
        this.mSmsScheduler.syncReadSmsFromTelephony();
        this.mMmsScheduler.syncReadMmsFromTelephony();
        setBufferDBLoaded(true);
        return;
        throw th;
        throw th;
        throw th;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void cleanAllBufferDB() {
        this.mSmsScheduler.cleanAllBufferDB();
        this.mMmsScheduler.cleanAllBufferDB();
        this.mRcsScheduler.cleanAllBufferDB();
        setBufferDBLoaded(false);
    }

    /* access modifiers changed from: protected */
    public void startInitialSyncDBCopyTask() {
        cleanAllBufferDB();
        ContentValues contentValues = new ContentValues();
        this.mMultiLnScheduler.insertNewLine(this.mStoreClient.getPrerenceManager().getUserTelCtn(), SyncMsgType.DEFAULT);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        Cursor querySMSfromTelephonyWithIMSI = this.mSmsScheduler.querySMSfromTelephonyWithIMSI(this.mStoreClient.getCurrentIMSI());
        if (querySMSfromTelephonyWithIMSI != null) {
            try {
                if (querySMSfromTelephonyWithIMSI.moveToFirst()) {
                    Log.d(this.TAG, "SMS DB loading");
                    this.mSmsScheduler.insertToSMSBufferDB(querySMSfromTelephonyWithIMSI, contentValues, false);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySMSfromTelephonyWithIMSI != null) {
            querySMSfromTelephonyWithIMSI.close();
        }
        Cursor queryMMSPduFromTelephonyDbWithIMSI = this.mMmsScheduler.queryMMSPduFromTelephonyDbWithIMSI(this.mStoreClient.getCurrentIMSI());
        if (queryMMSPduFromTelephonyDbWithIMSI != null) {
            try {
                if (queryMMSPduFromTelephonyDbWithIMSI.moveToFirst()) {
                    Log.d(this.TAG, "MMS DB loading");
                    this.mMmsScheduler.insertToMMSPDUBufferDB(queryMMSPduFromTelephonyDbWithIMSI, contentValues, false);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryMMSPduFromTelephonyDbWithIMSI != null) {
            queryMMSPduFromTelephonyDbWithIMSI.close();
        }
        if (!this.mIsCmsEnabled) {
            Cursor querySMSfromTelephonyWoIMSI = this.mSmsScheduler.querySMSfromTelephonyWoIMSI();
            if (querySMSfromTelephonyWoIMSI != null) {
                try {
                    if (querySMSfromTelephonyWoIMSI.moveToFirst()) {
                        Log.i(this.TAG, "SMS Loading for IMSI null case");
                        this.mSmsScheduler.insertToSMSBufferDB(querySMSfromTelephonyWoIMSI, contentValues, false);
                    }
                } catch (Throwable th3) {
                    th.addSuppressed(th3);
                }
            }
            if (querySMSfromTelephonyWoIMSI != null) {
                querySMSfromTelephonyWoIMSI.close();
            }
            Cursor queryMMSPduFromTelephonyDbWoIMSI = this.mMmsScheduler.queryMMSPduFromTelephonyDbWoIMSI();
            if (queryMMSPduFromTelephonyDbWoIMSI != null) {
                try {
                    if (queryMMSPduFromTelephonyDbWoIMSI.moveToFirst()) {
                        Log.i(this.TAG, "MMS Loading for IMSI null case");
                        this.mMmsScheduler.insertToMMSPDUBufferDB(queryMMSPduFromTelephonyDbWoIMSI, contentValues, false);
                    }
                } catch (Throwable th4) {
                    th.addSuppressed(th4);
                }
            }
            if (queryMMSPduFromTelephonyDbWoIMSI != null) {
                queryMMSPduFromTelephonyDbWoIMSI.close();
            }
        }
        Cursor queryAllSessionWithIMSI = this.mRcsScheduler.queryAllSessionWithIMSI(this.mStoreClient.getCurrentIMSI());
        if (queryAllSessionWithIMSI != null) {
            try {
                if (queryAllSessionWithIMSI.moveToFirst()) {
                    Log.d(this.TAG, "RCS DB loading");
                    this.mRcsScheduler.insertAllSessionToRCSSessionBufferDB(queryAllSessionWithIMSI);
                }
            } catch (Throwable th5) {
                th.addSuppressed(th5);
            }
        }
        if (queryAllSessionWithIMSI != null) {
            queryAllSessionWithIMSI.close();
        }
        if (CmsUtil.isMcsSupported(this.mContext, this.mPhoneId)) {
            Cursor queryAllSessionsFromTelephony = this.mRcsScheduler.queryAllSessionsFromTelephony(this.mStoreClient.getCurrentIMSI());
            try {
                Log.i(this.TAG, "TP DB loading");
                if (queryAllSessionsFromTelephony != null && queryAllSessionsFromTelephony.moveToFirst()) {
                    this.mRcsScheduler.insertSessionFromTPDBToRCSSessionBufferDB(queryAllSessionsFromTelephony);
                }
                if (queryAllSessionsFromTelephony != null) {
                    queryAllSessionsFromTelephony.close();
                }
            } catch (Throwable th6) {
                th.addSuppressed(th6);
            }
        }
        setBufferDBLoaded(true);
        return;
        throw th;
        throw th;
        throw th;
        throw th;
        throw th;
        throw th;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getTableIndex(java.lang.String r7, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag r8) {
        /*
            r6 = this;
            java.lang.String r6 = r7.toUpperCase()
            r6.hashCode()
            int r7 = r6.hashCode()
            r0 = 17
            r1 = 4
            r2 = 3
            r3 = 1
            r4 = 0
            r5 = -1
            switch(r7) {
                case -1980813630: goto L_0x00ed;
                case -1511670668: goto L_0x00e1;
                case -873347853: goto L_0x00d5;
                case -324399745: goto L_0x00c9;
                case 2254: goto L_0x00bd;
                case 76467: goto L_0x00b1;
                case 79221: goto L_0x00a5;
                case 82233: goto L_0x0099;
                case 2067288: goto L_0x008c;
                case 74650124: goto L_0x007f;
                case 310666545: goto L_0x0072;
                case 408556937: goto L_0x0065;
                case 445658549: goto L_0x0058;
                case 527850930: goto L_0x004b;
                case 806950032: goto L_0x003e;
                case 988049465: goto L_0x0031;
                case 1551214263: goto L_0x0024;
                case 2062991267: goto L_0x0018;
                default: goto L_0x0015;
            }
        L_0x0015:
            r6 = r5
            goto L_0x00f8
        L_0x0018:
            java.lang.String r7 = "MSG_ALL"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x0021
            goto L_0x0015
        L_0x0021:
            r6 = r0
            goto L_0x00f8
        L_0x0024:
            java.lang.String r7 = "VVMDATA"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x002d
            goto L_0x0015
        L_0x002d:
            r6 = 16
            goto L_0x00f8
        L_0x0031:
            java.lang.String r7 = "GREETING"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x003a
            goto L_0x0015
        L_0x003a:
            r6 = 15
            goto L_0x00f8
        L_0x003e:
            java.lang.String r7 = "V2TLANGUAGE"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x0047
            goto L_0x0015
        L_0x0047:
            r6 = 14
            goto L_0x00f8
        L_0x004b:
            java.lang.String r7 = "V2T_SMS"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x0054
            goto L_0x0015
        L_0x0054:
            r6 = 13
            goto L_0x00f8
        L_0x0058:
            java.lang.String r7 = "V2T_EMAIL"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x0061
            goto L_0x0015
        L_0x0061:
            r6 = 12
            goto L_0x00f8
        L_0x0065:
            java.lang.String r7 = "PROFILE"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x006e
            goto L_0x0015
        L_0x006e:
            r6 = 11
            goto L_0x00f8
        L_0x0072:
            java.lang.String r7 = "VOICEMAILTOTEXT"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x007b
            goto L_0x0015
        L_0x007b:
            r6 = 10
            goto L_0x00f8
        L_0x007f:
            java.lang.String r7 = "NUTON"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x0088
            goto L_0x0015
        L_0x0088:
            r6 = 9
            goto L_0x00f8
        L_0x008c:
            java.lang.String r7 = "CHAT"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x0095
            goto L_0x0015
        L_0x0095:
            r6 = 8
            goto L_0x00f8
        L_0x0099:
            java.lang.String r7 = "SMS"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00a3
            goto L_0x0015
        L_0x00a3:
            r6 = 7
            goto L_0x00f8
        L_0x00a5:
            java.lang.String r7 = "PIN"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00af
            goto L_0x0015
        L_0x00af:
            r6 = 6
            goto L_0x00f8
        L_0x00b1:
            java.lang.String r7 = "MMS"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00bb
            goto L_0x0015
        L_0x00bb:
            r6 = 5
            goto L_0x00f8
        L_0x00bd:
            java.lang.String r7 = "FT"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00c7
            goto L_0x0015
        L_0x00c7:
            r6 = r1
            goto L_0x00f8
        L_0x00c9:
            java.lang.String r7 = "ADHOCV2T"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00d3
            goto L_0x0015
        L_0x00d3:
            r6 = r2
            goto L_0x00f8
        L_0x00d5:
            java.lang.String r7 = "ACTIVATE"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00df
            goto L_0x0015
        L_0x00df:
            r6 = 2
            goto L_0x00f8
        L_0x00e1:
            java.lang.String r7 = "DEACTIVATE"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00eb
            goto L_0x0015
        L_0x00eb:
            r6 = r3
            goto L_0x00f8
        L_0x00ed:
            java.lang.String r7 = "NUTOFF"
            boolean r6 = r6.equals(r7)
            if (r6 != 0) goto L_0x00f7
            goto L_0x0015
        L_0x00f7:
            r6 = r4
        L_0x00f8:
            switch(r6) {
                case 0: goto L_0x0124;
                case 1: goto L_0x0124;
                case 2: goto L_0x0124;
                case 3: goto L_0x0126;
                case 4: goto L_0x0122;
                case 5: goto L_0x0120;
                case 6: goto L_0x011d;
                case 7: goto L_0x011b;
                case 8: goto L_0x0122;
                case 9: goto L_0x0124;
                case 10: goto L_0x0124;
                case 11: goto L_0x0124;
                case 12: goto L_0x0124;
                case 13: goto L_0x0124;
                case 14: goto L_0x0124;
                case 15: goto L_0x0118;
                case 16: goto L_0x0126;
                case 17: goto L_0x0116;
                default: goto L_0x00fb;
            }
        L_0x00fb:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.StartFullSync
            boolean r6 = r6.equals(r8)
            if (r6 != 0) goto L_0x0116
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.StopSync
            boolean r6 = r6.equals(r8)
            if (r6 != 0) goto L_0x0116
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.StartDeltaSync
            boolean r6 = r6.equals(r8)
            if (r6 == 0) goto L_0x0114
            goto L_0x0116
        L_0x0114:
            r0 = r5
            goto L_0x0126
        L_0x0116:
            r0 = r4
            goto L_0x0126
        L_0x0118:
            r0 = 18
            goto L_0x0126
        L_0x011b:
            r0 = r2
            goto L_0x0126
        L_0x011d:
            r0 = 19
            goto L_0x0126
        L_0x0120:
            r0 = r1
            goto L_0x0126
        L_0x0122:
            r0 = r3
            goto L_0x0126
        L_0x0124:
            r0 = 20
        L_0x0126:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.getTableIndex(java.lang.String, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag):int");
    }

    private ParamVvmUpdate getVvmParam(JsonElement jsonElement, String str, CloudMessageBufferDBConstants.MsgOperationFlag msgOperationFlag, int i) {
        String upperCase = str.toUpperCase();
        upperCase.hashCode();
        char c = 65535;
        switch (upperCase.hashCode()) {
            case -1980813630:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.NUTOFF)) {
                    c = 0;
                    break;
                }
                break;
            case -1511670668:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.DEACTIVATE)) {
                    c = 1;
                    break;
                }
                break;
            case -873347853:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.ACTIVATE)) {
                    c = 2;
                    break;
                }
                break;
            case -324399745:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.ADHOC_V2TLANGUAGE)) {
                    c = 3;
                    break;
                }
                break;
            case 79221:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.VVMPIN)) {
                    c = 4;
                    break;
                }
                break;
            case 74650124:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.NUTON)) {
                    c = 5;
                    break;
                }
                break;
            case 310666545:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.VOICEMAILTOTEXT)) {
                    c = 6;
                    break;
                }
                break;
            case 408556937:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.VVMPROFILE)) {
                    c = 7;
                    break;
                }
                break;
            case 445658549:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.V2T_EMAIL)) {
                    c = 8;
                    break;
                }
                break;
            case 527850930:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.V2T_SMS)) {
                    c = 9;
                    break;
                }
                break;
            case 806950032:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.V2TLANGUAGE)) {
                    c = 10;
                    break;
                }
                break;
            case 988049465:
                if (upperCase.equals(CloudMessageProviderContract.DataTypes.VVMGREETING)) {
                    c = 11;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.NUTOFF);
            case 1:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.DEACTIVATE);
            case 2:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.ACTIVATE);
            case 3:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.ADHOC_V2T);
            case 4:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.PIN);
            case 5:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.NUTON);
            case 6:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT);
            case 7:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.FULLPROFILE);
            case 8:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.V2T_EMAIL);
            case 9:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.V2T_SMS);
            case 10:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.V2TLANGUAGE);
            case 11:
                return getVvmChangeParam(jsonElement.toString(), i, ParamVvmUpdate.VvmTypeChange.GREETING);
            default:
                return null;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0273  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0280 A[SYNTHETIC, Splitter:B:110:0x0280] */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0288 A[SYNTHETIC, Splitter:B:113:0x0288] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x00b0 A[SYNTHETIC, Splitter:B:20:0x00b0] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00d8 A[SYNTHETIC, Splitter:B:29:0x00d8] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0101 A[SYNTHETIC, Splitter:B:38:0x0101] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0126 A[SYNTHETIC, Splitter:B:47:0x0126] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x014e  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x015c A[SYNTHETIC, Splitter:B:55:0x015c] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0185 A[SYNTHETIC, Splitter:B:64:0x0185] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01ac A[SYNTHETIC, Splitter:B:72:0x01ac] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01d2 A[SYNTHETIC, Splitter:B:80:0x01d2] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01f3 A[Catch:{ Exception -> 0x00a1 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.cmstore.params.ParamAppJsonValueList decodeJson(java.lang.String r37, java.lang.String r38, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag r39) {
        /*
            r36 = this;
            r1 = r36
            r0 = r39
            java.lang.String r15 = "group_sms_body"
            java.lang.String r14 = "group_sms_from"
            java.lang.String r13 = "group_sms_recipients"
            java.lang.String r12 = "is_group_sms"
            java.lang.String r11 = "imdn_message_id"
            java.lang.String r10 = "correlationId"
            java.lang.String r9 = "correlationTag"
            java.lang.String r8 = "chatid"
            java.lang.String r7 = "id"
            java.lang.String r6 = "type"
            java.lang.String r5 = "islocalonly"
            com.google.gson.JsonParser r2 = new com.google.gson.JsonParser
            r2.<init>()
            com.sec.internal.ims.cmstore.params.ParamAppJsonValueList r4 = new com.sec.internal.ims.cmstore.params.ParamAppJsonValueList
            r4.<init>()
            r18 = 0
            r3 = r38
            com.google.gson.JsonElement r2 = r2.parse(r3)     // Catch:{ Exception -> 0x02f8 }
            boolean r3 = r2.isJsonArray()     // Catch:{ Exception -> 0x02f8 }
            if (r3 == 0) goto L_0x02f5
            com.google.gson.JsonArray r3 = r2.getAsJsonArray()     // Catch:{ Exception -> 0x02f8 }
            r19 = 0
            r16 = r4
            r2 = r19
        L_0x003d:
            int r4 = r3.size()     // Catch:{ Exception -> 0x02f8 }
            if (r2 >= r4) goto L_0x02f2
            com.google.gson.JsonElement r4 = r3.get(r2)     // Catch:{ Exception -> 0x02f8 }
            java.lang.String r17 = ""
            r38 = r2
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r1.mStoreClient     // Catch:{ Exception -> 0x02f8 }
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r2.getPrerenceManager()     // Catch:{ Exception -> 0x02f8 }
            java.lang.String r2 = r2.getUserTelCtn()     // Catch:{ Exception -> 0x02f8 }
            r20 = r2
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x02f8 }
            com.google.gson.JsonElement r2 = r2.get(r5)     // Catch:{ Exception -> 0x02f8 }
            if (r2 == 0) goto L_0x00a4
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r5)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r2 != 0) goto L_0x00a4
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r5)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ Exception -> 0x00a1 }
            r21 = r3
            java.lang.String r3 = "true"
            boolean r2 = r3.equalsIgnoreCase(r2)     // Catch:{ Exception -> 0x00a1 }
            if (r2 == 0) goto L_0x00a6
            r32 = r38
            r23 = r5
            r24 = r6
            r34 = r7
            r22 = r8
            r25 = r9
            r26 = r10
            r35 = r11
            r20 = r12
            r31 = r13
            r0 = r14
            r27 = r15
            r33 = r16
            goto L_0x02d1
        L_0x00a1:
            r0 = move-exception
            goto L_0x02fb
        L_0x00a4:
            r21 = r3
        L_0x00a6:
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x02f8 }
            com.google.gson.JsonElement r2 = r2.get(r6)     // Catch:{ Exception -> 0x02f8 }
            if (r2 == 0) goto L_0x00cc
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r6)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r2 != 0) goto L_0x00cc
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r6)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ Exception -> 0x00a1 }
            r3 = r2
            goto L_0x00ce
        L_0x00cc:
            r3 = r17
        L_0x00ce:
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x02f8 }
            com.google.gson.JsonElement r2 = r2.get(r7)     // Catch:{ Exception -> 0x02f8 }
            if (r2 == 0) goto L_0x00f5
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r7)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r2 != 0) goto L_0x00f5
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r7)     // Catch:{ Exception -> 0x00a1 }
            int r2 = r2.getAsInt()     // Catch:{ Exception -> 0x00a1 }
            r17 = r2
            goto L_0x00f7
        L_0x00f5:
            r17 = r19
        L_0x00f7:
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x02f8 }
            com.google.gson.JsonElement r2 = r2.get(r8)     // Catch:{ Exception -> 0x02f8 }
            if (r2 == 0) goto L_0x011e
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r8)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r2 != 0) goto L_0x011e
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r8)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ Exception -> 0x00a1 }
            r22 = r2
            goto L_0x0120
        L_0x011e:
            r22 = r18
        L_0x0120:
            boolean r2 = r1.isValidPreferredLineValue(r4)     // Catch:{ Exception -> 0x02f8 }
            if (r2 == 0) goto L_0x014e
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            r23 = r5
            java.lang.String r5 = "preferred_line"
            com.google.gson.JsonElement r2 = r2.get(r5)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ Exception -> 0x00a1 }
            android.content.Context r5 = r1.mContext     // Catch:{ Exception -> 0x00a1 }
            r24 = r6
            int r6 = r1.mPhoneId     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r5 = com.sec.internal.ims.cmstore.utils.Util.getSimCountryCode(r5, r6)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r2, r5)     // Catch:{ Exception -> 0x00a1 }
            boolean r5 = android.text.TextUtils.isEmpty(r2)     // Catch:{ Exception -> 0x00a1 }
            if (r5 != 0) goto L_0x0152
            r20 = r2
            goto L_0x0152
        L_0x014e:
            r23 = r5
            r24 = r6
        L_0x0152:
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x02f8 }
            com.google.gson.JsonElement r2 = r2.get(r9)     // Catch:{ Exception -> 0x02f8 }
            if (r2 == 0) goto L_0x0179
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r9)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r2 != 0) goto L_0x0179
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r9)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ Exception -> 0x00a1 }
            r25 = r2
            goto L_0x017b
        L_0x0179:
            r25 = r18
        L_0x017b:
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x02f8 }
            com.google.gson.JsonElement r2 = r2.get(r10)     // Catch:{ Exception -> 0x02f8 }
            if (r2 == 0) goto L_0x01a0
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r10)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r2 != 0) goto L_0x01a0
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r10)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ Exception -> 0x00a1 }
            goto L_0x01a2
        L_0x01a0:
            r2 = r18
        L_0x01a2:
            com.google.gson.JsonObject r5 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x02f8 }
            com.google.gson.JsonElement r5 = r5.get(r11)     // Catch:{ Exception -> 0x02f8 }
            if (r5 == 0) goto L_0x01c6
            com.google.gson.JsonObject r5 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r5 = r5.get(r11)     // Catch:{ Exception -> 0x00a1 }
            boolean r5 = r5.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r5 != 0) goto L_0x01c6
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r11)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ Exception -> 0x00a1 }
        L_0x01c6:
            r26 = r2
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x02f8 }
            com.google.gson.JsonElement r2 = r2.get(r12)     // Catch:{ Exception -> 0x02f8 }
            if (r2 == 0) goto L_0x01ef
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r12)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r2 != 0) goto L_0x01ef
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r12)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.getAsBoolean()     // Catch:{ Exception -> 0x00a1 }
            r27 = r2
            goto L_0x01f1
        L_0x01ef:
            r27 = r19
        L_0x01f1:
            if (r27 == 0) goto L_0x0273
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r13)     // Catch:{ Exception -> 0x00a1 }
            if (r2 == 0) goto L_0x0218
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r13)     // Catch:{ Exception -> 0x00a1 }
            boolean r2 = r2.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r2 != 0) goto L_0x0218
            com.google.gson.JsonObject r2 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r2 = r2.get(r13)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ Exception -> 0x00a1 }
            goto L_0x021a
        L_0x0218:
            r2 = r18
        L_0x021a:
            com.google.gson.JsonObject r5 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r5 = r5.get(r14)     // Catch:{ Exception -> 0x00a1 }
            if (r5 == 0) goto L_0x023f
            com.google.gson.JsonObject r5 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r5 = r5.get(r14)     // Catch:{ Exception -> 0x00a1 }
            boolean r5 = r5.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r5 != 0) goto L_0x023f
            com.google.gson.JsonObject r5 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r5 = r5.get(r14)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r5 = r5.getAsString()     // Catch:{ Exception -> 0x00a1 }
            goto L_0x0241
        L_0x023f:
            r5 = r18
        L_0x0241:
            com.google.gson.JsonObject r6 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r6 = r6.get(r15)     // Catch:{ Exception -> 0x00a1 }
            if (r6 == 0) goto L_0x026c
            com.google.gson.JsonObject r6 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r6 = r6.get(r15)     // Catch:{ Exception -> 0x00a1 }
            boolean r6 = r6.isJsonNull()     // Catch:{ Exception -> 0x00a1 }
            if (r6 != 0) goto L_0x026c
            com.google.gson.JsonObject r6 = r4.getAsJsonObject()     // Catch:{ Exception -> 0x00a1 }
            com.google.gson.JsonElement r6 = r6.get(r15)     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r6 = r6.getAsString()     // Catch:{ Exception -> 0x00a1 }
            r28 = r2
            r29 = r5
            r30 = r6
            goto L_0x0279
        L_0x026c:
            r28 = r2
            r29 = r5
            r30 = r18
            goto L_0x0279
        L_0x0273:
            r28 = r18
            r29 = r28
            r30 = r29
        L_0x0279:
            int r5 = r1.getTableIndex(r3, r0)     // Catch:{ Exception -> 0x02f8 }
            r2 = -1
            if (r5 != r2) goto L_0x0288
            java.lang.String r0 = r1.TAG     // Catch:{ Exception -> 0x00a1 }
            java.lang.String r2 = "decodeJson: Invalid tableindex"
            android.util.Log.e(r0, r2)     // Catch:{ Exception -> 0x00a1 }
            return r18
        L_0x0288:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r31 = r1.getVvmParam(r4, r3, r0, r5)     // Catch:{ Exception -> 0x02f8 }
            r4 = r16
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.ParamAppJsonValue> r6 = r4.mOperationList     // Catch:{ Exception -> 0x02f8 }
            com.sec.internal.ims.cmstore.params.ParamAppJsonValue r2 = new com.sec.internal.ims.cmstore.params.ParamAppJsonValue     // Catch:{ Exception -> 0x02f8 }
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r1.mStoreClient     // Catch:{ Exception -> 0x02f8 }
            r32 = r38
            r38 = r2
            r2 = r38
            r16 = r3
            r3 = r37
            r33 = r4
            r4 = r16
            r1 = r6
            r6 = r17
            r34 = r7
            r7 = r22
            r22 = r8
            r8 = r25
            r25 = r9
            r9 = r26
            r26 = r10
            r10 = r39
            r35 = r11
            r11 = r20
            r20 = r12
            r12 = r31
            r31 = r13
            r13 = r0
            r0 = r14
            r14 = r27
            r27 = r15
            r15 = r28
            r16 = r29
            r17 = r30
            r2.<init>(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17)     // Catch:{ Exception -> 0x02f8 }
            r1.add(r2)     // Catch:{ Exception -> 0x02f8 }
        L_0x02d1:
            int r2 = r32 + 1
            r1 = r36
            r14 = r0
            r12 = r20
            r3 = r21
            r8 = r22
            r5 = r23
            r6 = r24
            r9 = r25
            r10 = r26
            r15 = r27
            r13 = r31
            r16 = r33
            r7 = r34
            r11 = r35
            r0 = r39
            goto L_0x003d
        L_0x02f2:
            r33 = r16
            goto L_0x02f7
        L_0x02f5:
            r33 = r4
        L_0x02f7:
            return r33
        L_0x02f8:
            r0 = move-exception
            r1 = r36
        L_0x02fb:
            java.lang.String r1 = r1.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "JsonSyntaxException: "
            r2.append(r3)
            java.lang.String r0 = r0.toString()
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            android.util.Log.e(r1, r0)
            return r18
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.decodeJson(java.lang.String, java.lang.String, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag):com.sec.internal.ims.cmstore.params.ParamAppJsonValueList");
    }

    private boolean isValidPreferredLineValue(JsonElement jsonElement) {
        boolean z = jsonElement.getAsJsonObject().get("preferred_line") != null;
        boolean z2 = z ? !jsonElement.getAsJsonObject().get("preferred_line").isJsonNull() : false;
        if (!z || !z2) {
            return false;
        }
        return true;
    }

    private ParamVvmUpdate getVvmChangeParam(String str, int i, ParamVvmUpdate.VvmTypeChange vvmTypeChange) {
        String str2 = this.TAG;
        Log.d(str2, "getVvmChangeParam: " + str + " tableindex: " + i + " VvmTypeChange: " + vvmTypeChange);
        try {
            ParamVvmUpdate paramVvmUpdate = (ParamVvmUpdate) new Gson().fromJson(str, ParamVvmUpdate.class);
            paramVvmUpdate.mVvmChange = vvmTypeChange;
            if (TextUtils.isEmpty(paramVvmUpdate.mLine)) {
                paramVvmUpdate.mLine = this.mStoreClient.getPrerenceManager().getUserTelCtn();
            }
            paramVvmUpdate.mLine = Util.getTelUri(paramVvmUpdate.mLine, Util.getSimCountryCode(this.mContext, this.mStoreClient.getClientID()));
            return paramVvmUpdate;
        } catch (Exception e) {
            String str3 = this.TAG;
            Log.e(str3, "getVvmChangeParam: " + e.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void handleReceivedMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.Received));
    }

    /* access modifiers changed from: protected */
    public void handleSentMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.Sent));
    }

    /* access modifiers changed from: protected */
    public void handleReadMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.Read));
    }

    /* access modifiers changed from: protected */
    public void handleCancelMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.Cancel));
    }

    /* access modifiers changed from: protected */
    public void handleStarredMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.Starred));
    }

    /* access modifiers changed from: protected */
    public void handleUnStarredMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.UnStarred));
    }

    /* access modifiers changed from: protected */
    public void handleUnReadMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.UnRead));
    }

    /* access modifiers changed from: protected */
    public void handleDeleteMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.Delete));
    }

    /* access modifiers changed from: protected */
    public void handleUploadMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.Upload));
    }

    /* access modifiers changed from: protected */
    public void handleDownloadMessageJson(String str) {
        processParamAppJsonList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.Download));
    }

    /* access modifiers changed from: protected */
    public void handleWipeOutMessageJson(String str) {
        processWipeOutList(decodeJson((String) null, str, CloudMessageBufferDBConstants.MsgOperationFlag.WipeOut));
    }

    private void processParamAppJsonList(ParamAppJsonValueList paramAppJsonValueList) {
        ArrayList<ParamAppJsonValue> arrayList;
        String str = this.TAG;
        IMSLog.s(str, "processParamAppJsonList: " + paramAppJsonValueList);
        if (paramAppJsonValueList != null && (arrayList = paramAppJsonValueList.mOperationList) != null && arrayList.size() >= 1) {
            BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
            Iterator<ParamAppJsonValue> it = paramAppJsonValueList.mOperationList.iterator();
            while (it.hasNext()) {
                ParamAppJsonValue next = it.next();
                int i = next.mDataContractType;
                if (i == 1) {
                    this.mRcsScheduler.onAppOperationReceived(next, bufferDBChangeParamList);
                } else if (i == 3) {
                    this.mSmsScheduler.onAppOperationReceived(next, bufferDBChangeParamList);
                } else if (i != 4) {
                    switch (i) {
                        case 17:
                        case 18:
                        case 19:
                        case 20:
                            this.mVVMScheduler.onAppOperationReceived(next, bufferDBChangeParamList);
                            break;
                    }
                } else {
                    this.mMmsScheduler.onAppOperationReceived(next, bufferDBChangeParamList);
                }
            }
            if (bufferDBChangeParamList.mChangelst.size() > 0) {
                this.mDeviceDataChangeListener.sendDeviceUpdate(bufferDBChangeParamList);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setBufferDBLoaded(boolean z) {
        this.mBufferDBloaded = z;
        this.mStoreClient.getPrerenceManager().saveBufferDbLoaded(this.mBufferDBloaded);
    }

    private void processWipeOutList(ParamAppJsonValueList paramAppJsonValueList) {
        ArrayList<ParamAppJsonValue> arrayList;
        if (paramAppJsonValueList != null && (arrayList = paramAppJsonValueList.mOperationList) != null && arrayList.size() >= 1) {
            String str = this.TAG;
            Log.d(str, "processWipeOutList: " + paramAppJsonValueList);
            Iterator<ParamAppJsonValue> it = paramAppJsonValueList.mOperationList.iterator();
            while (it.hasNext()) {
                String str2 = it.next().mLine;
                if (CloudMessageProviderContract.DataTypes.MSGAPP_ALL.equalsIgnoreCase(paramAppJsonValueList.mOperationList.get(0).mDataType)) {
                    this.mSmsScheduler.wipeOutData(3, str2);
                    this.mMmsScheduler.wipeOutData(4, str2);
                    this.mRcsScheduler.wipeOutData(1, str2);
                } else if ("VVMDATA".equalsIgnoreCase(paramAppJsonValueList.mOperationList.get(0).mDataType)) {
                    this.mDeviceDataChangeListener.onWipeOutResetSyncHandler();
                    this.mVVMScheduler.wipeOutData(17, str2);
                    this.mVVMScheduler.wipeOutData(18, str2);
                    this.mVVMScheduler.wipeOutData(19, str2);
                    this.mVVMScheduler.wipeOutData(20, str2);
                    this.mVVMScheduler.wipeOutData(36, str2);
                    this.mVVMScheduler.wipeOutData(23, str2);
                    this.mDeviceDataChangeListener.setVVMSyncState(false);
                }
            }
        }
    }

    private void onNmsEventExpiredObjSummaryDbAvailableUsingUrl(Cursor cursor, DeletedObject deletedObject) {
        int i = cursor.getInt(cursor.getColumnIndexOrThrow("messagetype"));
        String str = this.TAG;
        Log.d(str, "onNmsEventExpiredObjSummaryDbAvailableUsingUrl(), type: " + i);
        if (!(i == 1 || i == 14)) {
            if (i == 3) {
                this.mSmsScheduler.deleteSMSBufferDBwithResUrl(deletedObject.resourceURL.toString());
                return;
            } else if (i == 4) {
                this.mMmsScheduler.deleteMMSBufferDBwithResUrl(deletedObject.resourceURL.toString());
                return;
            } else if (!(i == 11 || i == 12)) {
                return;
            }
        }
        this.mRcsScheduler.deleteRCSBufferDBwithResUrl(deletedObject.resourceURL.toString());
    }

    /* access modifiers changed from: protected */
    public void appFetchingFailedMsg(String str) {
        Cursor querySMSMessagesBySycnDirection = this.mSmsScheduler.querySMSMessagesBySycnDirection(3, str);
        if (querySMSMessagesBySycnDirection != null) {
            try {
                if (querySMSMessagesBySycnDirection.moveToFirst()) {
                    this.mSmsScheduler.notifyMsgAppFetchBuffer(querySMSMessagesBySycnDirection, 3);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySMSMessagesBySycnDirection != null) {
            querySMSMessagesBySycnDirection.close();
        }
        Cursor queryMMSMessagesBySycnDirection = this.mMmsScheduler.queryMMSMessagesBySycnDirection(4, str);
        if (queryMMSMessagesBySycnDirection != null) {
            try {
                if (queryMMSMessagesBySycnDirection.moveToFirst()) {
                    this.mMmsScheduler.notifyMsgAppFetchBuffer(queryMMSMessagesBySycnDirection, 4);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryMMSMessagesBySycnDirection != null) {
            queryMMSMessagesBySycnDirection.close();
        }
        Cursor queryRCSMessagesBySycnDirection = this.mRcsScheduler.queryRCSMessagesBySycnDirection(1, str);
        if (queryRCSMessagesBySycnDirection != null) {
            try {
                if (queryRCSMessagesBySycnDirection.moveToFirst()) {
                    this.mRcsScheduler.notifyMsgAppFetchBuffer(queryRCSMessagesBySycnDirection, 1);
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        }
        if (queryRCSMessagesBySycnDirection != null) {
            queryRCSMessagesBySycnDirection.close();
            return;
        }
        return;
        throw th;
        throw th;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void fetchingPendingMsg() {
        String valueOf = String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId());
        Cursor querySMSMessagesBySycnDirection = this.mSmsScheduler.querySMSMessagesBySycnDirection(3, valueOf);
        if (querySMSMessagesBySycnDirection != null) {
            try {
                if (querySMSMessagesBySycnDirection.moveToFirst()) {
                    this.mSmsScheduler.msgAppFetchBuffer(querySMSMessagesBySycnDirection, CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.SMS);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (querySMSMessagesBySycnDirection != null) {
            querySMSMessagesBySycnDirection.close();
        }
        Cursor queryMMSMessagesBySycnDirection = this.mMmsScheduler.queryMMSMessagesBySycnDirection(4, valueOf);
        if (queryMMSMessagesBySycnDirection != null) {
            try {
                if (queryMMSMessagesBySycnDirection.moveToFirst()) {
                    this.mMmsScheduler.msgAppFetchBuffer(queryMMSMessagesBySycnDirection, CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MMS);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (queryMMSMessagesBySycnDirection != null) {
            queryMMSMessagesBySycnDirection.close();
        }
        Cursor queryRCSMessagesBySycnDirection = this.mRcsScheduler.queryRCSMessagesBySycnDirection(1, valueOf);
        if (queryRCSMessagesBySycnDirection != null) {
            try {
                if (queryRCSMessagesBySycnDirection.moveToFirst()) {
                    this.mRcsScheduler.msgAppFetchBuffer(queryRCSMessagesBySycnDirection, CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.CHAT);
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        }
        if (queryRCSMessagesBySycnDirection != null) {
            queryRCSMessagesBySycnDirection.close();
            return;
        }
        return;
        throw th;
        throw th;
        throw th;
    }
}
