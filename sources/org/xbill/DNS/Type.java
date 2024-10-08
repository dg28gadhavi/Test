package org.xbill.DNS;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import java.util.HashMap;

public final class Type {
    private static TypeMnemonic types;

    private static class TypeMnemonic extends Mnemonic {
        private HashMap objects = new HashMap();

        public TypeMnemonic() {
            super("Type", 2);
            setPrefix("TYPE");
        }

        public void add(int i, String str, Record record) {
            super.add(i, str);
            this.objects.put(Mnemonic.toInteger(i), record);
        }

        public void check(int i) {
            Type.check(i);
        }

        public Record getProto(int i) {
            check(i);
            return (Record) this.objects.get(Mnemonic.toInteger(i));
        }
    }

    static {
        TypeMnemonic typeMnemonic = new TypeMnemonic();
        types = typeMnemonic;
        typeMnemonic.add(1, "A", new ARecord());
        types.add(2, "NS", new NSRecord());
        types.add(3, "MD", new MDRecord());
        types.add(4, "MF", new MFRecord());
        types.add(5, "CNAME", new CNAMERecord());
        types.add(6, "SOA", new SOARecord());
        types.add(7, "MB", new MBRecord());
        types.add(8, "MG", new MGRecord());
        types.add(9, "MR", new MRRecord());
        types.add(10, "NULL", new NULLRecord());
        types.add(11, "WKS", new WKSRecord());
        types.add(12, "PTR", new PTRRecord());
        types.add(13, "HINFO", new HINFORecord());
        types.add(14, "MINFO", new MINFORecord());
        types.add(15, "MX", new MXRecord());
        types.add(16, "TXT", new TXTRecord());
        types.add(17, "RP", new RPRecord());
        types.add(18, "AFSDB", new AFSDBRecord());
        types.add(19, "X25", new X25Record());
        types.add(20, "ISDN", new ISDNRecord());
        types.add(21, "RT", new RTRecord());
        types.add(22, "NSAP", new NSAPRecord());
        types.add(23, "NSAP-PTR", new NSAP_PTRRecord());
        types.add(24, "SIG", new SIGRecord());
        types.add(25, "KEY", new KEYRecord());
        types.add(26, "PX", new PXRecord());
        types.add(27, "GPOS", new GPOSRecord());
        types.add(28, "AAAA", new AAAARecord());
        types.add(29, "LOC", new LOCRecord());
        types.add(30, "NXT", new NXTRecord());
        types.add(31, "EID");
        types.add(32, "NIMLOC");
        types.add(33, "SRV", new SRVRecord());
        types.add(34, "ATMA");
        types.add(35, "NAPTR", new NAPTRRecord());
        types.add(36, "KX", new KXRecord());
        types.add(37, "CERT", new CERTRecord());
        types.add(38, "A6", new A6Record());
        types.add(39, "DNAME", new DNAMERecord());
        types.add(41, "OPT", new OPTRecord());
        types.add(42, "APL", new APLRecord());
        types.add(43, "DS", new DSRecord());
        types.add(44, "SSHFP", new SSHFPRecord());
        types.add(45, "IPSECKEY", new IPSECKEYRecord());
        types.add(46, "RRSIG", new RRSIGRecord());
        types.add(47, "NSEC", new NSECRecord());
        types.add(48, "DNSKEY", new DNSKEYRecord());
        types.add(49, "DHCID", new DHCIDRecord());
        types.add(50, "NSEC3", new NSEC3Record());
        types.add(51, "NSEC3PARAM", new NSEC3PARAMRecord());
        types.add(52, "TLSA", new TLSARecord());
        types.add(53, "SMIMEA", new SMIMEARecord());
        types.add(61, "OPENPGPKEY", new OPENPGPKEYRecord());
        types.add(99, "SPF", new SPFRecord());
        types.add(249, "TKEY", new TKEYRecord());
        types.add(250, "TSIG", new TSIGRecord());
        types.add(MNO.SPARK, "IXFR");
        types.add(252, "AXFR");
        types.add(MNO.UTS_CURACAO, "MAILB");
        types.add(MNO.TIGO_HONDURAS, "MAILA");
        types.add(255, "ANY");
        types.add(256, Constants.SIG_PROPERTY_URI_NAME, new URIRecord());
        types.add(MNO.TIGO_NICARAGUA, "CAA", new CAARecord());
        types.add(32769, "DLV", new DLVRecord());
    }

    public static void check(int i) {
        if (i < 0 || i > 65535) {
            throw new InvalidTypeException(i);
        }
    }

    public static String string(int i) {
        return types.getText(i);
    }

    static Record getProto(int i) {
        return types.getProto(i);
    }
}
