package com.sec.internal.constants.ims.servicemodules.im;

public enum ImDirection implements IEnumerationWithId<ImDirection> {
    INCOMING(0),
    OUTGOING(1),
    IRRELEVANT(2);
    
    private static final ReverseEnumMap<ImDirection> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(ImDirection.class);
    }

    private ImDirection(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public ImDirection getFromId(int i) {
        return map.get(Integer.valueOf(i));
    }

    public static ImDirection fromId(int i) {
        return map.get(Integer.valueOf(i));
    }
}
