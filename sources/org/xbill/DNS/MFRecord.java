package org.xbill.DNS;

public class MFRecord extends SingleNameBase {
    private static final long serialVersionUID = -6670449036843028169L;

    MFRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MFRecord();
    }
}
