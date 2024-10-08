package com.sec.internal.ims.servicemodules.volte2.data;

public class EcholocateEvent {
    EcholocateRtpMessage mRtpData;
    EcholocateSignalMessage mSignalData;
    EcholocateType mType;

    public enum EcholocateType {
        signalMsg,
        rtpMsg
    }

    public static class EcholocateSignalMessage {
        String callId;
        String contents;
        String cseq;
        boolean isEpdgCall;
        String line1;
        String origin;
        String peerNumber;
        String reason;
        String sessionid;

        public String getOrigin() {
            return this.origin;
        }

        public String getLine1() {
            return this.line1;
        }

        public String getCallId() {
            return this.callId;
        }

        public String getCseq() {
            return this.cseq;
        }

        public String getSessionid() {
            return this.sessionid;
        }

        public String getReason() {
            return this.reason;
        }

        public String getContents() {
            return this.contents;
        }

        public String getPeerNumber() {
            return this.peerNumber;
        }

        public boolean isEpdgCall() {
            return this.isEpdgCall;
        }

        public EcholocateSignalMessage(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, boolean z) {
            this.origin = str;
            this.line1 = str2;
            this.callId = str3;
            this.cseq = str4;
            this.sessionid = str5;
            this.reason = str6;
            this.contents = str7;
            this.peerNumber = str8;
            this.isEpdgCall = z;
        }
    }

    public static class EcholocateRtpMessage {
        String delay;
        String dir;
        String id;
        String jitter;
        String lossrate;
        String measuredperiod;

        public String getDir() {
            return this.dir;
        }

        public String getId() {
            return this.id;
        }

        public String getLossrate() {
            return this.lossrate;
        }

        public String getDelay() {
            return this.delay;
        }

        public String getJitter() {
            return this.jitter;
        }

        public String getMeasuredperiod() {
            return this.measuredperiod;
        }

        public EcholocateRtpMessage(String str, String str2, String str3, String str4, String str5, String str6) {
            this.dir = str;
            this.id = str2;
            this.lossrate = str3;
            this.delay = str4;
            this.jitter = str5;
            this.measuredperiod = str6;
        }
    }

    public static class EcholocateHandoverMessage {
        private String callId;
        private String callNumber;
        private String callState;
        private String cellId;
        private String networkBand;
        private String networkSignal;
        private String networkType;
        private String time;

        public String getNetworkType() {
            return this.networkType;
        }

        public String getNetworkBand() {
            return this.networkBand;
        }

        public String getNetworkSignal() {
            return this.networkSignal;
        }

        public String getTime() {
            return this.time;
        }

        public String getCallId() {
            return this.callId;
        }

        public String getCallState() {
            return this.callState;
        }

        public String getCallNumber() {
            return this.callNumber;
        }

        public String getCellId() {
            return this.cellId;
        }

        public void setTime(long j) {
            this.time = Long.toString(j);
        }

        public EcholocateHandoverMessage(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8) {
            this.callNumber = str;
            this.callState = str2;
            this.networkType = str3;
            this.networkBand = str5;
            this.networkSignal = str4;
            this.callId = str6;
            this.time = str7;
            this.cellId = str8;
        }
    }

    public static class EcholocateEmergencyMessage {
        private String callId;
        private String callNumber;
        private boolean isEpdgCall;
        private String stateName;
        private String timerName;

        public String getCallNumber() {
            return this.callNumber;
        }

        public String getTimerName() {
            return this.timerName;
        }

        public String getStateName() {
            return this.stateName;
        }

        public String getCallId() {
            return this.callId;
        }

        public boolean isEpdgCall() {
            return this.isEpdgCall;
        }

        public EcholocateEmergencyMessage(String str, String str2, String str3, String str4, boolean z) {
            this.callNumber = str;
            this.timerName = str2;
            this.stateName = str3;
            this.callId = str4;
            this.isEpdgCall = z;
        }
    }

    public static class EchoSignallingIntentData {
        private String networkBand;
        private String networkSignal;
        private String networkType;
        private EcholocateSignalMessage signalMsg;
        private String time;

        public EcholocateSignalMessage getSignalMsg() {
            return this.signalMsg;
        }

        public String getNetworkBand() {
            return this.networkBand;
        }

        public String getNetworkSignal() {
            return this.networkSignal;
        }

        public String getNetworkType() {
            return this.networkType;
        }

        public String getTime() {
            return this.time;
        }

        public EchoSignallingIntentData(EcholocateSignalMessage echolocateSignalMessage, String str, String str2, String str3, String str4) {
            this.signalMsg = echolocateSignalMessage;
            this.networkBand = str;
            this.networkSignal = str2;
            this.networkType = str3;
            this.time = str4;
        }
    }

    public void setType(EcholocateType echolocateType) {
        this.mType = echolocateType;
    }

    public EcholocateType getType() {
        return this.mType;
    }

    public void setSignalData(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, boolean z) {
        this.mSignalData = new EcholocateSignalMessage(str, str2, str3, str4, str5, str6, str7, str8, z);
    }

    public EcholocateSignalMessage getSignalData() {
        return this.mSignalData;
    }

    public void setRtpData(String str, String str2, String str3, String str4, String str5, String str6) {
        this.mRtpData = new EcholocateRtpMessage(str, str2, str3, str4, str5, str6);
    }

    public EcholocateRtpMessage getRtpData() {
        return this.mRtpData;
    }
}
