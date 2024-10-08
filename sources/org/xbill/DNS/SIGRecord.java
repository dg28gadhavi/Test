package org.xbill.DNS;

public class SIGRecord extends SIGBase {
    private static final long serialVersionUID = 4963556060953589058L;

    SIGRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new SIGRecord();
    }
}
