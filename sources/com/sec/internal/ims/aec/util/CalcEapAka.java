package com.sec.internal.ims.aec.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.ims.config.util.AKAEapAuthHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.interfaces.ims.core.ISimManager;

public class CalcEapAka extends Handler {
    private static final int HANDLE_REQUEST_SIM_AUTHENTICATION = 0;
    private static final int HANDLE_RESPONSE_SIM_AUTHENTICATION = 1;
    private final String mImsi;
    private final int mPhoneId;
    private Message mReplyTo;

    public CalcEapAka(int i, String str) {
        this.mPhoneId = i;
        this.mImsi = str;
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 0) {
            requestSimAuthentication(message.getData().getString(WwwAuthenticateHeader.HEADER_PARAM_NONCE), message.getData().getString("akaChallenge"));
        } else if (i == 1) {
            processSimAuthResponse(message.getData().getString("akaChallenge"), (String) message.obj, message.getData().getString("imsiEap"));
        }
    }

    public void requestEapChallengeResp(Message message, String str) {
        this.mReplyTo = message;
        Message obtainMessage = obtainMessage();
        obtainMessage.what = 0;
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(str)) {
            bundle.putString(WwwAuthenticateHeader.HEADER_PARAM_NONCE, AKAEapAuthHelper.getNonce(str));
            bundle.putString("akaChallenge", str);
            obtainMessage.setData(bundle);
        }
        sendMessage(obtainMessage);
    }

    public String decodeChallenge(String str) {
        return AKAEapAuthHelper.decodeChallenge(str);
    }

    private void requestSimAuthentication(String str, String str2) {
        try {
            Message obtainMessage = obtainMessage(1);
            Bundle bundle = new Bundle();
            bundle.putString("akaChallenge", str2);
            bundle.putString("imsiEap", getImsiEap());
            obtainMessage.setData(bundle);
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
            if (simManagerFromSimSlot != null) {
                simManagerFromSimSlot.requestIsimAuthentication(str, obtainMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSimAuthResponse(String str, String str2, String str3) {
        Message message = this.mReplyTo;
        if (message != null) {
            message.obj = AKAEapAuthHelper.generateChallengeResponse(str, str2, str3);
            this.mReplyTo.sendToTarget();
        }
    }

    public String getImsiEap() throws Exception {
        String str;
        String str2;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (simManagerFromSimSlot != null) {
            String simOperator = simManagerFromSimSlot.getSimOperator();
            if (simOperator.length() == 5) {
                str = simOperator.substring(0, 3);
                str2 = "0" + simOperator.substring(3, 5);
            } else if (simOperator.length() == 6) {
                str = simOperator.substring(0, 3);
                str2 = simOperator.substring(3, 6);
            } else {
                throw new Exception("getImsiEap: invalid operator");
            }
            return "0" + this.mImsi + "@nai.epc.mnc" + str2 + ".mcc" + str + UtUtils.DOMAIN_NAME;
        }
        throw new Exception("getImsiEap: sim manager not ready");
    }
}
