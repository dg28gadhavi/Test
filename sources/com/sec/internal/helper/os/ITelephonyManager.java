package com.sec.internal.helper.os;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.ServiceState;
import android.telephony.TelephonyCallback;
import android.telephony.emergency.EmergencyNumber;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public interface ITelephonyManager {
    String checkCallControl(String str, int i);

    void clearCache();

    String getAidForAppType(int i);

    String getAidForAppType(int i, int i2);

    List<CellInfo> getAllCellInfo();

    List<CellInfo> getAllCellInfoBySubId(int i);

    String getBtid(int i);

    int getCallState();

    int getCallState(int i);

    int getCurrentPhoneTypeForSlot(int i);

    int getDataNetworkType();

    int getDataNetworkType(int i);

    int getDataServiceState(int i);

    String getDeviceSoftwareVersion(int i);

    Map<Integer, List<EmergencyNumber>> getEmergencyNumberList();

    String getGid2(int i);

    String getGroupIdLevel1();

    String getGroupIdLevel1(int i);

    String getIccAuthentication(int i, int i2, int i3, String str);

    String getImei();

    String getImei(int i);

    String getIsimDomain();

    String getIsimDomain(int i);

    String getIsimImpi(int i);

    String[] getIsimImpu(int i);

    String[] getIsimPcscf();

    String getKeyLifetime(int i);

    String getLine1Number();

    String getLine1Number(int i);

    String getMeid(int i);

    String getMsisdn();

    String getMsisdn(int i);

    String getNetworkCountryIso();

    String getNetworkCountryIso(int i);

    String getNetworkOperator(int i);

    String getNetworkOperatorForPhone(int i);

    int getNetworkType();

    long getNextRetryTime();

    int getPhoneCount();

    int getPreferredNetworkType(int i);

    byte[] getRand(int i);

    String getRilSimOperator(int i);

    int getServiceState();

    ServiceState getServiceState(int i);

    int getServiceStateForSubscriber(int i);

    String getSimCountryIso();

    String getSimCountryIsoForPhone(int i);

    String getSimCountryIsoForSubId(int i);

    String getSimOperator();

    String getSimOperator(int i);

    String getSimOperatorName(int i);

    String getSimSerialNumber();

    String getSimSerialNumber(int i);

    int getSimState();

    int getSimState(int i);

    String getSubscriberId(int i);

    String getSubscriberIdForUiccAppType(int i, int i2);

    String getTelephonyProperty(int i, String str, String str2);

    int getVoiceNetworkType();

    int getVoiceNetworkType(int i);

    boolean hasCall(String str);

    boolean iccCloseLogicalChannel(int i, int i2);

    int iccOpenLogicalChannelAndGetChannel(int i, String str);

    String iccTransmitApduLogicalChannel(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str);

    boolean isGbaSupported();

    boolean isGbaSupported(int i);

    boolean isNetworkRoaming();

    boolean isNetworkRoaming(int i);

    boolean isVoiceCapable();

    void registerTelephonyCallback(Executor executor, TelephonyCallback telephonyCallback);

    void registerTelephonyCallbackForSlot(int i, Executor executor, TelephonyCallback telephonyCallback);

    int semGetDataState(int i);

    int semGetNrMode(int i);

    String semGetTelephonyProperty(int i, String str, String str2);

    boolean semIsVoNrEnabled(int i);

    void semSetNrMode(int i, int i2);

    void sendRawRequestToTelephony(Context context, byte[] bArr);

    void setCallState(int i);

    void setGbaBootstrappingParams(int i, byte[] bArr, String str, String str2);

    void setGbaBootstrappingParams(byte[] bArr, String str, String str2);

    boolean setPreferredNetworkType(int i, int i2);

    void setRadioPower(boolean z);

    void unregisterTelephonyCallback(TelephonyCallback telephonyCallback);

    void unregisterTelephonyCallbackForSlot(int i, TelephonyCallback telephonyCallback);

    boolean validateMsisdn(int i);
}
