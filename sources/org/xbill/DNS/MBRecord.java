package org.xbill.DNS;

public class MBRecord extends SingleNameBase {
    private static final long serialVersionUID = 532349543479150419L;

    MBRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MBRecord();
    }
}
