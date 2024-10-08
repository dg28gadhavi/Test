package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.log.IMSLog;

public class ParamAppJsonValue {
    public final String mAppType;
    public final String mBody;
    public final String mChatId;
    public final String mCorrelationId;
    public final String mCorrelationTag;
    public final int mDataContractType;
    public final String mDataType;
    public final String mFromAddress;
    public final boolean mIsGroupSMS;
    public final String mLine;
    public final CloudMessageBufferDBConstants.MsgOperationFlag mOperation;
    public final int mRowId;
    public final String mToAddress;
    public final ParamVvmUpdate mVvmUpdate;

    public ParamAppJsonValue(String str, String str2, int i, int i2, String str3, String str4, String str5, CloudMessageBufferDBConstants.MsgOperationFlag msgOperationFlag, String str6, ParamVvmUpdate paramVvmUpdate, MessageStoreClient messageStoreClient, boolean z, String str7, String str8, String str9) {
        this.mAppType = str;
        this.mDataType = str2;
        this.mDataContractType = i;
        this.mRowId = i2;
        this.mChatId = str3;
        if (str6 != null) {
            this.mLine = str6;
        } else {
            this.mLine = messageStoreClient.getPrerenceManager().getUserTelCtn();
        }
        this.mOperation = msgOperationFlag;
        this.mCorrelationTag = str4;
        this.mCorrelationId = str5;
        this.mVvmUpdate = paramVvmUpdate;
        this.mIsGroupSMS = z;
        this.mToAddress = str7;
        this.mFromAddress = str8;
        this.mBody = str9;
    }

    public String toString() {
        return "ParamAppJsonValue [mAppType= " + this.mAppType + " mDataType = " + this.mDataType + " mDataContractType = " + this.mDataContractType + " mRowId = " + this.mRowId + " mChatId = " + this.mChatId + " mOperation = " + this.mOperation + " mLine = " + IMSLog.checker(this.mLine) + " mCorrelationTag = " + this.mCorrelationTag + " mCorrelationId = " + this.mCorrelationId + " mGroupSMS = " + this.mIsGroupSMS + " mVvmUpdate = " + this.mVvmUpdate + "]";
    }
}
