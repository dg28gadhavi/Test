package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class ImSubjectData {
    private final ImsUri mParticipant;
    private final String mSubject;
    private final Date mTimestamp;

    public ImSubjectData(String str, ImsUri imsUri, Date date) {
        this.mSubject = str;
        this.mParticipant = imsUri;
        this.mTimestamp = date;
    }

    public String getSubject() {
        return this.mSubject;
    }

    public ImsUri getParticipant() {
        return this.mParticipant;
    }

    public Date getTimestamp() {
        return this.mTimestamp;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImSubjectData imSubjectData = (ImSubjectData) obj;
        if (!this.mSubject.equals(imSubjectData.mSubject) || !this.mParticipant.equals(imSubjectData.mParticipant) || !this.mTimestamp.equals(imSubjectData.mTimestamp)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "ImSubjectData [subject=" + IMSLog.checker(this.mSubject) + ", participant=" + IMSLog.checker(this.mParticipant) + ", timestamp=" + this.mTimestamp + ']';
    }
}
