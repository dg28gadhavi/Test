package com.sec.internal.interfaces.ims.servicemodules.presence;

import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.List;

public interface IPresenceModule extends IServiceModule {
    int getListSubExpiry(int i);

    int getListSubMaxUri(int i);

    PresenceInfo getOwnPresenceInfo(int i);

    boolean getParalysed(int i);

    PresenceInfo getPresenceInfo(ImsUri imsUri, int i);

    PresenceInfo getPresenceInfoByContactId(String str, int i);

    int getPublishExpiry(int i);

    int getPublishSourceThrottle(int i);

    int getPublishTimer(int i);

    int isListSubGzipEnabled(int i);

    boolean isOwnPresenceInfoHasTuple(int i, long j);

    void removePresenceCache(List<ImsUri> list, int i);

    void setDisplayText(int i, String str);

    void setParalysed(boolean z, int i);

    void unpublish(int i);
}
