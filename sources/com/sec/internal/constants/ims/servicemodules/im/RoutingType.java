package com.sec.internal.constants.ims.servicemodules.im;

public enum RoutingType implements IEnumerationWithId<RoutingType> {
    NONE(0),
    SENT(1),
    RECEIVED(2);
    
    private static final ReverseEnumMap<RoutingType> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(RoutingType.class);
    }

    private RoutingType(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public RoutingType getFromId(int i) {
        return map.get(Integer.valueOf(i));
    }
}
