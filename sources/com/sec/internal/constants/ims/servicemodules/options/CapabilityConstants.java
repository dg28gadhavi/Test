package com.sec.internal.constants.ims.servicemodules.options;

public class CapabilityConstants {

    public static final class ContactCapability {
        public static final int CAPABLE_NONE = 0;
        public static final int CAPABLE_NULL = -1;
        public static final int RCS_CAPABLE_ONLY = 1;
        public static final int VIDEO_CAPA_ON_AVA_OFF = 7;
        public static final int VIDEO_CAPA_ON_AVA_ON = 6;
    }

    public enum RequestType {
        REQUEST_TYPE_NONE,
        REQUEST_TYPE_LAZY,
        REQUEST_TYPE_PERIODIC,
        REQUEST_TYPE_CONTACT_CHANGE,
        REQUEST_TYPE_SR_API
    }

    public enum CapExResult {
        SUCCESS,
        POLLING_SUCCESS,
        FAILURE,
        USER_NOT_FOUND,
        DOES_NOT_EXIST_ANYWHERE,
        USER_UNAVAILABLE,
        FORBIDDEN_403,
        REQUEST_TIMED_OUT,
        INVALID_DATA,
        NETWORK_ERROR,
        USER_AVAILABLE_OFFLINE,
        UNCLASSIFIED_ERROR,
        USER_NOT_REGISTERED;

        public boolean isOneOf(CapExResult... capExResultArr) {
            for (CapExResult capExResult : capExResultArr) {
                if (this == capExResult) {
                    return true;
                }
            }
            return false;
        }
    }
}
