package org.xbill.DNS;

public class TXTRecord extends TXTBase {
    private static final long serialVersionUID = -5780785764284221342L;

    TXTRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new TXTRecord();
    }
}
