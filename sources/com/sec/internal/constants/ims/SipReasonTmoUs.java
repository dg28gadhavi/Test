package com.sec.internal.constants.ims;

public class SipReasonTmoUs extends SipReason {
    public static final SipReason DEDICATED_BEARER_LOST = new SipReason("RELEASE_CAUSE", 3, "Media bearer loss", new String[0]);
    public static final SipReason INVITE_18X_TIMEOUT = new SipReason("RELEASE_CAUSE", 6, "Call-setup time-out", new String[0]);
    public static final SipReason INVITE_TIMEOUT = new SipReason("RELEASE_CAUSE", 5, "User ends call and SIP response time-out", new String[0]);
    public static final SipReason USER_TRIGGERED = new SipReason("RELEASE_CAUSE", 1, "User ends call", new String[0]);

    public SipReason getFromUserReason(int i) {
        if (i < 0) {
            i = 5;
        }
        if (i == 5) {
            return USER_TRIGGERED;
        }
        if (i == 11) {
            return DEDICATED_BEARER_LOST;
        }
        if (i == 17) {
            return INVITE_18X_TIMEOUT;
        }
        if (i != 28) {
            return super.getFromUserReason(i);
        }
        return INVITE_TIMEOUT;
    }
}
