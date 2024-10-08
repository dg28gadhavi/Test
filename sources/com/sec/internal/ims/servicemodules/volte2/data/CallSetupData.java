package com.sec.internal.ims.servicemodules.volte2.data;

import android.os.Bundle;
import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class CallSetupData {
    private String mAlertInfo;
    private int mCallSetupError = -1;
    private int mCallType;
    private String mCli;
    private int mCmcBoundSessionId = -1;
    private int mCmcEdCallSlot = -1;
    private Bundle mComposerData = null;
    private ImsUri mDestUri;
    private String mDialingNumber;
    private String mIdcExtra = null;
    private boolean mIsEmergency = false;
    private boolean mIsLteEpsOnlyAttached = false;
    private String mLetteringText;
    private ImsUri mOrigUri;
    private List<String> mP2p = null;
    private String mPEmergencyInfo;
    private String mReplaceCallId = null;

    private static boolean isE911Call(int i) {
        return i == 7 || i == 8 || i == 13 || i == 18 || i == 19;
    }

    public CallSetupData(ImsUri imsUri, String str, int i, String str2) {
        this.mDestUri = imsUri;
        this.mDialingNumber = str;
        this.mCallType = i;
        this.mCli = str2;
        this.mIsEmergency = isE911Call(i);
    }

    public void setOriginatingUri(ImsUri imsUri) {
        this.mOrigUri = imsUri;
    }

    public void setLetteringText(String str) {
        this.mLetteringText = str;
    }

    public void setAlertInfo(String str) {
        this.mAlertInfo = str;
    }

    public void setCli(String str) {
        this.mCli = str;
    }

    public void setP2p(List<String> list) {
        this.mP2p = list;
    }

    public void setLteEpsOnlyAttached(boolean z) {
        this.mIsLteEpsOnlyAttached = z;
    }

    public void setCmcBoundSessionId(int i) {
        this.mCmcBoundSessionId = i;
    }

    public void setCallSetupError(int i) {
        this.mCallSetupError = i;
    }

    public void setCmcEdCallSlot(int i) {
        this.mCmcEdCallSlot = i;
    }

    public void setIdcExtra(String str) {
        this.mIdcExtra = str;
    }

    public ImsUri getOriginatingUri() {
        return this.mOrigUri;
    }

    public ImsUri getDestinationUri() {
        return this.mDestUri;
    }

    public String getDialingNumber() {
        return this.mDialingNumber;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public String getLetteringText() {
        return this.mLetteringText;
    }

    public String getAlertInfo() {
        return this.mAlertInfo;
    }

    public String getCli() {
        return this.mCli;
    }

    public List<String> getP2p() {
        return this.mP2p;
    }

    public boolean isEmergency() {
        return this.mIsEmergency;
    }

    public void setPEmergencyInfo(String str) {
        this.mPEmergencyInfo = str;
    }

    public String getPEmergencyInfo() {
        return this.mPEmergencyInfo;
    }

    public boolean getLteEpsOnlyAttached() {
        return this.mIsLteEpsOnlyAttached;
    }

    public int getCmcBoundSessionId() {
        return this.mCmcBoundSessionId;
    }

    public int getCallSetupError() {
        return this.mCallSetupError;
    }

    public Bundle getComposerData() {
        return this.mComposerData;
    }

    public void setComposerData(Bundle bundle) {
        this.mComposerData = bundle;
    }

    public String getReplaceCallId() {
        return this.mReplaceCallId;
    }

    public void setReplaceCallId(String str) {
        this.mReplaceCallId = str;
    }

    public int getCmcEdCallSlot() {
        return this.mCmcEdCallSlot;
    }

    public String getIdcExtra() {
        return this.mIdcExtra;
    }

    public String toString() {
        return "CallSetupData [mOrigUri=" + IMSLog.checker(this.mOrigUri + "") + ", mDestUri=" + IMSLog.checker(this.mDestUri + "") + ", mDialingNumber=" + IMSLog.checker(this.mDialingNumber) + ", mCallType=" + this.mCallType + ", mLetteringText=" + IMSLog.checker(this.mLetteringText) + ", mIsEmergency=" + this.mIsEmergency + ", mPEmergencyInfo=" + this.mPEmergencyInfo + ", mCli=" + this.mCli + ", mAlertInfo=" + this.mAlertInfo + ", mIsLteEpsOnlyAttached=" + this.mIsLteEpsOnlyAttached + ", mCmcBoundSessionId=" + this.mCmcBoundSessionId + ", mReplaceCallId=" + this.mReplaceCallId + ", mCmcEdCallSlot=" + this.mCmcEdCallSlot + ", mIdcExtra=" + this.mIdcExtra + "]";
    }
}
