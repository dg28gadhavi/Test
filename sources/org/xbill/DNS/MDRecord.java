package org.xbill.DNS;

public class MDRecord extends SingleNameBase {
    private static final long serialVersionUID = 5268878603762942202L;

    MDRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MDRecord();
    }
}
