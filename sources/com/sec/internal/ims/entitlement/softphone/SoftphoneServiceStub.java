package com.sec.internal.ims.entitlement.softphone;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.SparseArray;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.entitlement.util.EncryptionHelper;
import com.sec.internal.ims.entitlement.util.SharedPrefHelper;
import com.sec.internal.log.IndentingPrintWriter;
import com.sec.vsim.attsoftphone.IEmergencyServiceListener;
import com.sec.vsim.attsoftphone.IProgressListener;
import com.sec.vsim.attsoftphone.ISoftphoneService;
import com.sec.vsim.attsoftphone.ISupplementaryServiceListener;
import com.sec.vsim.attsoftphone.data.CallForwardingInfo;
import com.sec.vsim.attsoftphone.data.CallWaitingInfo;
import com.sec.vsim.attsoftphone.data.DeviceInfo;
import java.util.List;
import javax.crypto.SecretKey;

public class SoftphoneServiceStub extends ISoftphoneService.Stub {
    /* access modifiers changed from: private */
    public final String LOG_TAG;
    private IntentFilter mAirplaneModeIntentFilter = null;
    private BroadcastReceiver mAirplaneModeReceiver;
    /* access modifiers changed from: private */
    public SparseArray<SoftphoneClient> mClients = new SparseArray<>();
    /* access modifiers changed from: private */
    public Context mContext = null;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    final ConnectivityManager.NetworkCallback mDefaultNetworkListener;
    public SimpleEventLog mEventLog;
    /* access modifiers changed from: private */
    public boolean mNetworkConnected = false;

    public SoftphoneServiceStub(Context context) {
        AnonymousClass1 r1 = new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                SimpleEventLog simpleEventLog = SoftphoneServiceStub.this.mEventLog;
                simpleEventLog.logAndAdd(SoftphoneServiceStub.this.LOG_TAG + ": mDefaultNetworkListener: onAvailable " + network);
                SoftphoneServiceStub.this.mNetworkConnected = true;
                SoftphoneServiceStub softphoneServiceStub = SoftphoneServiceStub.this;
                softphoneServiceStub.validateTokens(softphoneServiceStub.mCurrentUserId);
                for (int i = 0; i < SoftphoneServiceStub.this.mClients.size(); i++) {
                    SoftphoneClient softphoneClient = (SoftphoneClient) SoftphoneServiceStub.this.mClients.valueAt(i);
                    if (softphoneClient.getUserId() == SoftphoneServiceStub.this.mCurrentUserId) {
                        softphoneClient.onNetworkConnected();
                    }
                }
            }

