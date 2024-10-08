package com.sec.internal.ims.core;

import android.content.Context;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorTmoInbound extends RegistrationGovernorTmo {
    private static final String LOG_TAG = "RegiGvnTmoInbound";

    public RegistrationGovernorTmoInbound(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
    }

    /* access modifiers changed from: protected */
    public boolean isTmoInboundRoaming(int i, ISimManager iSimManager) {
        String simOperator = iSimManager.getSimOperator();
        String str = OmcCode.get();
        if (simOperator.equals("54715") && !str.matches("XNZ|XNF")) {
            return false;
        }
        Mno mnoFromNetworkPlmn = iSimManager.getMnoFromNetworkPlmn(this.mRegMan.getNetworkEvent(i).operatorNumeric);
        if (!this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming || mnoFromNetworkPlmn != Mno.TMOUS) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyInboundRoamingPolicy(Set<String> set, ISimManager iSimManager) {
        if (!set.isEmpty() && !isTmoInboundRoaming(this.mPhoneId, iSimManager)) {
            return new HashSet();
        }
        return set;
    }
}
