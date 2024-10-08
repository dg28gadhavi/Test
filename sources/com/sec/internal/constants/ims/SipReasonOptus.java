package com.sec.internal.constants.ims;

public class SipReasonOptus extends SipReason {
    public static final SipReason USER_TRIGGERED = new SipReason("SIP", 0, "User Triggered", new String[0]);

    public SipReason getFromUserReason(int i) {
        if (i < 0) {
            i = 5;
        }
        if (i != 5) {
            return super.getFromUserReason(i);
        }
        return USER_TRIGGERED;
    }
}
