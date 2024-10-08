package com.sec.sve.generalevent;

public abstract class VcidEvent {
    protected static final String BUNDLE_KEY_ACTION = "action";
    protected static final String BUNDLE_KEY_FILE_URL = "fileURL";
    protected static final String BUNDLE_KEY_SERVICE_TYPE = "serviceType";
    protected static final String BUNDLE_KEY_SESSION_ID = "sessionId";
    protected static final String BUNDLE_KEY_SUB_ID = "subId";
    public static final String BUNDLE_VALUE_ACTION_SET_VCID_ENGINE = "SetVCIDEngine";
    public static final String BUNDLE_VALUE_ACTION_START = "start";
    public static final String BUNDLE_VALUE_ACTION_STOP = "stop";
    public static final String BUNDLE_VALUE_SERVICE_TYPE_MYVIEW = "MyView";
    public static final String BUNDLE_VALUE_SERVICE_TYPE_VCID = "VCID";
    public static final String EVENT_NAME = "VCID";
    protected String mEvent;

    public String getEvent() {
        return this.mEvent;
    }
}