            public void onLost(Network network) {
                SimpleEventLog simpleEventLog = SoftphoneServiceStub.this.mEventLog;
                simpleEventLog.logAndAdd(SoftphoneServiceStub.this.LOG_TAG + ": mDefaultNetworkListener: onLost + " + network);
                SoftphoneServiceStub.this.mNetworkConnected = false;
            }
        };
        this.mDefaultNetworkListener = r1;
        this.mAirplaneModeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int i = Settings.Global.getInt(SoftphoneServiceStub.this.mContext.getContentResolver(), "airplane_mode_on", 1);
                SimpleEventLog simpleEventLog = SoftphoneServiceStub.this.mEventLog;
                simpleEventLog.logAndAdd("mAirplaneModeReceiver onChange: " + i);
                if (i == 1) {
                    for (int i2 = 0; i2 < SoftphoneServiceStub.this.mClients.size(); i2++) {
                        SoftphoneClient softphoneClient = (SoftphoneClient) SoftphoneServiceStub.this.mClients.valueAt(i2);
                        if (softphoneClient.getUserId() == SoftphoneServiceStub.this.mCurrentUserId) {
                            softphoneClient.onAirplaneModeOn();
                        }
                    }
                }
            }
        };
        this.mContext = context;
        this.mCurrentUserId = Extensions.ActivityManager.getCurrentUser();
        String str = SoftphoneServiceStub.class.getSimpleName() + '-' + this.mCurrentUserId;
        this.LOG_TAG = str;
        this.mEventLog = new SimpleEventLog(context, str, 100);
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerDefaultNetworkCallback(r1);
        IntentFilter intentFilter = new IntentFilter(ImsConstants.Intents.ACTION_AIRPLANE_MODE);
        this.mAirplaneModeIntentFilter = intentFilter;
        this.mContext.registerReceiver(this.mAirplaneModeReceiver, intentFilter);
        this.mEventLog.logAndAdd("SoftphoneServiceStub(): registering mAirplaneModeReceiver");
        reloadAccounts();
        clearUnusedAddresses();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(Extensions.Intent.ACTION_USER_SWITCHED);
        this.mContext.registerReceiver(new UserSwitchReceiver(), intentFilter2);
    }

    private void reloadAccounts() {
        this.mEventLog.logAndAdd("reloadAccounts()");
        Uri buildFunctionalAccountUri = SoftphoneContract.SoftphoneAccount.buildFunctionalAccountUri();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 1);
        this.mContext.getContentResolver().update(buildFunctionalAccountUri, contentValues, (String) null, (String[]) null);
        Uri buildActiveAccountUri = SoftphoneContract.SoftphoneAccount.buildActiveAccountUri();
        contentValues.clear();
        contentValues.put("status", 0);
        this.mContext.getContentResolver().update(buildActiveAccountUri, contentValues, (String) null, (String[]) null);
    }

    private void clearUnusedAddresses() {
        this.mEventLog.logAndAdd("clearUnusedAddresses()");
        this.mContext.getContentResolver().delete(SoftphoneContract.SoftphoneAddress.CONTENT_URI, "account_id is null OR account_id =?", new String[]{""});
    }

    private class UserSwitchReceiver extends BroadcastReceiver {
        private UserSwitchReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (Extensions.Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                int currentUser = Extensions.ActivityManager.getCurrentUser();
                SimpleEventLog simpleEventLog = SoftphoneServiceStub.this.mEventLog;
                simpleEventLog.logAndAdd("UserSwitchReceiver(): newUserId: " + currentUser);
                SoftphoneServiceStub.this.onUserSwitched(currentUser);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUserSwitched(int i) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onUserSwitched(): newUserId: " + i + ", mCurrentUserId: " + this.mCurrentUserId + ", size: " + this.mClients.size());
        this.mCurrentUserId = i;
        for (int i2 = 0; i2 < this.mClients.size(); i2++) {
            SoftphoneClient valueAt = this.mClients.valueAt(i2);
            if (valueAt.getUserId() != i) {
                valueAt.onUserSwitch();
                valueAt.onUserSwitchedAway();
            } else {
                valueAt.onUserSwitchedBack();
                if (this.mNetworkConnected) {
                    valueAt.onNetworkConnected();
                }
            }
        }
    }

    private void updateAccountStatus(String str, int i, int i2) {
        Uri buildAccountIdUri = SoftphoneContract.SoftphoneAccount.buildAccountIdUri(str, (long) i);
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", Integer.valueOf(i2));
        this.mContext.getContentResolver().update(buildAccountIdUri, contentValues, (String) null, (String[]) null);
    }

    /* access modifiers changed from: private */
    public void validateTokens(int i) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("validateTokens(): newUserId: " + i);
        Cursor query = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildPendingAccountUri((long) i), (String[]) null, (String) null, (String[]) null, (String) null);
        if (query != null) {
            try {
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd("validateTokens found " + query.getCount() + " records");
                if (query.getCount() > 0) {
                    SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(SoftphoneNamespaces.SoftphoneSharedPref.SHARED_PREF_NAME);
                    EncryptionHelper instance = EncryptionHelper.getInstance(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
                    while (query.moveToNext()) {
                        String string = query.getString(query.getColumnIndex("account_id"));
                        updateAccountStatus(string, i, 0);
                        SecretKey secretKey = EncryptionHelper.getSecretKey(query);
                        if (secretKey == null) {
                            SimpleEventLog simpleEventLog3 = this.mEventLog;
                            simpleEventLog3.logAndAdd("Cannot obtain secret key for account: " + string);
                            query.close();
                            query.close();
                            return;
                        }
                        Context context = this.mContext;
                        String str = sharedPrefHelper.get(context, string + ":" + i + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_TOKEN);
                        Context context2 = this.mContext;
                        String str2 = sharedPrefHelper.get(context2, string + ":" + i + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_APPID);
                        Context context3 = this.mContext;
                        String str3 = sharedPrefHelper.get(context3, string + ":" + i + ":" + "environment");
                        SimpleEventLog simpleEventLog4 = this.mEventLog;
                        StringBuilder sb = new StringBuilder();
                        sb.append("encodedTGaurdToken ");
                        sb.append(str);
                        simpleEventLog4.logAndAdd(sb.toString());
                        String decrypt = instance.decrypt(str, secretKey);
                        String decrypt2 = instance.decrypt(str2, secretKey);
                        SimpleEventLog simpleEventLog5 = this.mEventLog;
                        simpleEventLog5.logAndAdd("decodedTGaurdToken: " + decrypt + ", decodedTGaurdAppId: " + decrypt2);
                        if (!(decrypt == null || decrypt2 == null)) {
                            getClient(getClientId(string)).restoreAccessToken(decrypt, string, true, decrypt2, Integer.parseInt(str3));
                        }
                    }
                }
                query.close();
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
            return;
        }
        return;
        throw th;
    }

    public void dump(IndentingPrintWriter indentingPrintWriter) {
        indentingPrintWriter.println("Dump of " + this.LOG_TAG);
        indentingPrintWriter.increaseIndent();
        this.mEventLog.dump(indentingPrintWriter);
        indentingPrintWriter.decreaseIndent();
        for (int i = 0; i < this.mClients.size(); i++) {
            this.mClients.valueAt(i).dump(indentingPrintWriter);
        }
        indentingPrintWriter.close();
    }

    private synchronized SoftphoneClient getClient(int i) {
        SoftphoneClient softphoneClient;
        softphoneClient = this.mClients.get(i);
        if (softphoneClient == null) {
            throw new RuntimeException("client " + i + " cannot be found");
        }
        return softphoneClient;
    }

    public synchronized int getClientId(String str) {
        int hashCode;
        String str2 = str + CmcConstants.E_NUM_SLOT_SPLIT + this.mCurrentUserId;
        hashCode = str2.hashCode();
        if (this.mClients.get(hashCode) == null) {
            HandlerThread handlerThread = new HandlerThread("SoftphoneClient-" + str2);
            handlerThread.start();
            this.mClients.put(hashCode, new SoftphoneClient(str, this.mContext, handlerThread.getLooper()));
            this.mEventLog.logAndAdd("getClientId(): create new client SoftphoneClient-" + str2);
        }
        return hashCode;
    }

    public void registerProgressListener(int i, IProgressListener iProgressListener) {
        getClient(i).registerProgressListener(ISoftphoneService.Stub.getCallingUid(), iProgressListener);
    }

    public void deregisterProgressListener(int i, IProgressListener iProgressListener) {
        getClient(i).deregisterProgressListener(ISoftphoneService.Stub.getCallingUid());
    }

    public void exchangeForAccessToken(int i, String str, String str2, String str3, int i2) {
        getClient(i).exchangeForAccessToken(str, str2, false, str3, i2);
    }

    public void provisionAccount(int i) {
        getClient(i).provisionAccount();
    }

    public void validateE911Address(int i, int i2, boolean z, IEmergencyServiceListener iEmergencyServiceListener) {
        getClient(i).validateE911Address(i2, z, iEmergencyServiceListener);
    }

    public void tryRegister(int i) {
        getClient(i).tryRegister();
    }

    public void tryDeregister(int i) {
        getClient(i).tryDeregister();
    }

    public void logOut(int i) {
        getClient(i).logOut();
    }

    public void registerSupplementaryServiceListener(int i, ISupplementaryServiceListener iSupplementaryServiceListener) {
        getClient(i).registerSupplementaryServiceListener(ISoftphoneService.Stub.getCallingUid(), iSupplementaryServiceListener);
    }

    public void deregisterSupplementaryServiceListener(int i, ISupplementaryServiceListener iSupplementaryServiceListener) {
        getClient(i).deregisterSupplementaryServiceListener(ISoftphoneService.Stub.getCallingUid());
    }

    public void getCallWaitingInfo(int i) {
        getClient(i).getCallWaitingInfo();
    }

    public void getCallForwardingInfo(int i) {
        getClient(i).getCallForwardingInfo();
    }

    public void setCallWaitingInfo(int i, CallWaitingInfo callWaitingInfo) {
        getClient(i).setCallWaitingInfo(callWaitingInfo);
    }

    public void setCallForwardingInfo(int i, CallForwardingInfo callForwardingInfo) {
        getClient(i).setCallForwardingInfo(callForwardingInfo);
    }

    public void getTermsConditions(int i) {
        getClient(i).getTermsAndConditions();
    }

    public List<DeviceInfo> getDeviceList(int i) {
        return getClient(i).getDeviceList();
    }
}
