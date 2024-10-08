package org.xbill.DNS;

public class MRRecord extends SingleNameBase {
    private static final long serialVersionUID = -5617939094209927533L;

    MRRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MRRecord();
    }
}
