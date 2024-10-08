package com.sec.internal.ims.servicemodules.im;

import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.listener.ImMessageListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ImMessage extends MessageBase {
    private static final String LOG_TAG = ImMessage.class.getSimpleName();
    private final Set<ImsUri> mGroupCcListUri = new HashSet();
    private final ImMessageListener mListener;

    public String getServiceTag() {
        return "IM";
    }

    protected ImMessage(Builder<?> builder) {
        super(builder);
        this.mListener = builder.mListener;
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    public void onSendMessageDone(Result result, IMnoStrategy.StrategyResponse strategyResponse) {
        String str = LOG_TAG;
        Log.i(str, "onSendMessageDone() : mid = " + this.mId + ", mStatus = " + this.mStatus + ", mBody = " + IMSLog.checker(this.mBody));
        if (result.getImError() == ImError.SUCCESS) {
            ImConstants.Status status = this.mStatus;
            ImConstants.Status status2 = ImConstants.Status.SENT;
            if (status != status2) {
                setSentTimestamp(System.currentTimeMillis());
                updateStatus(status2);
                this.mListener.onMessageSendingSucceeded(this);
                return;
            }
            return;
        }
        updateStatus(ImConstants.Status.FAILED);
        this.mListener.onMessageSendingFailed(this, strategyResponse, result);
    }

    public void onReceived() {
        this.mListener.onMessageReceived(this);
    }

    public void onSendMessageResponseTimeout() {
        this.mListener.onMessageSendResponseTimeout(this);
    }

    public Set<ImsUri> getGroupCcListUri() {
        return this.mGroupCcListUri;
    }

    public void setGroupCcListUri(Collection<ImsUri> collection) {
        this.mGroupCcListUri.addAll(collection);
    }

    public void setSlmSvcMsg(boolean z) {
        this.mIsSlmSvcMsg = z;
        if (z) {
            setMessagingTech(getBody().length() > this.mConfig.getPagerModeLimit() ? ImConstants.MessagingTech.SLM_LARGE_MODE : ImConstants.MessagingTech.SLM_PAGER_MODE);
        } else {
            setMessagingTech(ImConstants.MessagingTech.NORMAL);
        }
    }

    public String toString() {
        return "ImMessage [" + super.toString() + "]";
    }

    public static abstract class Builder<T extends Builder<T>> extends MessageBase.Builder<T> {
        /* access modifiers changed from: private */
        public ImMessageListener mListener;

        public T listener(ImMessageListener imMessageListener) {
            this.mListener = imMessageListener;
            return (Builder) self();
        }

        public ImMessage build() {
            Preconditions.checkNotNull(this.mListener);
            return new ImMessage(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        /* access modifiers changed from: protected */
        public Builder2 self() {
            return this;
        }

        private Builder2() {
        }
    }
}
