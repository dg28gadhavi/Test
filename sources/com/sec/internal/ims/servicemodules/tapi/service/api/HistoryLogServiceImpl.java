package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.net.Uri;
import android.os.RemoteException;
import com.gsma.services.rcs.history.IHistoryService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HistoryLogServiceImpl extends IHistoryService.Stub {
    private Map<Integer, HistoryLogMember> mExternalProviderMap = new HashMap();
    private Set<Integer> mInternalProviderIds;

    public Map<Integer, HistoryLogMember> getExternalProviderMap() {
        return this.mExternalProviderMap;
    }

    public long createUniqueId(int i) throws RemoteException {
        return ((long) this.mExternalProviderMap.get(Integer.valueOf(i)).getProviderId()) + System.currentTimeMillis();
    }

    public void registerExtraHistoryLogMember(int i, Uri uri, Uri uri2, String str, Map map) throws RemoteException {
        if (this.mExternalProviderMap.containsKey(Integer.valueOf(i))) {
            throw new IllegalArgumentException("Cannot register external database for already registered provider id " + i + "!");
        } else if (getInternalMemberIds().contains(Integer.valueOf(i))) {
            throw new IllegalArgumentException("Cannot register internal database for provider id " + i + "!");
        } else if (uri != null) {
            this.mExternalProviderMap.put(Integer.valueOf(i), new HistoryLogMember(i, uri.toString(), str, map));
        } else {
            throw new IllegalArgumentException("providerUri cannot be null");
        }
    }

    public void unRegisterExtraHistoryLogMember(int i) throws RemoteException {
        this.mExternalProviderMap.remove(Integer.valueOf(i));
    }

    public Set<Integer> getInternalMemberIds() {
        if (this.mInternalProviderIds == null) {
            HashSet hashSet = new HashSet();
            this.mInternalProviderIds = hashSet;
            hashSet.add(1);
            this.mInternalProviderIds.add(2);
            this.mInternalProviderIds.add(3);
            this.mInternalProviderIds.add(4);
            this.mInternalProviderIds.add(5);
        }
        return this.mInternalProviderIds;
    }
}
