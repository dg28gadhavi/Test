package com.sec.internal.helper;

import android.os.Message;
import android.os.SystemClock;

public class DelayedMessage implements Comparable<DelayedMessage> {
    private final Message msg;
    private final long timeout;

    public DelayedMessage(Message message, long j) {
        this.msg = message;
        this.timeout = j;
    }

    public long getTimeout() {
        return this.timeout;
    }

    public Message getMsg() {
        return this.msg;
    }

    public int compareTo(DelayedMessage delayedMessage) {
        return (int) (this.timeout - delayedMessage.timeout);
    }

    public int hashCode() {
        Message message = this.msg;
        if (message != null) {
            return 7 + message.hashCode();
        }
        return 1;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DelayedMessage delayedMessage = (DelayedMessage) obj;
        Message message = this.msg;
        if (message == null) {
            if (delayedMessage.msg == null) {
                return true;
            }
            return false;
        } else if (message.what == delayedMessage.msg.what && message.getTarget() == delayedMessage.msg.getTarget() && this.msg.arg1 == delayedMessage.msg.arg1) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return "DelayedMessage: [target: " + this.msg.getTarget() + ", what: " + this.msg.what + ", timeout: " + this.timeout + " (will expired in " + (this.timeout - SystemClock.elapsedRealtime()) + "msec)]";
    }
}
