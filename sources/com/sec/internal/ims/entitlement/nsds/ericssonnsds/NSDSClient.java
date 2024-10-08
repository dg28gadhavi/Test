package com.sec.internal.ims.entitlement.nsds.ericssonnsds;

import android.content.Context;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.Request3gppAuthentication;
import com.sec.internal.constants.ims.entitilement.data.RequestGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.RequestManageConnectivity;
import com.sec.internal.constants.ims.entitilement.data.RequestManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.RequestManagePushToken;
import com.sec.internal.constants.ims.entitilement.data.RequestRegisteredMSISDN;
import com.sec.internal.constants.ims.entitilement.data.RequestServiceEntitlementStatus;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.ims.entitlement.util.DeviceNameHelper;
import com.sec.internal.ims.entitlement.util.HttpHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.SocketFactory;
import okhttp3.Dns;
import org.json.JSONArray;
import org.json.JSONException;

public class NSDSClient {
    private static final String DEVICE_ID = "device_id";
    private static final String HEADER_X_GENERIC_PROTOCOL_VERSION = "x-generic-protocol-version";
    private static final String LOG_TAG = "NSDSClient";
    private static final String X_GEN_PTC_VER = "1.0";
    private static Map<String, String> sNSDSHeaders = new ConcurrentHashMap();
    private static Looper sResponseLooper;
    private Integer mAreacode;
    private String mClientversion = null;
    private String mConfigversion = null;
    private final Context mContext;
    private Dns mDns = null;
    private String mMsisdn = null;
    private String mRequestUrl = null;
    private NSDSResponseHandler mResponseHandler;
    private ISimManager mSimManager;
    private SocketFactory mSocketFactory = null;
    private Integer mTriggercode = 1;

    private int getDeviceType() {
        return 0;
    }

    static {
        initNsdsCommonHeaders();
        initNsdsResponseLooper();
    }

    private static void initNsdsCommonHeaders() {
        sNSDSHeaders.put("Content-Type", "application/json");
        sNSDSHeaders.put("Content-Encoding", "gzip");
        sNSDSHeaders.put("Accept", "application/json");
        sNSDSHeaders.put("Accept-Encoding", "gzip");
        sNSDSHeaders.put(HEADER_X_GENERIC_PROTOCOL_VERSION, "1.0");
    }

    private static void initNsdsResponseLooper() {
        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
        sResponseLooper = handlerThread.getLooper();
    }

    public NSDSClient(Context context, ISimManager iSimManager) {
        this.mContext = context;
        this.mSimManager = iSimManager;
        this.mResponseHandler = new NSDSResponseHandler(sResponseLooper, context);
    }

    public String getDisplayName() {
        return DeviceNameHelper.getDeviceName(this.mContext);
    }

    public NSDSResponseHandler getResponseHandler() {
        return this.mResponseHandler;
    }

    public void executeRequestCollection(NSDSRequest[] nSDSRequestArr, Message message, String str, String str2, String str3) {
        JSONArray buildJSONArrayFromRequests = buildJSONArrayFromRequests(nSDSRequestArr);
        if (buildJSONArrayFromRequests != null) {
            Bundle buildMessageIdMethodBundle = buildMessageIdMethodBundle(nSDSRequestArr);
            new HttpHelper().executeNSDSRequest(getEntitlementServerUrl(str2, str3), buildNSDSRequestHeaders(str), buildJSONArrayFromRequests, this.mResponseHandler.obtainParseResponseMessage(message, buildMessageIdMethodBundle), this.mSocketFactory, this.mDns);
            return;
        }
        IMSLog.e(LOG_TAG, "executeRequestCollection: requestJsonArray is null");
        message.obj = null;
        message.sendToTarget();
    }

    public void executeRequestCollection(NSDSRequest[] nSDSRequestArr, Message message, String str, String str2, String str3, String str4, String str5) {
        JSONArray buildJSONArrayFromRequests = buildJSONArrayFromRequests(nSDSRequestArr);
        if (buildJSONArrayFromRequests != null) {
            Bundle buildMessageIdMethodBundle = buildMessageIdMethodBundle(nSDSRequestArr);
            HttpHelper httpHelper = new HttpHelper();
            Map<String, String> buildNSDSRequestHeaders = buildNSDSRequestHeaders(str, str2, str3);
            httpHelper.executeNSDSRequest(getEntitlementServerUrl(str5, str4), buildNSDSRequestHeaders, buildJSONArrayFromRequests, this.mResponseHandler.obtainParseResponseMessage(message, buildMessageIdMethodBundle), this.mSocketFactory, this.mDns);
            return;
        }
        IMSLog.e(LOG_TAG, "executeRequestCollection: requestJsonArray is null");
        message.obj = null;
        message.sendToTarget();
    }

