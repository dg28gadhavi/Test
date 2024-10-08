package com.sec.internal.ims.cmstore.params;

import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;

public class ParamVvmUpdate {
    @SerializedName("duration")
    public int mDuration;
    @SerializedName("email1")
    public String mEmail1;
    @SerializedName("email2")
    public String mEmail2;
    @SerializedName("greeting_type")
    public String mGreetingType;
    @SerializedName("filePath")
    public String mGreetingUri;
    @SerializedName("id")
    public int mId;
    @SerializedName("preferred_line")
    public String mLine;
    @SerializedName("mimeType")
    public String mMimeType;
    @SerializedName("new")
    public String mNewPwd;
    @SerializedName("old")
    public String mOldPwd;
    @SerializedName("type")
    public String mType;
    @SerializedName("v2tlang")
    public String mV2tLang;
    public VvmTypeChange mVvmChange;
    @SerializedName("fileName")
    public String mfileName;
    @SerializedName("v2t_email")
    public String mv2t_email;
    @SerializedName("v2t_sms")
    public String mv2t_sms;

    public enum VvmTypeChange {
        ACTIVATE(0),
        DEACTIVATE(1),
        VOICEMAILTOTEXT(2),
        GREETING(3),
        PIN(4),
        FULLPROFILE(5),
        NUTOFF(6),
        NUTON(7),
        V2TLANGUAGE(8),
        V2T_SMS(9),
        V2T_EMAIL(10),
        ADHOC_V2T(11);
        
        private final int mId;

        private VvmTypeChange(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum GreetingOnFlag {
        GreetingOff(0),
        GreetingOn(1);
        
        private final int mId;

        private GreetingOnFlag(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum VvmGreetingType {
        Default(0, ""),
        Name(1, "voice-signature"),
        Custom(2, "normal-greeting"),
        Busy(3, "busy-greeting"),
        ExtendAbsence(4, "extended-absence-greeting"),
        Fun(5, "fun-greeting");
        
        private final int mId;
        private String mName;

        private VvmGreetingType(int i, String str) {
            this.mId = i;
            this.mName = str;
        }

        public int getId() {
            return this.mId;
        }

        public String getName() {
            return this.mName;
        }

        public static VvmGreetingType valueOf(int i) {
            return (VvmGreetingType) Arrays.stream(values()).filter(new ParamVvmUpdate$VvmGreetingType$$ExternalSyntheticLambda1(i)).findFirst().get();
        }

        public static int nameOf(String str) {
            return ((VvmGreetingType) Arrays.stream(values()).filter(new ParamVvmUpdate$VvmGreetingType$$ExternalSyntheticLambda0(str)).findFirst().get()).getId();
        }
    }

    public String toString() {
        String str = null;
        String str2 = !TextUtils.isEmpty(this.mOldPwd) ? "xxxx" : null;
        if (!TextUtils.isEmpty(this.mNewPwd)) {
            str = "****";
        }
        return "ParamVvmUpdate [mVvmChange= " + this.mVvmChange + " mGreetingUri = " + IMSLog.checker(this.mGreetingUri) + " mLine = " + IMSLog.checker(this.mLine) + " mOldPwd = " + str2 + " mNewPwd = " + str + " mEmail1 = " + IMSLog.checker(this.mEmail1) + " mEmail2 = " + IMSLog.checker(this.mEmail2) + " mDuration = " + this.mDuration + " mType = " + this.mType + " mId = " + this.mId + " mMimeType = " + this.mMimeType + " mfileName = " + this.mfileName + " mGreetingType = " + this.mGreetingType + " mV2tLang = " + this.mV2tLang + "]";
    }
}
