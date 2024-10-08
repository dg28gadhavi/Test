package com.sec.internal.interfaces.ims.servicemodules.gls;

import android.location.Location;
import android.net.Uri;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public interface IGlsModule extends IServiceModule {
    void acceptLocationShare(String str, String str2, Uri uri);

    void cancelLocationShare(String str, ImDirection imDirection, String str2);

    Future<FtMessage> createInCallLocationShare(String str, ImsUri imsUri, Set<NotificationStatus> set, Location location, String str2, String str3, boolean z, boolean z2);

    void deleteGeolocSharings(List<String> list);

    int getPhoneIdByChatId(String str);

    int getPhoneIdByImdnId(String str, ImDirection imDirection);

    int getPhoneIdByMessageId(int i);

    void registerFtEventListener(ImConstants.Type type, IFtEventListener iFtEventListener);

    void registerMessageEventListener(ImConstants.Type type, IMessageEventListener iMessageEventListener);

    void rejectLocationShare(String str, String str2);

    Future<ImMessage> shareLocationInChat(int i, String str, Set<NotificationStatus> set, Location location, String str2, String str3, String str4, ImsUri imsUri, boolean z, String str5);

    Future<ImMessage> shareLocationInChat(String str, Set<NotificationStatus> set, Location location, String str2, String str3, String str4, ImsUri imsUri, boolean z, String str5);

    void startLocationShareInCall(String str);
}
