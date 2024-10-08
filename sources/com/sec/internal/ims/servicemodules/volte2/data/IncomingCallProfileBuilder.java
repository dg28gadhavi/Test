package com.sec.internal.ims.servicemodules.volte2.data;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.log.IMSLog;

public class IncomingCallProfileBuilder extends CallProfileBuilder {
    public CallProfileBuilder setDisplayName(IncomingCallEvent incomingCallEvent, Mno mno, boolean z) {
        if (!(mno == Mno.AVEA_TURKEY || mno == Mno.KDDI || mno == Mno.OPTUS || mno == Mno.GLOBE_PH || mno.isChn() || mno == Mno.ORANGE_SPAIN || mno == Mno.TELENOR_MM || mno == Mno.UMNIAH_JO)) {
            String displayName = incomingCallEvent.getPeerAddr().getDisplayName();
            if (mno == Mno.DU_UAE || mno == Mno.ZAIN_KSA || mno.isKor() || !displayName.matches("\\+?[0-9\\-]+")) {
                if (mno == Mno.SKT || mno == Mno.ATT) {
                    displayName = displayName.replace("\\\\", "\\").replace("\\\"", CmcConstants.E_NUM_STR_QUOTE);
                }
                if (mno != Mno.GRAMEENPHONE) {
                    Log.i("CallProfileBuilder", "onImsIncomingCallEvent: displayName is different with phone number, so setting extra mLetteringText " + IMSLog.checker(displayName));
                    this.mCallProfile.setLetteringText(displayName);
                }
                if (mno == Mno.VZW && "RESTRICTED".equals(displayName)) {
                    Log.i("CallProfileBuilder", "onImsIncomingCallEvent: set displayName to " + displayName + ", dialNumber = " + IMSLog.checker(this.mCallProfile.getDialingNumber()));
                    this.mCallProfile.setDialingNumber(displayName);
                }
            } else {
                Log.i("CallProfileBuilder", "onImsIncomingCallEvent: displayName match with phonenumber format, set as DialingNumber");
                if (!z && (mno == Mno.VZW || mno == Mno.USCC)) {
                    displayName = ImsCallUtil.removeUriPlusPrefix(displayName, Debug.isProductShip());
                }
                this.mCallProfile.setDialingNumber(displayName);
            }
        }
        return this;
    }

    public CallProfileBuilder setEchoCallId(IncomingCallEvent incomingCallEvent, Mno mno, String str) {
        if (mno.isOneOf(Mno.TMOUS, Mno.SPRINT) && !TextUtils.isEmpty(str)) {
            Log.i("CallProfileBuilder", "get echo call id " + str);
            this.mCallProfile.setEchoCallId(str);
        }
        return this;
    }

    public CallProfileBuilder setComposerData(IncomingCallEvent incomingCallEvent) {
        if (incomingCallEvent.getParams().getComposerData() != null && !incomingCallEvent.getParams().getComposerData().isEmpty()) {
            Log.i("CallProfileBuilder", "setComposerData: Setting composer data incoming flow");
            this.mCallProfile.setComposerData(incomingCallEvent.getParams().getComposerData());
        }
        return this;
    }

    public CallProfileBuilder setCmcEdCallSlot(int i) {
        this.mCallProfile.setCmcEdCallSlot(i);
        return this;
    }

    public CallProfileBuilder setCmcDeviceId(IncomingCallEvent incomingCallEvent, ImsRegistration imsRegistration, CallProfile callProfile) {
        if (ImsCallUtil.isCmcPrimaryType(imsRegistration.getImsProfile().getCmcType())) {
            callProfile.setCmcDeviceId(incomingCallEvent.getParams().getCmcDeviceId());
        }
        return this;
    }

    public CallProfile build(CallProfile callProfile) {
        return this.mCallProfile;
    }
}
