package com.sec.internal.ims.servicemodules.euc.cache;

import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EucCache implements IEucCache {
    private Map<EucMessageKey, IEucQuery> mEucrMap = new HashMap();

    public void put(IEucQuery iEucQuery) {
        IEucData eucData = iEucQuery.getEucData();
        this.mEucrMap.put(new EucMessageKey(eucData.getId(), eucData.getOwnIdentity(), eucData.getType(), eucData.getRemoteUri()), iEucQuery);
    }

    public IEucQuery get(EucMessageKey eucMessageKey) {
        return this.mEucrMap.get(eucMessageKey);
    }

    public Iterable<IEucQuery> getAllByType(EucType eucType) {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry next : this.mEucrMap.entrySet()) {
            if (eucType == ((EucMessageKey) next.getKey()).getEucType()) {
                arrayList.add((IEucQuery) next.getValue());
            }
        }
        return arrayList;
    }

    public IEucQuery remove(EucMessageKey eucMessageKey) {
        return this.mEucrMap.remove(eucMessageKey);
    }

    public void clearAllForOwnIdentity(String str) {
        Iterator<Map.Entry<EucMessageKey, IEucQuery>> it = this.mEucrMap.entrySet().iterator();
        while (it.hasNext()) {
            if (str.equals(((EucMessageKey) it.next().getKey()).getOwnIdentity())) {
                it.remove();
            }
        }
    }

    public boolean isEmpty() {
        return this.mEucrMap.isEmpty();
    }
}
