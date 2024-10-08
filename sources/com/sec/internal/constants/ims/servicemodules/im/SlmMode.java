package com.sec.internal.constants.ims.servicemodules.im;

public enum SlmMode implements IEnumerationWithId<SlmMode> {
    UNKOWN(0),
    PAGER(1),
    LARGE_MESSAGE(2);
    
    private static final ReverseEnumMap<SlmMode> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(SlmMode.class);
    }

    private SlmMode(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public SlmMode getFromId(int i) {
        return map.get(Integer.valueOf(i));
    }

    public static SlmMode fromId(int i) {
        return map.get(Integer.valueOf(i));
    }
}
