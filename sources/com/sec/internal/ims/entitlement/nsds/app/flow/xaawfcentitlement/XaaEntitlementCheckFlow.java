package com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LineDetail;
import com.sec.internal.constants.ims.entitilement.data.LocAndTcWebSheetData;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus;
import com.sec.internal.constants.ims.entitilement.data.ServiceEntitlement;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BulkEntitlementCheck;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.LocationRegistrationAndTCAcceptanceCheck;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;

public class XaaEntitlementCheckFlow extends NSDSAppFlowBase implements IEntitlementCheck {
    private static final int INIT_E911_ADDRESS_UPDATE = 7;
    private static final int INIT_ENTITLEMENT_CHECK = 6;
    protected static final int LOCATION_AND_TC_CHECK = 0;
    private static final String LOG_TAG = XaaEntitlementCheckFlow.class.getSimpleName();
    protected static final int OPEN_E911_ADDRESS_UPDATE_WEBSHEET = 3;
    protected static final int OPEN_LOC_AND_TC_WEBSHEET = 2;
    protected static final int RESULT_SVC_PROV_LOC_AND_TC_WEBSHEET = 4;
    protected static final int RESULT_UPDATE_LOC_AND_TC_WEBSHEET = 5;
    protected static final int VOWIFI_ENTITLEMENT_CHECK = 1;
    protected String mServerData;
    protected String mServerUrl;

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
        return z && (i == 1000 || i == 2303 || i == 2501 || i == 2502 || i == 2302);
    }

    public XaaEntitlementCheckFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
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
        if (responseServiceEntitlementStatus != null) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "ResponseServiceEntitlementStatus :messageId:" + responseServiceEntitlementStatus.messageId + "responseCode:" + responseServiceEntitlementStatus.responseCode);
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
                        IMSLog.i(str3, "service responseCode:" + serviceEntitlement.entitlementStatus);
                        nSDSResponseStatus.responseCode = serviceEntitlement.entitlementStatus;
                        break;
                    }
                }
            } else {
                nSDSResponseStatus.responseCode = i;
            }
        }
        return nSDSResponseStatus;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleManageLocationAndTcResponse(ResponseManageLocationAndTC responseManageLocationAndTC) {
        NSDSAppFlowBase.NSDSResponseStatus nSDSResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC, -1);
        if (responseManageLocationAndTC != null) {
            if (responseManageLocationAndTC.responseCode == 1000) {
                this.mServerData = responseManageLocationAndTC.serverData;
                this.mServerUrl = responseManageLocationAndTC.serverUrl;
                IMSLog.i(LOG_TAG, "onResponseAvailable: update location and tc status in db");
                NSDSDatabaseHelper nSDSDatabaseHelper = this.mNSDSDatabaseHelper;
                nSDSDatabaseHelper.updateLocationAndTcStatus((long) nSDSDatabaseHelper.getNativeLineId(this.mBaseFlowImpl.getDeviceId()), responseManageLocationAndTC, this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
            }
            nSDSResponseStatus.responseCode = responseManageLocationAndTC.responseCode;
        }
        return nSDSResponseStatus;
    }

    private void handleLocAndTcWebsheetResult(Bundle bundle, boolean z) {
        int i = bundle != null ? bundle.getInt(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_RESULT_CODE) : 0;
        notifyCallbackForNsdsEvent(5, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        String str = LOG_TAG;
        IMSLog.i(str, "handleLocAndTcWebsheetResult: result " + i);
        this.mDeviceEventType = z ? 7 : 12;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(getLocAndTcWebsheetRespCode(i), (String) null, -1), (Bundle) null);
    }

    public void performEntitlementCheck(int i, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, "performEntitlementCheck: deviceEventType " + i + " retryCount " + i2 + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(str, "performEntitlementCheck: entitlement in progress");
            deferMessage(obtainMessage(6, i, i2));
            return;
        }
        sendMessage(obtainMessage(6, i, i2));
    }

    public void performRemovePushToken(int i) {
        IMSLog.e(LOG_TAG, "performRemovePushToken: not supported");
        notifyNSDSFlowResponse(true, (String) null, -1, -1);
    }

    public void performE911AddressUpdate(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, "performE911AddressUpdate: deviceEventType " + i + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(str, "performE911AddressUpdate: entitlement in progress");
            deferMessage(obtainMessage(7, i, 0));
            return;
        }
        sendMessage(obtainMessage(7, i, 0));
    }

    private void performNextOperation(int i, int i2, String str) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "performNextOperation: deviceEventType " + i + " nsdsMethod " + str);
        NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE, NSDSNamespaces.NSDSDeviceState.ENTITLMENT_IN_PROGRESS);
        this.mDeviceEventType = i;
        this.mRetryCount = i2;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(1000, str, -1), (Bundle) null);
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
        new BulkEntitlementCheck(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").checkBulkEntitlement(arrayList, true, 30000);
    }

    private void openLocAndTCWebsheet(boolean z) {
        Intent intent;
        LocAndTcWebSheetData locAndTcWebSheetData = getLocAndTcWebSheetData();
        if (locAndTcWebSheetData != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "openLocAndTCWebsheet: url " + locAndTcWebSheetData.url + "serverData " + locAndTcWebSheetData.token + "clientName " + locAndTcWebSheetData.clientName + "title " + locAndTcWebSheetData.title);
            Bundle bundle = new Bundle();
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_URL, locAndTcWebSheetData.url);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_DATA, locAndTcWebSheetData.token);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_CLIENT_NAME, locAndTcWebSheetData.clientName);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_TITLE, locAndTcWebSheetData.title);
            bundle.putParcelable(NSDSNamespaces.NSDSExtras.LOC_AND_TC_WEBSHEET_RESULT_MESSAGE, obtainMessage(z ? 4 : 5));
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
            notifyCallbackForNsdsEvent(4, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
            return;
        }
        IMSLog.e(LOG_TAG, "openLocAndTCWebsheet: missing server info, failed");
        notifyNSDSFlowResponse(false, (String) null, -1, -1);
    }

    private boolean checkSntMode() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0011, code lost:
        r0 = r4.tcStatus;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.os.Bundle getLocationAndTCStatusBundle(com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC r4) {
        /*
            r3 = this;
            android.os.Bundle r3 = new android.os.Bundle
            r3.<init>()
            if (r4 == 0) goto L_0x0048
            java.lang.Boolean r0 = r4.locationStatus
            if (r0 == 0) goto L_0x0011
            boolean r0 = r0.booleanValue()
            if (r0 == 0) goto L_0x001c
        L_0x0011:
            java.lang.Boolean r0 = r4.tcStatus
            if (r0 == 0) goto L_0x001e
            boolean r0 = r0.booleanValue()
            if (r0 == 0) goto L_0x001c
            goto L_0x001e
        L_0x001c:
            r0 = 0
            goto L_0x001f
        L_0x001e:
            r0 = 1
        L_0x001f:
            java.lang.String r1 = "loc_and_tc_status"
            r3.putBoolean(r1, r0)
            java.lang.String r1 = "loc_and_tc_server_url"
            java.lang.String r2 = r4.serverUrl
            r3.putString(r1, r2)
            java.lang.String r1 = "loc_and_tc_server_data"
            java.lang.String r4 = r4.serverData
            r3.putString(r1, r4)
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "getLocationAndTCStatusBundle: "
            r1.append(r2)
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            com.sec.internal.log.IMSLog.i(r4, r0)
        L_0x0048:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement.XaaEntitlementCheckFlow.getLocationAndTCStatusBundle(com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC):android.os.Bundle");
    }

    private LocAndTcWebSheetData getLocAndTcWebSheetData() {
        if (getMnoNsdsStrategy() != null) {
            return getMnoNsdsStrategy().getLocAndTcWebSheetData(this.mServerUrl, this.mServerData);
        }
        return null;
    }

    private void updateEntitlementStatus(int i) {
        this.mDeviceEventType = 0;
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
        if (i == 2) {
            i2 = 1;
        } else if (i == 3) {
            i2 = 0;
        } else if (i != 8) {
            if (i != 13) {
                IMSLog.i(LOG_TAG, "queueOperation: did not match any nsds base operations");
                i2 = -1;
            } else {
                i2 = 3;
            }
        }
        if (i2 != -1) {
            Message obtainMessage = obtainMessage(i2);
            obtainMessage.setData(bundle);
            sendMessage(obtainMessage);
        }
    }

    /* JADX WARNING: type inference failed for: r5v5, types: [android.os.Parcelable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r5) {
        /*
            r4 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "msg:"
            r1.append(r2)
            int r2 = r5.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r1)
            int r0 = r5.what
            r1 = 101(0x65, float:1.42E-43)
            r2 = 0
            if (r0 == r1) goto L_0x007c
            r1 = 104(0x68, float:1.46E-43)
            java.lang.String r3 = "manageLocationAndTC"
            if (r0 == r1) goto L_0x005e
            r1 = 0
            r2 = 1
            switch(r0) {
                case 0: goto L_0x005a;
                case 1: goto L_0x0056;
                case 2: goto L_0x0052;
                case 3: goto L_0x004e;
                case 4: goto L_0x0046;
                case 5: goto L_0x003e;
                case 6: goto L_0x0033;
                case 7: goto L_0x002b;
                default: goto L_0x002a;
            }
        L_0x002a:
            goto L_0x0088
        L_0x002b:
            int r0 = r5.arg1
            int r5 = r5.arg2
            r4.performNextOperation(r0, r5, r3)
            goto L_0x0088
        L_0x0033:
            int r0 = r5.arg1
            int r5 = r5.arg2
            java.lang.String r1 = "serviceEntitlementStatus"
            r4.performNextOperation(r0, r5, r1)
            goto L_0x0088
        L_0x003e:
            android.os.Bundle r5 = r5.getData()
            r4.handleLocAndTcWebsheetResult(r5, r1)
            goto L_0x0088
        L_0x0046:
            android.os.Bundle r5 = r5.getData()
            r4.handleLocAndTcWebsheetResult(r5, r2)
            goto L_0x0088
        L_0x004e:
            r4.openLocAndTCWebsheet(r1)
            goto L_0x0088
        L_0x0052:
            r4.openLocAndTCWebsheet(r2)
            goto L_0x0088
        L_0x0056:
            r4.checkVoWifiEntitlement()
            goto L_0x0088
        L_0x005a:
            r4.checkLocationAndTC()
            goto L_0x0088
        L_0x005e:
            android.os.Bundle r0 = r5.getData()
            if (r0 == 0) goto L_0x006f
            android.os.Bundle r5 = r5.getData()
            android.os.Parcelable r5 = r5.getParcelable(r3)
            r2 = r5
            com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC r2 = (com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC) r2
        L_0x006f:
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r5 = r4.handleManageLocationAndTcResponse(r2)
            android.os.Bundle r0 = r4.getLocationAndTCStatusBundle(r2)
            r1 = 3
            r4.performNextOperationIf(r1, r5, r0)
            goto L_0x0088
        L_0x007c:
            android.os.Bundle r5 = r5.getData()
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r5 = r4.handleEntitlementCheckResponse(r5)
            r0 = 2
            r4.performNextOperationIf(r0, r5, r2)
        L_0x0088:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement.XaaEntitlementCheckFlow.handleMessage(android.os.Message):void");
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean z, String str, int i, int i2) {
        ArrayList arrayList = new ArrayList();
        int translateErrorCode = XaaWfcErrorCodeTranslator.translateErrorCode(this.mDeviceEventType, z, i2);
        arrayList.add(Integer.valueOf(translateErrorCode));
        boolean updateResponseResult = updateResponseResult(z, translateErrorCode);
        String str2 = LOG_TAG;
        IMSLog.i(str2, "notifyNSDSFlowResponse: success " + updateResponseResult);
        if (2304 != translateErrorCode) {
            Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, updateResponseResult);
            intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQ_TOGGLE_OFF_OP, true);
            intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, arrayList);
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        }
        updateEntitlementStatus(translateErrorCode);
    }
}
