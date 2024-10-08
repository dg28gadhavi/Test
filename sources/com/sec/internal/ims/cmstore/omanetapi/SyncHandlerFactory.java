package com.sec.internal.ims.cmstore.omanetapi;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.LineManager;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.MessageDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.VvmDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.OMAObjectUpdateScheduler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.MessageSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.VvmGreetingSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.VvmSyncHandler;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncHandlerFactory {
    public String TAG = SyncHandlerFactory.class.getSimpleName();
    private Context mContext;
    private Map<SyncParam, BaseDataChangeHandler> mDataChangeHandlerPool;
    private Map<SyncParam, BaseDeviceDataUpdateHandler> mDeviceDataUpdatePool;
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final INetAPIEventListener mINetAPIEventListener;
    private final LineManager mLineManager;
    private Looper mLooper;
    private final MessageStoreClient mStoreClient;
    private Map<SyncParam, BaseSyncHandler> mSyncHandlerPool;
    private final IUIEventCallback mUIInterface;

    public SyncHandlerFactory(Looper looper, MessageStoreClient messageStoreClient, INetAPIEventListener iNetAPIEventListener, IUIEventCallback iUIEventCallback, LineManager lineManager, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mContext = messageStoreClient.getContext();
        this.mLooper = looper;
        this.mINetAPIEventListener = iNetAPIEventListener;
        this.mUIInterface = iUIEventCallback;
        this.mLineManager = lineManager;
        this.mSyncHandlerPool = new HashMap();
        this.mDataChangeHandlerPool = new HashMap();
        this.mDeviceDataUpdatePool = new HashMap();
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        registerLineListener();
    }

    private void registerLineListener() {
        this.mLineManager.registerLineStatusOberser(new LineManager.LineStatusObserver() {
            public void onLineAdded(String str) {
                String str2 = SyncHandlerFactory.this.TAG;
                Log.i(str2, "onLineAdded: " + IMSLog.checker(str));
            }
        });
    }

    public BaseSyncHandler getSyncHandlerInstance(SyncParam syncParam) {
        return getSyncHandlerInstance(syncParam, true);
    }

    public BaseSyncHandler getSyncHandlerInstance(SyncParam syncParam, boolean z) {
        if (this.mSyncHandlerPool.containsKey(syncParam)) {
            return this.mSyncHandlerPool.get(syncParam);
        }
        String str = syncParam.mLine;
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[syncParam.mType.ordinal()];
        if (i == 1) {
            MessageSyncHandler messageSyncHandler = new MessageSyncHandler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, this.mUIInterface, str, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper, true);
            this.mSyncHandlerPool.put(syncParam, messageSyncHandler);
            return messageSyncHandler;
        } else if (i == 2) {
            VvmGreetingSyncHandler vvmGreetingSyncHandler = new VvmGreetingSyncHandler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, this.mUIInterface, str, SyncMsgType.VM_GREETINGS, this.mICloudMessageManagerHelper);
            this.mSyncHandlerPool.put(syncParam, vvmGreetingSyncHandler);
            return vvmGreetingSyncHandler;
        } else if (i != 3) {
            MessageSyncHandler messageSyncHandler2 = new MessageSyncHandler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, this.mUIInterface, str, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper, z);
            this.mSyncHandlerPool.put(syncParam, messageSyncHandler2);
            return messageSyncHandler2;
        } else {
            VvmSyncHandler vvmSyncHandler = new VvmSyncHandler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, this.mUIInterface, str, SyncMsgType.VM, this.mICloudMessageManagerHelper, z);
            this.mSyncHandlerPool.put(syncParam, vvmSyncHandler);
            return vvmSyncHandler;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.SyncHandlerFactory$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType[] r0 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType = r0
                com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.MESSAGE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM_GREETINGS     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.SyncHandlerFactory.AnonymousClass2.<clinit>():void");
        }
    }

    public List<BaseSyncHandler> getAllSyncHandlerInstances() {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<SyncParam, BaseSyncHandler> value : this.mSyncHandlerPool.entrySet()) {
            arrayList.add((BaseSyncHandler) value.getValue());
        }
        return arrayList;
    }

    public void clearAllSyncHandlerInstances() {
        this.mSyncHandlerPool.clear();
    }

    public List<BaseSyncHandler> getAllSyncHandlerInstancesByLine(String str) {
        ArrayList arrayList = new ArrayList();
        if (TextUtils.isEmpty(str)) {
            return arrayList;
        }
        for (Map.Entry next : this.mSyncHandlerPool.entrySet()) {
            if (((SyncParam) next.getKey()).mLine.equals(str)) {
                arrayList.add((BaseSyncHandler) next.getValue());
            }
        }
        return arrayList;
    }

    public BaseDataChangeHandler getDataChangeHandlerInstance(SyncParam syncParam) {
        if (this.mDataChangeHandlerPool.containsKey(syncParam)) {
            return this.mDataChangeHandlerPool.get(syncParam);
        }
        String str = syncParam.mLine;
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[syncParam.mType.ordinal()];
        if (i == 1) {
            MessageDataChangeHandler messageDataChangeHandler = new MessageDataChangeHandler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, this.mUIInterface, str, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mDataChangeHandlerPool.put(syncParam, messageDataChangeHandler);
            return messageDataChangeHandler;
        } else if (i == 2 || i == 3) {
            VvmDataChangeHandler vvmDataChangeHandler = new VvmDataChangeHandler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, this.mUIInterface, str, SyncMsgType.VM, this.mICloudMessageManagerHelper);
            this.mDataChangeHandlerPool.put(syncParam, vvmDataChangeHandler);
            return vvmDataChangeHandler;
        } else {
            MessageDataChangeHandler messageDataChangeHandler2 = new MessageDataChangeHandler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, this.mUIInterface, str, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mDataChangeHandlerPool.put(syncParam, messageDataChangeHandler2);
            return messageDataChangeHandler2;
        }
    }

    public List<BaseDataChangeHandler> getAllDataChangeHandlerInstances() {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<SyncParam, BaseDataChangeHandler> value : this.mDataChangeHandlerPool.entrySet()) {
            arrayList.add((BaseDataChangeHandler) value.getValue());
        }
        return arrayList;
    }

    public void clearAllDataChangeHandlerInstances() {
        this.mDataChangeHandlerPool.clear();
    }

    public List<BaseDataChangeHandler> getAllDataChangeHandlerInstancesByLine(String str) {
        ArrayList arrayList = new ArrayList();
        if (TextUtils.isEmpty(str)) {
            return arrayList;
        }
        for (Map.Entry next : this.mDataChangeHandlerPool.entrySet()) {
            if (((SyncParam) next.getKey()).mLine.equals(str)) {
                arrayList.add((BaseDataChangeHandler) next.getValue());
            }
        }
        return arrayList;
    }

    public BaseDeviceDataUpdateHandler getDeviceDataUpdateHandlerInstance(SyncParam syncParam) {
        OMAObjectUpdateScheduler oMAObjectUpdateScheduler;
        String str = this.TAG;
        Log.d(str, "getDeviceDataUpdateHandlerInstance: " + syncParam);
        if (this.mDeviceDataUpdatePool.containsKey(syncParam)) {
            return this.mDeviceDataUpdatePool.get(syncParam);
        }
        String str2 = syncParam.mLine;
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$SyncMsgType[syncParam.mType.ordinal()];
        if (i == 1) {
            oMAObjectUpdateScheduler = new OMAObjectUpdateScheduler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, str2, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mDeviceDataUpdatePool.put(syncParam, oMAObjectUpdateScheduler);
        } else if (i != 3) {
            oMAObjectUpdateScheduler = new OMAObjectUpdateScheduler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, str2, SyncMsgType.MESSAGE, this.mICloudMessageManagerHelper);
            this.mDeviceDataUpdatePool.put(syncParam, oMAObjectUpdateScheduler);
        } else {
            oMAObjectUpdateScheduler = new OMAObjectUpdateScheduler(this.mLooper, this.mStoreClient, this.mINetAPIEventListener, str2, SyncMsgType.VM, this.mICloudMessageManagerHelper);
            this.mDeviceDataUpdatePool.put(syncParam, oMAObjectUpdateScheduler);
        }
        oMAObjectUpdateScheduler.start();
        return oMAObjectUpdateScheduler;
    }

    public List<BaseDeviceDataUpdateHandler> getAllDeviceDataUpdateHandlerInstances() {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<SyncParam, BaseDeviceDataUpdateHandler> value : this.mDeviceDataUpdatePool.entrySet()) {
            arrayList.add((BaseDeviceDataUpdateHandler) value.getValue());
        }
        return arrayList;
    }

    public void clearAllDeviceDataUpdateHandlerInstances() {
        this.mDeviceDataUpdatePool.clear();
    }

    public List<BaseDeviceDataUpdateHandler> getAllDeviceDataUpdateHandlerInstancesByLine(String str) {
        ArrayList arrayList = new ArrayList();
        if (TextUtils.isEmpty(str)) {
            return arrayList;
        }
        for (Map.Entry next : this.mDeviceDataUpdatePool.entrySet()) {
            if (((SyncParam) next.getKey()).mLine.equals(str)) {
                arrayList.add((BaseDeviceDataUpdateHandler) next.getValue());
            }
        }
        return arrayList;
    }
}
