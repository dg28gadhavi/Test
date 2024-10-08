package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.os.Message;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;

public class TelephonyAdapterPrimaryDeviceTelstra extends TelephonyAdapterPrimaryDeviceBase {
    private static final String LOG_TAG = TelephonyAdapterPrimaryDeviceTelstra.class.getSimpleName();
    private static final String STANDARD_IMPI_TEMPLATE = "<imsi>@ims.mnc<mnc>.mcc<mcc>.3gppnetwork.org";

    public TelephonyAdapterPrimaryDeviceTelstra(Context context, IConfigModule iConfigModule, int i) {
        super(context, iConfigModule, i);
        registerSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void handleReceivedDataSms(Message message, boolean z, boolean z2) {
        String str = (String) message.obj;
        if (str == null || !str.contains(TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST)) {
            super.handleReceivedDataSms(message, z, z2);
            return;
        }
        if (isValidReconfigSmsFormatImsi(str)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isValidReconfigSmsFormatImsi");
        } else if (isValidReconfigSmsFormatStandardImpi(str)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isValidReconfigSmsFormatStandardImpi");
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "invalid reconfiguration SMS format for Telstra");
            return;
        }
        sendSmsPushForConfigRequest(z);
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + message.what);
        int i2 = message.what;
        if (i2 == 1) {
            handleReceivedDataSms(message, true, false);
        } else if (i2 != 3) {
            super.handleMessage(message);
        } else {
            IMSLog.i(str, this.mPhoneId, "getting otp is timed out");
            handleOtpTimeout(false);
        }
    }

    private boolean isValidReconfigSmsFormatImsi(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(getImsi());
        sb.append(TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST);
        return str.contains(sb.toString());
    }

    private boolean isValidReconfigSmsFormatStandardImpi(String str) {
        if (str.contains(genStandardImpi() + TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST)) {
            return true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getTelstraImpi());
        sb.append(TelephonyAdapterPrimaryDeviceBase.SMS_CONFIGURATION_REQUEST);
        return str.contains(sb.toString());
    }

    private String genStandardImpi() {
        if (getImsi() == null || getMcc() == null || getMnc() == null) {
            return STANDARD_IMPI_TEMPLATE;
        }
        String replace = STANDARD_IMPI_TEMPLATE.replace("<imsi>", getImsi()).replace("<mcc>", getMcc()).replace("<mnc>", getMnc());
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "Generated standard IMPI: " + replace);
        return replace;
    }

    private String getTelstraImpi() {
        String readStringParam = RcsConfigurationHelper.readStringParam(this.mContext, ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "get Telstra Impi: " + readStringParam);
        return readStringParam;
    }
}
