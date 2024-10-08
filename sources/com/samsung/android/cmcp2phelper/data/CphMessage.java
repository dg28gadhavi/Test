package com.samsung.android.cmcp2phelper.data;

import android.text.TextUtils;
import android.util.Log;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class CphMessage {
    public static final String LOG_TAG = ("cmcp2phelper/1.3.06/" + CphMessage.class.getSimpleName());
    double mCmcVersion;
    String mDeviceId;
    String mLineId;
    int mMessageUniqueNum;
    int mMsgType;
    String mResponderIP;
    int mResponderPort;
    JSONObject message;

    public String toString() {
        JSONObject jSONObject = this.message;
        if (jSONObject == null) {
            return "";
        }
        String jSONObject2 = jSONObject.toString();
        try {
            JSONObject jSONObject3 = new JSONObject(jSONObject2);
            jSONObject3.put("cph_cmc_line_id", "xxx");
            return jSONObject3.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return jSONObject2;
        }
    }

    public boolean isValid() {
        return this.message != null;
    }

    public String getResponderIP() {
        return this.mResponderIP;
    }

    public int getResponderPort() {
        return this.mResponderPort;
    }

    public int getMsgType() {
        return this.mMsgType;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public String getLineId() {
        return this.mLineId;
    }

    public int getMessageId() {
        return this.mMessageUniqueNum;
    }

    public CphMessage(int i, double d, String str, String str2, String str3, int i2, int i3) {
        this.mMsgType = i;
        this.mCmcVersion = d;
        this.mDeviceId = str;
        this.mLineId = str2;
        this.mResponderIP = str3;
        this.mResponderPort = i2;
        this.mMessageUniqueNum = i3;
        makeJsonObject();
    }

    public CphMessage(int i, double d, String str, String str2, int i2) {
        this.mMsgType = i;
        this.mCmcVersion = d;
        this.mDeviceId = str;
        this.mLineId = str2;
        this.mResponderIP = "";
        this.mResponderPort = 0;
        this.mMessageUniqueNum = i2;
        makeJsonObject();
    }

    public CphMessage(DatagramPacket datagramPacket) {
        try {
            String str = new String(datagramPacket.getData(), StandardCharsets.UTF_8);
            String str2 = LOG_TAG;
            Log.d(str2, "new cphMessage : " + str);
            this.message = new JSONObject(str);
            Log.d(str2, "json cphMessage : " + toString());
            parseFromJson(this.message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseFromJson(JSONObject jSONObject) {
        try {
            if (jSONObject.has("cph_message_id")) {
                this.mMessageUniqueNum = jSONObject.getInt("cph_message_id");
            }
            this.mMsgType = jSONObject.getInt("cph_message_type");
            this.mCmcVersion = jSONObject.getDouble("cph_cmc_version");
            this.mDeviceId = jSONObject.getString("cph_cmc_device_id");
            this.mLineId = jSONObject.getString("cph_cmc_line_id");
            if (jSONObject.has("cph_cmc_resp_ip")) {
                this.mResponderIP = jSONObject.getString("cph_cmc_resp_ip");
            }
            if (jSONObject.has("cph_cmc_resp_port")) {
                this.mResponderPort = jSONObject.getInt("cph_cmc_resp_port");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public byte[] getByte() {
        JSONObject jSONObject = this.message;
        if (jSONObject != null) {
            return jSONObject.toString().getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    private void makeJsonObject() {
        JSONObject jSONObject = new JSONObject();
        this.message = jSONObject;
        try {
            jSONObject.put("cph_message_id", this.mMessageUniqueNum);
            this.message.put("cph_message_type", this.mMsgType);
            this.message.put("cph_cmc_version", this.mCmcVersion);
            this.message.put("cph_cmc_device_id", this.mDeviceId);
            this.message.put("cph_cmc_line_id", this.mLineId);
            if (!TextUtils.isEmpty(this.mResponderIP)) {
                this.message.put("cph_cmc_resp_ip", this.mResponderIP);
            }
            int i = this.mResponderPort;
            if (i != 0) {
                this.message.put("cph_cmc_resp_port", i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
