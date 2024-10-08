package org.xbill.DNS;

public class NSAP_PTRRecord extends SingleNameBase {
    private static final long serialVersionUID = 2386284746382064904L;

    NSAP_PTRRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSAP_PTRRecord();
    }
}
