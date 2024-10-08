package com.sec.internal.ims.gba;

import java.util.HashMap;
import java.util.Map;

public class GbaStore {
    private Map<Gbakey, GbaValue> map = new HashMap();

    protected GbaStore() {
    }

    public void putKeys(Gbakey gbakey, GbaValue gbaValue) {
        this.map.put(gbakey, gbaValue);
    }

    public GbaValue getKeys(Gbakey gbakey) {
        return this.map.get(gbakey);
    }

    public boolean hasKey(Gbakey gbakey) {
        return this.map.containsKey(gbakey);
    }

    public void removeKey(Gbakey gbakey) {
        this.map.remove(gbakey);
    }
}
