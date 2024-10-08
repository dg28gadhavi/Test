package com.sec.internal.ims.entitlement.softphone;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.SparseArray;
import com.google.gson.Gson;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.entitilement.softphone.ImsNetworkIdentity;
import com.sec.internal.constants.ims.entitilement.softphone.requests.AddAddressRequest;
import com.sec.internal.constants.ims.entitilement.softphone.requests.AddressValidationRequest;
import com.sec.internal.constants.ims.entitilement.softphone.requests.ExchangeForAccessTokenRequest;
import com.sec.internal.constants.ims.entitilement.softphone.requests.ProvisionAccountRequest;
import com.sec.internal.constants.ims.entitilement.softphone.requests.ReleaseImsNetworkIdentifiersRequest;
import com.sec.internal.constants.ims.entitilement.softphone.requests.RevokeTokenRequest;
import com.sec.internal.constants.ims.entitilement.softphone.requests.SendSMSRequest;
import com.sec.internal.constants.ims.entitilement.softphone.responses.AccessTokenResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.AddAddressResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.AkaAuthenticationResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.CallWaitingResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.ImsNetworkIdentifiersResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.SoftphoneResponse;
import com.sec.internal.constants.ims.entitilement.softphone.responses.TermsAndConditionsResponse;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.entitlement.softphone.SoftphoneAuthUtils;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.util.EncryptionHelper;
import com.sec.internal.ims.entitlement.util.GeolocationUpdateFlow;
import com.sec.internal.ims.entitlement.util.SharedPrefHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IndentingPrintWriter;
import com.sec.vsim.attsoftphone.IEmergencyServiceListener;
import com.sec.vsim.attsoftphone.IProgressListener;
import com.sec.vsim.attsoftphone.ISupplementaryServiceListener;
import com.sec.vsim.attsoftphone.data.CallForwardingInfo;
import com.sec.vsim.attsoftphone.data.CallWaitingInfo;
import com.sec.vsim.attsoftphone.data.DeviceInfo;
import com.sec.vsim.attsoftphone.data.GeneralNotify;
import com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.SecretKey;
import org.json.JSONException;
import org.json.JSONObject;

