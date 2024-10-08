package com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LineDetail;
import com.sec.internal.constants.ims.entitilement.data.LocAndTcWebSheetData;
import com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus;
import com.sec.internal.constants.ims.entitilement.data.ServiceEntitlement;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BulkEntitlementCheck;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.LocationRegistrationAndTCAcceptanceCheck;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.OperationUsingManagePushToken;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist.PushTokenHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntitlementAndE911AidCheckFlow extends NSDSAppFlowBase implements IEntitlementCheck {
    private static final int FAIL_ENTITLEMENT_AUTO_ON = 14;
    private static final int INIT_E911_ADDRESS_UPDATE = 10;
    private static final int INIT_ENTITLEMENT_CHECK = 8;
    private static final int INIT_PUSH_TOKEN_REMOVAL = 9;
    private static final int LOCATION_AND_TC_CHECK = 0;
    private static final String LOG_TAG = EntitlementAndE911AidCheckFlow.class.getSimpleName();
    private static final int OPEN_E911_ADDRESS_UPDATE_WEBSHEET = 5;
    private static final int OPEN_LOC_AND_TC_WEBSHEET = 4;
    private static final int REGISTER_PUSH_TOKEN = 2;
    private static final int REMOVE_PUSH_TOKEN = 3;
    private static final int REMOVE_PUSH_TOKEN_AUTO_ON = 12;
    private static final int REMOVE_PUSH_TOKEN_AUTO_ON_AFTER = 13;
    private static final int RESULT_SVC_PROV_LOC_AND_TC_WEBSHEET = 6;
    private static final int RESULT_UPDATE_LOC_AND_TC_WEBSHEET = 7;
    private static final int RETRY_ENTITLEMENT_AUTO_ON = 11;
    private static final int VOWIFI_ENTITLEMENT_CHECK = 1;
    private final AtomicBoolean mOnSvcProv = new AtomicBoolean(false);
    private String mServerData;
    private String mServerUrl;
    private int mSimSlot = this.mBaseFlowImpl.getSimManager().getSimSlotIndex();

    private int getLocAndTcWebsheetRespCode(int i) {
        if (i == 0) {
            return NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_PENDING_ERROR_CODE;
        }
        if (i != 1) {
            return i != 2 ? i != 3 ? NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE : NSDSNamespaces.NSDSDefinedResponseCode.VOID_WEBSHEET_TRANSACTION : NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_CANCEL_CODE;
        }
        return 1000;
    }

    private boolean updateResponseResult(boolean z, int i) {
        if (z) {
            return i == 1000 || i == 2303 || i == 2501 || i == 2502 || i == 2302;
        }
        return false;
    }

    public EntitlementAndE911AidCheckFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        super(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleEntitlementCheckResponse(Bundle bundle) {
        NSDSAppFlowBase.NSDSResponseStatus nSDSResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, -1);
        if (bundle == null) {
            return nSDSResponseStatus;
        }
        int httpErrRespCode = getHttpErrRespCode(bundle);
        String httpErrRespReason = getHttpErrRespReason(bundle);
        if (httpErrRespCode > 0 || httpErrRespReason != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "handleEntitlementCheckResponse: http error code = " + httpErrRespCode + ", reason = " + httpErrRespReason);
            nSDSResponseStatus.responseCode = NSDSNamespaces.NSDSDefinedResponseCode.HTTP_TRANSACTION_ERROR_CODE;
            return nSDSResponseStatus;
        }
        ResponseServiceEntitlementStatus responseServiceEntitlementStatus = (ResponseServiceEntitlementStatus) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS);
        ResponseGetMSISDN responseGetMSISDN = (ResponseGetMSISDN) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN);
        if (responseServiceEntitlementStatus != null) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "ResponseServiceEntitlementStatus : messageId:" + responseServiceEntitlementStatus.messageId + ", responseCode:" + responseServiceEntitlementStatus.responseCode);
            int i = responseServiceEntitlementStatus.responseCode;
            if (i == 1000) {
                Iterator<T> it = emptyIfNull(responseServiceEntitlementStatus.serviceEntitlementList).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ServiceEntitlement serviceEntitlement = (ServiceEntitlement) it.next();
                    if ("vowifi".equals(serviceEntitlement.serviceName)) {
                        String str3 = LOG_TAG;
                        IMSLog.i(str3, "service responseCode: " + serviceEntitlement.entitlementStatus + ", onDemandProv: " + serviceEntitlement.onDemandProv);
                        int i2 = serviceEntitlement.entitlementStatus;
                        nSDSResponseStatus.responseCode = i2;
                        if (i2 == 1048) {
                            if (!EntFeatureDetector.checkWFCAutoOnEnabled(this.mSimSlot)) {
                                this.mOnSvcProv.set(true);
                            } else if (serviceEntitlement.onDemandProv.booleanValue()) {
                                this.mOnSvcProv.set(true);
                            }
                        }
                    }
                }
            } else {
                nSDSResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS;
                nSDSResponseStatus.responseCode = i;
            }
        }
        if (responseGetMSISDN != null) {
            int i3 = responseGetMSISDN.responseCode;
            if (i3 == 1000) {
                LineDetail lineDetail = new LineDetail();
                String str4 = LOG_TAG;
                IMSLog.i(str4, "responseGetMsisdn content : messageId:" + responseGetMSISDN.messageId + ", responseCode:" + responseGetMSISDN.responseCode + ", msisdn:" + responseGetMSISDN.msisdn);
                StringBuilder sb = new StringBuilder();
                sb.append("service_fingerprint:");
                sb.append(responseGetMSISDN.serviceFingerprint);
                IMSLog.s(str4, sb.toString());
                if (!(responseGetMSISDN.responseCode != 1000 || responseGetMSISDN.msisdn == null || responseGetMSISDN.serviceFingerprint == null)) {
                    lineDetail.lineId = this.mNSDSDatabaseHelper.insertOrUpdateNativeLine(0, this.mBaseFlowImpl.getDeviceId(), responseGetMSISDN);
                    lineDetail.msisdn = responseGetMSISDN.msisdn;
                    lineDetail.serviceFingerPrint = responseGetMSISDN.serviceFingerprint;
                }
            } else {
                nSDSResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN;
                nSDSResponseStatus.responseCode = i3;
            }
        }
        return nSDSResponseStatus;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleRegisterPushTokenResponse(Bundle bundle) {
        NSDSAppFlowBase.NSDSResponseStatus nSDSResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, (String) null, -1);
        if (bundle == null) {
            return nSDSResponseStatus;
        }
        ResponseManagePushToken responseManagePushToken = (ResponseManagePushToken) bundle.getParcelable("managePushToken");
        if (responseManagePushToken != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "responseManagePushToken for token registration : messageId:" + responseManagePushToken.messageId + ", responseCode:" + responseManagePushToken.responseCode);
            int i = responseManagePushToken.responseCode;
            nSDSResponseStatus.responseCode = i;
            if (i != 1000) {
                IMSLog.i(str, "responseManagePushToken failed");
                nSDSResponseStatus.failedOperation = 0;
            }
        } else {
            IMSLog.e(LOG_TAG, "responseManagePushToken is NULL");
        }
        return nSDSResponseStatus;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleRemovePushTokenResponse(Bundle bundle) {
        NSDSAppFlowBase.NSDSResponseStatus nSDSResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(1000, (String) null, -1);
        if (bundle == null) {
            return nSDSResponseStatus;
        }
        ResponseManagePushToken responseManagePushToken = (ResponseManagePushToken) bundle.getParcelable("managePushToken");
        if (responseManagePushToken != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "responseManagePushToken for token removal : messageId:" + responseManagePushToken.messageId + ", responseCode:" + responseManagePushToken.responseCode);
            int i = responseManagePushToken.responseCode;
            nSDSResponseStatus.responseCode = i;
            if (i != 1000) {
                IMSLog.i(str, "responseManagePushToken failed");
                nSDSResponseStatus.failedOperation = 1;
            }
        } else {
            IMSLog.e(LOG_TAG, "responseManagePushToken is NULL");
        }
        if (this.mDeviceEventType == 3) {
            nSDSResponseStatus.responseCode = 1000;
        }
        return nSDSResponseStatus;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleManageLocationAndTcResponse(ResponseManageLocationAndTC responseManageLocationAndTC) {
        NSDSAppFlowBase.NSDSResponseStatus nSDSResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC, -1);
        if (responseManageLocationAndTC != null) {
            if (responseManageLocationAndTC.responseCode == 1000) {
                this.mServerData = responseManageLocationAndTC.serverData;
                this.mServerUrl = responseManageLocationAndTC.serverUrl;
                String str = LOG_TAG;
                IMSLog.i(str, "onResponseAvailable: update location and tc status in db. E911 AID received: " + responseManageLocationAndTC.addressId);
                NSDSDatabaseHelper nSDSDatabaseHelper = this.mNSDSDatabaseHelper;
                nSDSDatabaseHelper.updateLocationAndTcStatus((long) nSDSDatabaseHelper.getNativeLineId(this.mBaseFlowImpl.getDeviceId()), responseManageLocationAndTC, this.mBaseFlowImpl.getDeviceId(), this.mSimSlot);
            }
            nSDSResponseStatus.responseCode = responseManageLocationAndTC.responseCode;
        }
        return nSDSResponseStatus;
    }

    private void handleLocAndTcWebsheetResult(Bundle bundle, boolean z) {
        int i = bundle != null ? bundle.getInt(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_RESULT_CODE) : 0;
        notifyCallbackForNsdsEvent(5, this.mSimSlot);
        String str = LOG_TAG;
        IMSLog.i(str, "handleLocAndTcWebsheetResult: result " + i);
        IMSLog.c(LogClass.ES_WEBSHEET_RESULT, "WBSHT RESULT:" + i);
        this.mDeviceEventType = z ? 7 : 12;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(getLocAndTcWebsheetRespCode(i), (String) null, -1), (Bundle) null);
    }

    private void retryEntitlementAutoOn(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, "[ATT_AutoOn] EntitlementAutoOn");
        String str2 = NSDSSharedPrefHelper.get(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS);
        if (TextUtils.equals(NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY, str2) || TextUtils.equals(NSDSNamespaces.VowifiAutoOnOperation.AUTOON_IN_PROGRESS, str2)) {
            NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
            if (i == 0 || !TextUtils.equals(NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY, str2)) {
                IMSLog.i(str, "[ATT_AutoOn] retry EntitlementAutoOn");
                NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY);
                return;
            }
            IMSLog.i(str, "[ATT_AutoOn] EntitlementAutoOn - fail : remove token");
            NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, NSDSNamespaces.VowifiAutoOnOperation.AUTOON_IN_PROGRESS);
            performRemovePushToken(3);
        }
    }

    private void failEntitlementAutoOn() {
        IMSLog.i(LOG_TAG, "[ATT_AutoOn] failEntitlementAutoOn - reset token in device");
        NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER);
        notifyNSDSFlowResponse(true, (String) null, -1, 1000);
    }

    public void performEntitlementCheck(int i, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, "performEntitlementCheck: deviceEventType " + i + " retryCount " + i2 + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(str, "performEntitlementCheck: entitlement in progress");
            deferMessage(obtainMessage(8, i, i2));
            return;
        }
        sendMessage(obtainMessage(8, i, i2));
    }

    public void performRemovePushToken(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, "performRemovePushToken: deviceEventType " + i + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(str, "performRemovePushToken: entitlement in progress");
            deferMessage(obtainMessage(9, i, 0));
            return;
        }
        sendMessage(obtainMessage(9, i, 0));
        clearDeferredMessage();
    }

    public void performE911AddressUpdate(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, "performE911AddressUpdate: deviceEventType " + i + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(str, "performE911AddressUpdate: entitlement in progress");
            deferMessage(obtainMessage(10, i, 0));
            return;
        }
        sendMessage(obtainMessage(10, i, 0));
    }

    private void performNextOperation(int i, int i2, String str) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "performNextOperation: deviceEventType " + i + " nsdsMethod " + str);
        NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE, NSDSNamespaces.NSDSDeviceState.ENTITLMENT_IN_PROGRESS);
        this.mDeviceEventType = i;
        this.mRetryCount = i2;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(1000, str, -1), getE911AidValidationBundle());
    }

    private void checkLocationAndTC() {
        String str = LOG_TAG;
        IMSLog.i(str, "checkLocationAndTC()");
        LineDetail nativeLineDetail = this.mNSDSDatabaseHelper.getNativeLineDetail(this.mBaseFlowImpl.getDeviceId(), true);
        if (nativeLineDetail == null) {
            IMSLog.e(str, "checkLocationAndTC: native line detail is null");
            NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE);
            notifyNSDSFlowResponse(false, (String) null, -1, -1);
            return;
        }
        new LocationRegistrationAndTCAcceptanceCheck(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").checkLocationAndTC(nativeLineDetail.serviceFingerPrint, 30000);
    }

    private void checkVoWifiEntitlement() {
        IMSLog.i(LOG_TAG, "checkVoWifiEntitlement: requesting entitlement check");
        ArrayList arrayList = new ArrayList();
        arrayList.add("vowifi");
        new BulkEntitlementCheck(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").checkBulkEntitlement(arrayList, false, 30000);
    }

    private void registerPushToken() {
        IMSLog.i(LOG_TAG, "registerPushToken: requesting push token registration");
        OperationUsingManagePushToken operationUsingManagePushToken = new OperationUsingManagePushToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0");
        OperationUsingManagePushToken operationUsingManagePushToken2 = operationUsingManagePushToken;
        operationUsingManagePushToken2.registerVoWiFiPushToken(this.mNSDSDatabaseHelper.getNativeMsisdn(this.mBaseFlowImpl.getDeviceId()), (String) null, PushTokenHelper.getPushToken(this.mContext, this.mBaseFlowImpl.getDeviceId()), NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM, 30000);
    }

    private void removePushToken() {
        IMSLog.i(LOG_TAG, "removePushToken: requesting push token de-registration");
        OperationUsingManagePushToken operationUsingManagePushToken = new OperationUsingManagePushToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0");
        OperationUsingManagePushToken operationUsingManagePushToken2 = operationUsingManagePushToken;
        operationUsingManagePushToken2.removeVoWiFiPushToken(this.mNSDSDatabaseHelper.getNativeMsisdn(this.mBaseFlowImpl.getDeviceId()), (String) null, PushTokenHelper.getPushToken(this.mContext, this.mBaseFlowImpl.getDeviceId()), NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM, 30000);
    }

    private void openLocAndTCWebsheet(boolean z) {
        Intent intent;
        LocAndTcWebSheetData locAndTcWebSheetData = getLocAndTcWebSheetData();
        if (locAndTcWebSheetData != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "openLocAndTCWebsheet: url " + locAndTcWebSheetData.url + ", serverData " + locAndTcWebSheetData.token + ", clientName " + locAndTcWebSheetData.clientName + ", title " + locAndTcWebSheetData.title);
            Bundle bundle = new Bundle();
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_URL, locAndTcWebSheetData.url);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_DATA, locAndTcWebSheetData.token);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_CLIENT_NAME, locAndTcWebSheetData.clientName);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_TITLE, locAndTcWebSheetData.title);
            bundle.putParcelable(NSDSNamespaces.NSDSExtras.LOC_AND_TC_WEBSHEET_RESULT_MESSAGE, obtainMessage(z ? 6 : 7));
            bundle.putParcelable(NSDSNamespaces.NSDSExtras.LOCATION_AND_TC_MESSENGER, new Messenger(this));
            Intent intent2 = new Intent();
            if (checkSntMode()) {
                intent = intent2.setAction(NSDSNamespaces.NSDSActions.SNT_MODE_LOCATIONANDTC_OPEN_WEBSHEET);
            } else {
                intent = intent2.setAction(NSDSNamespaces.NSDSActions.UNIFIED_WFC_LOCATIONANDTC_OPEN_WEBSHEET);
            }
            intent.putExtras(bundle);
            intent.setFlags(LogClass.SIM_EVENT);
            intent.setPackage(NSDSNamespaces.Packages.NSDS_WEBAPP);
            this.mContext.startActivity(intent);
            notifyCallbackForNsdsEvent(4, this.mSimSlot);
            return;
        }
        IMSLog.e(LOG_TAG, "openLocAndTCWebsheet: missing server info, failed");
        notifyNSDSFlowResponse(false, (String) null, -1, -1);
    }

    private boolean checkSntMode() {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            z = true;
        }
        return !z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0011, code lost:
        r1 = r4.tcStatus;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.os.Bundle getLocationAndTCStatusBundle(com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC r4) {
        /*
            r3 = this;
            android.os.Bundle r0 = new android.os.Bundle
            r0.<init>()
            if (r4 == 0) goto L_0x0054
            java.lang.Boolean r1 = r4.locationStatus
            if (r1 == 0) goto L_0x0011
            boolean r1 = r1.booleanValue()
            if (r1 == 0) goto L_0x001c
        L_0x0011:
            java.lang.Boolean r1 = r4.tcStatus
            if (r1 == 0) goto L_0x001e
            boolean r1 = r1.booleanValue()
            if (r1 == 0) goto L_0x001c
            goto L_0x001e
        L_0x001c:
            r1 = 0
            goto L_0x001f
        L_0x001e:
            r1 = 1
        L_0x001f:
            java.util.concurrent.atomic.AtomicBoolean r3 = r3.mOnSvcProv
            boolean r3 = r3.get()
            java.lang.String r2 = "svc_prov_status"
            r0.putBoolean(r2, r3)
            java.lang.String r3 = "loc_and_tc_status"
            r0.putBoolean(r3, r1)
            java.lang.String r3 = "loc_and_tc_server_url"
            java.lang.String r2 = r4.serverUrl
            r0.putString(r3, r2)
            java.lang.String r3 = "loc_and_tc_server_data"
            java.lang.String r4 = r4.serverData
            r0.putString(r3, r4)
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r2 = "getLocationAndTCStatusBundle: "
            r4.append(r2)
            r4.append(r1)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r3, r4)
        L_0x0054:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement.EntitlementAndE911AidCheckFlow.getLocationAndTCStatusBundle(com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC):android.os.Bundle");
    }

    private Bundle getE911AidValidationBundle() {
        String nativeLineE911AidExp = this.mNSDSDatabaseHelper.getNativeLineE911AidExp(this.mBaseFlowImpl.getDeviceId());
        Bundle bundle = new Bundle();
        bundle.putString(NSDSNamespaces.NSDSDataMapKey.E911_AID_EXP, nativeLineE911AidExp);
        bundle.putBoolean(NSDSNamespaces.NSDSDataMapKey.SVC_PROV_STATUS, this.mOnSvcProv.get());
        String str = LOG_TAG;
        IMSLog.s(str, "getE911AidValidationBundle: " + nativeLineE911AidExp + ", OnSvcProv:" + this.mOnSvcProv.get());
        return bundle;
    }

    private LocAndTcWebSheetData getLocAndTcWebSheetData() {
        if (getMnoNsdsStrategy() != null) {
            return getMnoNsdsStrategy().getLocAndTcWebSheetData(this.mServerUrl, this.mServerData);
        }
        return null;
    }

    private void updateEntitlementStatus(int i) {
        this.mDeviceEventType = 0;
        this.mOnSvcProv.set(false);
        this.mRetryCount = 0;
        NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
        if (i == 2303) {
            this.mNSDSDatabaseHelper.resetE911AidInfoForNativeLine(this.mBaseFlowImpl.getDeviceId());
            IMSLog.i(LOG_TAG, "updateEntitlementStatus: svc de-provision success");
            NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_SVC_PROV_STATE);
        }
        if (i == 2302 || i == 2502) {
            clearDeferredMessage();
            IMSLog.i(LOG_TAG, "updateEntitlementStatus: svc provision success");
            NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_SVC_PROV_STATE, NSDSNamespaces.NSDSDeviceState.SERVICE_PROVISIONED);
            return;
        }
        moveDeferredMessageAtFrontOfQueue();
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int i, Bundle bundle) {
        int i2 = 2;
        if (i != 2) {
            int i3 = 3;
            if (i == 3) {
                i2 = 0;
            } else if (i != 4) {
                i2 = 5;
                if (i != 5) {
                    if (i != 8) {
                        i3 = 13;
                        if (i != 13) {
                            switch (i) {
                                case 17:
                                    i2 = 11;
                                    break;
                                case 18:
                                    i2 = 12;
                                    break;
                                case 19:
                                    break;
                                case 20:
                                    i2 = 14;
                                    break;
                                default:
                                    IMSLog.i(LOG_TAG, "queueOperation: did not match any nsds base operations");
                                    i2 = -1;
                                    break;
                            }
                        }
                    } else {
                        i2 = 4;
                    }
                }
                i2 = i3;
            }
        } else {
            i2 = 1;
        }
        if (i2 != -1) {
            Message obtainMessage = obtainMessage(i2);
            obtainMessage.setData(bundle);
            sendMessage(obtainMessage);
        }
    }

    /* JADX WARNING: type inference failed for: r7v11, types: [android.os.Parcelable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r7) {
        /*
            r6 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "msg:"
            r1.append(r2)
            int r2 = r7.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r1)
            int r1 = r7.what
            r2 = 101(0x65, float:1.42E-43)
            r3 = 2
            if (r1 == r2) goto L_0x00fd
            r2 = 104(0x68, float:1.46E-43)
            r4 = 0
            java.lang.String r5 = "manageLocationAndTC"
            if (r1 == r2) goto L_0x00b7
            r0 = 112(0x70, float:1.57E-43)
            if (r1 == r0) goto L_0x00aa
            r0 = 113(0x71, float:1.58E-43)
            if (r1 == r0) goto L_0x0099
            r0 = 0
            r2 = 1
            switch(r1) {
                case 0: goto L_0x0094;
                case 1: goto L_0x008f;
                case 2: goto L_0x008a;
                case 3: goto L_0x0085;
                case 4: goto L_0x0080;
                case 5: goto L_0x007b;
                case 6: goto L_0x0072;
                case 7: goto L_0x0069;
                case 8: goto L_0x005d;
                case 9: goto L_0x0052;
                case 10: goto L_0x0049;
                case 11: goto L_0x0044;
                case 12: goto L_0x003f;
                case 13: goto L_0x003a;
                case 14: goto L_0x0035;
                default: goto L_0x0033;
            }
        L_0x0033:
            goto L_0x010c
        L_0x0035:
            r6.failEntitlementAutoOn()
            goto L_0x010c
        L_0x003a:
            r6.retryEntitlementAutoOn(r3)
            goto L_0x010c
        L_0x003f:
            r6.retryEntitlementAutoOn(r2)
            goto L_0x010c
        L_0x0044:
            r6.retryEntitlementAutoOn(r0)
            goto L_0x010c
        L_0x0049:
            int r0 = r7.arg1
            int r7 = r7.arg2
            r6.performNextOperation(r0, r7, r5)
            goto L_0x010c
        L_0x0052:
            int r0 = r7.arg1
            int r7 = r7.arg2
            java.lang.String r1 = "managePushToken"
            r6.performNextOperation(r0, r7, r1)
            goto L_0x010c
        L_0x005d:
            int r0 = r7.arg1
            int r7 = r7.arg2
            java.lang.String r1 = "serviceEntitlementStatus"
            r6.performNextOperation(r0, r7, r1)
            goto L_0x010c
        L_0x0069:
            android.os.Bundle r7 = r7.getData()
            r6.handleLocAndTcWebsheetResult(r7, r0)
            goto L_0x010c
        L_0x0072:
            android.os.Bundle r7 = r7.getData()
            r6.handleLocAndTcWebsheetResult(r7, r2)
            goto L_0x010c
        L_0x007b:
            r6.openLocAndTCWebsheet(r0)
            goto L_0x010c
        L_0x0080:
            r6.openLocAndTCWebsheet(r2)
            goto L_0x010c
        L_0x0085:
            r6.removePushToken()
            goto L_0x010c
        L_0x008a:
            r6.registerPushToken()
            goto L_0x010c
        L_0x008f:
            r6.checkVoWifiEntitlement()
            goto L_0x010c
        L_0x0094:
            r6.checkLocationAndTC()
            goto L_0x010c
        L_0x0099:
            android.os.Bundle r0 = r7.getData()
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r0 = r6.handleRemovePushTokenResponse(r0)
            android.os.Bundle r7 = r7.getData()
            r1 = 5
            r6.performNextOperationIf(r1, r0, r7)
            goto L_0x010c
        L_0x00aa:
            android.os.Bundle r7 = r7.getData()
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r7 = r6.handleRegisterPushTokenResponse(r7)
            r0 = 4
            r6.performNextOperationIf(r0, r7, r4)
            goto L_0x010c
        L_0x00b7:
            android.os.Bundle r1 = r7.getData()
            if (r1 == 0) goto L_0x00c8
            android.os.Bundle r7 = r7.getData()
            android.os.Parcelable r7 = r7.getParcelable(r5)
            r4 = r7
            com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC r4 = (com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC) r4
        L_0x00c8:
            android.content.Context r7 = r6.mContext
            com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl r1 = r6.mBaseFlowImpl
            java.lang.String r1 = r1.getDeviceId()
            java.lang.String r2 = "activate_after_oos"
            java.lang.String r7 = com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.get(r7, r1, r2)
            int r1 = r6.mSimSlot
            boolean r1 = com.sec.internal.ims.entitlement.util.EntFeatureDetector.checkWFCAutoOnEnabled(r1)
            if (r1 == 0) goto L_0x00f0
            if (r7 == 0) goto L_0x00f0
            java.lang.String r1 = "completed"
            boolean r7 = r1.equals(r7)
            if (r7 != 0) goto L_0x00f0
            java.lang.String r7 = "[ATT_AutoOn] InProgress - CHECK_LOC_AND_TC_AUTO_ON"
            com.sec.internal.log.IMSLog.i(r0, r7)
            r7 = 16
            goto L_0x00f1
        L_0x00f0:
            r7 = 3
        L_0x00f1:
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r0 = r6.handleManageLocationAndTcResponse(r4)
            android.os.Bundle r1 = r6.getLocationAndTCStatusBundle(r4)
            r6.performNextOperationIf(r7, r0, r1)
            goto L_0x010c
        L_0x00fd:
            android.os.Bundle r0 = r6.getE911AidValidationBundle()
            android.os.Bundle r7 = r7.getData()
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r7 = r6.handleEntitlementCheckResponse(r7)
            r6.performNextOperationIf(r3, r7, r0)
        L_0x010c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement.EntitlementAndE911AidCheckFlow.handleMessage(android.os.Message):void");
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean z, String str, int i, int i2) {
        ArrayList arrayList = new ArrayList();
        int translateErrorCode = ATTWfcErrorCodeTranslator.translateErrorCode(this.mNSDSDatabaseHelper, this.mDeviceEventType, z, i2, this.mRetryCount, this.mBaseFlowImpl.getDeviceId());
        arrayList.add(Integer.valueOf(translateErrorCode));
        boolean updateResponseResult = updateResponseResult(z, translateErrorCode);
        String str2 = LOG_TAG;
        IMSLog.i(str2, "notifyNSDSFlowResponse: success " + updateResponseResult);
        IMSLog.c(LogClass.ES_NSDS_RESULT, "SUCS:" + updateResponseResult + ", ERRC:" + arrayList);
        if (2304 != translateErrorCode) {
            Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mSimSlot);
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, updateResponseResult);
            intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQ_TOGGLE_OFF_OP, false);
            intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        }
        updateEntitlementStatus(translateErrorCode);
    }
}
