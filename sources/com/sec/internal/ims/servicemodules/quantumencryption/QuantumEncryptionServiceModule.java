package com.sec.internal.ims.servicemodules.quantumencryption;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.util.Log;
import android.widget.Toast;
import com.sec.internal.constants.ims.servicemodules.volte2.QuantumNotifyParam;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.volte2.ImsCallSessionManager;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.servicemodules.quantumencryption.IQuantumEncryptionServiceModule;
import com.sec.internal.log.IMSLog;
import com.voltecrypt.service.SXICTQMVoLTECallBack;
import com.voltecrypt.service.SxHangUpEntity;
import com.voltecrypt.service.SxRequestAuthenticationEntity;
import com.voltecrypt.service.SxRequestPeerProfileEntity;
import com.voltecrypt.service.SxRequestQMKeyEntity;
import java.util.Arrays;

public class QuantumEncryptionServiceModule extends ServiceModuleBase implements IQuantumEncryptionServiceModule {
    private static final String APP_KEY_DEBUG = "BBCC0DB28C9B291BCCA4F817AB8A58F79F60646F79D021578203AA2039BEB67E95B3F52FBB70165DC0D887D956691636E611579C19DC15A9A382BACB39098A8291F0D953E8D5D0F1169AB9328A6C3E4A0451784CDF69076DC87689CEC7D4CE4396335778A1FAB1E7B4680740CF45075AA75758F56582BC9B2436E796759B4D375FB054BFE495EFD1CDCBFE71D5E3624BBEBE7DF08DFF3E1B673524F713E3547DFF32D1186C2EDA295583F9053BEFD00C55DC079454BE7AC4DEBF3FF5675E843F862A7BB68A497A188A6B893D36DACDB1204EFC8B72A32FE2690C5FDA8B9095DD31027DC5930DE2ACDD04A810CAFBF8231902B8E366AC11C7BB5B2408219F56FD8C6788B92A3672FC4A60ABF85F6BB2ECE77C4128723CA670AF349EAF8AB9CB443977A017F321F49A8E93F4B7E5A3D32E5CCE8A1002EEB4A26F21BE94B6EBDD553211547111D6284DE2F6D3A0389833B59E24EB2B48DC956BD055F2A8112C3BD50A80E0F5298C3DF95D66FB8E9BE22669C8EC6D12BBD63F70C6460A088510CCFB";
    private static final String APP_KEY_USER = "BBCC0DB28C9B291BCCA4F817AB8A58F79F60646F79D021578203AA2039BEB67E95B3F52FBB70165DC0D887D956691636E611579C19DC15A9A382BACB39098A82058EC22645C2EB31B17EF2900AA67C6C7E5C702A9F43EA43E8BA291D66D005C36E8341D8081F42465CCA7903405CC4F8A7ADCE4C23210CC12B3B994740207CE746168B22203446D9BD74AA605A63B2710CFE556E742972916543ADB00BD38C2D7A6E8F8B17B5EBFA90E15ECD8BE03E2EC682C8A1F6BA812B45F009F9E13E3C6A148B7BF88F4EF3A01AE00AC54FC5638208894D2E6040EE682FECA86BF05B69FE48348C0090D378FD43DBE4E718495709257A5E764844E32B8EB2DE18F234831038887A2373E10ECC051D6394D0262AEB293E0BC2BFEF5A7704AD7298D854B1C67368034BC16335D49C79AB695697D6B099010AA9E1CA20EFD152696017F23ED0CC0B9C1E6E44EAF36AAB7D2691514048B363270EB007776B05726F20DA19FFDF27A23D03EDAB6465D8EF60498658421DB8A55968FE8E2D68DA45C6F85D9782CA";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = QuantumEncryptionServiceModule.class.getSimpleName();
    private static final String ORGANIZATION_CODE = "3401040010000004";
    private static final String QSS_PACKAGE_NAME = "com.ctq.simkey.pivot";
    private int mAuthStatus = -1;
    private Context mContext;
    private SimpleEventLog mEventLog;
    private int mLoginStatus = 0;
    private QuantumEncryptionNotifier mNotifier;

    public void handleIntent(Intent intent) {
    }

    public QuantumEncryptionServiceModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        this.mNotifier = new QuantumEncryptionNotifier();
        registerPackageEventReceiver();
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 200);
    }

    public String[] getServicesRequiring() {
        return new String[]{"mmtel"};
    }

    public Context getContext() {
        return this.mContext;
    }

    private synchronized int getAuthStatus() {
        return this.mAuthStatus;
    }

    private synchronized void setAuthStatus(int i) {
        this.mAuthStatus = i;
    }

    private void handleQuantumAuthenticationStatus(QuantumNotifyParam quantumNotifyParam) {
        if (quantumNotifyParam.getStatus() != 0) {
            String str = LOG_TAG;
            Log.i(str, "handleQuantumAuthenticationStatus fail: " + quantumNotifyParam.getStatus());
        }
        setAuthStatus(quantumNotifyParam.getStatus());
        if (!SemSystemProperties.get("ro.build.type", "user").equals("user")) {
            Toast.makeText(this.mContext, quantumNotifyParam.getStatus() == 0 ? "Auth Success!!!" : "Auth fail!!!", 1).show();
        }
    }

    private void handleQuantumLoginResult(QuantumNotifyParam quantumNotifyParam) {
        if (quantumNotifyParam.getStatus() != 0) {
            String str = LOG_TAG;
            Log.i(str, "handleQuantumLoginResult fail: " + quantumNotifyParam.getStatus());
        }
        if (getAuthStatus() != 0) {
            String str2 = LOG_TAG;
            Log.i(str2, "handleQuantumLoginResult abnormal authStatus: " + getAuthStatus());
        }
        this.mLoginStatus = quantumNotifyParam.getStatus();
    }

    private void handleQuantumPeerProfileStatus(QuantumNotifyParam quantumNotifyParam) {
        ImsCallSessionManager imsCallSessionManager = getServiceModuleManager().getVolteServiceModule().getImsCallSessionManager();
        imsCallSessionManager.updateQuantumPeerProfileStatus(imsCallSessionManager.getActiveCallSessionId(), quantumNotifyParam.getStatus(), quantumNotifyParam.getComment(), quantumNotifyParam.getQtSessionId(), quantumNotifyParam.getRequestMark());
    }

    private void handleQuantumQMKeyStatus(QuantumNotifyParam quantumNotifyParam) {
        ImsCallSessionManager imsCallSessionManager = getServiceModuleManager().getVolteServiceModule().getImsCallSessionManager();
        imsCallSessionManager.updateQuantumQMKeyStatus(imsCallSessionManager.getActiveCallSessionId(), quantumNotifyParam.getStatus(), quantumNotifyParam.getComment(), quantumNotifyParam.getQtSessionId(), quantumNotifyParam.getKey(), quantumNotifyParam.getRequestMark());
    }

    private void handleQuantumVoLteStatus(QuantumNotifyParam quantumNotifyParam) {
        if (quantumNotifyParam.getStatus() != 0) {
            String str = LOG_TAG;
            Log.i(str, "handleQuantumVoLteStatus fail: " + quantumNotifyParam.getStatus());
            resetAuthStatus();
            return;
        }
        onRequestAuthentication(new SxRequestAuthenticationEntity(ORGANIZATION_CODE, "com.sec.imsservice", SemSystemProperties.get("ro.build.type", "user").equals("user") ? APP_KEY_USER : APP_KEY_DEBUG, "0"));
    }

    public boolean isSuccessAuthAndLogin() {
        return getAuthStatus() == 0 && this.mLoginStatus == 0;
    }

    public int registerVoLTECallback(SXICTQMVoLTECallBack sXICTQMVoLTECallBack) {
        Log.i(LOG_TAG, "registerVoLTECallback");
        this.mNotifier.registerVoLTECallback(sXICTQMVoLTECallBack);
        this.mEventLog.logAndAdd("registerVoLTECallback");
        IMSLog.c(LogClass.QEC_REGISTER_VOLTE_CALLBACK);
        return 0;
    }

    public int notifyAuthenticationStatus(int i, String str, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "notifyQuantumAuthenticationStatus status: " + i + " comment: " + str + " requestMark: " + str2);
        sendMessage(obtainMessage(1, new QuantumNotifyParam.Builder().setStatus(i).setComment(str).setRequestMark(str2).build()));
        SimpleEventLog simpleEventLog = this.mEventLog;
        StringBuilder sb = new StringBuilder();
        sb.append("notifyAuthenticationStatus status: ");
        sb.append(i);
        simpleEventLog.logAndAdd(sb.toString());
        IMSLog.c(LogClass.QEC_NOTIFY_AUTH_STATUS, "" + i);
        return 0;
    }

    public int notifyPeerProfileStatus(int i, String str, String str2, String str3) {
        String str4 = LOG_TAG;
        Log.i(str4, "notifyPeerProfileStatus status: " + i + " comment: " + str + " qtSessionId: " + IMSLog.checker(str2) + " requestMark: " + str3);
        sendMessage(obtainMessage(3, new QuantumNotifyParam.Builder().setStatus(i).setComment(str).setQtSessionId(str2).setRequestMark(str3).build()));
        int convertToSessionId = getServiceModuleManager().getVolteServiceModule().getImsCallSessionManager().convertToSessionId(Integer.parseInt(str3));
        SimpleEventLog simpleEventLog = this.mEventLog;
        StringBuilder sb = new StringBuilder();
        sb.append("notifyPeerProfileStatus status: ");
        sb.append(i);
        sb.append(" sessionId: ");
        sb.append(convertToSessionId);
        simpleEventLog.logAndAdd(sb.toString());
        IMSLog.c(LogClass.QEC_NOTIFY_PEER_PROFILE, i + "," + convertToSessionId);
        return 0;
    }

    public int notifyQMKeyStatus(int i, String str, String str2, byte[] bArr, String str3) {
        String str4 = LOG_TAG;
        Log.i(str4, "notifyQMKeyStatus status: " + i + " comment: " + str + " qtSessionId: " + IMSLog.checker(str2) + " key: " + IMSLog.checker(Arrays.toString(bArr)) + " requestMark: " + str3);
        sendMessage(obtainMessage(4, new QuantumNotifyParam.Builder().setStatus(i).setComment(str).setQtSessionId(str2).setKey(bArr).setRequestMark(str3).build()));
        int convertToSessionId = getServiceModuleManager().getVolteServiceModule().getImsCallSessionManager().convertToSessionId(Integer.parseInt(str3));
        SimpleEventLog simpleEventLog = this.mEventLog;
        StringBuilder sb = new StringBuilder();
        sb.append("notifyQMKeyStatus status: ");
        sb.append(i);
        sb.append(" sessionId: ");
        sb.append(convertToSessionId);
        simpleEventLog.logAndAdd(sb.toString());
        IMSLog.c(LogClass.QEC_NOTIFY_QMKEY_STATUS, i + "," + convertToSessionId);
        return 0;
    }

    public int notifyVoLTEStatus(int i, String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyVoLTEStatus status: " + i + " comment: " + str);
        sendMessage(obtainMessage(5, new QuantumNotifyParam.Builder().setStatus(i).setComment(str).build()));
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("notifyVoLTEStatus status: " + i);
        IMSLog.c(LogClass.QEC_NOTIFY_VOLTE_STATUS, "" + i);
        return 0;
    }

    public void notifyLoginResult(int i, String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyLoginResult status: " + i + " token: " + str);
        sendMessage(obtainMessage(2, new QuantumNotifyParam.Builder().setStatus(i).setToken(str).build()));
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("notifyLoginResult status: " + i);
        IMSLog.c(LogClass.QEC_NOTIFY_LOGIN_RESULT, "" + i);
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 1) {
            handleQuantumAuthenticationStatus((QuantumNotifyParam) message.obj);
        } else if (i == 2) {
            handleQuantumLoginResult((QuantumNotifyParam) message.obj);
        } else if (i == 3) {
            handleQuantumPeerProfileStatus((QuantumNotifyParam) message.obj);
        } else if (i == 4) {
            handleQuantumQMKeyStatus((QuantumNotifyParam) message.obj);
        } else if (i == 5) {
            handleQuantumVoLteStatus((QuantumNotifyParam) message.obj);
        }
    }

    public void onRequestAuthentication(SxRequestAuthenticationEntity sxRequestAuthenticationEntity) {
        String str = LOG_TAG;
        Log.i(str, "onRequestAuthentication " + sxRequestAuthenticationEntity);
        post(new QuantumEncryptionServiceModule$$ExternalSyntheticLambda3(this, sxRequestAuthenticationEntity));
        this.mEventLog.logAndAdd("onRequestAuthentication");
        IMSLog.c(LogClass.QEC_REQUEST_AUTH);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onRequestAuthentication$0(SxRequestAuthenticationEntity sxRequestAuthenticationEntity) {
        this.mNotifier.onRequestAuthentication(sxRequestAuthenticationEntity);
    }

    public int onRequestPeerProfileStatus(SxRequestPeerProfileEntity sxRequestPeerProfileEntity) {
        String str = LOG_TAG;
        Log.i(str, "onRequestPeerProfileStatus " + sxRequestPeerProfileEntity);
        int convertToSessionId = getServiceModuleManager().getVolteServiceModule().getImsCallSessionManager().convertToSessionId(Integer.parseInt(sxRequestPeerProfileEntity.getRequestMark()));
        if (!isSuccessAuthAndLogin()) {
            Log.i(str, "onRequestPeerProfileStatus(" + convertToSessionId + ") auth and login fail");
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("onRequestPeerProfileStatus(" + convertToSessionId + ") auth and login fail");
            StringBuilder sb = new StringBuilder();
            sb.append(convertToSessionId);
            sb.append(",not requested");
            IMSLog.c(LogClass.QEC_REQUEST_PEER_PROFILE, sb.toString());
            return -1;
        }
        post(new QuantumEncryptionServiceModule$$ExternalSyntheticLambda4(this, sxRequestPeerProfileEntity));
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("onRequestPeerProfileStatus sessionId: " + convertToSessionId);
        IMSLog.c(LogClass.QEC_REQUEST_PEER_PROFILE, "" + convertToSessionId);
        return 0;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onRequestPeerProfileStatus$1(SxRequestPeerProfileEntity sxRequestPeerProfileEntity) {
        this.mNotifier.onRequestPeerProfileStatus(sxRequestPeerProfileEntity);
    }

    public int onRequestQMKey(SxRequestQMKeyEntity sxRequestQMKeyEntity) {
        String str = LOG_TAG;
        Log.i(str, "onRequestQMKey " + sxRequestQMKeyEntity);
        int convertToSessionId = getServiceModuleManager().getVolteServiceModule().getImsCallSessionManager().convertToSessionId(Integer.parseInt(sxRequestQMKeyEntity.getRequestMark()));
        if (!isSuccessAuthAndLogin()) {
            Log.i(str, "onRequestQMKey(" + convertToSessionId + ") auth and login fail");
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("onRequestQMKey(" + convertToSessionId + ") auth and login fail");
            StringBuilder sb = new StringBuilder();
            sb.append(convertToSessionId);
            sb.append(",not requested");
            IMSLog.c(LogClass.QEC_REQUEST_QMKEY, sb.toString());
            return -1;
        }
        post(new QuantumEncryptionServiceModule$$ExternalSyntheticLambda1(this, sxRequestQMKeyEntity));
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("onRequestQMKey sessionId: " + convertToSessionId);
        IMSLog.c(LogClass.QEC_REQUEST_QMKEY, "" + convertToSessionId);
        return 0;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onRequestQMKey$2(SxRequestQMKeyEntity sxRequestQMKeyEntity) {
        this.mNotifier.onRequestQMKey(sxRequestQMKeyEntity);
    }

    public int onRequestQMKeyWithDelay(SxRequestQMKeyEntity sxRequestQMKeyEntity, int i) {
        String str = LOG_TAG;
        Log.i(str, "onRequestQMKeyWithDelay " + sxRequestQMKeyEntity);
        int convertToSessionId = getServiceModuleManager().getVolteServiceModule().getImsCallSessionManager().convertToSessionId(Integer.parseInt(sxRequestQMKeyEntity.getRequestMark()));
        if (!isSuccessAuthAndLogin()) {
            Log.i(str, "onRequestQMKeyWithDelay(" + convertToSessionId + ") auth and login fail");
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("onRequestQMKeyWithDelay(" + convertToSessionId + ") auth and login fail");
            StringBuilder sb = new StringBuilder();
            sb.append(convertToSessionId);
            sb.append(",not requested");
            IMSLog.c(LogClass.QEC_REQUEST_QMKEY_WITH_DELAY, sb.toString());
            return -1;
        }
        postDelayed(new QuantumEncryptionServiceModule$$ExternalSyntheticLambda5(this, sxRequestQMKeyEntity), (long) i);
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("onRequestQMKeyWithDelay sessionId: " + convertToSessionId + " delayMs: " + i);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(convertToSessionId);
        sb2.append(",");
        sb2.append(i);
        IMSLog.c(LogClass.QEC_REQUEST_QMKEY_WITH_DELAY, sb2.toString());
        return 0;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onRequestQMKeyWithDelay$3(SxRequestQMKeyEntity sxRequestQMKeyEntity) {
        this.mNotifier.onRequestQMKey(sxRequestQMKeyEntity);
    }

    public int onGetVoLTEStatus() {
        String str = LOG_TAG;
        Log.i(str, "onGetVoLTEStatus");
        if (!isSuccessAuthAndLogin()) {
            Log.i(str, "onGetVoLTEStatus auth and login fail");
            this.mEventLog.logAndAdd("onGetVoLTEStatus auth and login fail");
            IMSLog.c(LogClass.QEC_GET_VOLTE_STATUS, "not requested");
            return -1;
        }
        post(new QuantumEncryptionServiceModule$$ExternalSyntheticLambda2(this));
        this.mEventLog.logAndAdd("onGetVoLTEStatus");
        IMSLog.c(LogClass.QEC_GET_VOLTE_STATUS);
        return 0;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onGetVoLTEStatus$4() {
        this.mNotifier.onGetVoLTEStatus();
    }

    public int onGetVoLTEStatusComment() {
        String str = LOG_TAG;
        Log.i(str, "onGetVoLTEStatusComment");
        if (!isSuccessAuthAndLogin()) {
            Log.i(str, "onGetVoLTEStatusComment auth and login fail");
            this.mEventLog.logAndAdd("onGetVoLTEStatusComment auth and login fail");
            IMSLog.c(LogClass.QEC_GET_VOLTE_STATUS_COMMENT, "not requested");
            return -1;
        }
        post(new QuantumEncryptionServiceModule$$ExternalSyntheticLambda6(this));
        this.mEventLog.logAndAdd("onGetVoLTEStatusComment");
        IMSLog.c(LogClass.QEC_GET_VOLTE_STATUS_COMMENT);
        return 0;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onGetVoLTEStatusComment$5() {
        this.mNotifier.onGetVoLTEStatusComment();
    }

    public void onHangUp(SxHangUpEntity sxHangUpEntity) {
        String str = LOG_TAG;
        Log.i(str, "onHangUp " + sxHangUpEntity);
        if (!isSuccessAuthAndLogin()) {
            Log.i(str, "onHangUp auth and login fail");
            this.mEventLog.logAndAdd("onHangUp auth and login fail");
            IMSLog.c(LogClass.QEC_HANGUP, "not requested");
            return;
        }
        post(new QuantumEncryptionServiceModule$$ExternalSyntheticLambda0(this, sxHangUpEntity));
        this.mEventLog.logAndAdd("onHangUp");
        IMSLog.c(LogClass.QEC_HANGUP);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onHangUp$6(SxHangUpEntity sxHangUpEntity) {
        this.mNotifier.onHangUp(sxHangUpEntity);
    }

    public void resetAuthStatus() {
        setAuthStatus(-1);
    }

    private void registerPackageEventReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        intentFilter.addDataSchemeSpecificPart(QSS_PACKAGE_NAME, 0);
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String str = "";
                if (intent.getData() != null) {
                    str = intent.getData().toString().replace("package:", str);
                }
                String action = intent.getAction();
                String r5 = QuantumEncryptionServiceModule.LOG_TAG;
                IMSLog.d(r5, "packageStatus : " + action + ", packageName : " + str);
                QuantumEncryptionServiceModule.this.resetAuthStatus();
            }
        }, intentFilter);
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of QuantumEncryptionService:");
        IMSLog.increaseIndent(str);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(str);
    }
}
