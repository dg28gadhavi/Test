package com.sec.internal.ims.servicemodules.im.data;

import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import java.util.Objects;

public class MessageKey {
    public final String chatId;
    public final ImDirection direction;
    public final String imdnId;

    public MessageKey(String str, ImDirection imDirection, String str2) {
        this.imdnId = str;
        this.direction = imDirection;
        this.chatId = imDirection != ImDirection.INCOMING ? null : str2;
    }

    public boolean equals(Object obj) {
        String str;
        if (!(obj instanceof MessageKey)) {
            return false;
        }
        MessageKey messageKey = (MessageKey) obj;
        if (!Objects.equals(messageKey.imdnId, this.imdnId) || !Objects.equals(messageKey.direction, this.direction)) {
            return false;
        }
        String str2 = this.chatId;
        if (str2 == null || (str = messageKey.chatId) == null || Objects.equals(str, str2)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        String str = this.imdnId;
        int i = 0;
        int hashCode = str == null ? 0 : str.hashCode();
        ImDirection imDirection = this.direction;
        if (imDirection != null) {
            i = imDirection.hashCode();
        }
        return hashCode ^ i;
    }
}
