package org.xbill.DNS;

import java.io.PrintStream;

public class Compression {
    private Entry[] table = new Entry[17];
    private boolean verbose = Options.check("verbosecompression");

    private static class Entry {
        Name name;
        Entry next;
        int pos;

        private Entry() {
        }
    }

    public void add(int i, Name name) {
        if (i <= 16383) {
            int hashCode = (name.hashCode() & Integer.MAX_VALUE) % 17;
            Entry entry = new Entry();
            entry.name = name;
            entry.pos = i;
            Entry[] entryArr = this.table;
            entry.next = entryArr[hashCode];
            entryArr[hashCode] = entry;
            if (this.verbose) {
                PrintStream printStream = System.err;
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Adding ");
                stringBuffer.append(name);
                stringBuffer.append(" at ");
                stringBuffer.append(i);
                printStream.println(stringBuffer.toString());
            }
        }
    }

    public int get(Name name) {
        int i = -1;
        for (Entry entry = this.table[(name.hashCode() & Integer.MAX_VALUE) % 17]; entry != null; entry = entry.next) {
            if (entry.name.equals(name)) {
                i = entry.pos;
            }
        }
        if (this.verbose) {
            PrintStream printStream = System.err;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Looking for ");
            stringBuffer.append(name);
            stringBuffer.append(", found ");
            stringBuffer.append(i);
            printStream.println(stringBuffer.toString());
        }
        return i;
    }
}
