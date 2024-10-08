package com.sec.internal.ims.servicemodules.volte2.data;

import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.log.IMSLog;

public class RelayStreams {
    private static final String LOG_TAG = "RelayStreams";
    private int mBoundStreamId = -1;
    private int mCmcType = 0;
    private int mRelayChannelId = -1;
    private int mSessionId = -1;
    private int mStreamId = -1;

    public RelayStreams() {
        IMSLog.i(LOG_TAG, LOG_TAG);
    }

    public RelayStreams(int i, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, "streamId: " + i + " sessionId: " + i2);
        this.mStreamId = i;
        this.mSessionId = i2;
    }

    public RelayStreams(IMSMediaEvent iMSMediaEvent, int i) {
        String str = LOG_TAG;
        IMSLog.i(str, "streamId: " + iMSMediaEvent.getStreamId() + " sessionId: " + iMSMediaEvent.getSessionID() + " state: " + iMSMediaEvent.getRelayStreamEvent() + " cmcType: " + i);
        this.mStreamId = iMSMediaEvent.getStreamId();
        this.mSessionId = iMSMediaEvent.getSessionID();
        this.mCmcType = i;
    }

    public int getStreamId() {
        return this.mStreamId;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public int getCmcType() {
        return this.mCmcType;
    }

    public void setBoundStreamId(int i) {
        this.mBoundStreamId = i;
    }

    public int getBoundStreamId() {
        return this.mBoundStreamId;
    }

    public void setRelayChannelId(int i) {
        this.mRelayChannelId = i;
    }

    public int getRelayChannelId() {
        return this.mRelayChannelId;
    }

    public String toString() {
        return "RelayStreams [mStreamId=" + this.mStreamId + ", mSessionId=" + this.mSessionId + ", mBoundStreamId=" + this.mBoundStreamId + ", mRelayChannelId=" + this.mRelayChannelId + "]";
    }
}
