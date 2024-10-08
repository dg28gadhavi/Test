package com.sec.internal.ims.cmstore;

public interface CloudMessageIntent {
    public static final String CATEGORY_ACTION = "com.samsung.rcs.framework.cloudmessage.category.ACTION";
    public static final String INTENT_ACTION_CMS_RESTART = "com.samsung.rcs.framework.cloudmessage.RESTART";

    public static class Action {
        public static final String MSGAPPREQUEST = "com.samsung.rcs.framework.cloudmessage.action.MSGAPPREQUEST";
        public static final String MSGDELETEFAILURE = "com.samsung.rcs.framework.cloudmessage.action.MSGDELETEFAILURE";
        public static final String MSGINTENT = "com.samsung.rcs.framework.cloudmessage.action.MSGDATA";
        public static final String MSGINTENT_INITSYNCEND = "com.samsung.rcs.framework.cloudmessage.action.MSGDATA.INITIALSYNCEND";
        public static final String MSGINTENT_INITSYNCFAIL = "com.samsung.rcs.framework.cloudmessage.action.MSGDATA.INITIALSYNCFAIL";
        public static final String MSGINTENT_INITSYNSTART = "com.samsung.rcs.framework.cloudmessage.action.MSGDATA.INITIALSYNCSTART";
        public static final String MSGUIINTENT = "com.samsung.rcs.framework.cloudmessage.action.MSGUI";
        public static final String VVMDATADELETEFAILURE = "com.samsung.rcs.framework.cloudmessage.action.VVMDATADELETEFAILURE";
        public static final String VVMINTENT = "com.samsung.rcs.framework.cloudmessage.action.VVMDATA";
        public static final String VVMINTENT_INITIALSYNCEND = "com.samsung.rcs.framework.cloudmessage.action.VVMDATA.SYNCFINISHED";
        public static final String VVMINTENT_INITIALSYNCFAIL = "com.samsung.rcs.framework.cloudmessage.action.VVMDATA.SYNCFAIL";
        public static final String VVMINTENT_INITIALSYNCSTART = "com.samsung.rcs.framework.cloudmessage.action.VVMDATA.SYNCSTART";
        public static final String VVMINTENT_NORMALSYNCPROCESSING = "com.samsung.rcs.framework.cloudmessage.action.VVMDATA.BUSY";
    }

    public static class Extras {
        public static final String FETCH_URI_RESPONSE = "fetch_uri_response";
        public static final String FULLSYNC = "fullsync";
        public static final String LINENUM = "linenum";
        public static final String MSGTYPE = "msgtype";
        public static final String NETWORK_OP_IN_PROGRESS = "network_op_in_progress";
        public static final String ROWIDS = "rowids";
        public static final String SIMSLOT = "sim_slot";
    }

    public static class ExtrasAMBSUI {
        public static final String PARAM = "param";
        public static final String SCREENNAME = "screenname";
        public static final String STYLE = "style";
    }

    public static class McsExtras {
        public static final String BODY = "body";
        public static final String STATUSCODE = "code";
    }

    public static class Permission {
        public static final String MSGAPP = "com.samsung.app.cmstore.MSGDATA_PERMISSION";
    }
}