public class SoftphoneClient {
    private static final int HANDLE_EVENT_ADS_CHANGED = 0;
    public final String LOG_TAG;
    String mAccessToken = null;
    private String mAccessTokenType = null;
    final String mAccountId;
    AlarmManager mAlarmManager;
    private String mAppKey;
    private String mAppSecret;
    private AtomicBoolean mAutoRetry = new AtomicBoolean(false);
    ConnectivityManager mConnectivityManager;
    private final Context mContext;
    EncryptionHelper mEncryptionHelper;
    private int mEnvironment;
    public SimpleEventLog mEventLog;
    private Handler mHandler = null;
    public String mHost;
    protected ImsNetworkIdentity mIdentity = new ImsNetworkIdentity();
    protected final IImsRegistrationListener mImsRegistrationListener = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration imsRegistration) {
            SimpleEventLog simpleEventLog = SoftphoneClient.this.mEventLog;
            simpleEventLog.logAndAdd("onRegistered(): imsprofile id: " + imsRegistration.getImsProfile().getId() + ", mProfileId: " + SoftphoneClient.this.mProfileId);
            int id = imsRegistration.getImsProfile().getId();
            SoftphoneClient softphoneClient = SoftphoneClient.this;
            if (id == softphoneClient.mProfileId) {
                softphoneClient.updateAccountStatus(softphoneClient.mAccountId, 5);
                ImsUri uri = imsRegistration.getPreferredImpu().getUri();
                SoftphoneClient.this.mStateHandler.sendMessage(1016, (Object) uri != null ? uri.getMsisdn() : "");
            }
        }

        public void onDeregistered(ImsRegistration imsRegistration, ImsRegistrationError imsRegistrationError) {
            SimpleEventLog simpleEventLog = SoftphoneClient.this.mEventLog;
            simpleEventLog.logAndAdd("onDeregistered(): imsprofile id: " + imsRegistration.getImsProfile().getId() + ", mProfileId: " + SoftphoneClient.this.mProfileId);
            int id = imsRegistration.getImsProfile().getId();
            SoftphoneClient softphoneClient = SoftphoneClient.this;
            if (id == softphoneClient.mProfileId) {
                softphoneClient.updateAccountStatus(softphoneClient.mAccountId, 4);
                SoftphoneClient.this.mStateHandler.sendMessage(1017, imsRegistrationError.getSipErrorCode(), -1, imsRegistration.getOwnNumber() != null ? imsRegistration.getOwnNumber() : "");
                SoftphoneClient softphoneClient2 = SoftphoneClient.this;
                if (softphoneClient2.mLoggedOut) {
                    softphoneClient2.mProfileId = -1;
                }
            }
        }
    };
    protected AtomicBoolean mIsRecovery = new AtomicBoolean(false);
    private SparseArray<Object> mListeners = new SparseArray<>();
    protected boolean mLoggedOut = true;
    protected int mPhoneId = 0;
    protected int mProfileId = -1;
    ConcurrentHashMap<Integer, IProgressListener> mProgressListeners = new ConcurrentHashMap<>();
    protected PendingIntent mRefreshIdentityIntent = null;
    String mRefreshToken = null;
    PendingIntent mRefreshTokenIntent = null;
    /* access modifiers changed from: private */
    public IRegistrationManager mRegistrationManager = null;
    SoftphoneRequestBuilder mRequestBuilder;
    PendingIntent mResendSmsIntent = null;
    SecretKey mSecretKey;
    protected SharedPrefHelper mSharedPrefHelper;
    SoftphoneEmergencyService mSoftphoneEmergencyServcie;
    protected ContentObserver mSoftphoneLabelObserver;
    protected SoftphoneStateHandler mStateHandler;
    ConcurrentHashMap<Integer, ISupplementaryServiceListener> mSupplementaryServiceListeners = new ConcurrentHashMap<>();
    private String mTGaurdAppId = null;
    private String mTGaurdToken = null;
    TelephonyManager mTelephonyManager;
    private long mTokenExpiresTime = -1;
    private UserHandle mUserHandle = null;
    int mUserId = 0;
    UserManager mUserManager;

    public SoftphoneClient(String str, Context context, Looper looper) {
        this.mContext = context;
        this.mAccountId = str;
        this.mRegistrationManager = ImsRegistry.getRegistrationManager();
        this.mTelephonyManager = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mUserManager = (UserManager) context.getSystemService("user");
        int currentUser = Extensions.ActivityManager.getCurrentUser();
        this.mUserId = currentUser;
        UserHandle userForSerialNumber = this.mUserManager.getUserForSerialNumber((long) currentUser);
        this.mUserHandle = userForSerialNumber;
        if (userForSerialNumber == null) {
            this.mUserHandle = ContextExt.CURRENT_OR_SELF;
        }
        String str2 = SoftphoneClient.class.getSimpleName() + '-' + str + '-' + this.mUserId;
        this.LOG_TAG = str2;
        this.mEventLog = new SimpleEventLog(context, str2, 200);
        this.mSharedPrefHelper = new SharedPrefHelper(SoftphoneNamespaces.SoftphoneSharedPref.SHARED_PREF_NAME);
        this.mEncryptionHelper = EncryptionHelper.getInstance(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
        try {
            this.mSecretKey = EncryptionHelper.generateKey(SoftphoneNamespaces.SoftphoneSettings.ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            IMSLog.s(this.LOG_TAG, "exception" + e.getMessage());
        }
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        this.mPhoneId = activeDataPhoneId;
        this.mRegistrationManager.registerListener(this.mImsRegistrationListener, activeDataPhoneId);
        AnonymousClass1 r2 = new Handler(looper) {
            public void handleMessage(Message message) {
                super.handleMessage(message);
                if (message.what == 0 && SoftphoneClient.this.mPhoneId != SimUtil.getActiveDataPhoneId()) {
                    SoftphoneClient.this.handleDeRegisterRequest();
                    IRegistrationManager r3 = SoftphoneClient.this.mRegistrationManager;
                    SoftphoneClient softphoneClient = SoftphoneClient.this;
                    r3.unregisterListener(softphoneClient.mImsRegistrationListener, softphoneClient.mPhoneId);
                    SoftphoneClient.this.mPhoneId = SimUtil.getActiveDataPhoneId();
                    IRegistrationManager r32 = SoftphoneClient.this.mRegistrationManager;
                    SoftphoneClient softphoneClient2 = SoftphoneClient.this;
                    r32.registerListener(softphoneClient2.mImsRegistrationListener, softphoneClient2.mPhoneId);
                    SoftphoneClient.this.handleTryRegisterRequest();
                }
            }
        };
        this.mHandler = r2;
        SimManagerFactory.registerForADSChange(r2, 0, (Object) null);
        this.mStateHandler = new SoftphoneStateHandler(looper, context, str, this);
        this.mSoftphoneEmergencyServcie = new SoftphoneEmergencyService(context);
        this.mRequestBuilder = new SoftphoneRequestBuilder(context);
        this.mSoftphoneLabelObserver = new ContentObserver(new Handler(looper)) {
            public void onChange(boolean z) {
                super.onChange(z);
            }

            public void onChange(boolean z, Uri uri) {
                super.onChange(z);
                SoftphoneClient softphoneClient = SoftphoneClient.this;
                Uri buildAccountLabelUri = SoftphoneContract.SoftphoneAccount.buildAccountLabelUri(softphoneClient.mAccountId, (long) softphoneClient.mUserId);
                SimpleEventLog simpleEventLog = SoftphoneClient.this.mEventLog;
                simpleEventLog.logAndAdd("mSoftphoneLabelObserver onChange: " + uri);
                if (buildAccountLabelUri.equals(uri)) {
                    SoftphoneClient.this.mStateHandler.sendMessage(1019);
                }
            }
        };
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        this.mEventLog.logAndAdd("finalize()");
        super.finalize();
    }

    public String getAccessToken() {
        return this.mAccessToken;
    }

    public String getAccessTokenType() {
        return this.mAccessTokenType;
    }

    public int getProfileId() {
        return this.mProfileId;
    }

    public boolean getAutoRetryComSet(boolean z, boolean z2) {
        return this.mAutoRetry.compareAndSet(z, z2);
    }

    public void onUserSwitchedAway() {
        this.mEventLog.logAndAdd("onUserSwitchedAway()");
        this.mRegistrationManager.unregisterListener(this.mImsRegistrationListener, this.mPhoneId);
    }

    public void onUserSwitchedBack() {
        this.mEventLog.logAndAdd("onUserSwitchedBack()");
        this.mRegistrationManager.registerListener(this.mImsRegistrationListener, this.mPhoneId);
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_USER_SWITCH_BACK);
    }

    public void updateAccountStatus(String str, int i) {
        Uri uri;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("updateAccountStatus(): " + i);
        ContentValues contentValues = new ContentValues();
        if (i == 0) {
            uri = SoftphoneContract.SoftphoneAccount.buildDeActivateAccountUri(str);
        } else if (i == 2) {
            uri = SoftphoneContract.SoftphoneAccount.buildActivateAccountUri(str);
        } else if (i != 5) {
            Uri buildAccountIdUri = SoftphoneContract.SoftphoneAccount.buildAccountIdUri(str);
            contentValues.put("status", Integer.valueOf(i));
            this.mContext.getContentResolver().update(buildAccountIdUri, contentValues, "status > ?", new String[]{String.valueOf(0)});
            return;
        } else {
            uri = SoftphoneContract.SoftphoneAccount.buildRegisteredAccountUri(str);
        }
        if (uri != null) {
            if (this.mContext.getContentResolver().update(uri, contentValues, "userid = ?", new String[]{String.valueOf(this.mUserId)}) == 0) {
                contentValues.put(SoftphoneContract.AccountColumns.USERID, Integer.valueOf(this.mUserId));
                this.mContext.getContentResolver().insert(uri, contentValues);
            }
        }
    }

    private void updateAccountInfo(String str, String str2) {
        Uri buildAccountIdUri = SoftphoneContract.SoftphoneAccount.buildAccountIdUri(this.mAccountId, (long) this.mUserId);
        ContentValues contentValues = new ContentValues();
        contentValues.put("impi", str);
        contentValues.put("msisdn", str2.substring(str2.indexOf(":") + 1, str2.indexOf("@")));
        contentValues.put(SoftphoneContract.AccountColumns.SECRET_KEY, Base64.encodeToString(this.mSecretKey.getEncoded(), 2));
        contentValues.put("environment", Integer.valueOf(this.mEnvironment));
        this.mContext.getContentResolver().update(buildAccountIdUri, contentValues, (String) null, (String[]) null);
    }

    private void saveAccountIdentities(String str, String str2, String str3) {
        HashMap hashMap = new HashMap();
        hashMap.put(this.mAccountId + ":" + this.mUserId + ":" + "impi", this.mEncryptionHelper.encrypt(str, this.mSecretKey));
        hashMap.put(this.mAccountId + ":" + this.mUserId + ":" + "impu", this.mEncryptionHelper.encrypt(str2, this.mSecretKey));
        hashMap.put(this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_FQDN, this.mEncryptionHelper.encrypt(str3, this.mSecretKey));
        this.mSharedPrefHelper.save(this.mContext, hashMap);
    }

    private int storeTokens(String str, String str2) {
        Uri buildAccountIdUri = SoftphoneContract.SoftphoneAccount.buildAccountIdUri(this.mAccountId, (long) this.mUserId);
        ContentValues contentValues = new ContentValues();
        contentValues.put("access_token", str);
        contentValues.put(SoftphoneContract.AccountColumns.TOKEN_TYPE, str2);
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        sharedPrefHelper.save(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token", this.mEncryptionHelper.encrypt(this.mRefreshToken, this.mSecretKey));
        return this.mContext.getContentResolver().update(buildAccountIdUri, contentValues, (String) null, (String[]) null);
    }

    /* access modifiers changed from: package-private */
    public void saveTokens(String str, String str2, long j, String str3) {
        this.mAccessToken = str;
        this.mAccessTokenType = str2;
        this.mTokenExpiresTime = j;
        this.mRefreshToken = str3;
        storeTokens(this.mEncryptionHelper.encrypt(str, this.mSecretKey), this.mEncryptionHelper.encrypt(this.mAccessTokenType, this.mSecretKey));
    }

    /* access modifiers changed from: package-private */
    public void scheduleRefreshTokenAlarm(long j, int i) {
        Intent intent = new Intent();
        intent.setAction("refresh_token");
        intent.putExtra(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, i);
        this.mRefreshTokenIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 201326592);
        this.mAlarmManager.setAndAllowWhileIdle(0, System.currentTimeMillis() + j, this.mRefreshTokenIntent);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("refresh token after " + j + ", attempt: " + i);
    }

    public synchronized void scheduleSmsAlarm() {
        Calendar instance = Calendar.getInstance();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("current time: " + instance.get(2) + CmcConstants.E_NUM_SLOT_SPLIT + instance.get(5) + CmcConstants.E_NUM_SLOT_SPLIT + instance.get(1) + " " + instance.get(10) + ":" + instance.get(12) + ":" + instance.get(13));
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        StringBuilder sb = new StringBuilder();
        sb.append(this.mAccountId);
        sb.append(":");
        sb.append(this.mUserId);
        sb.append(":");
        sb.append(SoftphoneNamespaces.SoftphoneSharedPref.LAST_SMS_TIME);
        sharedPrefHelper.save(context, sb.toString(), instance.getTimeInMillis());
        instance.add(5, 30);
        Intent intent = new Intent();
        intent.setPackage(this.mContext.getPackageName());
        intent.setAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_RESEND_SMS);
        this.mResendSmsIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        this.mAlarmManager.setAndAllowWhileIdle(0, instance.getTimeInMillis(), this.mResendSmsIntent);
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("schedule to send SMS at: " + instance.get(2) + CmcConstants.E_NUM_SLOT_SPLIT + instance.get(5) + CmcConstants.E_NUM_SLOT_SPLIT + instance.get(1) + " " + instance.get(10) + ":" + instance.get(12) + ":" + instance.get(13));
    }

    /* access modifiers changed from: package-private */
    public void resumeSmsAlarm(long j) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("last sms time: " + instance.get(2) + CmcConstants.E_NUM_SLOT_SPLIT + instance.get(5) + CmcConstants.E_NUM_SLOT_SPLIT + instance.get(1) + " " + instance.get(10) + ":" + instance.get(12) + ":" + instance.get(13));
        instance.add(5, 30);
        Intent intent = new Intent();
        intent.setPackage(this.mContext.getPackageName());
        intent.setAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_RESEND_SMS);
        this.mResendSmsIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        this.mAlarmManager.setAndAllowWhileIdle(0, instance.getTimeInMillis(), this.mResendSmsIntent);
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("schedule to send SMS at: " + instance.get(2) + CmcConstants.E_NUM_SLOT_SPLIT + instance.get(5) + CmcConstants.E_NUM_SLOT_SPLIT + instance.get(1) + " " + instance.get(10) + ":" + instance.get(12) + ":" + instance.get(13));
    }

    private void scheduleRefreshIdentityAlarm(long j) {
        Intent intent = new Intent();
        intent.setPackage(this.mContext.getPackageName());
        intent.setAction(SoftphoneNamespaces.SoftphoneAlarm.ACTION_REFRESH_IDENTITY);
        this.mRefreshIdentityIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
        this.mAlarmManager.setAndAllowWhileIdle(0, System.currentTimeMillis() + j, this.mRefreshIdentityIntent);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("refresh identity after " + j);
    }

    private void saveListener(int i, Object obj) {
        this.mListeners.append(i, obj);
    }

    private Object findAndRemoveListener(int i) {
        Object obj = this.mListeners.get(i);
        this.mListeners.delete(i);
        return obj;
    }

    private void setupEnvironment(int i) {
        this.mEnvironment = i;
        String str = Build.MODEL;
        this.mAppKey = SoftphoneAuthUtils.setupAppKey(i, str);
        this.mAppSecret = SoftphoneAuthUtils.setupAppSecret(i, str);
        this.mHost = SoftphoneNamespaces.SoftphoneSettings.PROD_HOST;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setupEnvironment(): appKey: " + IMSLog.checker(this.mAppKey) + ", appSecret: " + IMSLog.checker(this.mAppSecret));
    }

    private void removeSharedPreferences() {
        this.mEventLog.logAndAdd("removeSharedPreferences()");
        this.mSharedPrefHelper.remove(this.mContext, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_TOKEN, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_APPID, this.mAccountId + ":" + this.mUserId + ":" + "environment", this.mAccountId + ":" + this.mUserId + ":" + "impi", this.mAccountId + ":" + this.mUserId + ":" + "impu", this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_FQDN, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token", this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_PD_COOKIES, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.LAST_SMS_TIME);
    }

    public boolean isTarget(String str) {
        boolean z = !this.mIdentity.impiEmpty() && this.mIdentity.getImpi().equalsIgnoreCase(str);
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "isTarget(): impi: " + str + ", " + z);
        return z;
    }

    /* access modifiers changed from: package-private */
    public void resetAccountStatus() {
        updateAccountStatus(this.mAccountId, 0);
        broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_REQUEST_LOGOUT, (String) null);
        PendingIntent pendingIntent = this.mRefreshTokenIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mRefreshTokenIntent = null;
        }
        PendingIntent pendingIntent2 = this.mResendSmsIntent;
        if (pendingIntent2 != null) {
            this.mAlarmManager.cancel(pendingIntent2);
            this.mResendSmsIntent = null;
        }
    }

    public void registerProgressListener(int i, IProgressListener iProgressListener) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("registerProgressListener current size: " + this.mProgressListeners.size() + " UID: " + i + " listener: " + iProgressListener);
        this.mProgressListeners.put(Integer.valueOf(i), iProgressListener);
    }

    public void deregisterProgressListener(int i) {
        this.mProgressListeners.remove(Integer.valueOf(i));
    }

    public void registerSupplementaryServiceListener(int i, ISupplementaryServiceListener iSupplementaryServiceListener) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("SupplementaryServiceListener current size: " + this.mSupplementaryServiceListeners.size() + " UID: " + i + " listener: " + iSupplementaryServiceListener);
        this.mSupplementaryServiceListeners.put(Integer.valueOf(i), iSupplementaryServiceListener);
    }

    public void deregisterSupplementaryServiceListener(int i) {
        this.mSupplementaryServiceListeners.remove(Integer.valueOf(i));
    }

    /* access modifiers changed from: package-private */
    public void restoreAccessToken(String str, String str2, boolean z, String str3, int i) {
        if (tokenExist() && this.mIsRecovery.compareAndSet(false, true)) {
            this.mEventLog.logAndAdd("restoreAccessToken(): Softphone Service is recovering");
            setupEnvironment(i);
            updateAccountStatus(this.mAccountId, 2);
            getAccountSecretKey();
            refreshTokenAfterRecovery();
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_REDISTATE);
        } else if (!this.mIsRecovery.get()) {
            exchangeForAccessToken(str, str2, z, str3, i, 0, SoftphoneNamespaces.mTimeoutType4[0]);
        }
    }

    public void exchangeForAccessToken(String str, String str2, boolean z, String str3, int i) {
        exchangeForAccessToken(str, str2, z, str3, i, 0, SoftphoneNamespaces.mTimeoutType4[0]);
    }

    public void startInitstate() {
        if (this.mAutoRetry.getAndSet(false)) {
            exchangeForAccessToken(this.mTGaurdToken, this.mAccountId, true, this.mTGaurdAppId, this.mEnvironment);
        }
    }

    /* access modifiers changed from: package-private */
    public void exchangeForAccessToken(String str, String str2, boolean z, String str3, int i, int i2, long j) {
        int currentUser = Extensions.ActivityManager.getCurrentUser();
        this.mUserId = currentUser;
        UserHandle userForSerialNumber = this.mUserManager.getUserForSerialNumber((long) currentUser);
        this.mUserHandle = userForSerialNumber;
        if (userForSerialNumber == null) {
            this.mUserHandle = ContextExt.CURRENT_OR_SELF;
        }
        String str4 = this.LOG_TAG;
        IMSLog.s(str4, "exchangeForAccessToken request: authCode: " + str + ", tGuardAppId: " + str3);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("exchangeForAccessToken request: accountId: " + str2 + ", autoRegister: " + z + ", environment: " + i + ", retryCount: " + i2 + ", timeout: " + j + ", mUserId: " + this.mUserId);
        if (str == null) {
            this.mEventLog.logAndAdd("authorizationCode is null");
            return;
        }
        this.mTGaurdToken = str;
        this.mTGaurdAppId = str3;
        HashMap hashMap = new HashMap();
        hashMap.put(this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_TOKEN, this.mEncryptionHelper.encrypt(str, this.mSecretKey));
        hashMap.put(this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_TGUARD_APPID, this.mEncryptionHelper.encrypt(str3, this.mSecretKey));
        hashMap.put(this.mAccountId + ":" + this.mUserId + ":" + "environment", Integer.toString(i));
        this.mSharedPrefHelper.save(this.mContext, hashMap);
        setupEnvironment(i);
        ExchangeForAccessTokenRequest buildExchangeForAccessTokenRequest = SoftphoneRequestBuilder.buildExchangeForAccessTokenRequest(this.mAppKey, this.mAppSecret, str2, str);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.TOKEN_PATH);
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            softphoneHttpTransaction.setJsonBody(new JSONObject(new Gson().toJson(buildExchangeForAccessTokenRequest)));
        } catch (JSONException e) {
            String str5 = this.LOG_TAG;
            IMSLog.s(str5, "could not build JSONObject:" + e.getMessage());
        }
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.POST);
        softphoneHttpTransaction.setTimeout(j);
        if (i2 > 0) {
            this.mStateHandler.sendMessage(SoftphoneNamespaces.SoftphoneEvents.EVENT_RETRY_OBTAIN_ACCESS_TOKEN, i2, z, softphoneHttpTransaction);
        } else {
            this.mStateHandler.sendMessage(0, i2, z ? 1 : 0, softphoneHttpTransaction);
        }
    }

    public void provisionAccount() {
        provisionAccount(0, SoftphoneNamespaces.mTimeoutType3[0]);
    }

    /* access modifiers changed from: package-private */
    public void provisionAccount(int i, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("provisionAccount(): retryCount: " + i + ", timeout: " + j);
        ProvisionAccountRequest buildProvisionAccountRequest = SoftphoneRequestBuilder.buildProvisionAccountRequest();
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.PROVISION_ACCOUNT_PATH);
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/json");
        try {
            softphoneHttpTransaction.setJsonBody(new JSONObject(new Gson().toJson(buildProvisionAccountRequest)));
        } catch (JSONException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "could not build JSONObject:" + e.getMessage());
        }
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.POST);
        softphoneHttpTransaction.setTimeout(j);
        this.mStateHandler.sendMessage(3, i, -1, softphoneHttpTransaction);
    }

    public void validateE911Address(int i, boolean z, IEmergencyServiceListener iEmergencyServiceListener) {
        validateE911Address(i, z, iEmergencyServiceListener, 0, SoftphoneNamespaces.mTimeoutType2[0]);
    }

    private void validateE911Address(int i, boolean z, IEmergencyServiceListener iEmergencyServiceListener, int i2, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("validateE911Address(): addressId: " + i + ", confirmed: " + z + ", retryCount: " + i2 + ", timeout: " + j);
        AddressValidationRequest buildAddressValidationRequest = this.mRequestBuilder.buildAddressValidationRequest(i, z);
        int httpTransactionId = this.mStateHandler.getHttpTransactionId();
        if (iEmergencyServiceListener != null) {
            saveListener(httpTransactionId, iEmergencyServiceListener);
        }
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.E911ADDRESS_VALIDATION_PATH);
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/json");
        try {
            softphoneHttpTransaction.setJsonBody(new JSONObject(new Gson().toJson(buildAddressValidationRequest)));
        } catch (JSONException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "could not build JSONObject:" + e.getMessage());
        }
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.POST);
        softphoneHttpTransaction.setTimeout(j);
        Message obtainMessage = this.mStateHandler.obtainMessage(6, httpTransactionId, i, softphoneHttpTransaction);
        Bundle bundle = new Bundle();
        bundle.putInt("retry_count", i2);
        bundle.putBoolean(SoftphoneNamespaces.SoftphoneSettings.CONFIRMED, z);
        obtainMessage.setData(bundle);
        this.mStateHandler.sendMessage(obtainMessage);
    }

    public void tryRegister() {
        this.mStateHandler.sendMessage(14);
    }

    public void tryDeregister() {
        updateAccountStatus(this.mAccountId, 3);
        this.mStateHandler.sendMessage(17);
    }

    public void logOut() {
        this.mEventLog.logAndAdd("logOut()");
        this.mStateHandler.sendMessage(17);
        this.mStateHandler.removeMessages(14);
        this.mStateHandler.removeMessages(1);
        this.mStateHandler.removeMessages(4);
        this.mStateHandler.removeMessages(SoftphoneNamespaces.SoftphoneEvents.EVENT_RELOGIN);
        this.mAutoRetry.set(false);
        this.mIsRecovery.set(false);
        resetAccountStatus();
        resetCurrentAddresses();
        removeSharedPreferences();
        deregisterSoftphoneLabelObserver();
        this.mStateHandler.sendMessage(1018);
    }

    public void reLogin(int i, boolean z) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("reLogin(): retryCount: " + i + ", needNewToken: " + z + ", callState: " + this.mTelephonyManager.getCallState());
        if (this.mTelephonyManager.getCallState() != 0) {
            long j = (1 << i) * SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF;
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("reLogin(): backoff: " + j);
            int i2 = i + 1;
            this.mStateHandler.sendMessageDelayed(SoftphoneNamespaces.SoftphoneEvents.EVENT_RELOGIN, i2 > 6 ? 6 : i2, z ? 1 : 0, j);
        } else if (Optional.ofNullable(this.mConnectivityManager.getActiveNetwork()).map(new SoftphoneClient$$ExternalSyntheticLambda1(this)).filter(new SoftphoneClient$$ExternalSyntheticLambda2()).filter(new SoftphoneClient$$ExternalSyntheticLambda3()).isPresent()) {
            this.mStateHandler.removeMessages(14);
            this.mStateHandler.removeMessages(4);
            deregisterSoftphoneLabelObserver();
            if (z) {
                this.mStateHandler.sendMessage(17);
                SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
                Context context = this.mContext;
                sharedPrefHelper.remove(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token");
                this.mAutoRetry.set(true);
                this.mIsRecovery.set(false);
                this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_START_RELOGIN);
                return;
            }
            this.mStateHandler.sendMessage(1029);
        } else {
            this.mEventLog.logAndAdd("reLogin(): network is not connected");
            this.mStateHandler.sendMessage(17);
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_OUT_OF_SERVICE);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ NetworkCapabilities lambda$reLogin$0(Network network) {
        return this.mConnectivityManager.getNetworkCapabilities(network);
    }

    public void onUserSwitch() {
        this.mEventLog.logAndAdd("onUserSwitch()");
        this.mStateHandler.sendMessage(17);
        this.mStateHandler.sendMessage(1025);
    }

    public void getCallWaitingInfo() {
        getCallWaitingInfo(0, SoftphoneNamespaces.mTimeoutType1[0]);
    }

    /* access modifiers changed from: package-private */
    public void getCallWaitingInfo(int i, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getCallWaitingInfo(): retryCount: " + i + ", timeout: " + j);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.CALL_WAITING_PATH);
        softphoneHttpTransaction.addRequestHeader("Accept", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.GET);
        softphoneHttpTransaction.setTimeout(j);
        this.mStateHandler.sendMessage(8, i, -1, softphoneHttpTransaction);
    }

    public void getCallForwardingInfo() {
        getCallForwardingInfo(0, SoftphoneNamespaces.mTimeoutType1[0]);
    }

    private void getCallForwardingInfo(int i, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getCallForwardingInfo():retryCount: " + i + ", timeout: " + j);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.CALL_FORWARDING_PATH);
        softphoneHttpTransaction.addRequestHeader("Accept", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.GET);
        softphoneHttpTransaction.setTimeout(j);
        this.mStateHandler.sendMessage(9, i, -1, softphoneHttpTransaction);
    }

    public void setCallWaitingInfo(CallWaitingInfo callWaitingInfo) {
        setCallWaitingInfo(callWaitingInfo, 0, SoftphoneNamespaces.mTimeoutType2[0]);
    }

    private void setCallWaitingInfo(CallWaitingInfo callWaitingInfo, int i, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setCallWaitingInfo(): retryCount: " + i + ", timeout: " + j);
        if (callWaitingInfo == null) {
            notifySsProgress(new SupplementaryServiceNotify(10, false, "null info"));
            return;
        }
        SoftphoneHttpTransaction callHandlingTxn = getCallHandlingTxn(SoftphoneNamespaces.SoftphoneSettings.CALL_WAITING_PATH, SoftphoneRequestBuilder.buildSetCallWaitingInfoRequest(callWaitingInfo));
        callHandlingTxn.setTimeout(j);
        Message obtainMessage = this.mStateHandler.obtainMessage(10, i, -1, callHandlingTxn);
        Bundle bundle = new Bundle();
        bundle.putParcelable("communication-waiting", callWaitingInfo);
        obtainMessage.setData(bundle);
        this.mStateHandler.sendMessage(obtainMessage);
    }

    public void setCallForwardingInfo(CallForwardingInfo callForwardingInfo) {
        setCallForwardingInfo(callForwardingInfo, 0, SoftphoneNamespaces.mTimeoutType2[0]);
    }

    private void setCallForwardingInfo(CallForwardingInfo callForwardingInfo, int i, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setCallForwardingInfo() retryCount: " + i + ", timeout: " + j);
        if (callForwardingInfo == null) {
            notifySsProgress(new SupplementaryServiceNotify(11, false, "null info"));
            return;
        }
        SoftphoneHttpTransaction callHandlingTxn = getCallHandlingTxn(SoftphoneNamespaces.SoftphoneSettings.CALL_FORWARDING_PATH, SoftphoneRequestBuilder.buildSetCallForwardingInfoRequest(callForwardingInfo));
        callHandlingTxn.setTimeout(j);
        Message obtainMessage = this.mStateHandler.obtainMessage(11, i, -1, callHandlingTxn);
        Bundle bundle = new Bundle();
        bundle.putParcelable("communication-diversion", callForwardingInfo);
        obtainMessage.setData(bundle);
        this.mStateHandler.sendMessage(obtainMessage);
    }

    private SoftphoneHttpTransaction getCallHandlingTxn(String str, String str2) {
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(str);
        softphoneHttpTransaction.addRequestHeader("Content-Type", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        softphoneHttpTransaction.addRequestHeader("Accept", HttpController.CONTENT_TYPE_XCAP_EL_XML);
        softphoneHttpTransaction.setStringBody(str2);
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.PUT);
        return softphoneHttpTransaction;
    }

    public List<DeviceInfo> getDeviceList() {
        String str;
        ArrayList arrayList = new ArrayList();
        ImsRegistration[] registrationInfoByPhoneId = this.mRegistrationManager.getRegistrationInfoByPhoneId(this.mPhoneId);
        if (registrationInfoByPhoneId != null && registrationInfoByPhoneId.length > 0) {
            List<NameAddr> deviceList = registrationInfoByPhoneId[0].getDeviceList();
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("reg.deviceList: " + deviceList);
            for (NameAddr nameAddr : deviceList) {
                String displayName = nameAddr.getDisplayName();
                ImsUri uri = nameAddr.getUri();
                if (uri == null || (str = uri.getParam("gr")) == null) {
                    str = "";
                }
                if (!displayName.isEmpty() || !str.isEmpty()) {
                    arrayList.add(new DeviceInfo(displayName, str));
                } else {
                    arrayList.add(new DeviceInfo("D;" + this.mAppKey + ";Smartphone", ""));
                }
            }
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("getDeviceList(): " + arrayList);
        return arrayList;
    }

    public int getUserId() {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getUserId(): " + this.mUserId);
        return this.mUserId;
    }

    public void onAirplaneModeOn() {
        this.mEventLog.logAndAdd("onAirplaneModeOn()");
        this.mStateHandler.sendMessage(17);
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_AIRPLANE_MODE_ON);
    }

    public void onNetworkConnected() {
        this.mEventLog.logAndAdd("onNetworkConnected()");
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_NETWORK_CONNECTED);
    }

    public void dump(IndentingPrintWriter indentingPrintWriter) {
        indentingPrintWriter.println("Dump of " + this.LOG_TAG);
        indentingPrintWriter.increaseIndent();
        this.mEventLog.dump(indentingPrintWriter);
        indentingPrintWriter.decreaseIndent();
    }

    public void notifyProgress(GeneralNotify generalNotify) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("listener size: " + this.mProgressListeners.size());
        Enumeration<IProgressListener> elements = this.mProgressListeners.elements();
        while (elements.hasMoreElements()) {
            try {
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd("Notify: " + generalNotify.mRequestId);
                elements.nextElement().onNotify(generalNotify);
            } catch (RemoteException e) {
                String str = this.LOG_TAG;
                IMSLog.s(str, "exception" + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifySsProgress(SupplementaryServiceNotify supplementaryServiceNotify) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("listener size: " + this.mSupplementaryServiceListeners.size());
        Enumeration<ISupplementaryServiceListener> elements = this.mSupplementaryServiceListeners.elements();
        while (elements.hasMoreElements()) {
            try {
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd("Notify: " + supplementaryServiceNotify.mRequestId);
                elements.nextElement().onNotify(supplementaryServiceNotify);
            } catch (RemoteException e) {
                String str = this.LOG_TAG;
                IMSLog.s(str, "exception" + e.getMessage());
            }
        }
    }

    public void notifyRegisterStatus(boolean z, String str) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("notifyRegisterStatus(): registered: " + z + ", reason: " + str);
        notifyProgress(new GeneralNotify(14, z, str));
    }

    public void getImsNetworkIdentifiers(boolean z, boolean z2, int i, long j, int i2) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getImsNetworkIdentifiers(): justProvisioned: " + z + ", autoRegister: " + z2 + ", retryCount: " + i + ", timeout: " + j + ", attempt: " + i2);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.OBTAIN_IDENTIFIERS_PATH);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mAccessTokenType);
        sb.append(" ");
        sb.append(this.mAccessToken);
        softphoneHttpTransaction.addRequestHeader("Authorization", sb.toString());
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.GET);
        softphoneHttpTransaction.setTimeout(j);
        if (z) {
            Message obtainMessage = this.mStateHandler.obtainMessage(104, i, z2, (Object) null);
            Bundle bundle = new Bundle();
            bundle.putInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, i2);
            obtainMessage.setData(bundle);
            softphoneHttpTransaction.commit(obtainMessage);
            return;
        }
        Message obtainMessage2 = this.mStateHandler.obtainMessage(1, i, z2 ? 1 : 0, softphoneHttpTransaction);
        Bundle bundle2 = new Bundle();
        bundle2.putInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, i2);
        obtainMessage2.setData(bundle2);
        if (i2 > 0) {
            long j2 = (1 << i2) * SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF;
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("SoftphoneEvents(): backoff: " + j2);
            this.mStateHandler.sendMessageDelayed(obtainMessage2, j2);
            return;
        }
        this.mStateHandler.sendMessage(obtainMessage2);
    }

    public void broadcastIntent(String str, String str2) {
        Intent intent = new Intent(str);
        intent.putExtra("account_id", this.mAccountId);
        if (str2 != null) {
            intent.putExtra("msisdn", str2);
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("broadcastIntent: " + intent);
        String str3 = this.LOG_TAG;
        IMSLog.s(str3, "broadcastIntent: extras: " + intent.getExtras());
        IntentUtil.sendBroadcast(this.mContext, intent, this.mUserHandle);
    }

    public void broadcastExplicitIntent(String str, String str2) {
        ArrayList arrayList = new ArrayList();
        arrayList.add("com.android.phone");
        arrayList.add("com.sec.android.softphone");
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String str3 = (String) it.next();
            Intent intent = new Intent(str);
            intent.putExtra("account_id", this.mAccountId);
            if (str2 != null) {
                intent.putExtra("msisdn", str2);
            }
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("broadcastIntent: " + intent);
            String str4 = this.LOG_TAG;
            IMSLog.s(str4, "broadcastIntent: extras: " + intent.getExtras());
            intent.setPackage(str3);
            IntentUtil.sendBroadcast(this.mContext, intent, this.mUserHandle);
        }
    }

    public void processImsNetworkIdentifiersResponse(ImsNetworkIdentifiersResponse imsNetworkIdentifiersResponse, boolean z, int i, boolean z2, int i2) {
        int i3;
        ImsNetworkIdentifiersResponse imsNetworkIdentifiersResponse2 = imsNetworkIdentifiersResponse;
        boolean z3 = z;
        int i4 = i;
        boolean z4 = z2;
        int i5 = i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processImsNetworkIdentifiersResponse(): success: " + imsNetworkIdentifiersResponse2.mSuccess + ", justProvisioned: " + z + ", autoRegister: " + z4 + ", retryCount: " + i + ", attempt: " + i5);
        if (imsNetworkIdentifiersResponse2.mSuccess && isImsNetworkIdentifiersResponseValid(imsNetworkIdentifiersResponse)) {
            this.mSoftphoneEmergencyServcie.compareAndSaveE911Address(imsNetworkIdentifiersResponse2.mIdentitiesResponse.mLocations, this.mAccountId);
            registerSoftphoneLabelObserver(this.mAccountId);
            ImsNetworkIdentifiersResponse.IdentitiesResponse.SubscriberIdentities subscriberIdentities = imsNetworkIdentifiersResponse2.mIdentitiesResponse.mSubscriberIdentities;
            this.mIdentity = new ImsNetworkIdentity(subscriberIdentities.mPrivateUserId, subscriberIdentities.mPublicUserId, new ArrayList(Arrays.asList(new String[]{imsNetworkIdentifiersResponse2.mIdentitiesResponse.mSubscriberIdentities.mFQDN})), this.mAppKey);
            scheduleRefreshIdentityAlarm(10800000);
            ImsNetworkIdentifiersResponse.IdentitiesResponse.SubscriberIdentities subscriberIdentities2 = imsNetworkIdentifiersResponse2.mIdentitiesResponse.mSubscriberIdentities;
            updateAccountInfo(subscriberIdentities2.mPrivateUserId, subscriberIdentities2.mPublicUserId);
            ImsNetworkIdentifiersResponse.IdentitiesResponse.SubscriberIdentities subscriberIdentities3 = imsNetworkIdentifiersResponse2.mIdentitiesResponse.mSubscriberIdentities;
            saveAccountIdentities(subscriberIdentities3.mPrivateUserId, subscriberIdentities3.mPublicUserId, subscriberIdentities3.mFQDN);
            String str = imsNetworkIdentifiersResponse2.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId;
            broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_LOGIN_COMPLETED, str.substring(str.indexOf(":") + 1, imsNetworkIdentifiersResponse2.mIdentitiesResponse.mSubscriberIdentities.mPublicUserId.indexOf("@")));
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_ACTIVATEDSTATE);
            if (z4) {
                checkAutoRegistrationCondition();
            }
            this.mIsRecovery.set(false);
        } else if (imsNetworkIdentifiersResponse2.mStatusCode == -1 && (i3 = i4 + 1) < 3) {
            getImsNetworkIdentifiers(z, z2, i3, SoftphoneNamespaces.mTimeoutType1[i3], i2);
            return;
        } else if (z3) {
            int i6 = i5 + 1;
            if (i6 < 3) {
                this.mStateHandler.sendMessageDelayed(4, i6, 45000);
                return;
            } else {
                imsNetworkIdentifiersResponse2.mReason = "Please try again later or call AT&T Customer Care.";
                this.mEventLog.logAndAdd("processImsNetworkIdentifiersResponse(): notify getImsNetworkIdentity failure after 3 attempts");
            }
        } else {
            String str2 = imsNetworkIdentifiersResponse2.mReason;
            if (str2 != null && str2.contains("LDAP Record not found")) {
                getTermsAndConditions();
                return;
            } else if (z4) {
                if (imsNetworkIdentifiersResponse2.mStatusCode == 401) {
                    SimpleEventLog simpleEventLog2 = this.mEventLog;
                    simpleEventLog2.logAndAdd("processImsNetworkIdentifiersResponse(): statusCode: " + imsNetworkIdentifiersResponse2.mStatusCode + ", invalid access token, reLogin");
                    reLogin(0, true);
                    return;
                }
                int i7 = i5 + 1;
                if (i7 < 6) {
                    getImsNetworkIdentifiers(false, z2, 0, SoftphoneNamespaces.mTimeoutType1[0], i7);
                    return;
                }
                logOut();
            }
        }
        notifyProgress(new GeneralNotify(4, imsNetworkIdentifiersResponse2.mSuccess, imsNetworkIdentifiersResponse2.mReason));
    }

    /* access modifiers changed from: package-private */
    public boolean isImsNetworkIdentifiersResponseValid(ImsNetworkIdentifiersResponse imsNetworkIdentifiersResponse) {
        ImsNetworkIdentifiersResponse.IdentitiesResponse.SubscriberIdentities subscriberIdentities;
        ImsNetworkIdentifiersResponse.IdentitiesResponse identitiesResponse = imsNetworkIdentifiersResponse.mIdentitiesResponse;
        if (identitiesResponse != null && (subscriberIdentities = identitiesResponse.mSubscriberIdentities) != null && subscriberIdentities.mPrivateUserId != null && subscriberIdentities.mPublicUserId != null && subscriberIdentities.mFQDN != null && identitiesResponse.mLocations != null) {
            return true;
        }
        imsNetworkIdentifiersResponse.mSuccess = false;
        imsNetworkIdentifiersResponse.mReason = "Cannot retrieve account info. Please call AT&T Customer Care.";
        notifyProgress(new GeneralNotify(4, false, imsNetworkIdentifiersResponse.mReason));
        return false;
    }

    /* access modifiers changed from: package-private */
    public void checkAutoRegistrationCondition() {
        if (!tryRegisterWithDefaultAddress()) {
            new GeolocationUpdateFlow(this.mContext).requestGeolocationUpdate(new SoftphoneClient$$ExternalSyntheticLambda0(this));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$checkAutoRegistrationCondition$3(Address address) {
        if (address != null) {
            String countryCode = address.getCountryCode();
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("onAddressObtained(): " + countryCode);
            if ("US".equalsIgnoreCase(countryCode) || "VI".equalsIgnoreCase(countryCode) || "PR".equalsIgnoreCase(countryCode)) {
                broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_MISSING_E911, (String) null);
                return;
            }
            tryRegister();
            broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_IN_INTERNATIONAL, (String) null);
            return;
        }
        this.mEventLog.logAndAdd("onAddressObtained(): cannot determine location");
        broadcastIntent(SoftphoneNamespaces.Intent.Action.ACCOUNT_LOCATION_UNKNOWN, (String) null);
    }

    /* access modifiers changed from: package-private */
    public boolean tryRegisterWithDefaultAddress() {
        if (getCurrentAddress() != -1) {
            tryRegister();
            return true;
        }
        long defaultAddress = getDefaultAddress();
        if (defaultAddress == -1) {
            return false;
        }
        setAddressCurrent(defaultAddress);
        tryRegister();
        return true;
    }

    /* access modifiers changed from: package-private */
    public long getDefaultAddress() {
        Cursor query = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAddress.buildGetDefaultAddressUri(this.mAccountId), (String[]) null, (String) null, (String[]) null, (String) null);
        long j = -1;
        if (query != null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("found " + query.getCount() + " default addresses");
            if (query.moveToFirst()) {
                j = query.getLong(query.getColumnIndex("_id"));
            }
            query.close();
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("getDefaultAddress(): id = " + j);
        return j;
    }

    private long getCurrentAddress() {
        Cursor query = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAddress.buildGetCurrentAddressUri(this.mAccountId), (String[]) null, (String) null, (String[]) null, (String) null);
        long j = -1;
        if (query != null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("found " + query.getCount() + " current addresses");
            if (query.moveToFirst()) {
                j = query.getLong(query.getColumnIndex("_id"));
            }
            query.close();
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("getCurrentAddress(): id = " + j);
        return j;
    }

    private void setAddressCurrent(long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setAddressCurrent(): id = " + j);
        this.mContext.getContentResolver().update(SoftphoneContract.SoftphoneAddress.buildSetCurrentAddressUri(this.mAccountId, j), new ContentValues(), (String) null, (String[]) null);
    }

    public void getTermsAndConditions() {
        getTermsAndConditions(0, SoftphoneNamespaces.mTimeoutType1[0]);
    }

    /* access modifiers changed from: package-private */
    public void getTermsAndConditions(int i, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("getTermsAndConditions(): retryCount:" + i + ", timeout: " + j);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.TERMS_AND_CONDITIONS_PATH);
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.GET);
        softphoneHttpTransaction.setTimeout(j);
        this.mStateHandler.sendMessage(2, i, -1, softphoneHttpTransaction);
    }

    public void processTermsAndConditionsResponse(TermsAndConditionsResponse termsAndConditionsResponse, int i) {
        int i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processTermsAndConditionsResponse(): success: " + termsAndConditionsResponse.mSuccess + ", retryCount: " + i);
        if (termsAndConditionsResponse.mSuccess) {
            termsAndConditionsResponse.mReason = termsAndConditionsResponse.mTCResponse.mUrl;
        } else if (termsAndConditionsResponse.mStatusCode == -1 && (i2 = i + 1) < 3) {
            getTermsAndConditions(i2, SoftphoneNamespaces.mTimeoutType1[i2]);
            return;
        }
        notifyProgress(new GeneralNotify(2, termsAndConditionsResponse.mSuccess, termsAndConditionsResponse.mReason));
    }

    public void processProvisionAccountResponse(SoftphoneResponse softphoneResponse, int i) {
        int i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processProvisionAccountResponse(): success: " + softphoneResponse.mSuccess + ", retryCount: " + i);
        if (softphoneResponse.mSuccess) {
            this.mStateHandler.sendMessageDelayed(4, 0, 45000);
        } else if (softphoneResponse.mStatusCode == -1 && (i2 = i + 1) < 3) {
            provisionAccount(i2, SoftphoneNamespaces.mTimeoutType3[i2]);
            return;
        }
        notifyProgress(new GeneralNotify(3, softphoneResponse.mSuccess, softphoneResponse.mReason));
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0080 A[SYNTHETIC, Splitter:B:15:0x0080] */
    /* JADX WARNING: Removed duplicated region for block: B:20:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void processValidateE911AddressResponse(com.sec.internal.constants.ims.entitilement.softphone.responses.AddressValidationResponse r12, int r13) {
        /*
            r11 = this;
            com.sec.internal.helper.SimpleEventLog r0 = r11.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "processAddressValidationResponse(): success: "
            r1.append(r2)
            boolean r2 = r12.mSuccess
            r1.append(r2)
            java.lang.String r2 = ", retryCount: "
            r1.append(r2)
            r1.append(r13)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            boolean r0 = r12.mSuccess
            r1 = 0
            if (r0 == 0) goto L_0x003b
            int r13 = r12.mAddressId
            com.sec.internal.constants.ims.entitilement.softphone.responses.AddressValidationResponse$E911Locations r0 = r12.mE911Locations
            java.lang.String r2 = r0.mAddressIdentifier
            java.lang.String r0 = r0.mExpirationDate
            r11.updateE911AddressLocally(r13, r2, r0)
            int r13 = r12.mAddressId
            long[] r0 = com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces.mTimeoutType1
            r2 = r0[r1]
            r11.addE911Address(r13, r1, r2)
            goto L_0x0068
        L_0x003b:
            int r0 = r12.mStatusCode
            r2 = -1
            r3 = 1
            if (r0 != r2) goto L_0x005c
            int r8 = r13 + 1
            r13 = 3
            if (r8 >= r13) goto L_0x005c
            long[] r13 = com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces.mTimeoutType2
            r9 = r13[r8]
            int r13 = r12.mTransactionId
            java.lang.Object r13 = r11.findAndRemoveListener(r13)
            r7 = r13
            com.sec.vsim.attsoftphone.IEmergencyServiceListener r7 = (com.sec.vsim.attsoftphone.IEmergencyServiceListener) r7
            int r5 = r12.mAddressId
            boolean r6 = r12.mConfirmed
            r4 = r11
            r4.validateE911Address(r5, r6, r7, r8, r9)
            return
        L_0x005c:
            java.lang.String r13 = r12.mReason
            java.lang.String r0 = "Address Confirmation Required"
            boolean r13 = r13.contains(r0)
            if (r13 == 0) goto L_0x0068
            r9 = r3
            goto L_0x0069
        L_0x0068:
            r9 = r1
        L_0x0069:
            com.sec.vsim.attsoftphone.data.AddressValidationNotify r13 = new com.sec.vsim.attsoftphone.data.AddressValidationNotify
            r5 = 6
            boolean r6 = r12.mSuccess
            java.lang.String r7 = r12.mReason
            int r8 = r12.mAddressId
            r4 = r13
            r4.<init>(r5, r6, r7, r8, r9)
            int r12 = r12.mTransactionId
            java.lang.Object r12 = r11.findAndRemoveListener(r12)
            com.sec.vsim.attsoftphone.IEmergencyServiceListener r12 = (com.sec.vsim.attsoftphone.IEmergencyServiceListener) r12
            if (r12 == 0) goto L_0x009f
            r12.onNotify(r13)     // Catch:{ RemoteException -> 0x0084 }
            goto L_0x009f
        L_0x0084:
            r12 = move-exception
            java.lang.String r11 = r11.LOG_TAG
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            java.lang.String r0 = "exception"
            r13.append(r0)
            java.lang.String r12 = r12.getMessage()
            r13.append(r12)
            java.lang.String r12 = r13.toString()
            com.sec.internal.log.IMSLog.s(r11, r12)
        L_0x009f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.softphone.SoftphoneClient.processValidateE911AddressResponse(com.sec.internal.constants.ims.entitilement.softphone.responses.AddressValidationResponse, int):void");
    }

    private void updateE911AddressLocally(int i, String str, String str2) {
        this.mEventLog.logAndAdd("updateE911AddressLocally()");
        Uri buildAddressUri = SoftphoneContract.SoftphoneAddress.buildAddressUri((long) i);
        ContentValues contentValues = new ContentValues();
        contentValues.put("account_id", this.mAccountId);
        contentValues.put(SoftphoneContract.AddressColumns.E911AID, str);
        contentValues.put(SoftphoneContract.AddressColumns.EXPIRE_DATE, str2);
        this.mContext.getContentResolver().update(buildAddressUri, contentValues, (String) null, (String[]) null);
    }

    /* access modifiers changed from: package-private */
    public void addE911Address(int i, int i2, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("addE911Address(): addressId: " + i + ", retryCount: " + i2 + ", timeout: " + j);
        AddAddressRequest buildAddAddressRequest = this.mRequestBuilder.buildAddAddressRequest(i);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.E911ADDRESS_PATH);
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/json");
        try {
            softphoneHttpTransaction.setJsonBody(new JSONObject(new Gson().toJson(buildAddAddressRequest)));
        } catch (JSONException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "could not build JSONObject:" + e.getMessage());
        }
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.POST);
        softphoneHttpTransaction.setTimeout(j);
        this.mStateHandler.sendMessage(7, i2, i, softphoneHttpTransaction);
    }

    public void processAddE911AddressResponse(AddAddressResponse addAddressResponse, int i, int i2) {
        int i3;
        AddAddressResponse.LocationResponse locationResponse;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processAddAddressResponse(): success: " + addAddressResponse.mSuccess + ", retryCount: " + i + ", addressId: " + i2);
        if (addAddressResponse.mSuccess && (locationResponse = addAddressResponse.mLocationResponse) != null) {
            this.mSoftphoneEmergencyServcie.compareAndSaveE911Address(locationResponse.mLocations, this.mAccountId);
        } else if (addAddressResponse.mStatusCode == -1 && (i3 = i + 1) < 3) {
            addE911Address(i2, i3, SoftphoneNamespaces.mTimeoutType1[i3]);
            return;
        }
        notifyProgress(new GeneralNotify(7, addAddressResponse.mSuccess, addAddressResponse.mReason));
    }

    private void registerSoftphoneLabelObserver(String str) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("registerSoftphoneLabelObserver() for " + str);
        this.mContext.getContentResolver().registerContentObserver(SoftphoneContract.SoftphoneAccount.buildAccountLabelUri(str, (long) this.mUserId), false, this.mSoftphoneLabelObserver);
    }

    private void deregisterSoftphoneLabelObserver() {
        this.mEventLog.logAndAdd("deregisterSoftphoneLabelObserver()");
        this.mContext.getContentResolver().unregisterContentObserver(this.mSoftphoneLabelObserver);
    }

    public void handleTryRegisterRequest() {
        if (!this.mLoggedOut) {
            this.mEventLog.logAndAdd("There is an ongoing profile registration.");
            return;
        }
        ImsProfile createProfileFromTemplate = SoftphoneAuthUtils.createProfileFromTemplate(this.mContext, this.mIdentity, this.mAccountId, this.mUserId);
        if (createProfileFromTemplate != null) {
            int registerProfile = this.mRegistrationManager.registerProfile(createProfileFromTemplate, this.mPhoneId);
            this.mProfileId = registerProfile;
            if (registerProfile != -1) {
                this.mLoggedOut = false;
            }
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("injected profile ID: " + this.mProfileId);
            return;
        }
        this.mEventLog.logAndAdd("fail to build profile");
        notifyRegisterStatus(false, "Fail to build profile.");
    }

    public void handleDeRegisterRequest() {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("handleDeRegisterRequest(): mProfileId = " + this.mProfileId);
        int i = this.mProfileId;
        if (i != -1) {
            this.mRegistrationManager.deregisterProfile(i, this.mPhoneId);
            this.mLoggedOut = true;
        }
    }

    public void handleLabelUpdated() {
        this.mEventLog.logAndAdd("handleLabelUpdated()");
        handleDeRegisterRequest();
        SoftphoneStateHandler softphoneStateHandler = this.mStateHandler;
        softphoneStateHandler.deferMessage(softphoneStateHandler.obtainMessage(14));
    }

    public void resetCurrentAddresses() {
        this.mEventLog.logAndAdd("resetCurrentAddresses()");
        this.mContext.getContentResolver().update(SoftphoneContract.SoftphoneAddress.buildResetCurrentAddressUri(this.mAccountId), new ContentValues(), (String) null, (String[]) null);
    }

    public void releaseImsNetworkIdentities(int i, long j) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("releaseImsNetworkIdentities(): retryCount: " + i + ", timeout: " + j);
        if (this.mIdentity.impiEmpty()) {
            this.mEventLog.logAndAdd("No IMS network identifiers to release.");
            this.mStateHandler.sendMessage(18);
            return;
        }
        ReleaseImsNetworkIdentifiersRequest buildReleaseImsNetworkIdentifiersRequest = SoftphoneRequestBuilder.buildReleaseImsNetworkIdentifiersRequest(this.mIdentity.getImpi(), this.mIdentity.getImpu());
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.RELEASE_IDENTIFIERS_PATH);
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/json");
        try {
            softphoneHttpTransaction.setJsonBody(new JSONObject(new Gson().toJson(buildReleaseImsNetworkIdentifiersRequest)));
        } catch (JSONException e) {
            String str = this.LOG_TAG;
            IMSLog.s(str, "could not build JSONObject:" + e.getMessage());
        }
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.PUT);
        softphoneHttpTransaction.setTimeout(j);
        this.mStateHandler.sendMessage(5, i, -1, softphoneHttpTransaction);
    }

    public void processReleaseImsNetworkIdentitiesResponse(SoftphoneResponse softphoneResponse, int i) {
        int i2;
        this.mEventLog.logAndAdd("processReleaseImsNetworkIdentitiesResponse(): success: " + softphoneResponse.mSuccess + ", retryCount: " + i);
        if (softphoneResponse.mSuccess) {
            this.mIdentity.clear();
            this.mSharedPrefHelper.remove(this.mContext, this.mAccountId + ":" + this.mUserId + ":" + "impi", this.mAccountId + ":" + this.mUserId + ":" + "impu", this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_FQDN);
        } else if (softphoneResponse.mStatusCode != -1 || (i2 = i + 1) >= 3) {
            this.mEventLog.logAndAdd("Fail to ReleaseImsNetworkIdentities: " + softphoneResponse.mReason);
        } else {
            releaseImsNetworkIdentities(i2, SoftphoneNamespaces.mTimeoutType1[i2]);
            return;
        }
        PendingIntent pendingIntent = this.mRefreshIdentityIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mRefreshIdentityIntent = null;
        }
        this.mStateHandler.sendMessage(18);
    }

    public void revokeAccessToken() {
        this.mEventLog.logAndAdd("revokeAccessToken()");
        this.mStateHandler.removeMessages(15);
        revokeToken("access_token", this.mAccessToken);
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        sharedPrefHelper.remove(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token");
    }

    public void processRevokeAccessTokenResponse(SoftphoneResponse softphoneResponse) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processRevokeAccessTokenResponse(): success: " + softphoneResponse.mSuccess);
        if (softphoneResponse.mSuccess) {
            this.mAccessToken = null;
            this.mAccessTokenType = null;
            this.mTokenExpiresTime = -1;
        } else {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("Fail to RevokeAccessToken: " + softphoneResponse.mReason);
        }
        revokeToken("refresh_token", this.mRefreshToken);
    }

    public void processRevokeRefreshTokenResponse(SoftphoneResponse softphoneResponse) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processRevokeRefreshTokenResponse(): success: " + softphoneResponse.mSuccess);
        if (softphoneResponse.mSuccess) {
            this.mRefreshToken = null;
        } else {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("Fail to RevokeRefreshToken: " + softphoneResponse.mReason);
        }
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_INITSTATE);
    }

    /* access modifiers changed from: package-private */
    public void revokeToken(String str, String str2) {
        this.mEventLog.logAndAdd("revokeToken()");
        String str3 = this.LOG_TAG;
        IMSLog.s(str3, "revokeToken(): tokenType: " + str + " , token: " + str2);
        RevokeTokenRequest buildRevokeTokenRequest = SoftphoneRequestBuilder.buildRevokeTokenRequest(this.mAppKey, this.mAppSecret, str2, str);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.REVOKE_TOKEN_PATH);
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            softphoneHttpTransaction.setJsonBody(new JSONObject(new Gson().toJson(buildRevokeTokenRequest)));
        } catch (JSONException e) {
            String str4 = this.LOG_TAG;
            IMSLog.s(str4, "could not build JSONObject:" + e.getMessage());
        }
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.POST);
        if (str.equalsIgnoreCase("access_token")) {
            this.mStateHandler.sendMessage(12, (Object) softphoneHttpTransaction);
        } else if (str.equalsIgnoreCase("refresh_token")) {
            this.mStateHandler.sendMessage(13, (Object) softphoneHttpTransaction);
        }
    }

    public void refreshToken(int i, long j, int i2) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("refreshToken(): retryCount: " + i + ", timeout: " + j + ", attempt: " + i2);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.TOKEN_PATH);
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        softphoneHttpTransaction.setStringBody("client_id=" + this.mAppKey + "&client_secret=" + this.mAppSecret + "&grant_type=refresh_token&refresh_token=" + this.mRefreshToken);
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.POST);
        softphoneHttpTransaction.setTimeout(j);
        softphoneHttpTransaction.commit(this.mStateHandler.obtainMessage(1015, i, i2));
    }

    public void processRefreshTokenResponse(AccessTokenResponse accessTokenResponse, int i, int i2, int i3) {
        int i4;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processRefreshTokenResponse(): success: " + accessTokenResponse.mSuccess + ", statusCode: " + i + ", retryCount: " + i2 + ", attempt: " + i3);
        if (accessTokenResponse.mSuccess) {
            saveTokens(accessTokenResponse.mAccessToken, accessTokenResponse.mTokenType, Long.parseLong(accessTokenResponse.mExpiresIn), accessTokenResponse.mRefreshToken);
            scheduleRefreshTokenAlarm(this.mTokenExpiresTime * 900, 0);
            if (this.mIsRecovery.get()) {
                handleImsNetworkIdentityAfterRecovery();
            }
        } else if (accessTokenResponse.mStatusCode == -1 && (i4 = i2 + 1) < 3) {
            refreshToken(i4, SoftphoneNamespaces.mTimeoutType4[i4], i3);
        } else if (i == 401) {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("processRefreshTokenResponse(): statusCode: " + i + ", invalid access token, reLogin");
            reLogin(0, true);
        } else {
            int i5 = i3 + 1;
            if (i5 <= 3) {
                long j = SoftphoneNamespaces.mTimeoutType4[0];
                long j2 = this.mTokenExpiresTime;
                if (j2 > 0) {
                    j = (j2 * 100) / 3;
                }
                scheduleRefreshTokenAlarm(j, i5);
            } else if (this.mTGaurdToken != null) {
                SimpleEventLog simpleEventLog3 = this.mEventLog;
                simpleEventLog3.logAndAdd("processRefreshTokenResponse(): statusCode: " + i + ", unable to refresh token, try reLogin");
                reLogin(0, true);
            } else {
                SimpleEventLog simpleEventLog4 = this.mEventLog;
                simpleEventLog4.logAndAdd("processRefreshTokenResponse(): statusCode: " + i + ", unable to refresh token, logOut");
                logOut();
            }
        }
    }

    public void processGetCallWaitingInfoResponse(CallWaitingResponse callWaitingResponse, int i) {
        int i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processGetCallWaitingInfoResponse(): success: " + callWaitingResponse.mSuccess + ", retryCount: " + i);
        if (callWaitingResponse.mSuccess) {
            this.mEventLog.logAndAdd(callWaitingResponse.mActive);
            notifySsProgress(new SupplementaryServiceNotify(8, callWaitingResponse.mSuccess, callWaitingResponse.mReason, new CallWaitingInfo(CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(callWaitingResponse.mActive))));
        } else if (callWaitingResponse.mStatusCode != -1 || (i2 = i + 1) >= 3) {
            notifySsProgress(new SupplementaryServiceNotify(8, false, callWaitingResponse.mReason));
        } else {
            getCallWaitingInfo(i2, SoftphoneNamespaces.mTimeoutType1[i2]);
        }
    }

    private void checkWithCondition(List<CallForwardingInfo> list, CallForwardingResponse.Ruleset.Rule rule, boolean z, int i, String str) {
        CallForwardingResponse.Ruleset.Rule.Condition condition = rule.mConditions;
        if (condition.mBusy != null) {
            this.mEventLog.logAndAdd("condition: busy");
            list.add(new CallForwardingInfo(z && !str.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, i, 1, str));
        } else if (condition.mNoAnswer != null) {
            this.mEventLog.logAndAdd("condition: no-answer");
            list.add(new CallForwardingInfo(z && !str.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, i, 2, str));
        } else if (condition.mNotReachable != null) {
            this.mEventLog.logAndAdd("condition: not-reachable");
            list.add(new CallForwardingInfo(z && !str.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, i, 3, str));
        } else if (condition.mNotRegistered != null) {
            this.mEventLog.logAndAdd("condition: not-registered");
            list.add(new CallForwardingInfo(z && !str.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, i, 8, str));
        } else if (condition.mUnconditional != null) {
            this.mEventLog.logAndAdd("condition: unconditional");
            list.add(new CallForwardingInfo(z && !str.isEmpty() && rule.mConditions.mRuleDeactivated == null, false, i, 0, str));
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void processGetCallForwardingInfoResponse(com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse r24, int r25) {
        /*
            r23 = this;
            r6 = r23
            r7 = r24
            r0 = r25
            com.sec.internal.helper.SimpleEventLog r1 = r6.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "processGetCallForwardingInfoResponse(): success: "
            r2.append(r3)
            boolean r3 = r7.mSuccess
            r2.append(r3)
            java.lang.String r3 = ", retryCount: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            boolean r1 = r7.mSuccess
            r8 = 3
            r9 = 9
            r10 = -1
            r11 = 0
            r12 = 1
            if (r1 == 0) goto L_0x01e9
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.String r1 = r7.mActive
            r0.logAndAdd(r1)
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.String r1 = r7.mNoReplyTimer
            r0.logAndAdd(r1)
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "num of rules: "
            r1.append(r2)
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset r2 = r7.mRuleset
            java.util.List<com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule> r2 = r2.mRules
            int r2 = r2.size()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            java.util.ArrayList r13 = new java.util.ArrayList
            r13.<init>()
            java.lang.String r0 = "true"
            java.lang.String r1 = r7.mActive
            boolean r14 = r0.equalsIgnoreCase(r1)
            java.lang.String r0 = r7.mNoReplyTimer
            int r21 = java.lang.Integer.parseInt(r0)
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset r0 = r7.mRuleset
            java.util.List<com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule> r0 = r0.mRules
            java.util.Iterator r22 = r0.iterator()
        L_0x0079:
            boolean r0 = r22.hasNext()
            if (r0 == 0) goto L_0x01dc
            java.lang.Object r0 = r22.next()
            r2 = r0
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule r2 = (com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse.Ruleset.Rule) r2
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "ruleId: "
            r1.append(r3)
            java.lang.String r3 = r2.mId
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Action r0 = r2.mActions
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Action$ForwardTo r0 = r0.mForwardTo
            java.lang.String r0 = r0.mTarget
            java.lang.String r1 = ":"
            java.lang.String[] r0 = r0.split(r1)
            int r1 = r0.length
            int r1 = r1 - r12
            r5 = r0[r1]
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            r0.logAndAdd(r5)
            java.lang.String r0 = r2.mId
            r0.hashCode()
            int r1 = r0.hashCode()
            switch(r1) {
                case -2094921849: goto L_0x00ee;
                case -1169678268: goto L_0x00e3;
                case -225471283: goto L_0x00d8;
                case 107705890: goto L_0x00cd;
                case 424630474: goto L_0x00c2;
                default: goto L_0x00c0;
            }
        L_0x00c0:
            r0 = r10
            goto L_0x00f8
        L_0x00c2:
            java.lang.String r1 = "call-diversion-not-reachable"
            boolean r0 = r0.equals(r1)
            if (r0 != 0) goto L_0x00cb
            goto L_0x00c0
        L_0x00cb:
            r0 = 4
            goto L_0x00f8
        L_0x00cd:
            java.lang.String r1 = "call-diversion-busy"
            boolean r0 = r0.equals(r1)
            if (r0 != 0) goto L_0x00d6
            goto L_0x00c0
        L_0x00d6:
            r0 = r8
            goto L_0x00f8
        L_0x00d8:
            java.lang.String r1 = "call-diversion-not-logged-in"
            boolean r0 = r0.equals(r1)
            if (r0 != 0) goto L_0x00e1
            goto L_0x00c0
        L_0x00e1:
            r0 = 2
            goto L_0x00f8
        L_0x00e3:
            java.lang.String r1 = "call-diversion-unconditional"
            boolean r0 = r0.equals(r1)
            if (r0 != 0) goto L_0x00ec
            goto L_0x00c0
        L_0x00ec:
            r0 = r12
            goto L_0x00f8
        L_0x00ee:
            java.lang.String r1 = "call-diversion-no-reply"
            boolean r0 = r0.equals(r1)
            if (r0 != 0) goto L_0x00f7
            goto L_0x00c0
        L_0x00f7:
            r0 = r11
        L_0x00f8:
            switch(r0) {
                case 0: goto L_0x01b6;
                case 1: goto L_0x0190;
                case 2: goto L_0x016a;
                case 3: goto L_0x0144;
                case 4: goto L_0x011e;
                default: goto L_0x00fb;
            }
        L_0x00fb:
            com.sec.internal.helper.SimpleEventLog r0 = r6.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "Unknown ruleId: "
            r1.append(r3)
            java.lang.String r3 = r2.mId
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r0 = r23
            r1 = r13
            r3 = r14
            r4 = r21
            r0.checkWithCondition(r1, r2, r3, r4, r5)
            goto L_0x0079
        L_0x011e:
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x0131
            boolean r1 = r5.isEmpty()
            if (r1 != 0) goto L_0x0131
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r1 = r2.mConditions
            java.lang.String r1 = r1.mRuleDeactivated
            if (r1 != 0) goto L_0x0131
            r16 = r12
            goto L_0x0133
        L_0x0131:
            r16 = r11
        L_0x0133:
            r17 = 0
            r19 = 3
            r15 = r0
            r18 = r21
            r20 = r5
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x0079
        L_0x0144:
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x0157
            boolean r1 = r5.isEmpty()
            if (r1 != 0) goto L_0x0157
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r1 = r2.mConditions
            java.lang.String r1 = r1.mRuleDeactivated
            if (r1 != 0) goto L_0x0157
            r16 = r12
            goto L_0x0159
        L_0x0157:
            r16 = r11
        L_0x0159:
            r17 = 0
            r19 = 1
            r15 = r0
            r18 = r21
            r20 = r5
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x0079
        L_0x016a:
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x017d
            boolean r1 = r5.isEmpty()
            if (r1 != 0) goto L_0x017d
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r1 = r2.mConditions
            java.lang.String r1 = r1.mRuleDeactivated
            if (r1 != 0) goto L_0x017d
            r16 = r12
            goto L_0x017f
        L_0x017d:
            r16 = r11
        L_0x017f:
            r17 = 0
            r19 = 8
            r15 = r0
            r18 = r21
            r20 = r5
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x0079
        L_0x0190:
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x01a3
            boolean r1 = r5.isEmpty()
            if (r1 != 0) goto L_0x01a3
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r1 = r2.mConditions
            java.lang.String r1 = r1.mRuleDeactivated
            if (r1 != 0) goto L_0x01a3
            r16 = r12
            goto L_0x01a5
        L_0x01a3:
            r16 = r11
        L_0x01a5:
            r17 = 0
            r19 = 0
            r15 = r0
            r18 = r21
            r20 = r5
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x0079
        L_0x01b6:
            com.sec.vsim.attsoftphone.data.CallForwardingInfo r0 = new com.sec.vsim.attsoftphone.data.CallForwardingInfo
            if (r14 == 0) goto L_0x01c9
            boolean r1 = r5.isEmpty()
            if (r1 != 0) goto L_0x01c9
            com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse$Ruleset$Rule$Condition r1 = r2.mConditions
            java.lang.String r1 = r1.mRuleDeactivated
            if (r1 != 0) goto L_0x01c9
            r16 = r12
            goto L_0x01cb
        L_0x01c9:
            r16 = r11
        L_0x01cb:
            r17 = 0
            r19 = 2
            r15 = r0
            r18 = r21
            r20 = r5
            r15.<init>(r16, r17, r18, r19, r20)
            r13.add(r0)
            goto L_0x0079
        L_0x01dc:
            com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify r0 = new com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify
            boolean r1 = r7.mSuccess
            java.lang.String r2 = r7.mReason
            r0.<init>(r9, r1, r2, r13)
            r6.notifySsProgress(r0)
            goto L_0x0202
        L_0x01e9:
            int r1 = r7.mStatusCode
            if (r1 != r10) goto L_0x01f8
            int r0 = r0 + r12
            if (r0 >= r8) goto L_0x01f8
            long[] r1 = com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces.mTimeoutType1
            r1 = r1[r0]
            r6.getCallForwardingInfo(r0, r1)
            return
        L_0x01f8:
            com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify r0 = new com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify
            java.lang.String r1 = r7.mReason
            r0.<init>(r9, r11, r1)
            r6.notifySsProgress(r0)
        L_0x0202:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.softphone.SoftphoneClient.processGetCallForwardingInfoResponse(com.sec.internal.constants.ims.entitilement.softphone.responses.CallForwardingResponse, int):void");
    }

    public void processSetCallWaitingInfoResponse(SoftphoneResponse softphoneResponse, int i, CallWaitingInfo callWaitingInfo) {
        int i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processSetCallWaitingInfoResponse(): success: " + softphoneResponse.mSuccess + ", retryCount:" + i);
        if (softphoneResponse.mStatusCode != -1 || (i2 = i + 1) >= 3) {
            notifySsProgress(new SupplementaryServiceNotify(10, softphoneResponse.mSuccess, softphoneResponse.mReason));
        } else {
            setCallWaitingInfo(callWaitingInfo, i2, SoftphoneNamespaces.mTimeoutType2[i2]);
        }
    }

    public void processSetCallForwardingInfoResponse(SoftphoneResponse softphoneResponse, int i, CallForwardingInfo callForwardingInfo) {
        int i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processSetCallForwardingInfoResponse(): success: " + softphoneResponse.mSuccess + ", retryCount:" + i);
        if (softphoneResponse.mStatusCode != -1 || (i2 = i + 1) >= 3) {
            notifySsProgress(new SupplementaryServiceNotify(11, softphoneResponse.mSuccess, softphoneResponse.mReason));
        } else {
            setCallForwardingInfo(callForwardingInfo, i2, SoftphoneNamespaces.mTimeoutType2[i2]);
        }
    }

    public void processAkaChallengeResponse(AkaAuthenticationResponse akaAuthenticationResponse, int i, String str) {
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "processAkaChallengeResponse(): retryCount: " + i + "response: " + akaAuthenticationResponse);
        String processAkaAuthenticationResponse = SoftphoneAuthUtils.processAkaAuthenticationResponse(akaAuthenticationResponse);
        if (processAkaAuthenticationResponse.isEmpty()) {
            String str3 = this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("aka failed ");
            int i2 = i + 1;
            sb.append(i2);
            sb.append(" time(s)");
            IMSLog.e(str3, sb.toString());
            if (i2 < 3) {
                onRequestAkaChallenge(str, i2);
                return;
            }
            IMSLog.e(this.LOG_TAG, "aka failed over 3 times, deregister...");
            ContextExt.sendBroadcastAsUser(this.mContext, new Intent("com.sec.imsservice.AKA_CHALLENGE_FAILED"), ContextExt.ALL);
            return;
        }
        IMSLog.i(this.LOG_TAG, "Sending AKA response Intent to SimManager");
        Intent intent = new Intent("com.sec.imsservice.AKA_CHALLENGE_COMPLETE");
        intent.putExtra("result", processAkaAuthenticationResponse);
        intent.putExtra("id", this.mProfileId);
        intent.setPackage(this.mContext.getPackageName());
        ContextExt.sendBroadcastAsUser(this.mContext, intent, ContextExt.ALL);
    }

    private String getContextInfo() {
        return "mdl=" + SoftphoneAuthUtils.getDeviceType(this.mContext) + ",os=" + Build.VERSION.RELEASE;
    }

    @SuppressLint({"MissingPermission"})
    private SoftphoneHttpTransaction addMsipHeaders(SoftphoneHttpTransaction softphoneHttpTransaction) {
        String string = SemFloatingFeature.getInstance().getString(SecFeature.FLOATING.CONFIG_BRAND_NAME);
        softphoneHttpTransaction.addRequestHeader("x-att-clientId", SoftphoneNamespaces.SoftphoneSettings.MSIP_CLIENTID_PREFIX + string);
        softphoneHttpTransaction.addRequestHeader("x-att-clientVersion", "1.0");
        softphoneHttpTransaction.addRequestHeader("x-att-deviceId", this.mTelephonyManager.getImei());
        softphoneHttpTransaction.addRequestHeader("x-att-contextInfo", getContextInfo());
        return softphoneHttpTransaction;
    }

    public void obtainPdCookies(int i) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("obtainPdCookies(): retryCount: " + i);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        if (1 == this.mEnvironment) {
            softphoneHttpTransaction.setRequestURL("https://tprodsmsx.att.net/commonLogin/nxsEDAM/controller.do");
            softphoneHttpTransaction.setQueryParameters(this.mRequestBuilder.buildObtainPdCookiesQueryParams(this.mAccountId, this.mUserId, this.mSecretKey, "messagessd.att.net"), true);
            softphoneHttpTransaction.addRequestHeader(HttpController.HEADER_HOST, SoftphoneNamespaces.SoftphoneSettings.MSIP_PROD_TOKEN_HOST);
        } else {
            softphoneHttpTransaction.setRequestURL("https://tstagesms.stage.att.net/commonLogin/nxsEDAM/controller.do");
            softphoneHttpTransaction.setQueryParameters(this.mRequestBuilder.buildObtainPdCookiesQueryParams(this.mAccountId, this.mUserId, this.mSecretKey, "messagessd.stage.att.net"), true);
            softphoneHttpTransaction.addRequestHeader(HttpController.HEADER_HOST, SoftphoneNamespaces.SoftphoneSettings.MSIP_STAGE_TOKEN_HOST);
        }
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        SoftphoneHttpTransaction addMsipHeaders = addMsipHeaders(softphoneHttpTransaction);
        addMsipHeaders.setRequestMethod(HttpRequestParams.Method.POST);
        this.mStateHandler.sendMessageDelayed(1020, i, -1, addMsipHeaders, SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF * ((long) i));
    }

    public void onRequestAkaChallenge(String str, int i) {
        long j = SoftphoneNamespaces.mTimeoutType1[i];
        String str2 = this.LOG_TAG;
        IMSLog.i(str2, "onRequestAkaChallenge : nonce = " + str + ", retryCount = " + i + ", timeout = " + j);
        String[] splitRandAutn = SoftphoneAuthUtils.splitRandAutn(str);
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        softphoneHttpTransaction.initHttpRequest(SoftphoneNamespaces.SoftphoneSettings.AKA_AUTH_PATH);
        softphoneHttpTransaction.addRequestHeader("randomChallenge", splitRandAutn[0]);
        softphoneHttpTransaction.addRequestHeader("networkAuthenticatorToken", splitRandAutn[1]);
        softphoneHttpTransaction.setRequestMethod(HttpRequestParams.Method.GET);
        softphoneHttpTransaction.setTimeout(j);
        Message obtain = Message.obtain((Handler) null, 19, i, -1, softphoneHttpTransaction);
        Bundle bundle = new Bundle();
        bundle.putString(WwwAuthenticateHeader.HEADER_PARAM_NONCE, str);
        obtain.setData(bundle);
        this.mStateHandler.sendMessage(obtain);
    }

    public void processObtainPdCookiesResponse(HttpResponseParams httpResponseParams, int i) {
        if (httpResponseParams != null) {
            Map<String, List<String>> headers = httpResponseParams.getHeaders();
            if (headers != null) {
                List<String> list = headers.get(HttpController.HEADER_SET_COOKIE);
                StringBuilder sb = new StringBuilder();
                if (list != null) {
                    for (String split : list) {
                        for (String str : split.split("[;,]")) {
                            if (str.contains("PD-ID=") || str.contains("PD-H-SESSION-ID")) {
                                sb.append(str);
                                sb.append(";");
                            }
                        }
                    }
                }
                String sb2 = sb.toString();
                this.mEventLog.logAndAdd("processObtainPdCookiesResponse()");
                IMSLog.s(this.LOG_TAG, "processObtainPdCookiesResponse(): " + sb2);
                if (!sb2.isEmpty()) {
                    this.mSharedPrefHelper.save(this.mContext, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_PD_COOKIES, this.mEncryptionHelper.encrypt(sb2.substring(0, sb2.length() - 1), this.mSecretKey));
                    sendSMS(sb2, 0);
                    return;
                }
                retryObtainPdCookies(i);
                return;
            }
            retryObtainPdCookies(i);
            return;
        }
        retryObtainPdCookies(i);
    }

    private void retryObtainPdCookies(int i) {
        int i2 = i + 1;
        if (i2 < 3) {
            obtainPdCookies(i2);
        }
    }

    private void sendSMS(String str, int i) {
        this.mEventLog.logAndAdd("sendSMS()");
        String impu = this.mIdentity.getImpu();
        SendSMSRequest buildSendSMSRequest = SoftphoneRequestBuilder.buildSendSMSRequest(impu.substring(impu.indexOf(":") + 1, impu.indexOf("@")));
        int httpTransactionId = this.mStateHandler.getHttpTransactionId();
        SoftphoneHttpTransaction softphoneHttpTransaction = new SoftphoneHttpTransaction(this);
        if (1 == this.mEnvironment) {
            softphoneHttpTransaction.setRequestURL("https://messagessd.att.net/messaging/v0/outbound");
            softphoneHttpTransaction.addRequestHeader(HttpController.HEADER_HOST, "messagessd.att.net");
        } else {
            softphoneHttpTransaction.setRequestURL("https://messagessd.stage.att.net/messaging/v0/outbound");
            softphoneHttpTransaction.addRequestHeader(HttpController.HEADER_HOST, "messagessd.stage.att.net");
        }
        softphoneHttpTransaction.addRequestHeader(HttpController.HEADER_COOKIE, str);
        softphoneHttpTransaction.addRequestHeader("Content-Type", "application/json");
        softphoneHttpTransaction.addRequestHeader("Accept", "application/json");
        softphoneHttpTransaction.addRequestHeader("transactionId", Integer.toString(httpTransactionId));
        SoftphoneHttpTransaction addMsipHeaders = addMsipHeaders(softphoneHttpTransaction);
        try {
            String replace = new JSONObject(new Gson().toJson(buildSendSMSRequest)).toString().replace("\\/", "/");
            String str2 = this.LOG_TAG;
            IMSLog.s(str2, "sendSMS(): " + replace);
            addMsipHeaders.setByteData(replace.getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            String str3 = this.LOG_TAG;
            IMSLog.s(str3, "could not build JSONObject:" + e.getMessage());
        }
        addMsipHeaders.setRequestMethod(HttpRequestParams.Method.POST);
        this.mStateHandler.sendMessageDelayed(1022, i, -1, addMsipHeaders, ((long) i) * SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
    }

    public void processSendSMSResponse(SoftphoneResponse softphoneResponse, int i) {
        int i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processSendSMSResponse(): success: " + softphoneResponse.mSuccess + ", retryCount: " + i);
        if (!softphoneResponse.mSuccess && (i2 = i + 1) < 3) {
            SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
            Context context = this.mContext;
            sendSMS(this.mEncryptionHelper.decrypt(sharedPrefHelper.get(context, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_PD_COOKIES), this.mSecretKey), i2);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean tokenExist() {
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        if (sharedPrefHelper != null) {
            Context context = this.mContext;
            this.mRefreshToken = sharedPrefHelper.get(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token");
            SimpleEventLog simpleEventLog = this.mEventLog;
            StringBuilder sb = new StringBuilder();
            sb.append("tokenExist: ");
            sb.append(this.mRefreshToken);
            simpleEventLog.logAndAdd(sb.toString());
            if (this.mRefreshToken != null) {
                return true;
            }
            return false;
        }
        this.mEventLog.logAndAdd("sharedPrefHelper is null");
        return false;
    }

    private void getAccountSecretKey() {
        Cursor query = this.mContext.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildAccountIdUri(this.mAccountId, (long) this.mUserId), (String[]) null, "userid = ?", new String[]{String.valueOf(this.mUserId)}, (String) null);
        if (query != null && query.moveToFirst()) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("found " + query.getCount() + " secretKey");
            this.mSecretKey = EncryptionHelper.getSecretKey(query);
            query.close();
        }
    }

    /* access modifiers changed from: package-private */
    public void refreshTokenAfterRecovery() {
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        String str = sharedPrefHelper.get(context, this.mAccountId + ":" + this.mUserId + ":" + "refresh_token");
        this.mRefreshToken = str;
        this.mRefreshToken = this.mEncryptionHelper.decrypt(str, this.mSecretKey);
        Message obtainMessage = this.mStateHandler.obtainMessage(15, 0, (int) SoftphoneNamespaces.mTimeoutType4[0], (Object) null);
        Bundle bundle = new Bundle();
        bundle.putInt(SoftphoneNamespaces.SoftphoneSettings.ATTEMPT, 0);
        obtainMessage.setData(bundle);
        this.mStateHandler.sendMessage(obtainMessage);
    }

    /* access modifiers changed from: package-private */
    public void handleImsNetworkIdentityAfterRecovery() {
        registerSoftphoneLabelObserver(this.mAccountId);
        SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
        Context context = this.mContext;
        String str = sharedPrefHelper.get(context, this.mAccountId + ":" + this.mUserId + ":" + "impi");
        SharedPrefHelper sharedPrefHelper2 = this.mSharedPrefHelper;
        Context context2 = this.mContext;
        String str2 = sharedPrefHelper2.get(context2, this.mAccountId + ":" + this.mUserId + ":" + "impu");
        SharedPrefHelper sharedPrefHelper3 = this.mSharedPrefHelper;
        Context context3 = this.mContext;
        String str3 = sharedPrefHelper3.get(context3, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.PREF_FQDN);
        if (str == null || str2 == null || str3 == null) {
            this.mEventLog.logAndAdd("Recovery: no previous identity");
            getImsNetworkIdentifiers(false, true, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
            if (this.mResendSmsIntent == null) {
                SharedPrefHelper sharedPrefHelper4 = this.mSharedPrefHelper;
                Context context4 = this.mContext;
                resumeSmsAlarm(sharedPrefHelper4.getLong(context4, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.LAST_SMS_TIME, Calendar.getInstance().getTimeInMillis()));
                return;
            }
            return;
        }
        this.mEventLog.logAndAdd("Recovery: identity found. Try to relogin");
        this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_REFRESHSTATE);
        this.mIdentity = new ImsNetworkIdentity(this.mEncryptionHelper.decrypt(str, this.mSecretKey), this.mEncryptionHelper.decrypt(str2, this.mSecretKey), new ArrayList(Arrays.asList(new String[]{this.mEncryptionHelper.decrypt(str3, this.mSecretKey)})), this.mAppKey);
        releaseImsNetworkIdentities(0, SoftphoneNamespaces.mTimeoutType1[0]);
    }

    public void processExchangeForAccessTokenResponse(AccessTokenResponse accessTokenResponse, int i, boolean z) {
        int i2;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processExchangeForAccessTokenResponse(): success: " + accessTokenResponse.mSuccess + ", mReason: " + accessTokenResponse.mReason + ", retryCount: " + i + ", autoRegister: " + z);
        if (accessTokenResponse.mSuccess) {
            updateAccountStatus(this.mAccountId, 2);
            saveTokens(accessTokenResponse.mAccessToken, accessTokenResponse.mTokenType, Long.parseLong(accessTokenResponse.mExpiresIn), accessTokenResponse.mRefreshToken);
            scheduleRefreshTokenAlarm(this.mTokenExpiresTime * 900, 0);
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_REDISTATE);
            getImsNetworkIdentifiers(false, z, 0, SoftphoneNamespaces.mTimeoutType1[0], 0);
            if (!z) {
                scheduleSmsAlarm();
                obtainPdCookies(0);
            } else if (this.mResendSmsIntent == null) {
                SharedPrefHelper sharedPrefHelper = this.mSharedPrefHelper;
                Context context = this.mContext;
                resumeSmsAlarm(sharedPrefHelper.getLong(context, this.mAccountId + ":" + this.mUserId + ":" + SoftphoneNamespaces.SoftphoneSharedPref.LAST_SMS_TIME, Calendar.getInstance().getTimeInMillis()));
            }
        } else if (accessTokenResponse.mStatusCode != -1 || (i2 = i + 1) >= 3) {
            resetAccountStatus();
            this.mStateHandler.sendMessage((int) SoftphoneNamespaces.SoftphoneEvents.EVENT_TRANSITION_TO_INITSTATE);
        } else {
            boolean z2 = z;
            exchangeForAccessToken(this.mTGaurdToken, this.mAccountId, z2, this.mTGaurdAppId, this.mEnvironment, i2, SoftphoneNamespaces.mTimeoutType4[i2]);
            return;
        }
        notifyProgress(new GeneralNotify(0, accessTokenResponse.mSuccess, accessTokenResponse.mReason));
    }
}
