package com.sec.internal.ims.servicemodules.ss;

public class UtConstant {
    public static final String CONDITION = "condition";
    public static final String IMSSSINFO = "imsSsInfo";
    public static final String NOREPLYTIMER = "NoReplyTimer";
    public static final String NUMBER = "number";
    public static final String QUERYCLIR = "queryClir";
    public static final String SERVICECLASS = "serviceClass";
    public static final String STATUS = "status";
    public static final String TOA = "ToA";
    public static final int UT_TIMEOUT_TIMER = 32500;

    public static class UtError {
        public static final int ALREADY_FORBIDDEN = 1003;
        public static final int EMPTY_DNS = 1018;
        public static final int HTTP_200_OK = 200;
        public static final int HTTP_201_CREATED = 201;
        public static final int HTTP_403_FORBIDDEN = 403;
        public static final int HTTP_404_NOT_FOUND = 404;
        public static final int HTTP_412_PRECONDITION_FAILED = 412;
        public static final int HTTP_500_INTERNAL_SERVER_ERROR = 500;
        public static final int HTTP_503_SERVICE_UNAVAILABLE = 503;
        public static final int HTTP_CONNECTION_ERROR = 1015;
        public static final int INVALID_PDN_REQUEST = 1016;
        public static final int INVALID_REQUEST = 1008;
        private static final int MINIMUM_VALUE_OF_DATA_FAIL_CAUSE = 22;
        public static final int NOT_CONFIGURED = 1007;
        public static final int NOT_REGISTERED_IN_VOLTEREGIED = 1013;
        public static final int NOT_SUPPORT_BARRING = 1010;
        public static final int NOT_SUPPORT_BA_ALL = 1002;
        public static final int NO_DDS_SLOT = 1005;
        public static final int NO_ERROR = 0;
        public static final int NO_MATCHED_PROFILE = 1006;
        public static final int NO_XCAP_APN = 1009;
        public static final int PDN_REJECT = 10022;
        public static final int PUT_BLOCKED = 1012;
        public static final int SERVICE_DEACTIVATED = 1011;
        public static final int SIM_NOT_READY = 1004;
        public static final int START_PDN_FAILURE = 1014;
        public static final int TIMEOUT = 1017;
    }

    public static class UtMessage {
        public static final int EVENT_CACHE_RESULT_PARSE = 13;
        public static final int EVENT_DISCONNECT_PDN = 2;
        public static final int EVENT_DOCUMENT_CACHE_RESET = 5;
        public static final int EVENT_HTTP_COMPLETE = 10;
        public static final int EVENT_HTTP_FAIL = 11;
        public static final int EVENT_INIT_SS_403 = 14;
        public static final int EVENT_PDN_CONNECTED = 1;
        public static final int EVENT_PDN_DISCONNECTED = 3;
        public static final int EVENT_REQUEST_FAIL = 12;
        public static final int EVENT_REQUEST_PDN = 100;
        public static final int EVENT_REQUEST_TIMEOUT = 15;
        public static final int EVENT_SEPARATE_CFNL = 6;
        public static final int EVENT_SEPARATE_CFNRY = 7;
        public static final int EVENT_SEPARATE_CF_ALL = 8;
        public static final int EVENT_SEPARATE_MEDIA = 9;
        public static final int EVENT_TERMINAL_REQUEST = 4;
    }
}
