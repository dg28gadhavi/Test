package com.sec.internal.ims.cmstore.ambs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.entitlement.util.BinarySmsBase64;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

public class DataSMSReceiver extends BroadcastReceiver {
    static final String JSON_ACTION_TAG = "action";
    static final String SUB = "sub";
    static final String TAG_ACTION = "action";
    static final String TAG_SID = "serviceId";
    static final String VAL_ACTION = "OptIn";
    static final String VAL_NoOPTIN = "RestartService:noOptIn";
    static final String VAL_PauseService = "PauseService";
    static final String VAL_SID = "msgstoreoem";
    static final String VAL_StopService = "StopService";
    private String TAG = "DataSMSReceiver";
    String expiry = "";
    private final IWorkingStatusProvisionListener mIWorkingStatusProvisionListener;
    protected final IAPICallFlowListener mListener;
    private final MessageStoreClient mStoreClient;
    String sub_val = "";

    public DataSMSReceiver(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, IWorkingStatusProvisionListener iWorkingStatusProvisionListener) {
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mStoreClient = messageStoreClient;
        this.mListener = iAPICallFlowListener;
        this.mIWorkingStatusProvisionListener = iWorkingStatusProvisionListener;
    }

    public void onReceive(Context context, Intent intent) {
        byte[] userData;
        Log.i(this.TAG, "On receive");
        if (intent.getAction() != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            int i = extras.getInt(PhoneConstants.SUBSCRIPTION_KEY, -1);
            Log.i(this.TAG, "subid from intent:" + i);
            if (!"android.test.ambsphasev.SIGNEDBINARYSMS".equals(intent.getAction()) || intent.getExtras() == null) {
                StringBuilder sb = new StringBuilder();
                Object[] objArr = (Object[]) extras.get("pdus");
                if (objArr == null) {
                    Log.d(this.TAG, "invalid pdus");
                    return;
                }
                int length = objArr.length;
                SmsMessage[] smsMessageArr = new SmsMessage[length];
                int i2 = 0;
                while (i2 < length) {
                    if (objArr[i2] != null) {
                        SmsMessage createFromPdu = SmsMessage.createFromPdu((byte[]) objArr[i2], ((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).getPhoneType() == 2 ? com.sec.internal.constants.ims.servicemodules.sms.SmsMessage.FORMAT_3GPP2 : com.sec.internal.constants.ims.servicemodules.sms.SmsMessage.FORMAT_3GPP);
                        smsMessageArr[i2] = createFromPdu;
                        if (createFromPdu != null && (userData = createFromPdu.getUserData()) != null) {
                            Log.i(this.TAG, "Sms encoded Data :" + Arrays.toString(userData));
                            for (int i3 = 0; i3 < userData.length; i3++) {
                                sb.append(Character.toString((char) userData[i3]));
                            }
                            i2++;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                Log.i(this.TAG, " messages = " + sb.toString() + " SignedBinarySupported: " + ATTGlobalVariables.supportSignedBinary());
                if (ATTGlobalVariables.supportSignedBinary()) {
                    byte[] decodeBase64 = BinarySmsBase64.decodeBase64(sb.toString().getBytes(StandardCharsets.UTF_8));
                    if (decodeBase64.length > 0) {
                        String str = new String(decodeBase64, StandardCharsets.UTF_8);
                        String parseJson = parseJson(str);
                        try {
                            long parseLong = Long.parseLong(this.expiry);
                            long currentTimeMillis = System.currentTimeMillis();
                            Log.i(this.TAG, "channel has expired? curr:" + currentTimeMillis + " expTime:" + parseLong + " expiry:" + this.expiry);
                            if (parseLong >= currentTimeMillis) {
                                if (!TextUtils.isEmpty(parseJson)) {
                                    processSignedBinaryAction(parseJson, i);
                                    return;
                                }
                                Log.i(this.TAG, "Print SMS decoded " + IMSLog.checker(str));
                            }
                        } catch (Exception unused) {
                            Log.e(this.TAG, "error in parsing expiry time");
                        }
                    } else {
                        Log.e(this.TAG, "Binary SMS Decode Failure");
                    }
                } else {
                    String[] parse = parse(sb.toString());
                    if (parse != null) {
                        if (i != this.mStoreClient.getSimManager().getSubscriptionId()) {
                            Log.d(this.TAG, "ignore this sms message, currentNum:" + this.mStoreClient.getPrerenceManager().getUserCtn());
                            return;
                        }
                        Log.i(this.TAG, "resp " + Arrays.toString(parse));
                        if (VAL_SID.equals(parse[0])) {
                            Log.d(this.TAG, "binary SMS received to provision!");
                            RequestAccount.handleExternalUserOptIn(this.mListener, this.mStoreClient);
                        }
                    }
                }
            } else {
                Bundle extras2 = intent.getExtras();
                String string = extras2.getString("sbsstring");
                int i4 = extras2.getInt(NSDSContractExt.QueryParams.SLOT_ID, -1);
                int subId = SimUtil.getSubId(i4);
                long currentTimeMillis2 = System.currentTimeMillis();
                long j = extras2.getLong(CloudMessageProviderContract.BufferDBMMSpdu.EXP, 0);
                Log.d(this.TAG, "test slot: " + i4 + " testSubid:" + subId + " currTime:" + currentTimeMillis2 + " expTime:" + j);
                if (j < currentTimeMillis2) {
                    Log.e(this.TAG, "time expired, do not process");
                } else if (!TextUtils.isEmpty(string)) {
                    processSignedBinaryAction(string, subId);
                } else {
                    Log.i(this.TAG, "Action is empty");
                }
            }
        }
    }

    public void processSignedBinaryAction(String str, int i) {
        String str2 = this.TAG;
        Log.i(str2, "processSignedBinaryAction action: " + str + " subid:" + i + "tp_subid:" + this.mStoreClient.getSimManager().getSubscriptionId());
        if (!Mno.ATT.equals(SimUtil.getSimMno(this.mStoreClient.getClientID()))) {
            Log.e(this.TAG, "Not ATT SIM Card, do not process");
        } else if (str.equals(VAL_StopService)) {
            this.mIWorkingStatusProvisionListener.stopService();
        } else if (i != this.mStoreClient.getSimManager().getSubscriptionId()) {
            String str3 = this.TAG;
            Log.d(str3, "ignore this sms message, requested for other slot currentNum: " + this.mStoreClient.getPrerenceManager().getUserCtn());
        } else if (str.equals(VAL_PauseService)) {
            this.mIWorkingStatusProvisionListener.pauseService();
        } else if (str.equals(VAL_NoOPTIN)) {
            this.mIWorkingStatusProvisionListener.onRestartService(false);
        }
    }

    public String[] parse(String str) {
        String substring;
        int indexOf;
        int indexOf2 = str.indexOf(TAG_SID);
        if (indexOf2 < 0 || (indexOf = substring.indexOf(VAL_ACTION)) < 0) {
            return null;
        }
        String substring2 = (substring = str.substring(indexOf2)).substring(0, indexOf + 5);
        String[] strArr = new String[2];
        for (String split : substring2.split(":")) {
            String[] split2 = split.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            if (TAG_SID.equals(split2[0])) {
                strArr[0] = split2[1];
            }
            if ("action".equals(split2[0])) {
                strArr[1] = split2[1];
            }
        }
        return strArr;
    }

    public String parseJson(String str) {
        this.expiry = "";
        String str2 = null;
        try {
            if (str.length() > 2) {
                int indexOf = str.indexOf(123, 2);
                int indexOf2 = indexOf != -1 ? str.indexOf(125, indexOf) : -1;
                String str3 = this.TAG;
                Log.i(str3, "subJsonBegin " + indexOf + " subJsonEnd " + indexOf2);
                if (!(indexOf == -1 || indexOf2 == -1)) {
                    str = str.substring(indexOf, indexOf2 + 1);
                }
            }
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.isNull("sub")) {
                this.sub_val = jSONObject.getString("sub");
                String str4 = this.TAG;
                Log.i(str4, "action_alg " + this.sub_val);
            }
            if (!jSONObject.isNull("action")) {
                str2 = jSONObject.getString("action");
                String str5 = this.TAG;
                Log.i(str5, "action_alg " + str2);
            }
            if (!jSONObject.isNull(CloudMessageProviderContract.BufferDBMMSpdu.EXP)) {
                this.expiry = jSONObject.getString(CloudMessageProviderContract.BufferDBMMSpdu.EXP);
                String str6 = this.TAG;
                Log.i(str6, "expiry: " + this.expiry);
            }
        } catch (JSONException e) {
            String str7 = this.TAG;
            Log.e(str7, "Error " + e.getMessage());
        }
        return str2;
    }
}
