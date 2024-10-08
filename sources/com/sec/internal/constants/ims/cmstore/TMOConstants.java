package com.sec.internal.constants.ims.cmstore;

import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;

public class TMOConstants {

    public static class TmoHttpHeaderNames {
        public static final String DEVICE_ID = "device_id";
    }

    public static class TmoMessageContextValues extends MessageContextValues {
        public static final String chatMessage = "X-RCS-Chat";
        public static final String fileMessage = "X-RCS-FT";
        public static final String greetingvoice = "x-voice-grtng";
        public static final String gsomessage = "X-RCS-Chat-GSO";
        public static final String gsosession = "X-RCS-Chat-Session";
        public static final String imdnMessage = "imdn-message";
        public static final String standaloneMessageLLM = "X-RCS-LM";
        public static final String standaloneMessagePager = "X-RCS-PM";
    }
}
