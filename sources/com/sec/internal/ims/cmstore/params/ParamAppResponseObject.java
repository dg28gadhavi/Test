package com.sec.internal.ims.cmstore.params;

public class ParamAppResponseObject {
    public int errorCode;
    public String jsonResult;

    public ParamAppResponseObject(int i, String str) {
        this.errorCode = i;
        this.jsonResult = str;
    }
}
