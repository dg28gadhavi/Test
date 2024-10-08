package com.sec.internal.omanetapi.nms.data;

import com.google.gson.annotations.SerializedName;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.TmoGcmMessage;
import java.net.URL;

public class ChangedObject {
    public String correlationId;
    public String correlationTag;
    @SerializedName("message")
    public TmoGcmMessage extendedMessage;
    public FlagList flags;
    public ImdnList imdns;
    public Long lastModSeq;
    public URL parentFolder;
    public String protocol;
    public URL resourceURL;
}
