package com.sec.internal.ims.servicemodules.volte2.data;

import com.sec.internal.log.IMSLog;

public class RelayChannel {
    private static final String LOG_TAG = "RelayChannel";
    private int mChannelId = -1;
    private RelayStreams mExtStream;
    private RelayStreams mIntStream;

    public RelayChannel() {
        IMSLog.i(LOG_TAG, "RelayChannel created");
    }

    public RelayChannel(RelayStreams relayStreams, RelayStreams relayStreams2, int i) {
        String str = LOG_TAG;
        IMSLog.i(str, "RelayChannel created channelId: " + i);
        this.mIntStream = relayStreams;
        this.mExtStream = relayStreams2;
        this.mChannelId = i;
    }

    public void setIntStream(RelayStreams relayStreams) {
        this.mIntStream = relayStreams;
    }

    public RelayStreams getIntStream() {
        return this.mIntStream;
    }

    public void setExtStream(RelayStreams relayStreams) {
        this.mExtStream = relayStreams;
    }

    public RelayStreams getExtStream() {
        return this.mExtStream;
    }

    public int getChannelId() {
        return this.mChannelId;
    }

    public String toString() {
        return "RelayChannel [mIntStream=" + this.mIntStream + ", mExtStream=" + this.mExtStream + ", mChannelId=" + this.mChannelId + "]";
    }
}
