package com.sec.internal.constants.ims;

public class SipReasonUscc extends SipReason {
    public static final SipReason INTER_RAT = new SipReason("SIP", 0, "Moved to CDMA", new String[0]);
    public static final SipReason SESSION_EXPIRED = new SipReason("SIP", 0, "Session Expired", new String[0]);
    public static final SipReason TIMER_EXPIRED = new SipReason("SIP", 0, "Timer Expired", new String[0]);
    public static final SipReason USER_TRIGGERED = new SipReason("SIP", 0, "User Triggered", new String[0]);

    public SipReason getFromUserReason(int i) {
        if (i < 0) {
            i = 5;
        }
        if (i == 4) {
            return INTER_RAT;
        }
        if (i == 5) {
            return USER_TRIGGERED;
        }
        if (i != 1802) {
            return super.getFromUserReason(i);
        }
        return TIMER_EXPIRED;
    }
}
