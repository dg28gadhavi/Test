package com.sec.internal.constants.ims.core;

public enum PdnFailReason {
    NOT_DEFINED(-1),
    NETWORK_SELECTION_ONGOING(-25),
    PDN_MAX_TIMEOUT(-22),
    PDN_THROTTLED(-8),
    INSUFFICIENT_RESOURCES(26),
    MISSING_UNKNOWN_APN(27),
    UNKNOWN_PDP_ADDRESS_TYPE(28),
    ACTIVATION_REJECT_GGSN(30),
    SERVICE_OPTION_NOT_SUPPORTED(32),
    SERVICE_OPTION_NOT_SUBSCRIBED(33),
    MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED(55),
    PROTOCOL_ERRORS(111),
    NETWORK_INITIATED_DETACH_WITH_AUTO_REATTACH(2153);
    
    private final int mFailCause;

    private PdnFailReason(int i) {
        this.mFailCause = i;
    }

    public static PdnFailReason valueOf(int i) {
        PdnFailReason pdnFailReason = NOT_DEFINED;
        for (PdnFailReason pdnFailReason2 : values()) {
            if (pdnFailReason2.mFailCause == i) {
                return pdnFailReason2;
            }
        }
        return pdnFailReason;
    }

    public int getCause() {
        return this.mFailCause;
    }
}
