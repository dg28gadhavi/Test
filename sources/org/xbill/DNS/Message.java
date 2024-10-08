package org.xbill.DNS;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Message implements Cloneable {
    private static RRset[] emptyRRsetArray = new RRset[0];
    private static Record[] emptyRecordArray = new Record[0];
    private Header header;
    private List[] sections;
    int sig0start;
    private int size;
    int tsigState;
    int tsigstart;

    private Message(Header header2) {
        this.sections = new List[4];
        this.header = header2;
    }

    public Message() {
        this(new Header());
    }

    public static Message newQuery(Record record) {
        Message message = new Message();
        message.header.setOpcode(0);
        message.header.setFlag(7);
        message.addRecord(record, 0);
        return message;
    }

    Message(DNSInput dNSInput) throws IOException {
        this(new Header(dNSInput));
        boolean z = this.header.getOpcode() == 5;
        boolean flag = this.header.getFlag(6);
        int i = 0;
        while (i < 4) {
            try {
                int count = this.header.getCount(i);
                if (count > 0) {
                    this.sections[i] = new ArrayList(count);
                }
                for (int i2 = 0; i2 < count; i2++) {
                    int current = dNSInput.current();
                    Record fromWire = Record.fromWire(dNSInput, i, z);
                    this.sections[i].add(fromWire);
                    if (i == 3) {
                        if (fromWire.getType() == 250) {
                            this.tsigstart = current;
                        }
                        if (fromWire.getType() == 24 && ((SIGRecord) fromWire).getTypeCovered() == 0) {
                            this.sig0start = current;
                        }
                    }
                }
                i++;
            } catch (WireParseException e) {
                if (!flag) {
                    throw e;
                }
            }
        }
        this.size = dNSInput.current();
    }

    public Message(byte[] bArr) throws IOException {
        this(new DNSInput(bArr));
    }

    public void addRecord(Record record, int i) {
        List[] listArr = this.sections;
        if (listArr[i] == null) {
            listArr[i] = new LinkedList();
        }
        this.header.incCount(i);
        this.sections[i].add(record);
    }

    public boolean isSigned() {
        int i = this.tsigState;
        return i == 3 || i == 1 || i == 4;
    }

    public boolean isVerified() {
        return this.tsigState == 1;
    }

    public OPTRecord getOPT() {
        Record[] sectionArray = getSectionArray(3);
        for (Record record : sectionArray) {
            if (record instanceof OPTRecord) {
                return (OPTRecord) record;
            }
        }
        return null;
    }

    public int getRcode() {
        int rcode = this.header.getRcode();
        OPTRecord opt = getOPT();
        return opt != null ? rcode + (opt.getExtendedRcode() << 4) : rcode;
    }

    public Record[] getSectionArray(int i) {
        List list = this.sections[i];
        if (list == null) {
            return emptyRecordArray;
        }
        return (Record[]) list.toArray(new Record[list.size()]);
    }

    /* access modifiers changed from: package-private */
    public void toWire(DNSOutput dNSOutput) {
        this.header.toWire(dNSOutput);
        Compression compression = new Compression();
        for (int i = 0; i < 4; i++) {
            if (this.sections[i] != null) {
                for (int i2 = 0; i2 < this.sections[i].size(); i2++) {
                    ((Record) this.sections[i].get(i2)).toWire(dNSOutput, i, compression);
                }
            }
        }
    }

    public byte[] toWire() {
        DNSOutput dNSOutput = new DNSOutput();
        toWire(dNSOutput);
        this.size = dNSOutput.current();
        return dNSOutput.toByteArray();
    }

    public int numBytes() {
        return this.size;
    }

    public String sectionToString(int i) {
        if (i > 3) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        Record[] sectionArray = getSectionArray(i);
        for (Record record : sectionArray) {
            if (i == 0) {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append(";;\t");
                stringBuffer2.append(record.name);
                stringBuffer.append(stringBuffer2.toString());
                StringBuffer stringBuffer3 = new StringBuffer();
                stringBuffer3.append(", type = ");
                stringBuffer3.append(Type.string(record.type));
                stringBuffer.append(stringBuffer3.toString());
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append(", class = ");
                stringBuffer4.append(DClass.string(record.dclass));
                stringBuffer.append(stringBuffer4.toString());
            } else {
                stringBuffer.append(record);
            }
            stringBuffer.append("\n");
        }
        return stringBuffer.toString();
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        if (getOPT() != null) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(this.header.toStringWithRcode(getRcode()));
            stringBuffer2.append("\n");
            stringBuffer.append(stringBuffer2.toString());
        } else {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append(this.header);
            stringBuffer3.append("\n");
            stringBuffer.append(stringBuffer3.toString());
        }
        if (isSigned()) {
            stringBuffer.append(";; TSIG ");
            if (isVerified()) {
                stringBuffer.append(EucTestIntent.Extras.ACK_STATUS_OK);
            } else {
                stringBuffer.append("invalid");
            }
            stringBuffer.append(10);
        }
        for (int i = 0; i < 4; i++) {
            if (this.header.getOpcode() != 5) {
                StringBuffer stringBuffer4 = new StringBuffer();
                stringBuffer4.append(";; ");
                stringBuffer4.append(Section.longString(i));
                stringBuffer4.append(":\n");
                stringBuffer.append(stringBuffer4.toString());
            } else {
                StringBuffer stringBuffer5 = new StringBuffer();
                stringBuffer5.append(";; ");
                stringBuffer5.append(Section.updString(i));
                stringBuffer5.append(":\n");
                stringBuffer.append(stringBuffer5.toString());
            }
            StringBuffer stringBuffer6 = new StringBuffer();
            stringBuffer6.append(sectionToString(i));
            stringBuffer6.append("\n");
            stringBuffer.append(stringBuffer6.toString());
        }
        StringBuffer stringBuffer7 = new StringBuffer();
        stringBuffer7.append(";; Message size: ");
        stringBuffer7.append(numBytes());
        stringBuffer7.append(" bytes");
        stringBuffer.append(stringBuffer7.toString());
        return stringBuffer.toString();
    }

    public Object clone() {
        Message message = new Message();
        int i = 0;
        while (true) {
            List[] listArr = this.sections;
            if (i < listArr.length) {
                if (listArr[i] != null) {
                    message.sections[i] = new LinkedList(this.sections[i]);
                }
                i++;
            } else {
                message.header = (Header) this.header.clone();
                message.size = this.size;
                return message;
            }
        }
    }
}
