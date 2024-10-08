package com.sec.internal.ims.core.cmc;

import android.util.Log;
import com.sec.internal.ims.core.cmc.CmcInfo;
import java.util.HashSet;
import java.util.Set;

public class CmcInfoUpdateResult {
    private static String LOG_TAG = "CmcInfoUpdateResult";
    private Set<CmcInfo.CmcInfoType> mChangedCmcInfoList = new HashSet();
    private boolean mForceUpdate = false;
    private String mProfileUpdateReason = "";
    private ProfileUpdateResult mProfileUpdatedResult = ProfileUpdateResult.FAILED;

    enum ProfileUpdateResult {
        UPDATED,
        NOT_UPDATED,
        FAILED
    }

    public ProfileUpdateResult getProfileUpdatedResult() {
        return this.mProfileUpdatedResult;
    }

    public void setProfileUpdatedResult(ProfileUpdateResult profileUpdateResult) {
        if (this.mForceUpdate && profileUpdateResult == ProfileUpdateResult.NOT_UPDATED) {
            Log.i(LOG_TAG, "setProfileUpdatedResult: force update");
            profileUpdateResult = ProfileUpdateResult.UPDATED;
        }
        this.mForceUpdate = false;
        this.mProfileUpdatedResult = profileUpdateResult;
    }

    public boolean isFailed() {
        return getProfileUpdatedResult() == ProfileUpdateResult.FAILED;
    }

    public boolean isNotUpdated() {
        return getProfileUpdatedResult() == ProfileUpdateResult.NOT_UPDATED;
    }

    public boolean isUpdated() {
        return getProfileUpdatedResult() == ProfileUpdateResult.UPDATED;
    }

    public Set<CmcInfo.CmcInfoType> getChangedCmcInfoList() {
        return this.mChangedCmcInfoList;
    }

    public void addChangedCmcInfo(CmcInfo.CmcInfoType cmcInfoType) {
        this.mChangedCmcInfoList.add(cmcInfoType);
    }

    public void clearChangedCmcInfoList() {
        this.mChangedCmcInfoList.clear();
    }

    public String getProfileUpdateReason() {
        return this.mProfileUpdateReason;
    }

    public void setProfileUpdateReason(String str) {
        this.mProfileUpdateReason = str;
    }

    public void setForceUpdate() {
        this.mForceUpdate = true;
    }
}
