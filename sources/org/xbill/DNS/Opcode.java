package org.xbill.DNS;

public final class Opcode {
    private static Mnemonic opcodes;

    static {
        Mnemonic mnemonic = new Mnemonic("DNS Opcode", 2);
        opcodes = mnemonic;
        mnemonic.setMaximum(15);
        opcodes.setPrefix("RESERVED");
        opcodes.setNumericAllowed(true);
        opcodes.add(0, "QUERY");
        opcodes.add(1, "IQUERY");
        opcodes.add(2, "STATUS");
        opcodes.add(4, "NOTIFY");
        opcodes.add(5, "UPDATE");
    }

    public static String string(int i) {
        return opcodes.getText(i);
    }
}
