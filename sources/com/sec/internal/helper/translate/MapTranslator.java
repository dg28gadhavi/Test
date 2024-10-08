package com.sec.internal.helper.translate;

import java.util.Map;

public class MapTranslator<T, S> {
    private Map<T, S> mMap;

    public MapTranslator(Map<T, S> map) {
        this.mMap = map;
    }

    public S translate(T t) {
        return this.mMap.get(t);
    }
}
