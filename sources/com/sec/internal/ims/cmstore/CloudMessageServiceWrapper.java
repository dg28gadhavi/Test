package com.sec.internal.ims.cmstore;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.ims.ICentralMsgStoreService;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.CmsJsonConstants;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class CloudMessageServiceWrapper extends StateMachine {
    private static final int EVENT_CANCEL_RCSMESSAGE_LIST = 19;
    private static final int EVENT_CMS_SERVICE_STARTED = 20;
    private static final int EVENT_CREATE_PARTICIPANT = 13;
    private static final int EVENT_CREATE_SESSION = 14;
    private static final int EVENT_DEFAULT_SMS_APP_CHANGED = 17;
    private static final int EVENT_DELETE_RCSMESSAGELIST_USINGCHATID = 11;
    private static final int EVENT_DELETE_RCSMESSAGELIST_USINGIMDNID = 16;
    private static final int EVENT_DELETE_RCSMESSAGELIST_USINGMSGID = 10;
    private static final int EVENT_GETPROFILE = 3;
    private static final int EVENT_ON_IMSDEREGISTERED = 18;
    private static final int EVENT_ON_IMSREGISTERED = 15;
    private static final int EVENT_READ_RCSMESSAGE_LIST = 12;
    private static final int EVENT_RECEIVE_RCS_MESSAGE = 8;
    private static final int EVENT_SENT_RCS_MESSAGE = 9;
    private static final int EVENT_SERVICE_ACTIVE = 6;
    private static final int EVENT_SERVICE_CONNECTED = 4;
    private static final int EVENT_SERVICE_DEACTIVE = 7;
    private static final int EVENT_SERVICE_DISCONNECTED = 5;
    private static final int EVENT_SIM_READY = 1;
    private static final int EVENT_SIM_REFRESH = 2;
    private static final int mReadImsProfileValueDelay = 1200;
    /* access modifiers changed from: private */
    public String LOG_TAG = CloudMessageServiceWrapper.class.getSimpleName();
    private boolean isCmsServiceActive = false;
    private ServiceConnection mCloudMessageConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            String r0 = CloudMessageServiceWrapper.this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("service connect : ");
            sb.append(componentName != null ? componentName.toString() : "");
            Log.i(r0, sb.toString());
            CloudMessageServiceWrapper.this.mCloudMessageService = ICentralMsgStoreService.Stub.asInterface(iBinder);
            CloudMessageServiceWrapper.this.sendMessage(4);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            String r0 = CloudMessageServiceWrapper.this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("service disconnect : ");
            sb.append(componentName != null ? componentName.toString() : "");
            Log.i(r0, sb.toString());
            CloudMessageServiceWrapper.this.mCloudMessageService = null;
            CloudMessageServiceWrapper cloudMessageServiceWrapper = CloudMessageServiceWrapper.this;
            cloudMessageServiceWrapper.transitionTo(cloudMessageServiceWrapper.mSimReadyState);
        }
    };
    /* access modifiers changed from: private */
    public ICentralMsgStoreService mCloudMessageService = null;
    private Context mContext;
    /* access modifiers changed from: private */
    public State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public State mServiceConnectedState = new ServiceConnectedState();
    /* access modifiers changed from: private */
    public ISimManager mSimManager;
    /* access modifiers changed from: private */
    public State mSimReadyState = new SimReadyState();
    /* access modifiers changed from: private */
    public int mSimSlot;

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            CloudMessageServiceWrapper.this.log("DefaultState, enter");
        }

        public boolean processMessage(Message message) {
            String r0 = CloudMessageServiceWrapper.this.LOG_TAG;
            Log.i(r0, "DefaultState, processMessage: " + message.what);
            if (message.what != 1) {
                return false;
            }
            if (CloudMessageServiceWrapper.this.hasMessages(2)) {
                Log.d(CloudMessageServiceWrapper.this.LOG_TAG, "Sim refresh is ongoing. SIM readyretry after");
                CloudMessageServiceWrapper.this.sendMessageDelayed(1, 800);
                return true;
            } else if (!Util.isSimExist(CloudMessageServiceWrapper.this.mSimManager)) {
                return true;
            } else {
                CloudMessageServiceWrapper.this.onSimReady();
                CloudMessageServiceWrapper cloudMessageServiceWrapper = CloudMessageServiceWrapper.this;
                cloudMessageServiceWrapper.transitionTo(cloudMessageServiceWrapper.mSimReadyState);
                return true;
            }
        }

        public void exit() {
            CloudMessageServiceWrapper.this.log("DefaultState, exit");
        }
    }

    private class SimReadyState extends State {
        private SimReadyState() {
        }

        public void enter() {
            CloudMessageServiceWrapper.this.log("SimReadyState, enter");
        }

        public boolean processMessage(Message message) {
            String r0 = CloudMessageServiceWrapper.this.LOG_TAG;
            Log.i(r0, "SimReadyState, processMessage: " + message.what);
            int i = message.what;
            if (i != 2) {
                if (i == 3) {
                    CloudMessageServiceWrapper.this.onProfileReady();
                } else if (i == 4) {
                    CloudMessageServiceWrapper.this.sendMessage(6);
                    CloudMessageServiceWrapper cloudMessageServiceWrapper = CloudMessageServiceWrapper.this;
                    cloudMessageServiceWrapper.transitionTo(cloudMessageServiceWrapper.mServiceConnectedState);
                } else if (i == 7) {
                    try {
                        CloudMessageServiceWrapper.this.onDisableCms();
                        if (OmcCode.isKorOpenOnlyOmcCode() || OmcCode.isSKTOmcCode()) {
                            CloudMessageServiceWrapper.this.disconnect();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else if (i != 20) {
                    return false;
                } else {
                    boolean r4 = CloudMessageServiceWrapper.this.isCmsEnabled();
                    String r02 = CloudMessageServiceWrapper.this.LOG_TAG;
                    Log.i(r02, "is CMS enabled: " + r4);
                    IMSLog.c(LogClass.MCS_RESTART_RECEIVED, CloudMessageServiceWrapper.this.mSimSlot + "," + r4);
                    if (r4) {
                        CloudMessageServiceWrapper.this.connect();
                    }
                }
            } else if (!Util.isSimExist(CloudMessageServiceWrapper.this.mSimManager)) {
                try {
                    CloudMessageServiceWrapper.this.onSimRemoved();
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                }
                if (!ImsUtil.isMcsSupported(CloudMessageServiceWrapper.this.mSimSlot)) {
                    CloudMessageServiceWrapper.this.disconnect();
                }
                CloudMessageServiceWrapper cloudMessageServiceWrapper2 = CloudMessageServiceWrapper.this;
                cloudMessageServiceWrapper2.transitionTo(cloudMessageServiceWrapper2.mDefaultState);
            }
            return true;
        }

        public void exit() {
            CloudMessageServiceWrapper.this.log("SimReadyState, exit");
        }
    }

    private class ServiceConnectedState extends State {
        private ServiceConnectedState() {
        }

        public void enter() {
            CloudMessageServiceWrapper.this.log("ServiceConnectedState, enter");
        }

        public boolean processMessage(Message message) {
            String r0 = CloudMessageServiceWrapper.this.LOG_TAG;
            Log.i(r0, "ServiceConnectedState, processMessage: " + message.what);
            switch (message.what) {
                case 5:
                    Log.e(CloudMessageServiceWrapper.this.LOG_TAG, "lost service connection for unknow reason, retry connection ");
                    break;
                case 6:
                    try {
                        CloudMessageServiceWrapper.this.onRCSDbReady();
                        break;
                    } catch (RemoteException e) {
                        String r3 = CloudMessageServiceWrapper.this.LOG_TAG;
                        Log.e(r3, "onRCSDbReady: " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                case 7:
                    try {
                        CloudMessageServiceWrapper.this.onDisableCms();
                        break;
                    } catch (RemoteException e2) {
                        String r32 = CloudMessageServiceWrapper.this.LOG_TAG;
                        Log.e(r32, "onDisableRCS: " + e2.getMessage());
                        e2.printStackTrace();
                        break;
                    }
                case 8:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.receivedMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e3) {
                            e3.printStackTrace();
                            break;
                        }
                    }
                case 9:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.sentMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e4) {
                            e4.printStackTrace();
                            break;
                        }
                    }
                case 10:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.deleteMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e5) {
                            e5.printStackTrace();
                            break;
                        }
                    }
                case 11:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.deleteMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e6) {
                            e6.printStackTrace();
                            break;
                        }
                    }
                case 12:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.readMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e7) {
                            e7.printStackTrace();
                            break;
                        }
                    }
                case 13:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.createParticipant(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e8) {
                            e8.printStackTrace();
                            break;
                        }
                    }
                case 14:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.createSession(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e9) {
                            e9.printStackTrace();
                            break;
                        }
                    }
                case 15:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            ImsRegistration imsRegistration = (ImsRegistration) message.obj;
                            String ownNumber = imsRegistration.getOwnNumber();
                            if (!TextUtils.isEmpty(ownNumber)) {
                                CloudMessageServiceWrapper.this.mCloudMessageService.manualSync(CloudMessageProviderContract.ApplicationTypes.RCSDATA, ownNumber);
                            }
                            CloudMessageServiceWrapper.this.mCloudMessageService.onRegistered(imsRegistration);
                            break;
                        } catch (RemoteException e10) {
                            e10.printStackTrace();
                            break;
                        }
                    }
                case 16:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.deleteMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e11) {
                            e11.printStackTrace();
                            break;
                        }
                    }
                case 17:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.onDefaultSmsPackageChanged();
                            break;
                        } catch (RemoteException e12) {
                            e12.printStackTrace();
                            break;
                        }
                    }
                case 18:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        CloudMessageServiceWrapper.this.serviceNotBindYet();
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.onDeregistered((ImsRegistration) message.obj);
                            break;
                        } catch (RemoteException e13) {
                            e13.printStackTrace();
                            break;
                        }
                    }
                case 19:
                    if (CloudMessageServiceWrapper.this.mCloudMessageService == null) {
                        Log.e(CloudMessageServiceWrapper.this.LOG_TAG, "Service is not binded yet");
                        break;
                    } else {
                        try {
                            CloudMessageServiceWrapper.this.mCloudMessageService.cancelMessage(CloudMessageProviderContract.ApplicationTypes.RCSDATA, (String) message.obj);
                            break;
                        } catch (RemoteException e14) {
                            e14.printStackTrace();
                            break;
                        }
                    }
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            CloudMessageServiceWrapper.this.log("ServiceConnectedState, exit");
        }
    }

    public CloudMessageServiceWrapper(int i, Context context, Looper looper) {
        super("CloudMessageServiceWrapper[" + i + "]", looper);
        this.mSimSlot = i;
        this.LOG_TAG += "[" + this.mSimSlot + "]";
        this.mContext = context;
        initStates();
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mSimSlot);
        this.mSimManager = simManagerFromSimSlot;
        if (simManagerFromSimSlot != null) {
            simManagerFromSimSlot.registerForSimReady(getHandler(), 1, (Object) null);
            this.mSimManager.registerForSimRefresh(getHandler(), 2, (Object) null);
            this.mSimManager.registerForSimRemoved(getHandler(), 2, (Object) null);
        }
        registerCMSStartIntent();
    }

    private void initStates() {
        addState(this.mDefaultState);
        addState(this.mSimReadyState, this.mDefaultState);
        addState(this.mServiceConnectedState, this.mSimReadyState);
        setInitialState(this.mDefaultState);
        start();
    }

    /* access modifiers changed from: private */
    public void onProfileReady() {
        boolean isCmsEnabled = isCmsEnabled();
        String str = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onProfileReady isCMSServiceEnabled: ");
        sb.append(isCmsEnabled);
        sb.append(" isCmsServiceActive: ");
        sb.append(this.isCmsServiceActive);
        sb.append(" cloudsvcstatus ");
        sb.append(this.mCloudMessageService != null);
        Log.i(str, sb.toString());
        if ("AIO".equals(OmcCode.getNWCode(this.mSimSlot)) || !isCmsEnabled) {
            Log.e(this.LOG_TAG, "Central message store not enabled.");
            sendMessage(7);
        } else if (!this.isCmsServiceActive || this.mCloudMessageService == null) {
            connect();
        } else {
            sendMessage(4);
        }
    }

    /* access modifiers changed from: private */
    public void onSimReady() {
        Log.v(this.LOG_TAG, "onSimReady");
        sendMessageDelayed(3, 1200);
    }

    /* access modifiers changed from: private */
    public void serviceNotBindYet() {
        Log.e(this.LOG_TAG, "Service is not binded yet");
    }

    public String getImsi() {
        return this.mSimManager.getImsi();
    }

    /* access modifiers changed from: private */
    public boolean isCmsEnabled() {
        String string = ImsRegistry.getString(this.mSimSlot, GlobalSettingsConstants.Registration.EXTENDED_SERVICES, "");
        ArrayList arrayList = new ArrayList();
        if (string != null) {
            arrayList.addAll(Arrays.asList(string.split(",")));
        }
        return arrayList.contains("cms");
    }

    private String getJsonStringChatIdList(List<String> list) {
        return CmsUtil.getJsonElements(list, CmsJsonConstants.JSON_TYPE.CHAT);
    }

    public void receiveRCSMessage(int i, String str, String str2) {
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(8);
            obtainMessage.obj = CmsUtil.getJsonElements(i, str, str2);
            sendMessage(obtainMessage);
        }
    }

    public void sentRCSMessage(int i, String str, String str2) {
        String str3 = this.LOG_TAG;
        Log.i(str3, "sentRCSMessage, isCmsServiceActive: " + this.isCmsServiceActive);
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(9);
            obtainMessage.obj = CmsUtil.getJsonElements(i, str, str2);
            sendMessage(obtainMessage);
        }
    }

    public void deleteRCSMessageListUsingMsgId(List<String> list) {
        String str = this.LOG_TAG;
        Log.i(str, "deleteRCSMessageListUsingMsgId, isCmsServiceActive: " + this.isCmsServiceActive);
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(10);
            obtainMessage.obj = CmsUtil.getJsonElements(list, CmsJsonConstants.JSON_TYPE.DEFAULT);
            sendMessage(obtainMessage);
        }
    }

    public void deleteRCSMessageListUsingImdnId(List<String> list) {
        String str = this.LOG_TAG;
        Log.i(str, "deleteRCSMessageListUsingImdnId, isCmsServiceActive: " + this.isCmsServiceActive);
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(16);
            obtainMessage.obj = CmsUtil.getJsonElements(list, CmsJsonConstants.JSON_TYPE.IMDN);
            sendMessage(obtainMessage);
        }
    }

    public void deleteRCSMessageListUsingChatId(List<String> list) {
        String str = this.LOG_TAG;
        Log.i(str, "deleteRCSMessageListUsingChatId, isCmsServiceActive: " + this.isCmsServiceActive);
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(11);
            obtainMessage.obj = getJsonStringChatIdList(list);
            sendMessage(obtainMessage);
        }
    }

    public void readRCSMessageList(List<String> list) {
        String str = this.LOG_TAG;
        Log.i(str, "readRCSMessageList, isCmsServiceActive: " + this.isCmsServiceActive);
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(12);
            obtainMessage.obj = CmsUtil.getJsonElements(list, CmsJsonConstants.JSON_TYPE.IMDN);
            sendMessage(obtainMessage);
        }
    }

    public void cancelRCSMessageList(List<String> list) {
        String str = this.LOG_TAG;
        Log.i(str, "cancelRCSMessageList, isCmsServiceActive: " + this.isCmsServiceActive);
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(19);
            obtainMessage.obj = CmsUtil.getJsonElements(list, CmsJsonConstants.JSON_TYPE.IMDN);
            sendMessage(obtainMessage);
        }
    }

    public void createParticipant(String str) {
        if (this.isCmsServiceActive) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "createParticipant : " + str);
            Message obtainMessage = obtainMessage(13);
            obtainMessage.obj = str;
            sendMessage(obtainMessage);
        }
    }

    public void createSession(String str) {
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(14);
            obtainMessage.obj = str;
            sendMessage(obtainMessage);
        }
    }

    /* access modifiers changed from: private */
    public void onRCSDbReady() throws RemoteException {
        Log.i(this.LOG_TAG, "onRCSDbReady: ");
        notifyStatusChanged(CloudMessageProviderContract.SimStatusValue.SIM_READY, CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_ENABLE);
    }

    /* access modifiers changed from: private */
    public void onDisableCms() throws RemoteException {
        Log.d(this.LOG_TAG, "onDisableCms: ");
        notifyStatusChanged(CloudMessageProviderContract.SimStatusValue.SIM_READY, CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_DISABLE);
    }

    /* access modifiers changed from: private */
    public void onSimRemoved() throws RemoteException {
        Log.d(this.LOG_TAG, "onSimRemoved: ");
        notifyStatusChanged(CloudMessageProviderContract.SimStatusValue.SIM_REMOVED, CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_DISABLE);
    }

    /* access modifiers changed from: protected */
    public void registerCMSStartIntent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CloudMessageIntent.INTENT_ACTION_CMS_RESTART);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && CloudMessageIntent.INTENT_ACTION_CMS_RESTART.equals(intent.getAction())) {
                    Log.i(CloudMessageServiceWrapper.this.LOG_TAG, "CMS Restart received");
                    CloudMessageServiceWrapper.this.sendMessage(20);
                }
            }
        }, intentFilter);
    }

    public void onImsRegistered(ImsRegistration imsRegistration) throws RemoteException {
        String str = this.LOG_TAG;
        Log.i(str, "onImsRegistered: isCmsServiceActive: " + this.isCmsServiceActive);
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(15);
            obtainMessage.obj = imsRegistration;
            sendMessage(obtainMessage);
        }
    }

    public void onImsDeregistered(ImsRegistration imsRegistration) throws RemoteException {
        String str = this.LOG_TAG;
        Log.i(str, "onImsDeregistered: isCmsServiceActive: " + this.isCmsServiceActive);
        if (this.isCmsServiceActive) {
            Message obtainMessage = obtainMessage(18);
            obtainMessage.obj = imsRegistration;
            sendMessage(obtainMessage);
        }
    }

    public void onDefaultSmsPackageChanged() throws RemoteException {
        Log.i(this.LOG_TAG, "onDefaultSmsPackageChanged: ");
        if (this.isCmsServiceActive) {
            sendMessage(obtainMessage(17));
        }
    }

    public void connect() {
        Mno simMno = SimUtil.getSimMno(this.mSimSlot);
        String str = this.LOG_TAG;
        IMSLog.i(str, "connect mCloudMsgService:" + this.mCloudMessageService + ", mno: " + simMno);
        if (!Mno.ATT.equals(simMno) || (!TextUtils.isEmpty(SemCscFeature.getInstance().getString(SecFeature.CSC.TAG_CSCFEATURE_MESSAGE_CONFIGOPBACKUPSYNC)) && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) <= 33)) {
            if (this.mCloudMessageService == null) {
                Intent intent = new Intent(this.mContext, CloudMessageService.class);
                intent.setAction(this.mContext.getPackageName() + ":" + this.mSimSlot);
                intent.putExtra("appName", this.mContext.getPackageName());
                intent.putExtra("sim_slot", this.mSimSlot);
                boolean bindService = this.mContext.bindService(intent, this.mCloudMessageConnection, 1);
                String str2 = this.LOG_TAG;
                Log.i(str2, "bind to cloud message service " + bindService);
            }
            this.isCmsServiceActive = true;
            return;
        }
        IMSLog.e(this.LOG_TAG, "AMBS has been disabled for this model");
    }

    public void disconnect() {
        try {
            if (this.mCloudMessageService != null) {
                this.mContext.unbindService(this.mCloudMessageConnection);
                this.mCloudMessageService = null;
            }
            Log.i(this.LOG_TAG, "disconnected");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        this.isCmsServiceActive = false;
    }

    private void notifyStatusChanged(String str, String str2) throws RemoteException {
        try {
            IMSLog.c(LogClass.MCS_ONRCS_DB_READY_STATUS, this.mSimSlot + "," + str + "," + str2);
            if (this.mCloudMessageService != null) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put(CloudMessageProviderContract.JsonParamTags.SIM_STATUS, str);
                jSONObject.put(CloudMessageProviderContract.JsonParamTags.CMS_PROFILE_EVENT, str2);
                this.mCloudMessageService.onRCSDBReady(jSONObject.toString());
                return;
            }
            serviceNotBindYet();
        } catch (NullPointerException | SecurityException | JSONException e) {
            String str3 = this.LOG_TAG;
            Log.e(str3, "notifyStatusChanged Failed due to Exception: " + e.getMessage());
        }
    }
}
