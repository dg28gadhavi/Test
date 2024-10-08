package com.sec.internal.constants.ims.cmstore.mcs.contactsync;

public class McsContactSyncConstants {

    public static class Intents {
        public static final String ACTION_MCS_SHARE_ACCESS_TOKEN = "com.samsung.ims.mcs.SHARE_ACCESS_TOKEN";
        public static final String ACTION_MCS_START_CONTACT_SYNC = "com.samsung.ims.mcs.START_CONTACT_SYNC";
        public static final String ACTION_MCS_STOP_CONTACT_SYNC = "com.samsung.ims.mcs.STOP_CONTACT_SYNC";
        public static final String EXTRA_ACCESS_TOKEN = "ACCESS_TOKEN";
        public static final String EXTRA_FORCE_REFRESH = "FORCE_REFRESH";
        public static final String EXTRA_INITIAL_SYNC = "INITIAL_SYNC";
        public static final String EXTRA_PUSH_EVENT = "PUSH_EVENT";
        public static final String EXTRA_SERVER_ROOT = "SERVER_ROOT";
        public static final String EXTRA_SERVICE_OFF = "SERVICE_OFF";
        public static final String EXTRA_USER_MDN = "USER_MDN";
    }

    public static class Packages {
        public static final String CS_LAUNCH_URI = "contactsync://launch";
        public static final String CS_PACKAGE_NAME = "com.skt.contactsync";
    }
}
