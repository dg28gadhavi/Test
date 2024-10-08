package org.xbill.DNS;

public final class Rcode {
    private static Mnemonic rcodes = new Mnemonic("DNS Rcode", 2);
    private static Mnemonic tsigrcodes = new Mnemonic("TSIG rcode", 2);

    static {
        rcodes.setMaximum(4095);
        rcodes.setPrefix("RESERVED");
        rcodes.setNumericAllowed(true);
        rcodes.add(0, "NOERROR");
        rcodes.add(1, "FORMERR");
        rcodes.add(2, "SERVFAIL");
        rcodes.add(3, "NXDOMAIN");
        rcodes.add(4, "NOTIMP");
        rcodes.addAlias(4, "NOTIMPL");
        rcodes.add(5, "REFUSED");
        rcodes.add(6, "YXDOMAIN");
        rcodes.add(7, "YXRRSET");
        rcodes.add(8, "NXRRSET");
        rcodes.add(9, "NOTAUTH");
        rcodes.add(10, "NOTZONE");
        rcodes.add(16, "BADVERS");
        tsigrcodes.setMaximum(65535);
        tsigrcodes.setPrefix("RESERVED");
        tsigrcodes.setNumericAllowed(true);
        tsigrcodes.addAll(rcodes);
        tsigrcodes.add(16, "BADSIG");
        tsigrcodes.add(17, "BADKEY");
        tsigrcodes.add(18, "BADTIME");
        tsigrcodes.add(19, "BADMODE");
    }

    public static String string(int i) {
        return rcodes.getText(i);
    }

    public static String TSIGstring(int i) {
        return tsigrcodes.getText(i);
    }
}
