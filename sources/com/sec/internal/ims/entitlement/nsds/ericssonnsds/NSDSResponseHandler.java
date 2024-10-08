package com.sec.internal.ims.entitlement.nsds.ericssonnsds;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageService;
import com.sec.internal.constants.ims.entitilement.data.ResponseRegisteredDevices;
import com.sec.internal.constants.ims.entitilement.data.ResponseRegisteredMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NSDSResponseHandler extends Handler {
    private static final String KEY_CALLBACK = "callback";
    private static final String KEY_MESSAGE_ID_METHOD_BUNDLE = "messageIdMethodBundle";
    private static final String LOG_TAG = NSDSResponseHandler.class.getSimpleName();
    private static final int METHOD_ID_GET_MSISDN = 3;
    private static final int METHOD_ID_MANAGE_CONNECTIVITY = 2;
    private static final int METHOD_ID_MANAGE_LOC_AND_TC = 5;
    private static final int METHOD_ID_MANAGE_PUSH_TOKEN = 4;
    private static final int METHOD_ID_MANAGE_SERVICE = 6;
    private static final int METHOD_ID_REGISTERED_DEVICES = 8;
    private static final int METHOD_ID_REGISTERED_MSISDN = 7;
    private static final int METHOD_ID_REQ_3GPP_AUTH = 1;
    private static final int METHOD_ID_SERVICE_ENTITLEMENT_STATUS = 9;
    private static final int PARSE_REPSONSE = 0;
    private static final Map<String, Integer> sMapNSDSMethods;
    private Context mContext;
    private SimpleEventLog mEventLog = null;

    private String toString(int i) {
        if (i == 1000) {
            return "REQUEST_SUCCESSFUL";
        }
        if (i == 1010) {
            return "ERROR_MAX_DEVICE_REACHED";
        }
        if (i == 1080) {
            return "ERROR_INVALID_PUSH_TOKEN";
        }
        if (i == 1500) {
            return "ERROR_REQUEST_ONGOING";
        }
        if (i == 9999) {
            return "ERROR_UNSUPPORTED_OPERATION";
        }
        if (i == 1028) {
            return "ERROR_DEVICE_LOCKED";
        }
        if (i == 1029) {
            return "ERROR_INVALID_DEVICE_STATUS";
        }
        if (i == 1048) {
            return "ERROR_SERVICE_NOT_ENTITLED";
        }
        if (i == 1049) {
            return "ERROR_SERVICE_NOT_PERMITTED";
        }
        if (i == 1053) {
            return "ERROR_INVALID_SERVICE_INSTANCEID";
        }
        if (i == 1054) {
            return "ERROR_INVALID_DEVICE_GROUP";
        }
        if (i == 1060) {
            return "ERROR_NO_MSISDN_FOUND";
        }
        if (i == 1061) {
            return "ERROR_CREATION_FAILURE";
        }
        if (i == 1111) {
            return "ERROR_SERVER_ERROR";
        }
        if (i == 1112) {
            return "ERROR_3GPP_AUTH_ONGOING";
        }
        switch (i) {
            case 1003:
                return "REQUEST_AKA_CHALLENGE";
            case 1004:
                return "ERROR_INVALID_REQUEST";
            case 1005:
                return "ERROR_INVALID_IP_AUTHENTICATION";
            case 1006:
                return "ERROR_AKA_AUTHENTICATION_FAILED";
            case 1007:
                return "FORBIDDEN_REQUEST";
            case 1008:
                return "INVALID_CLIENT_ID";
            default:
                switch (i) {
                    case 1020:
                        return "ERROR_INVALID_DEVICE_ID";
                    case 1021:
                        return "ERROR_NO_EPDG";
                    case 1022:
                        return "ERROR_CERTIFICATE_GENERATION_FAILURE";
                    case 1023:
                        return "ERROR_REMOVAL_SERVICE_FAILURE";
                    case 1024:
                        return "ERROR_INVALID_OWNERID";
                    case 1025:
                        return "ERROR_INVALID_CSR";
                    default:
                        switch (i) {
                            case NSDSNamespaces.NSDSResponseCode.ERROR_MAX_SERVICE_REACHED:
                                return "ERROR_MAX_SERVICE_REACHED";
                            case NSDSNamespaces.NSDSResponseCode.ERROR_INVALID_FINGERPRINT:
                                return "ERROR_INVALID_FINGERPRINT";
                            case 1042:
                                return "ERROR_INVALID_TARGET_DEVICEID";
                            case 1043:
                                return "ERROR_INVALID_TARGET_USER";
                            case NSDSNamespaces.NSDSResponseCode.ERROR_MAX_SERVICE_INSTANCE_REACHED:
                                return "ERROR_MAX_SERVICE_INSTANCE_REACHED";
                            case 1045:
                                return "ERROR_COPY_FORBIDDEN";
                            case NSDSNamespaces.NSDSResponseCode.ERROR_INVALID_SERVICE_NAME:
                                return "ERROR_INVALID_SERVICE_NAME";
                            default:
                                return "ERROR_UNKNOWN";
                        }
                }
        }
    }

    static {
        HashMap hashMap = new HashMap();
        sMapNSDSMethods = hashMap;
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH, 1);
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, 2);
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN, 3);
        hashMap.put("managePushToken", 4);
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC, 5);
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_SERVICE, 6);
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.REGISTERED_MSISDN, 7);
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.REGISTERED_DEVICES, 8);
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, 9);
    }

    public NSDSResponseHandler(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 20);
    }

    public Message obtainParseResponseMessage(Message message, Bundle bundle) {
        Message obtainMessage = obtainMessage(0);
        Bundle bundle2 = new Bundle();
        bundle2.putParcelable(KEY_CALLBACK, message);
        bundle2.putBundle(KEY_MESSAGE_ID_METHOD_BUNDLE, bundle);
        obtainMessage.setData(bundle2);
        return obtainMessage;
    }

    public void handleMessage(Message message) {
        Message message2;
        Bundle bundle;
        Bundle data = message.getData();
        if (data != null) {
            message2 = (Message) data.getParcelable(KEY_CALLBACK);
            bundle = data.getBundle(KEY_MESSAGE_ID_METHOD_BUNDLE);
        } else {
            message2 = null;
            bundle = null;
        }
        if (message2 == null) {
            IMSLog.e(LOG_TAG, "handleMessage(): callback is null. return...");
        } else if (message.what != 0) {
            IMSLog.i(LOG_TAG, "Response for Unknown EricssonNSDSRequest: " + message.what);
        } else {
            Bundle parseResponse = parseResponse(bundle, (HttpResponseParams) message.obj);
            if (!(parseResponse == null || message2.getData() == null)) {
                message2.getData().putAll(parseResponse);
            }
            message2.sendToTarget();
        }
    }

    private Bundle parseHttpErrorResponse(HttpResponseParams httpResponseParams) {
        String str = LOG_TAG;
        IMSLog.i(str, "parseHttpErrorResponse: status code " + httpResponseParams.getStatusCode());
        Bundle bundle = new Bundle();
        bundle.putInt(NSDSNamespaces.NSDSDataMapKey.HTTP_RESP_CODE, httpResponseParams.getStatusCode());
        bundle.putString(NSDSNamespaces.NSDSDataMapKey.HTTP_RESP_REASON, httpResponseParams.getStatusReason());
        return bundle;
    }

    private Bundle parseResponse(Bundle bundle, HttpResponseParams httpResponseParams) {
        NSDSResponse nSDSResponse;
        if (httpResponseParams == null) {
            IMSLog.i(LOG_TAG, "parseJsonData: Check for http failure. most likely connection reset by peer");
            return null;
        } else if (httpResponseParams.getStatusCode() != 200) {
            return parseHttpErrorResponse(httpResponseParams);
        } else {
            String dataString = httpResponseParams.getDataString();
            if (dataString == null) {
                IMSLog.i(LOG_TAG, "parseResponse: null json data");
                return null;
            }
            JsonParser jsonParser = new JsonParser();
            Gson gson = new Gson();
            try {
                JsonArray asJsonArray = jsonParser.parse(dataString).getAsJsonArray();
                if (asJsonArray == null || asJsonArray.size() == 0) {
                    IMSLog.e(LOG_TAG, "empty result");
                    return null;
                }
                Bundle bundle2 = new Bundle();
                Iterator it = asJsonArray.iterator();
                while (it.hasNext()) {
                    JsonElement jsonElement = (JsonElement) it.next();
                    try {
                        NSDSResponse nSDSResponse2 = (NSDSResponse) gson.fromJson(jsonElement, NSDSResponse.class);
                        int i = nSDSResponse2.messageId;
                        String string = bundle.getString(String.valueOf(i));
                        if (string == null) {
                            IMSLog.e(LOG_TAG, "Cannot find method for message id: " + i);
                            return null;
                        }
                        this.mEventLog.logAndAdd("parseResponse: method: " + string + " (" + toString(nSDSResponse2.responseCode) + ")");
                        StringBuilder sb = new StringBuilder();
                        sb.append(string);
                        sb.append(",");
                        sb.append(nSDSResponse2.responseCode);
                        IMSLog.c(LogClass.ES_HTTP_RESPONSE, sb.toString());
                        switch (sMapNSDSMethods.get(string).intValue()) {
                            case 1:
                                nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, Response3gppAuthentication.class);
                                break;
                            case 2:
                                nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, ResponseManageConnectivity.class);
                                break;
                            case 3:
                                nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, ResponseGetMSISDN.class);
                                break;
                            case 4:
                                nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, ResponseManagePushToken.class);
                                break;
                            case 5:
                                nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, ResponseManageLocationAndTC.class);
                                break;
                            case 6:
                                nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, ResponseManageService.class);
                                break;
                            case 7:
                                nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, ResponseRegisteredMSISDN.class);
                                break;
                            case 8:
                                nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, ResponseRegisteredDevices.class);
                                break;
                            case 9:
                                try {
                                    nSDSResponse = (NSDSResponse) gson.fromJson(jsonElement, ResponseServiceEntitlementStatus.class);
                                    break;
                                } catch (JsonSyntaxException e) {
                                    IMSLog.e(LOG_TAG, "Syntax error while parsing individual response: " + string + e.getMessage());
                                    return null;
                                }
                            default:
                                nSDSResponse = null;
                                break;
                        }
                        if (nSDSResponse != null) {
                            nSDSResponse.method = string;
                            bundle2.putParcelable(string, nSDSResponse);
                        }
                    } catch (JsonSyntaxException e2) {
                        IMSLog.e(LOG_TAG, "Syntax error while parsing generic response" + e2.getMessage());
                        return null;
                    }
                }
                return bundle2;
            } catch (JsonSyntaxException e3) {
                IMSLog.s(LOG_TAG, "cannot parse result" + e3.getMessage());
                return null;
            }
        }
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName());
        IMSLog.increaseIndent(str);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(str);
    }
}