    private Bundle buildMessageIdMethodBundle(NSDSRequest[] nSDSRequestArr) {
        Bundle bundle = new Bundle();
        for (NSDSRequest nSDSRequest : nSDSRequestArr) {
            bundle.putString(String.valueOf(nSDSRequest.messageId), nSDSRequest.method);
        }
        return bundle;
    }

    private String getEntitlementServerUrl(String str, String str2) {
        if (!TextUtils.isEmpty(this.mRequestUrl)) {
            return this.mRequestUrl;
        }
        IMnoNsdsStrategy mnoStrategy = MnoNsdsStrategyCreator.getInstance(this.mContext, this.mSimManager.getSimSlotIndex()).getMnoStrategy();
        if (mnoStrategy != null) {
            return mnoStrategy.getEntitlementServerUrl(str, str2);
        }
        IMSLog.e(LOG_TAG, "getEntitlementServerUrl: mnoStrategy is null");
        return null;
    }

    private Map<String, String> buildNSDSRequestHeaders(String str) {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "buildNSDSRequestHeaders: version " + str);
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        concurrentHashMap.putAll(sNSDSHeaders);
        concurrentHashMap.put(HEADER_X_GENERIC_PROTOCOL_VERSION, str);
        return concurrentHashMap;
    }

    private Map<String, String> buildNSDSRequestHeaders(String str, String str2, String str3) {
        String str4 = LOG_TAG;
        IMSLog.s(str4, "buildNSDSRequestHeaders: version " + str + " imei " + str3 + "userAgent " + str2);
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        concurrentHashMap.putAll(sNSDSHeaders);
        concurrentHashMap.put(HEADER_X_GENERIC_PROTOCOL_VERSION, str);
        if (str3 != null) {
            concurrentHashMap.put("device_id", str3);
        }
        if (str2 != null) {
            concurrentHashMap.put("User-Agent", str2);
        }
        return concurrentHashMap;
    }

    private JSONArray buildJSONArrayFromRequests(NSDSRequest[] nSDSRequestArr) {
        JSONArray jSONArray = null;
        try {
            JSONArray jSONArray2 = new JSONArray(new Gson().toJson(nSDSRequestArr));
            try {
                String str = LOG_TAG;
                IMSLog.s(str, "buildJSONArrayFromRequests:" + jSONArray2.toString());
                return jSONArray2;
            } catch (JSONException e) {
                e = e;
                jSONArray = jSONArray2;
            }
        } catch (JSONException e2) {
            e = e2;
            String str2 = LOG_TAG;
            IMSLog.s(str2, "could not buld JSONArrayRequests:" + e.getMessage());
            return jSONArray;
        }
    }

    public Request3gppAuthentication buildAuthenticationRequest(int i, boolean z, String str, String str2, String str3, String str4, String str5) {
        Request3gppAuthentication request3gppAuthentication = new Request3gppAuthentication();
        request3gppAuthentication.messageId = i;
        request3gppAuthentication.method = NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH;
        request3gppAuthentication.deviceId = str5;
        request3gppAuthentication.deviceType = getDeviceType();
        request3gppAuthentication.osType = 0;
        if (!TextUtils.isEmpty(str3)) {
            request3gppAuthentication.deviceName = str3;
        } else {
            request3gppAuthentication.deviceName = getDisplayName();
        }
        if (z) {
            request3gppAuthentication.imsiEap = str4;
            String str6 = LOG_TAG;
            IMSLog.s(str6, "buildAuthenticationRequest getimsi: " + request3gppAuthentication.imsiEap);
        }
        String str7 = LOG_TAG;
        IMSLog.s(str7, "buildAuthenticationRequest imsi: " + request3gppAuthentication.imsiEap);
        request3gppAuthentication.akaToken = str2;
        request3gppAuthentication.akaChallengeRsp = str;
        return request3gppAuthentication;
    }

    public RequestManageConnectivity buildManageConnectivityRequest(int i, int i2, String str, String str2, String str3, String str4, String str5) {
        RequestManageConnectivity requestManageConnectivity = new RequestManageConnectivity();
        requestManageConnectivity.messageId = i;
        requestManageConnectivity.method = NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY;
        requestManageConnectivity.deviceId = str5;
        requestManageConnectivity.operation = i2;
        requestManageConnectivity.vimsi = str;
        requestManageConnectivity.remoteDeviceId = str2;
        requestManageConnectivity.deviceGroup = str3;
        requestManageConnectivity.csr = str4;
        if (!TextUtils.isEmpty(this.mClientversion)) {
            RequestManageConnectivity.DeviceParameter deviceParameter = new RequestManageConnectivity.DeviceParameter();
            deviceParameter.msisdn = this.mMsisdn;
            deviceParameter.clientversion = this.mClientversion;
            deviceParameter.configversion = this.mConfigversion;
            deviceParameter.triggercode = this.mTriggercode;
            deviceParameter.areacode = this.mAreacode;
            requestManageConnectivity.deviceParameterInfo = deviceParameter;
        }
        return requestManageConnectivity;
    }

    public RequestManagePushToken buildManagePushTokenRequest(int i, String str, String str2, String str3, int i2, String str4, String str5) {
        RequestManagePushToken requestManagePushToken = new RequestManagePushToken();
        requestManagePushToken.messageId = i;
        requestManagePushToken.method = "managePushToken";
        requestManagePushToken.deviceId = str5;
        requestManagePushToken.msisdn = str;
        requestManagePushToken.serviceName = str2;
        requestManagePushToken.clientId = str3;
        requestManagePushToken.operation = i2;
        requestManagePushToken.pushToken = str4;
        return requestManagePushToken;
    }

    public RequestGetMSISDN buildGetMSISDNRequest(int i, String str) {
        RequestGetMSISDN requestGetMSISDN = new RequestGetMSISDN();
        requestGetMSISDN.messageId = i;
        requestGetMSISDN.method = NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN;
        requestGetMSISDN.deviceId = str;
        return requestGetMSISDN;
    }

    public RequestRegisteredMSISDN buildRegisteredMSISDNRequest(int i, String str, int i2, Boolean bool, String str2) {
        RequestRegisteredMSISDN requestRegisteredMSISDN = new RequestRegisteredMSISDN();
        requestRegisteredMSISDN.messageId = i;
        requestRegisteredMSISDN.method = NSDSNamespaces.NSDSMethodNamespace.REGISTERED_MSISDN;
        requestRegisteredMSISDN.deviceId = str2;
        requestRegisteredMSISDN.operation = i2;
        requestRegisteredMSISDN.serviceName = str;
        requestRegisteredMSISDN.isAvailable = bool;
        return requestRegisteredMSISDN;
    }

    public RequestManageLocationAndTC buildManageLocationAndTCRequest(int i, String str, String str2) {
        RequestManageLocationAndTC requestManageLocationAndTC = new RequestManageLocationAndTC();
        requestManageLocationAndTC.messageId = i;
        requestManageLocationAndTC.method = NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC;
        requestManageLocationAndTC.deviceId = str2;
        requestManageLocationAndTC.serviceFingerprint = str;
        return requestManageLocationAndTC;
    }

    public RequestServiceEntitlementStatus buildServiceEntitlementStatusRequest(int i, ArrayList<String> arrayList, String str) {
        RequestServiceEntitlementStatus requestServiceEntitlementStatus = new RequestServiceEntitlementStatus();
        requestServiceEntitlementStatus.messageId = i;
        requestServiceEntitlementStatus.method = NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS;
        requestServiceEntitlementStatus.deviceId = str;
        requestServiceEntitlementStatus.serviceList = arrayList;
        return requestServiceEntitlementStatus;
    }

    public void setRequestUrl(String str) {
        this.mRequestUrl = str;
    }

    public void setDeviceParameter(String str, String str2, String str3, int i, int i2) {
        if (str == null) {
            str = this.mMsisdn;
        }
        this.mMsisdn = str;
        if (str2 == null) {
            str2 = this.mClientversion;
        }
        this.mClientversion = str2;
        if (str3 == null) {
            str3 = this.mConfigversion;
        }
        this.mConfigversion = str3;
        this.mAreacode = Integer.valueOf(i);
        if (i2 > -1) {
            this.mTriggercode = Integer.valueOf(i2);
        }
    }

    public void setNetwork(Dns dns, SocketFactory socketFactory) {
        this.mDns = dns;
        this.mSocketFactory = socketFactory;
    }
}
