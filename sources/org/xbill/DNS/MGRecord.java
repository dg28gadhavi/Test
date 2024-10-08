package org.xbill.DNS;

public class MGRecord extends SingleNameBase {
    private static final long serialVersionUID = -3980055550863644582L;

    MGRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MGRecord();
    }
}
