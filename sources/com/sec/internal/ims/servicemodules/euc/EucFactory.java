package com.sec.internal.ims.servicemodules.euc;

import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEuc;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucAcknowledgment;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucNotification;
import com.sec.internal.ims.servicemodules.euc.data.resip.IEucRequest;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class EucFactory implements IEucFactory {
    EucFactory() {
    }

    public Iterable<IEucQuery> combine(List<IEucData> list, List<IDialogData> list2) {
        HashMap hashMap = new HashMap();
        for (IEucData eUCQuery : list) {
            EUCQuery eUCQuery2 = new EUCQuery(eUCQuery);
            hashMap.put(eUCQuery2.getEucData().getKey(), eUCQuery2);
        }
        for (IDialogData next : list2) {
            IEucQuery iEucQuery = (IEucQuery) hashMap.get(next.getKey());
            Preconditions.checkNotNull(iEucQuery, "Database Integrity Error");
            iEucQuery.addDialogData(next);
        }
        return hashMap.values();
    }

    public IEucQuery createEUC(IEucRequest iEucRequest) {
        EucType eucType;
        Long l;
        Preconditions.checkNotNull(iEucRequest);
        if (iEucRequest.getType() == IEucRequest.EucRequestType.PERSISTENT) {
            eucType = EucType.PERSISTENT;
        } else {
            eucType = EucType.VOLATILE;
        }
        EucType eucType2 = eucType;
        if (iEucRequest.getTimeOut() == null) {
            l = null;
        } else {
            l = Long.valueOf(iEucRequest.getTimestamp() + (iEucRequest.getTimeOut().longValue() * 1000));
        }
        IEucRequest iEucRequest2 = iEucRequest;
        IEucData createEucData = createEucData(iEucRequest2, eucType2, iEucRequest.isPinRequested(), iEucRequest.isExternal(), l);
        EUCQuery eUCQuery = new EUCQuery(createEucData);
        for (Map.Entry entry : iEucRequest.getLanguageMapping().entrySet()) {
            eUCQuery.addDialogData(createDialogData((IEucRequest.IEucMessageData) entry.getValue(), createEucData.getKey(), (String) entry.getKey()));
        }
        eUCQuery.addDialogData(createDialogData((IEucRequest.IEucMessageData) iEucRequest.getDefaultData(), createEucData.getKey(), DeviceLocale.DEFAULT_LANG_VALUE));
        return eUCQuery;
    }

    public IEucQuery createEUC(IEucNotification iEucNotification) {
        Preconditions.checkNotNull(iEucNotification);
        IEucData createEucData = createEucData(iEucNotification, EucType.NOTIFICATION, false, false, (Long) null);
        EUCQuery eUCQuery = new EUCQuery(createEucData);
        for (Map.Entry entry : iEucNotification.getLanguageMapping().entrySet()) {
            eUCQuery.addDialogData(createDialogData((IEucNotification.IEucMessageData) entry.getValue(), createEucData.getKey(), (String) entry.getKey()));
        }
        eUCQuery.addDialogData(createDialogData((IEucNotification.IEucMessageData) iEucNotification.getDefaultData(), createEucData.getKey(), DeviceLocale.DEFAULT_LANG_VALUE));
        return eUCQuery;
    }

    public IEucQuery createEUC(IEucAcknowledgment iEucAcknowledgment) {
        Preconditions.checkNotNull(iEucAcknowledgment);
        IEucData createEucData = createEucData(iEucAcknowledgment, EucType.ACKNOWLEDGEMENT, false, false, (Long) null);
        EUCQuery eUCQuery = new EUCQuery(createEucData);
        for (Map.Entry entry : iEucAcknowledgment.getLanguageMapping().entrySet()) {
            eUCQuery.addDialogData(createDialogData((IEucAcknowledgment.IEUCMessageData) entry.getValue(), createEucData.getKey(), (String) entry.getKey()));
        }
        if (iEucAcknowledgment.getDefaultData() != null) {
            eUCQuery.addDialogData(createDialogData(iEucAcknowledgment.getDefaultData(), createEucData.getKey(), DeviceLocale.DEFAULT_LANG_VALUE));
        }
        return eUCQuery;
    }

    private <T> IEucData createEucData(IEuc<T> iEuc, EucType eucType, boolean z, boolean z2, Long l) {
        return createEucData(new EucMessageKey(iEuc.getEucId(), iEuc.getOwnIdentity(), eucType, iEuc.getFromHeader()), z, (String) null, z2, EucState.NONE, iEuc.getTimestamp(), l);
    }

    public IEucData createEucData(EucMessageKey eucMessageKey, boolean z, String str, boolean z2, EucState eucState, long j, Long l) {
        final EucMessageKey eucMessageKey2 = eucMessageKey;
        final boolean z3 = z;
        final boolean z4 = z2;
        final EucState eucState2 = eucState;
        final long j2 = j;
        final Long l2 = l;
        final String str2 = str;
        return new IEucData() {
            public EucMessageKey getKey() {
                return eucMessageKey2;
            }

            public String getId() {
                return eucMessageKey2.getEucId();
            }

            public boolean getPin() {
                return z3;
            }

            public boolean getExternal() {
                return z4;
            }

            public EucState getState() {
                return eucState2;
            }

            public EucType getType() {
                return eucMessageKey2.getEucType();
            }

            public ImsUri getRemoteUri() {
                return eucMessageKey2.getRemoteUri();
            }

            public String getOwnIdentity() {
                return eucMessageKey2.getOwnIdentity();
            }

            public long getTimestamp() {
                return j2;
            }

            public Long getTimeOut() {
                return l2;
            }

            public String getUserPin() {
                return str2;
            }
        };
    }

    public IDialogData createDialogData(EucMessageKey eucMessageKey, String str, String str2, String str3, String str4, String str5) {
        final EucMessageKey eucMessageKey2 = eucMessageKey;
        final String str6 = str;
        final String str7 = str2;
        final String str8 = str3;
        final String str9 = str4;
        final String str10 = str5;
        return new IDialogData() {
            public EucMessageKey getKey() {
                return eucMessageKey2;
            }

            public String getLanguage() {
                return str6;
            }

            public String getSubject() {
                return str7;
            }

            public String getText() {
                return str8;
            }

            public String getAcceptButton() {
                return str9;
            }

            public String getRejectButton() {
                return str10;
            }
        };
    }

    private IDialogData createDialogData(IEucRequest.IEucMessageData iEucMessageData, EucMessageKey eucMessageKey, String str) {
        return createDialogData(eucMessageKey, str, iEucMessageData.getSubject(), iEucMessageData.getText(), iEucMessageData.getAcceptButton(), iEucMessageData.getRejectButton());
    }

    private IDialogData createDialogData(IEucNotification.IEucMessageData iEucMessageData, EucMessageKey eucMessageKey, String str) {
        return createDialogData(eucMessageKey, str, iEucMessageData.getSubject(), iEucMessageData.getText(), iEucMessageData.getOkButton(), (String) null);
    }

    private IDialogData createDialogData(IEucAcknowledgment.IEUCMessageData iEUCMessageData, EucMessageKey eucMessageKey, String str) {
        return createDialogData(eucMessageKey, str, iEUCMessageData.getSubject(), iEUCMessageData.getText(), (String) null, (String) null);
    }

    private static class EUCQuery implements IEucQuery {
        private final Map<String, IDialogData> mDialogMap = new HashMap();
        private IEucData mEUCData;

        EUCQuery(IEucData iEucData) {
            this.mEUCData = iEucData;
        }

        public void addDialogData(IDialogData iDialogData) {
            this.mDialogMap.put(iDialogData.getLanguage(), iDialogData);
        }

        public IEucData getEucData() {
            return this.mEUCData;
        }

        public IDialogData getDialogData(String str) {
            IDialogData iDialogData = this.mDialogMap.get(str);
            return iDialogData == null ? this.mDialogMap.get(DeviceLocale.DEFAULT_LANG_VALUE) : iDialogData;
        }

        public boolean hasDialog(String str) {
            return this.mDialogMap.containsKey(str);
        }

        public Iterator<IDialogData> iterator() {
            return this.mDialogMap.values().iterator();
        }
    }
}
