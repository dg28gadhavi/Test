package com.sec.internal.omanetapi.nms.data;

public class AttributeContent {
    public String clientCorrelator;
    public String contentType;
    public String date;
    public String direction;
    public String from;
    public String messageContext;
    public String messageId;
    public String miMeVersion;
    public String reportRequested;
    public String[] to;

    public AttributeContent(String str, String str2, String str3, String str4, String str5, String str6, String str7, String[] strArr, String str8, String str9) {
        this.date = str;
        this.messageContext = str2;
        this.messageId = str3;
        this.miMeVersion = str4;
        this.direction = str5;
        this.clientCorrelator = str6;
        this.from = str7;
        this.to = new String[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            this.to[i] = strArr[i];
        }
        this.contentType = str8;
        this.reportRequested = str9;
    }
}
