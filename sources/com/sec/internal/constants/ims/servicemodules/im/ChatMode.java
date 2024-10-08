package com.sec.internal.constants.ims.servicemodules.im;

public enum ChatMode implements IEnumerationWithId<ChatMode> {
    OFF(0),
    ON(1),
    LINK(2);
    
    private static final ReverseEnumMap<ChatMode> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(ChatMode.class);
    }

    private ChatMode(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public ChatMode getFromId(int i) {
        return map.get(Integer.valueOf(i));
    }

    public static ChatMode fromId(int i) {
        return map.get(Integer.valueOf(i));
    }
}
