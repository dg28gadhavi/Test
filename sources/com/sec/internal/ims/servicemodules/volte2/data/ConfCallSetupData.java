package com.sec.internal.ims.servicemodules.volte2.data;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfCallSetupData {
    private int mCallType;
    private String mConferenceUri;
    private HashMap<String, String> mExtraSipHeaders;
    private ImsUri mOrigUri;
    private List<String> mParticipants;
    private String mReferRemoveUriType;
    private String mReferUriAsserted;
    private String mReferUriType;
    private List<Integer> mSessionIds;
    private String mSubscribeDialogType;
    private String mSubscribeRequired;
    private boolean mSupportPrematureEnd;
    private String mUseAnonymousUpdate;

    public ConfCallSetupData(String str, int i, int i2, int i3) {
        ArrayList arrayList = new ArrayList();
        this.mSessionIds = arrayList;
        this.mExtraSipHeaders = null;
        this.mConferenceUri = str;
        arrayList.add(Integer.valueOf(i));
        this.mSessionIds.add(Integer.valueOf(i2));
        this.mCallType = i3;
    }

    public ConfCallSetupData(String str, List<String> list, int i) {
        this.mSessionIds = new ArrayList();
        this.mExtraSipHeaders = null;
        this.mConferenceUri = str;
        this.mParticipants = new ArrayList(list);
        this.mCallType = i;
    }

    public String getConferenceUri() {
        return this.mConferenceUri;
    }

    public void setOriginatingUri(ImsUri imsUri) {
        this.mOrigUri = imsUri;
    }

    public ImsUri getOriginatingUri() {
        return this.mOrigUri;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public List<Integer> getSessionIds() {
        return this.mSessionIds;
    }

    public List<String> getParticipants() {
        return this.mParticipants;
    }

    public void enableSubscription(String str) {
        this.mSubscribeRequired = str;
    }

    public String isSubscriptionEnabled() {
        return this.mSubscribeRequired;
    }

    public void setSubscribeDialogType(String str) {
        this.mSubscribeDialogType = str;
    }

    public String getSubscribeDialogType() {
        return this.mSubscribeDialogType;
    }

    public void setReferUriType(String str) {
        this.mReferUriType = str;
    }

    public String getReferUriType() {
        return this.mReferUriType;
    }

    public void setRemoveReferUriType(String str) {
        this.mReferRemoveUriType = str;
    }

    public String getRemoveReferUriType() {
        return this.mReferRemoveUriType;
    }

    public void setReferUriAsserted(String str) {
        this.mReferUriAsserted = str;
    }

    public String getReferUriAsserted() {
        return this.mReferUriAsserted;
    }

    public void setUseAnonymousUpdate(String str) {
        this.mUseAnonymousUpdate = str;
    }

    public String getUseAnonymousUpdate() {
        return this.mUseAnonymousUpdate;
    }

    public void setSupportPrematureEnd(boolean z) {
        this.mSupportPrematureEnd = z;
    }

    public boolean getSupportPrematureEnd() {
        return this.mSupportPrematureEnd;
    }

    public void setExtraSipHeaders(HashMap<String, String> hashMap) {
        this.mExtraSipHeaders = hashMap;
    }

    public HashMap<String, String> getExtraSipHeaders() {
        return this.mExtraSipHeaders;
    }

    public String toString() {
        return "ConfCallSetupData [mConferenceUri=" + IMSLog.checker(this.mConferenceUri) + ", mOrigUri=" + IMSLog.checker(this.mOrigUri + "") + ", mSessionIds=" + this.mSessionIds + ", mParticipants=" + IMSLog.checker(this.mParticipants + "") + ", mCallType=" + this.mCallType + ", mSubscribeRequired=" + this.mSubscribeRequired + ", mSubscribeDialogType=" + this.mSubscribeDialogType + ", mReferUriType=" + this.mReferUriType + ", mReferRemoveUriType=" + this.mReferRemoveUriType + ", use Asserted=" + this.mReferUriAsserted + ", useAnonymousUpdate=" + this.mUseAnonymousUpdate + ", mSupportPrematureEnd=" + this.mSupportPrematureEnd + "]";
    }
}
