package com.sec.internal.ims.cmstore.mcs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.sec.ims.ICentralMsgStoreService;
import com.sec.ims.ICentralMsgStoreServiceListener;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.ims.cmstore.CloudMessageProvider;
import com.sec.internal.ims.cmstore.CloudMessageService;
import com.sec.internal.ims.cmstore.JanskyIntentTranslation;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.NetAPIWorkingStatusControllerMcs;
import com.sec.internal.ims.cmstore.adapters.McsRetryMapAdapter;
import com.sec.internal.ims.cmstore.adapters.RetryMapAdapter;
import com.sec.internal.ims.cmstore.adapters.RetryStackAdapter;
import com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.mcs.contactsync.McsContactSync;
import com.sec.internal.ims.cmstore.mcs.provision.workflow.WorkflowMcs;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.AppRequestHandler;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CmsHttpController;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.gba.GbaServiceModule;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class MCSClient extends Handler implements MessageStoreClient, IBufferDBEventListener, IUIEventCallback {
    /* access modifiers changed from: private */
    public String LOG_TAG = MCSClient.class.getSimpleName();
    /* access modifiers changed from: private */
    public AppRequestHandler mAppRequestHandler = null;
    ICentralMsgStoreService.Stub mBinder = new ICentralMsgStoreService.Stub() {
        public void deleteOldLegacyMessage(String str, String str2) throws RemoteException {
        }

        public void deleteParticipant(String str, String str2) throws RemoteException {
        }

        public void deleteSession(String str, String str2) throws RemoteException {
        }

        public void disableAutoSync(String str, String str2) throws RemoteException {
        }

        public void enableAutoSync(String str, String str2) throws RemoteException {
        }

        public int getRestartScreenName(String str) throws RemoteException {
            return 0;
        }

        public void manualSync(String str, String str2) throws RemoteException {
        }

        public void notifyCloudMessageUpdate(String str, String str2, String str3) throws RemoteException {
        }

        public void notifyUIScreen(String str, int i, String str2, int i2) throws RemoteException {
        }

        public void onBufferDBReadResultBatch(String str, String str2) throws RemoteException {
        }

        public boolean onUIButtonProceed(String str, int i, String str2) throws RemoteException {
            return false;
        }

        public void onUserEnterApp(String str) throws RemoteException {
        }

        public void onUserLeaveApp(String str) throws RemoteException {
        }

        public void registerCallback(String str, ICentralMsgStoreService iCentralMsgStoreService) throws RemoteException {
        }

        public void restartService(String str) throws RemoteException {
        }

        public void resumeSync(String str) throws RemoteException {
        }

        public void startDeltaSync(String str, String str2) throws RemoteException {
        }

        public void startFullSync(String str, String str2) throws RemoteException {
        }

        public void stopSync(String str, String str2) throws RemoteException {
        }

        public void wipeOutMessage(String str, String str2) throws RemoteException {
        }

        public void receivedMessage(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.d(r0, r1, "receivedMessage " + str + ": " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.receivedMessageJson(str2);
            }
        }

        public void sentMessage(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.d(r0, r1, "sentMessage " + str + ": " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.sentMessageJson(str2);
            }
        }

        public void readMessage(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.i(r0, r1, "readMessage " + str + ": " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.readMessageJson(str, str2);
            }
        }

        public void cancelMessage(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.i(r0, r1, "cancelMessage " + str + ": " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.cancelMessageJson(str, str2);
            }
        }

        public void requestMessageProcess(String str, String str2, int i) {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.i(r0, r1, "requestMessageProcess " + str + " function: " + i + ": " + str2);
            if (!MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else if (i == 0) {
                MCSClient.this.mCloudMessageScheduler.starredRCSMessageList(str2);
            } else if (i == 1) {
                MCSClient.this.mCloudMessageScheduler.unStarredRCSMessageList(str2);
            }
        }

        public void unReadMessage(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.d(r0, r1, "unReadMessage " + str + ": " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.unReadMessageJson(str2);
            }
        }

        public void deleteMessage(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.i(r0, r1, "deleteMessage " + str + ": " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.deleteMessageJson(str2);
            }
        }

        public void uploadMessage(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.d(r0, r1, "uploadMessage " + str + ": " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.uploadMessageJson(str2);
            }
        }

        public void downloadMessage(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.d(r0, r1, "downloadMessage " + str + ": " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.downloadMessageJson(str2);
            }
        }

        public void requestOperation(int i, int i2, String str, String str2) throws RemoteException {
            String str3 = MCSClient.this.getCloudMessageStrategyManager().getStrategy().getNmsHost() + str;
            IMSLog.d(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "requestOperation " + i2 + " url: " + str3 + ": " + str2);
            if (MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                MCSClient.this.mAppRequestHandler.processAppRequest(str2, str3, i2);
            } else {
                IMSLog.e(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "Cms Profile is inactive");
            }
        }

        private boolean isValidAppType(String str) {
            if (!CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(str) && !"VVMDATA".equalsIgnoreCase(str)) {
                return CloudMessageProviderContract.ApplicationTypes.RCSDATA.equalsIgnoreCase(str);
            }
            return true;
        }

        private void logInvalidAppType() {
            IMSLog.e(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "invalid apptype ");
        }

        public void onBufferDBReadResult(String str, String str2, String str3, String str4, int i, boolean z) throws RemoteException {
            IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "onBufferDBReadResult: " + str + " msgType: " + str2 + " bufferRowID: " + str3 + " appMessageId: " + str4 + " syncAction: " + i + " isSuccess: " + z);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.isCmsProfileActive()) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "onBufferDBReadResult ignore");
            } else if (!MCSClient.this.getCloudMessageStrategyManager().getStrategy().getIsInitSyncIndicatorRequired() || Integer.valueOf(str3).intValue() >= 0) {
                MCSClient.this.mCloudMessageScheduler.onBufferDBReadResult(str2, str3, str4, i, z);
            } else {
                IMSLog.d(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "rowID < 0");
                MCSClient.this.mNetAPIWorkingController.hideIndicator();
            }
        }

        public void createSession(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.d(r0, r1, "createSession " + str + " chatId: " + str2);
            MCSClient.this.mCloudMessageScheduler.createSession(str2);
        }

        public void createParticipant(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.d(r0, r1, "createParticipant " + str + " chatId: " + str2);
            MCSClient.this.mCloudMessageScheduler.createParticipant(str2);
        }

        public void onRCSDBReady(String str) throws RemoteException {
            IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "onRCSDBReady");
            try {
                JSONObject jSONObject = new JSONObject(str);
                String string = jSONObject.getString(CloudMessageProviderContract.JsonParamTags.CMS_PROFILE_EVENT);
                String string2 = jSONObject.getString(CloudMessageProviderContract.JsonParamTags.SIM_STATUS);
                String r1 = MCSClient.this.LOG_TAG;
                int r2 = MCSClient.this.mPhoneId;
                IMSLog.d(r1, r2, "eventType = " + string + ", simStatus = " + string2);
                if (CloudMessageProviderContract.SimStatusValue.SIM_REMOVED.equals(string2)) {
                    EventLogHelper.infoLogAndAdd(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "SIM removed");
                    IMSLog.c(LogClass.MCS_PV_SIM_EVENT, MCSClient.this.mPhoneId + ",PV:SIM RM");
                    MCSClient.this.mNetAPIWorkingController.setCmsProfileEnabled(false);
                    MCSClient.this.setProvisionStatus(false);
                    MCSClient.this.mProvisionWorkflow.clearWorkflow();
                    return;
                }
                if (CmsUtil.isSimChanged(MCSClient.this.msc)) {
                    EventLogHelper.infoLogAndAdd(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "SIM changed");
                    IMSLog.c(LogClass.MCS_PV_SIM_EVENT, MCSClient.this.mPhoneId + ",PV:SIM CH");
                    MCSClient.this.mNetAPIWorkingController.clearData();
                    MCSClient.this.mProvisionWorkflow.clearData();
                }
                if (CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_ENABLE.equals(string)) {
                    EventLogHelper.infoLogAndAdd(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "CMS profile enabled");
                    IMSLog.c(LogClass.MCS_PV_SIM_EVENT, MCSClient.this.mPhoneId + ",PV:CMS EN");
                    MCSClient.this.mNetAPIWorkingController.setCmsProfileEnabled(true);
                    MCSClient.this.startProvisioning();
                    MCSClient.this.mCloudMessageScheduler.onRCSDbReady();
                }
            } catch (JSONException unused) {
                IMSLog.e(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "Json parsing exception");
            }
        }

        public void onFTUriResponse(String str, String str2) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.i(r0, r1, "onFtUriResponse " + str + " " + str2);
            if (!isValidAppType(str) || !MCSClient.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                logInvalidAppType();
            } else {
                MCSClient.this.mCloudMessageScheduler.onFtUriResponseJson(str, str2);
            }
        }

        public void sendTryRegisterCms(int i, String str) throws RemoteException {
            if (MCSClient.this.mPhoneId != i) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "phoneid is not matched. ignore");
            } else if (DmConfigHelper.getImsUserSetting(MCSClient.this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), MCSClient.this.mPhoneId) != 1 || !CmsUtil.isMcsSupported(MCSClient.this.mContext, MCSClient.this.mPhoneId)) {
                EventLogHelper.infoLogAndAdd(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "Registration is not allowed in non RCS user");
                MCSClient.this.mProvisionWorkflow.notifyMcsProvisionListener(1, 200, 2, (Object) null);
            } else {
                String r5 = MCSClient.this.LOG_TAG;
                int r1 = MCSClient.this.mPhoneId;
                EventLogHelper.infoLogAndAdd(r5, r1, "sendTryRegisterCms consent_context " + str);
                IMSLog.c(LogClass.MCS_PV_INIT_REGISTRATION, MCSClient.this.mPhoneId + ",PV:TRY REGI");
                MCSClient mCSClient = MCSClient.this;
                mCSClient.sendMessage(mCSClient.obtainMessage(1, str));
            }
        }

        public void sendTryDeregisterCms(int i) throws RemoteException {
            if (MCSClient.this.mPhoneId != i) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "phoneid is not matched. ignore");
                return;
            }
            EventLogHelper.infoLogAndAdd(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "sendTryDeregisterCms");
            IMSLog.c(LogClass.MCS_PV_DEREGISTRATION, MCSClient.this.mPhoneId + ",PV:TRY DEREGI");
            MCSClient.this.sendEmptyMessage(7);
        }

        public void manageSd(int i, int i2, String str) throws RemoteException {
            if (MCSClient.this.mPhoneId != i) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "phoneid is not matched. ignore");
                return;
            }
            String r4 = MCSClient.this.LOG_TAG;
            int r0 = MCSClient.this.mPhoneId;
            IMSLog.i(r4, r0, "manageSd type : " + i2);
            MCSClient mCSClient = MCSClient.this;
            mCSClient.sendMessage(mCSClient.obtainMessage(2, i2, 0, str));
        }

        public void getSd(int i, boolean z, String str) throws RemoteException {
            if (MCSClient.this.mPhoneId != i) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "phoneid is not matched. ignore");
                return;
            }
            String r4 = MCSClient.this.LOG_TAG;
            int r0 = MCSClient.this.mPhoneId;
            IMSLog.i(r4, r0, "getSd getAll : " + z);
            Bundle bundle = new Bundle();
            bundle.putBoolean(McsConstants.BundleData.GET_ALL, z);
            bundle.putString(McsConstants.BundleData.INFO, str);
            MCSClient mCSClient = MCSClient.this;
            mCSClient.sendMessage(mCSClient.obtainMessage(3, bundle));
        }

        public void getAccount(int i) throws RemoteException {
            if (MCSClient.this.mPhoneId != i) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "phoneid is not matched. ignore");
                return;
            }
            IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "getAccount");
            MCSClient mCSClient = MCSClient.this;
            mCSClient.sendMessage(mCSClient.obtainMessage(6));
        }

        public void updateAccountInfo(int i, String str) throws RemoteException {
            if (MCSClient.this.mPhoneId != i) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "phoneid is not matched. ignore");
                return;
            }
            IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "updateAccountInfo");
            MCSClient mCSClient = MCSClient.this;
            mCSClient.sendMessage(mCSClient.obtainMessage(5, str));
        }

        public void startContactSyncActivity(int i, boolean z) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            IMSLog.i(r0, i, "startContactSyncActivity: initialSync: " + z);
            if (i != MCSClient.this.mPhoneId) {
                IMSLog.i(MCSClient.this.LOG_TAG, i, "phoneId is not matched. ignore");
            } else if (z) {
                MCSClient.this.mMcsContactSync.sendMessage(MCSClient.this.mMcsContactSync.obtainMessage(1, Boolean.TRUE));
            } else {
                MCSClient.this.mMcsContactSync.sendMessage(MCSClient.this.mMcsContactSync.obtainMessage(2, Boolean.TRUE));
            }
        }

        public void onDefaultSmsPackageChanged() throws RemoteException {
            MCSClient.this.mProvisionWorkflow.onDefaultSmsPackageChanged();
            if (!CmsUtil.isDefaultMessageAppInUse(MCSClient.this.mContext)) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "msg app is not Samsung Messages");
                MCSClient.this.mMcsContactSync.sendEmptyMessage(8);
                MCSClient.this.setProvisionStatus(false);
                return;
            }
            EventLogHelper.infoLogAndAdd(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "onDefaultSmsPackageChanged");
            IMSLog.c(LogClass.MCS_PV_INIT_REGISTRATION, MCSClient.this.mPhoneId + ",PV:DMA CH");
            if (MCSClient.this.mCloudMessagePreferenceManager.getMcsUser() == 1) {
                MCSClient.this.sendEmptyMessage(4);
            }
        }

        public void onRegistered(ImsRegistration imsRegistration) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.i(r0, r1, "onRegistered " + imsRegistration);
            if (imsRegistration.hasRcsService() && imsRegistration.getPhoneId() == MCSClient.this.mPhoneId && !MCSClient.this.getProvisionStatus()) {
                MCSClient.this.startProvisioning();
            }
        }

        public void onDeregistered(ImsRegistration imsRegistration) throws RemoteException {
            String r0 = MCSClient.this.LOG_TAG;
            int r1 = MCSClient.this.mPhoneId;
            IMSLog.i(r0, r1, "onDeregistered " + imsRegistration);
            if (imsRegistration.hasRcsService() && imsRegistration.getPhoneId() == MCSClient.this.mPhoneId && DmConfigHelper.getImsUserSetting(MCSClient.this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), MCSClient.this.mPhoneId) == 0 && CmsUtil.isMcsSupported(MCSClient.this.mContext, MCSClient.this.mPhoneId)) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "It is RCS OFF, so try deregister MCS service");
                MCSClient.this.sendEmptyMessage(7);
            }
        }

        public void registerCmsProvisioningListenerByPhoneId(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener, int i) throws RemoteException {
            if (MCSClient.this.mPhoneId != i) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "registerCmsProvisioningListenerByPhoneId phoneid is not matched. ignore");
                return;
            }
            EventLogHelper.infoLogAndAdd(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "registerCmsProvisioningListener from app");
            MCSClient.this.registerCmsProvisioningListener(iCentralMsgStoreServiceListener, true);
        }

        public void unregisterCmsProvisioningListenerByPhoneId(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener, int i) throws RemoteException {
            if (MCSClient.this.mPhoneId != i) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "unregisterCmsProvisioningListenerByPhoneId phoneid is not matched. ignore");
                return;
            }
            IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "unregisterCmsProvisioningListener from app");
            MCSClient.this.unregisterCmsProvisioningListener(iCentralMsgStoreServiceListener);
        }
    };
    private HandlerThread mBufferDBHandlingThread;
    /* access modifiers changed from: private */
    public CloudMessagePreferenceManager mCloudMessagePreferenceManager;
    /* access modifiers changed from: private */
    public CloudMessageBufferSchedulingHandler mCloudMessageScheduler = null;
    private final CloudMessageService mCloudMessageService;
    private CloudMessageStrategyManager mCloudMessageStrategyManager;
    /* access modifiers changed from: private */
    public Context mContext;
    private ConnectivityManager.NetworkCallback mDefaultNetworkCallback = null;
    private CmsHttpController mHttpController;
    public boolean mIsProvisioned = false;
    private JanskyIntentTranslation mJanskyTranslation;
    protected final Object mLock = new Object();
    /* access modifiers changed from: private */
    public McsContactSync mMcsContactSync;
    private ArrayList<IMcsFcmPushNotificationListener> mMcsFcmPushNotificationListener = new ArrayList<>();
    private McsFcmPushNotifier mMcsFcmPushNotifier;
    protected final RemoteCallbackList<ICentralMsgStoreServiceListener> mMcsProvisioningListener = new RemoteCallbackList<>();
    private HandlerThread mNetAPIHandlingThread;
    /* access modifiers changed from: private */
    public NetAPIWorkingStatusControllerMcs mNetAPIWorkingController = null;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private HandlerThread mProvisionHandlingThread;
    /* access modifiers changed from: private */
    public WorkflowMcs mProvisionWorkflow;
    private McsRetryMapAdapter mRetryMapAdapter = null;
    private ISimManager mSimManager;
    /* access modifiers changed from: private */
    public MessageStoreClient msc;

    public RetryMapAdapter getRetryMapAdapter() {
        return null;
    }

    public RetryStackAdapter getRetryStackAdapter() {
        return null;
    }

    public void notifyAppCloudDeleteFail(String str, String str2, String str3) {
    }

    public void notifyAppNetworkOperationResult(boolean z) {
    }

    public void notifyAppUIScreen(int i, String str, int i2) {
    }

    public void onDestroy() {
    }

    public void showInitsyncIndicator(boolean z) {
    }

    public MCSClient(int i, Context context, CloudMessageService cloudMessageService) {
        this.mContext = context;
        this.mPhoneId = i;
        this.mSimManager = SimManagerFactory.getSimManagerFromSimSlot(i);
        this.mCloudMessageService = cloudMessageService;
        this.msc = this;
    }

    private void initializeRetryAdapter() {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "initializeRetryAdapter ");
        if (this.mRetryMapAdapter == null) {
            this.mRetryMapAdapter = new McsRetryMapAdapter();
        }
        this.mRetryMapAdapter.initRetryMapAdapter(this);
    }

    public synchronized ArrayList<IMcsFcmPushNotificationListener> getMcsFcmPushNotificationListener() {
        return this.mMcsFcmPushNotificationListener;
    }

    public synchronized void setMcsFcmPushNotificationListener(IMcsFcmPushNotificationListener iMcsFcmPushNotificationListener) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setMcsFcmPushNotificationListener: listener: " + iMcsFcmPushNotificationListener);
        if (iMcsFcmPushNotificationListener == null) {
            this.mMcsFcmPushNotificationListener.clear();
        } else {
            this.mMcsFcmPushNotificationListener.add(iMcsFcmPushNotificationListener);
        }
    }

    public void updateEvent(int i) {
        sendEmptyMessage(i);
    }

    public boolean updateDelay(int i, long j) {
        String str = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "update with " + i + " delayed " + j);
        return sendMessageDelayed(obtainMessage(i), j);
    }

    /* access modifiers changed from: private */
    public void startProvisioning() {
        EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "startProvisioning");
        if (!isRcsRegistered()) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "not RCS ready");
        } else if (!NetworkUtil.isConnected(this.mContext)) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "not network connection");
            registerDefaultNetworkCallback();
        } else if (!CmsUtil.isDefaultMessageAppInUse(this.mContext)) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "not samsung msg app");
        } else {
            if (Util.isRegistrationCodeInvalid(this.mCloudMessagePreferenceManager.getRegCode())) {
                IMSLog.i(this.LOG_TAG, this.mPhoneId, "registration code is expired, remove it");
                this.mCloudMessagePreferenceManager.saveRegCode("");
            }
            unregisterNetworkCallback();
            sendEmptyMessage(0);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x006f  */
    /* JADX WARNING: Removed duplicated region for block: B:19:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isRcsRegistered() {
        /*
            r7 = this;
            android.net.Uri r0 = com.sec.internal.constants.ims.cmstore.McsConstants.Uris.RCS_REGISTRATION_STATUS_URI
            android.net.Uri$Builder r0 = r0.buildUpon()
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simslot"
            r1.append(r2)
            int r2 = r7.mPhoneId
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.net.Uri$Builder r0 = r0.fragment(r1)
            android.net.Uri r2 = r0.build()
            android.content.Context r0 = r7.mContext
            android.content.ContentResolver r1 = r0.getContentResolver()
            java.lang.String r0 = "registration_status"
            java.lang.String[] r3 = new java.lang.String[]{r0}
            r4 = 0
            r5 = 0
            r6 = 0
            android.database.Cursor r0 = r1.query(r2, r3, r4, r5, r6)
            r1 = 0
            if (r0 == 0) goto L_0x004e
            boolean r2 = r0.moveToFirst()     // Catch:{ all -> 0x0044 }
            if (r2 == 0) goto L_0x004e
            int r2 = r0.getInt(r1)     // Catch:{ all -> 0x0044 }
            goto L_0x004f
        L_0x0044:
            r7 = move-exception
            r0.close()     // Catch:{ all -> 0x0049 }
            goto L_0x004d
        L_0x0049:
            r0 = move-exception
            r7.addSuppressed(r0)
        L_0x004d:
            throw r7
        L_0x004e:
            r2 = r1
        L_0x004f:
            if (r0 == 0) goto L_0x0054
            r0.close()
        L_0x0054:
            java.lang.String r0 = r7.LOG_TAG
            int r7 = r7.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "isRcsRegistered "
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r0, r7, r3)
            r7 = 1
            if (r2 != r7) goto L_0x0070
            r1 = r7
        L_0x0070:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.mcs.MCSClient.isRcsRegistered():boolean");
    }

    private void initializeSimInfo() {
        String msisdn = getSimManager().getMsisdn();
        this.mCloudMessagePreferenceManager.saveSimImsi(getSimManager().getImsi());
        this.mCloudMessagePreferenceManager.saveUserCtn(msisdn, false);
    }

    public void handleMessage(Message message) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: msg: " + message.what);
        switch (message.what) {
            case 0:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_INIT");
                initializeSimInfo();
                if (this.mCloudMessagePreferenceManager.getMcsUser() == 0) {
                    IMSLog.i(this.LOG_TAG, this.mPhoneId, "Do not start for not MCS user");
                    return;
                } else {
                    sendEmptyMessage(1);
                    return;
                }
            case 1:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_START");
                Object obj = message.obj;
                String str2 = obj != null ? (String) obj : null;
                WorkflowMcs workflowMcs = this.mProvisionWorkflow;
                if (workflowMcs != null) {
                    workflowMcs.startProvisioning(str2);
                    return;
                }
                return;
            case 2:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_MANAGE_SD");
                WorkflowMcs workflowMcs2 = this.mProvisionWorkflow;
                if (workflowMcs2 != null) {
                    workflowMcs2.manageSd(message.arg1, (String) message.obj);
                    return;
                }
                return;
            case 3:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_GET_SD");
                if (this.mProvisionWorkflow != null) {
                    this.mProvisionWorkflow.getSd(Boolean.valueOf(((Bundle) message.obj).getBoolean(McsConstants.BundleData.GET_ALL)), ((Bundle) message.obj).getString(McsConstants.BundleData.INFO));
                    return;
                }
                return;
            case 4:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_RE_AUTHENTICATION");
                WorkflowMcs workflowMcs3 = this.mProvisionWorkflow;
                if (workflowMcs3 != null) {
                    workflowMcs3.requestMcsReAuthentication();
                    return;
                }
                return;
            case 5:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_UPDATE_ACCOUNT");
                WorkflowMcs workflowMcs4 = this.mProvisionWorkflow;
                if (workflowMcs4 != null) {
                    workflowMcs4.updateAccountInfo((String) message.obj);
                    return;
                }
                return;
            case 6:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_GET_ACCOUNT");
                WorkflowMcs workflowMcs5 = this.mProvisionWorkflow;
                if (workflowMcs5 != null) {
                    workflowMcs5.getAccount();
                    return;
                }
                return;
            case 7:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_DEREGISTER");
                WorkflowMcs workflowMcs6 = this.mProvisionWorkflow;
                if (workflowMcs6 != null) {
                    workflowMcs6.disableMCS();
                    return;
                }
                return;
            case 8:
                EventLogHelper.infoLogAndAdd(this.LOG_TAG, this.mPhoneId, "HANDLE_MCS_PROVISION_COMPLETED");
                return;
            default:
                return;
        }
    }

    private ConnectivityManager.NetworkCallback getDefaultNetworkCallback() {
        return new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "onAvailable");
                if (network != null && !MCSClient.this.getProvisionStatus()) {
                    MCSClient.this.startProvisioning();
                }
            }

            public void onLost(Network network) {
                IMSLog.i(MCSClient.this.LOG_TAG, MCSClient.this.mPhoneId, "onLost");
            }
        };
    }

    private void registerDefaultNetworkCallback() {
        ConnectivityManager connectivityManager;
        if (this.mDefaultNetworkCallback == null && (connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity")) != null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "registerDefaultNetworkCallback");
            ConnectivityManager.NetworkCallback defaultNetworkCallback = getDefaultNetworkCallback();
            this.mDefaultNetworkCallback = defaultNetworkCallback;
            connectivityManager.registerDefaultNetworkCallback(defaultNetworkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        ConnectivityManager connectivityManager;
        if (this.mDefaultNetworkCallback != null && (connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity")) != null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "unregisterNetworkCallback");
            connectivityManager.unregisterNetworkCallback(this.mDefaultNetworkCallback);
            this.mDefaultNetworkCallback = null;
        }
    }

    public void setProvisionStatus(boolean z) {
        String str = this.LOG_TAG;
        IMSLog.i(str, "setProvisionStatus:" + z);
        this.mIsProvisioned = z;
    }

    public boolean getProvisionStatus() {
        return this.mIsProvisioned;
    }

    public void registerCmsProvisioningListener(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener, boolean z) {
        if (iCentralMsgStoreServiceListener == null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "listener: null");
            return;
        }
        synchronized (this.mLock) {
            if (this.mMcsProvisioningListener != null) {
                String str = this.LOG_TAG;
                int i = this.mPhoneId;
                EventLogHelper.infoLogAndAdd(str, i, "register listener: " + iCentralMsgStoreServiceListener);
                this.mMcsProvisioningListener.register(iCentralMsgStoreServiceListener);
            }
            if (z) {
                int i2 = this.mCloudMessagePreferenceManager.getMcsUser() == 1 ? 100 : 200;
                try {
                    String str2 = this.LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.i(str2, i3, "broadcast : " + i2);
                    iCentralMsgStoreServiceListener.onCmsRegistrationCompleted(i2, 1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void unregisterCmsProvisioningListener(ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener) {
        if (iCentralMsgStoreServiceListener == null) {
            IMSLog.i(this.LOG_TAG, this.mPhoneId, "listener: null");
            return;
        }
        synchronized (this.mLock) {
            if (this.mMcsProvisioningListener != null) {
                String str = this.LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "unregister listener: " + iCentralMsgStoreServiceListener);
                this.mMcsProvisioningListener.unregister(iCentralMsgStoreServiceListener);
            }
        }
    }

    public boolean requestMcsAccessToken(boolean z) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "requestMcsAccessToken: forceRefresh = " + z);
        WorkflowMcs workflowMcs = this.mProvisionWorkflow;
        if (workflowMcs == null) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "requestMcsAccessToken: workflow is null");
            return false;
        } else if (!z && workflowMcs.isValidAccessToken()) {
            return false;
        } else {
            this.mProvisionWorkflow.requestMcsAccessToken();
            return true;
        }
    }

    public RemoteCallbackList<ICentralMsgStoreServiceListener> getMcsProvisioningListener() {
        return this.mMcsProvisioningListener;
    }

    public String getRcsConfigurationValue(String str) {
        RcsConfigurationHelper.ConfigData configData = RcsConfigurationHelper.getConfigData(this.mContext, "root/*", this.mPhoneId);
        String str2 = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "getConfigurationValue: key: " + str);
        return configData.readString(str, "");
    }

    public int getClientID() {
        return this.mPhoneId;
    }

    public Context getContext() {
        return this.mContext;
    }

    public Binder getBinder() {
        return this.mBinder;
    }

    public CloudMessagePreferenceManager getPrerenceManager() {
        return this.mCloudMessagePreferenceManager;
    }

    public WorkflowMcs getProvisionWorkFlow() {
        return this.mProvisionWorkflow;
    }

    public CmsHttpController getHttpController() {
        return this.mHttpController;
    }

    public void onCreate(IImsFramework iImsFramework, GbaServiceModule gbaServiceModule) {
        this.mJanskyTranslation = new JanskyIntentTranslation(getContext(), this);
        this.mCloudMessagePreferenceManager = new CloudMessagePreferenceManager(this);
        this.mCloudMessageStrategyManager = new CloudMessageStrategyManager(this);
        this.mBufferDBHandlingThread = new HandlerThread("cloud message service buffer DB thread");
        this.mNetAPIHandlingThread = new HandlerThread("cloud message service NetAPI thread");
        this.mProvisionHandlingThread = new HandlerThread("cloud message service Provision thread");
        this.mBufferDBHandlingThread.start();
        this.mNetAPIHandlingThread.start();
        this.mProvisionHandlingThread.start();
        Looper looper = this.mBufferDBHandlingThread.getLooper();
        Looper looper2 = this.mNetAPIHandlingThread.getLooper();
        Looper looper3 = this.mProvisionHandlingThread.getLooper();
        initializeRetryAdapter();
        CloudMessageProvider.createBufferDBInstance(this);
        this.mHttpController = new CmsHttpController(this.mContext, this.mPhoneId);
        this.mNetAPIWorkingController = new NetAPIWorkingStatusControllerMcs(looper2, this, this);
        this.mProvisionWorkflow = new WorkflowMcs(looper3, this, this, this.mNetAPIWorkingController);
        this.mNetAPIWorkingController.registerCentralMsgStoreServiceListener();
        this.mMcsContactSync = new McsContactSync(this, this.mContext, this.mPhoneId);
        this.mCloudMessageScheduler = new CloudMessageBufferSchedulingHandler(looper, this, this.mNetAPIWorkingController, this, (ICloudMessageManagerHelper) null, true);
        this.mAppRequestHandler = new AppRequestHandler(this, this);
        this.mMcsFcmPushNotifier = new McsFcmPushNotifier(this, this.mPhoneId);
        startProvisioning();
    }

    public CloudMessageStrategyManager getCloudMessageStrategyManager() {
        return this.mCloudMessageStrategyManager;
    }

    public String getCurrentIMSI() {
        return this.mSimManager.getImsi();
    }

    public NetAPIWorkingStatusControllerMcs getNetAPIWorkingStatusController() {
        return this.mNetAPIWorkingController;
    }

    public CloudMessageBufferSchedulingHandler getCloudMessageBufferSchedulingHandler() {
        return this.mCloudMessageScheduler;
    }

    public ISimManager getSimManager() {
        return this.mSimManager;
    }

    public McsRetryMapAdapter getMcsRetryMapAdapter() {
        return this.mRetryMapAdapter;
    }

    public void notifyCloudMessageUpdate(String str, String str2, String str3, boolean z) {
        String str4 = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str4, i, "notifyCloudMessageUpdate, apptype: " + str + " msgType: " + str2 + " rowIDs: " + str3);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(str)) {
            this.mJanskyTranslation.onNotifyMessageApp(str2, str3, z);
        }
    }

    public void notifyAppInitialSyncStatus(String str, String str2, String str3, CloudMessageBufferDBConstants.InitialSyncStatusFlag initialSyncStatusFlag, boolean z) {
        String str4 = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str4, i, "notifyAppInitialSyncStatus, apptype: " + str + " msgType: " + str2 + " SyncStatus: " + initialSyncStatusFlag);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(str)) {
            this.mJanskyTranslation.onNotifyMessageAppInitialSyncStatus(str3, str2, initialSyncStatusFlag);
        }
    }

    public void notifyAppOperationResult(String str, int i) {
        IMSLog.i(this.LOG_TAG, this.mPhoneId, "notifyAppOperationResult");
        this.mJanskyTranslation.notifyAppOperationResult(str, i);
    }
}
