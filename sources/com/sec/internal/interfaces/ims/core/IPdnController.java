package com.sec.internal.interfaces.ims.core;

import android.net.Network;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import com.sec.internal.constants.ims.os.EmcBsIndication;
import com.sec.internal.constants.ims.os.NetworkState;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.os.CellIdentityWrapper;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import java.util.List;

public interface IPdnController extends ISequentialInitializable {
    public static final int APN_REQUEST_STARTED = 1;
    public static final int APN_TYPE_NOT_AVAILABLE = 2;

    List<CellInfo> getAllCellInfo(int i, boolean z);

    CellIdentity getCellIdentity(int i, boolean z);

    CellIdentityWrapper getCurrentCellIdentity(int i, int i2);

    List<String> getDnsServers(PdnEventListener pdnEventListener);

    EmcBsIndication getEmcBsIndication(int i);

    int getEpdgPhysicalInterface(int i);

    String getInterfaceName(PdnEventListener pdnEventListener);

    String getIntfNameByNetType(Network network);

    LinkPropertiesWrapper getLinkProperties(PdnEventListener pdnEventListener);

    int getMobileDataRegState(int i);

    NetworkState getNetworkState(int i);

    int getVoiceRegState(int i);

    VoPsIndication getVopsIndication(int i);

    boolean isConnected(int i, PdnEventListener pdnEventListener);

    boolean isDisconnecting();

    boolean isEmergencyEpdgConnected(int i);

    boolean isEpdgConnected(int i);

    boolean isEpsOnlyReg(int i);

    boolean isNetworkRequested(PdnEventListener pdnEventListener);

    boolean isPendedEPDGWeakSignal(int i);

    boolean isVoiceRoaming(int i);

    boolean isWifiConnected();

    void registerForNetworkState(NetworkStateListener networkStateListener);

    boolean removeRouteToHostAddress(int i, String str);

    boolean requestRouteToHostAddress(int i, String str);

    void setPendedEPDGWeakSignal(int i, boolean z);

    int startPdnConnectivity(int i, PdnEventListener pdnEventListener, int i2);

    int stopPdnConnectivity(int i, int i2, PdnEventListener pdnEventListener);

    void unregisterForNetworkState(NetworkStateListener networkStateListener);
}
