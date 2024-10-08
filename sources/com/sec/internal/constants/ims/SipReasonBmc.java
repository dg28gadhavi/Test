package com.sec.internal.constants.ims;

public class SipReasonBmc extends SipReason {
    public static final SipReason NWAY_CONFERENCE = new SipReason("SIP", 0, "Conference Fail", new String[0]);
    public static final SipReason USER_TRIGGERED = new SipReason("SIP", 0, "User Triggered", new String[0]);

    public SipReason getFromUserReason(int i) {
        if (i < 0) {
            i = 5;
        }
        if (i == 5) {
            return USER_TRIGGERED;
        }
        if (i != 7) {
            return super.getFromUserReason(i);
        }
        return NWAY_CONFERENCE;
    }
}
