package com.sec.internal.helper;

import android.annotation.SuppressLint;
import android.net.DnsResolver;
import android.net.Network;
import android.net.ParseException;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.util.Log;
import com.android.net.module.util.DnsPacket;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class NaptrDnsResolver {
    private static final int MAXLABELCOUNT = 128;
    private static final int MAXLABELSIZE = 63;
    private static final int NAME_COMPRESSION = 192;
    private static final int NAME_NORMAL = 0;
    public static final int QUERY_TYPE_NAPTR = 35;
    private static final String TAG = "NaptrDnsResolver";
    public static final int TYPE_A = 0;
    public static final int TYPE_P = 3;
    public static final int TYPE_SRV = 1;
    public static final int TYPE_U = 2;
    private static final DecimalFormat sByteFormat = new DecimalFormat();
    private static final FieldPosition sPos = new FieldPosition(0);

    @Retention(RetentionPolicy.SOURCE)
    @interface NaptrRecordType {
    }

    public static class NaptrTarget {
        public final String mName;
        public final int mType;

        public NaptrTarget(String str, int i) {
            this.mName = str;
            this.mType = i;
        }
    }

    static class NaptrResponse extends DnsPacket {
        private final int mQueryType;

        static class NaptrRecord {
            private static final int MAXNAMESIZE = 255;
            public final String flag;
            public final int order;
            public final int preference;
            public final String regex;
            public final String replacement;
            public final String service;

            private String parseNextField(ByteBuffer byteBuffer) throws BufferUnderflowException {
                int i = (short) byteBuffer.get();
                byte[] bArr = new byte[i];
                byteBuffer.get(bArr, 0, i);
                return new String(bArr, StandardCharsets.UTF_8);
            }

            public int getTypeFromFlagString() {
                String str = this.flag;
                str.hashCode();
                char c = 65535;
                switch (str.hashCode()) {
                    case 65:
                        if (str.equals("A")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 83:
                        if (str.equals("S")) {
                            c = 1;
                            break;
                        }
                        break;
                    case MNO.TMOBILE_CZECH /*97*/:
                        if (str.equals("a")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 115:
                        if (str.equals("s")) {
                            c = 3;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                    case 2:
                        return 0;
                    case 1:
                    case 3:
                        return 1;
                    default:
                        throw new DnsPacket.ParseException("Unsupported flag type: " + this.flag);
                }
            }

            NaptrRecord(byte[] bArr) throws DnsPacket.ParseException {
                if (bArr != null) {
                    ByteBuffer wrap = ByteBuffer.wrap(bArr);
                    try {
                        this.order = Short.toUnsignedInt(wrap.getShort());
                        this.preference = Short.toUnsignedInt(wrap.getShort());
                        this.flag = parseNextField(wrap);
                        this.service = parseNextField(wrap);
                        String parseNextField = parseNextField(wrap);
                        this.regex = parseNextField;
                        if (parseNextField.length() == 0) {
                            String parseName = NaptrDnsResolver.parseName(wrap, 0, true);
                            this.replacement = parseName;
                            if (parseName == null) {
                                throw new DnsPacket.ParseException("NAPTR: replacement field not expected to be empty!");
                            } else if (parseName.length() > 255) {
                                throw new DnsPacket.ParseException("Parse name fail, replacement name size is too long: " + parseName.length());
                            } else if (wrap.hasRemaining()) {
                                throw new DnsPacket.ParseException("Parsing NAPTR record data failed: more bytes than expected!");
                            }
                        } else {
                            throw new DnsPacket.ParseException("NAPTR: regex field expected to be empty!");
                        }
                    } catch (BufferUnderflowException e) {
                        throw new DnsPacket.ParseException("Parsing NAPTR Record data failed with cause", e);
                    }
                } else {
                    throw new DnsPacket.ParseException("NAPTR: No record data");
                }
            }
        }

        NaptrResponse(byte[] bArr) throws DnsPacket.ParseException {
            super(bArr);
            if (this.mHeader.isResponse()) {
                int recordCount = this.mHeader.getRecordCount(0);
                if (recordCount == 1) {
                    int i = ((DnsPacket.DnsRecord) this.mRecords[0].get(0)).nsType;
                    this.mQueryType = i;
                    if (i != 35) {
                        throw new DnsPacket.ParseException("Unexpected query type: " + i);
                    }
                    return;
                }
                throw new DnsPacket.ParseException("Unexpected query count: " + recordCount);
            }
            throw new DnsPacket.ParseException("Not an answer packet");
        }

        public List<NaptrRecord> parseNaptrRecords() throws DnsPacket.ParseException {
            ArrayList arrayList = new ArrayList();
            if (this.mHeader.getRecordCount(1) == 0) {
                return arrayList;
            }
            for (DnsPacket.DnsRecord dnsRecord : this.mRecords[1]) {
                int i = dnsRecord.nsType;
                if (i == 35) {
                    NaptrRecord naptrRecord = new NaptrRecord(dnsRecord.getRR());
                    arrayList.add(naptrRecord);
                    Log.d(NaptrDnsResolver.TAG, "NaptrRecord name: " + dnsRecord.dName + " replacement field: " + naptrRecord.replacement);
                } else {
                    throw new DnsPacket.ParseException("Unexpected DNS record type in ANSECTION: " + i);
                }
            }
            return arrayList;
        }
    }

    public static class NaptrRecordAnswerAccumulator implements DnsResolver.Callback<byte[]> {
        private static final String TAG = "NaptrRecordAnswerAccum";
        private final String mTransport;
        private final DnsResolver.Callback<List<NaptrTarget>> mUserCallback;
        private final Executor mUserExecutor;

        private static class LazyExecutor {
            public static final Executor INSTANCE = Executors.newSingleThreadExecutor();

            private LazyExecutor() {
            }
        }

        static Executor getInternalExecutor() {
            return LazyExecutor.INSTANCE;
        }

        public NaptrRecordAnswerAccumulator(DnsResolver.Callback<List<NaptrTarget>> callback, Executor executor, String str) {
            this.mUserCallback = callback;
            this.mUserExecutor = executor;
            str.hashCode();
            char c = 65535;
            switch (str.hashCode()) {
                case 82881:
                    if (str.equals("TCP")) {
                        c = 0;
                        break;
                    }
                    break;
                case 83163:
                    if (str.equals("TLS")) {
                        c = 1;
                        break;
                    }
                    break;
                case 83873:
                    if (str.equals("UDP")) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    this.mTransport = "SIP+D2T";
                    return;
                case 1:
                    this.mTransport = "SIPS+D2T";
                    return;
                case 2:
                    this.mTransport = "SIP+D2U";
                    return;
                default:
                    this.mTransport = "";
                    return;
            }
        }

        private List<NaptrTarget> composeNaptrRecordResult(List<NaptrResponse.NaptrRecord> list) throws ParseException {
            ArrayList arrayList = new ArrayList();
            if (list.isEmpty()) {
                return arrayList;
            }
            for (NaptrResponse.NaptrRecord next : list) {
                if (this.mTransport.equalsIgnoreCase(next.service)) {
                    arrayList.add(new NaptrTarget(next.replacement, next.getTypeFromFlagString()));
                }
            }
            return arrayList;
        }

        public void onAnswer(byte[] bArr, int i) {
            try {
                this.mUserExecutor.execute(new NaptrDnsResolver$NaptrRecordAnswerAccumulator$$ExternalSyntheticLambda0(this, composeNaptrRecordResult(new NaptrResponse(bArr).parseNaptrRecords()), i));
            } catch (DnsPacket.ParseException unused) {
                Log.d(TAG, "Exception occurs, send error to do ARES DNS query once again");
                this.mUserExecutor.execute(new NaptrDnsResolver$NaptrRecordAnswerAccumulator$$ExternalSyntheticLambda1(this));
            }
        }

        /* access modifiers changed from: private */
        public /* synthetic */ void lambda$onAnswer$0(List list, int i) {
            this.mUserCallback.onAnswer(list, i);
        }

        /* access modifiers changed from: private */
        public /* synthetic */ void lambda$onAnswer$1() {
            this.mUserCallback.onError((DnsResolver.DnsException) null);
        }

        public void onError(DnsResolver.DnsException dnsException) {
            Log.e(TAG, "onError: " + dnsException);
            this.mUserExecutor.execute(new NaptrDnsResolver$NaptrRecordAnswerAccumulator$$ExternalSyntheticLambda2(this, dnsException));
        }

        /* access modifiers changed from: private */
        public /* synthetic */ void lambda$onError$2(DnsResolver.DnsException dnsException) {
            this.mUserCallback.onError(dnsException);
        }
    }

    @SuppressLint({"WrongConstant"})
    public static void query(Network network, String str, Executor executor, CancellationSignal cancellationSignal, DnsResolver.Callback<List<NaptrTarget>> callback, String str2) {
        Network network2 = network;
        String str3 = str;
        DnsResolver.getInstance().rawQuery(network2, str3, 1, 35, 0, NaptrRecordAnswerAccumulator.getInternalExecutor(), cancellationSignal, new NaptrRecordAnswerAccumulator(callback, executor, str2));
    }

    public static String parseName(ByteBuffer byteBuffer, int i, boolean z) throws BufferUnderflowException, DnsPacket.ParseException {
        if (i <= 128) {
            int unsignedInt = Byte.toUnsignedInt(byteBuffer.get());
            int i2 = unsignedInt & 192;
            if (unsignedInt == 0) {
                return "";
            }
            if ((i2 != 0 && i2 != 192) || (!z && i2 == 192)) {
                throw new DnsPacket.ParseException("Parse name fail, bad label type: " + i2);
            } else if (i2 == 192) {
                int unsignedInt2 = ((unsignedInt & -193) << 8) + Byte.toUnsignedInt(byteBuffer.get());
                int position = byteBuffer.position();
                if (unsignedInt2 < position - 2) {
                    byteBuffer.position(unsignedInt2);
                    String parseName = parseName(byteBuffer, i + 1, z);
                    byteBuffer.position(position);
                    return parseName;
                }
                throw new DnsPacket.ParseException("Parse compression name fail, invalid compression");
            } else {
                byte[] bArr = new byte[unsignedInt];
                byteBuffer.get(bArr);
                String labelToString = labelToString(bArr);
                if (labelToString.length() <= 63) {
                    String parseName2 = parseName(byteBuffer, i + 1, z);
                    if (TextUtils.isEmpty(parseName2)) {
                        return labelToString;
                    }
                    return labelToString + "." + parseName2;
                }
                throw new DnsPacket.ParseException("Parse name fail, invalid label length");
            }
        } else {
            throw new DnsPacket.ParseException("Failed to parse name, too many labels");
        }
    }

    private static String labelToString(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte unsignedInt : bArr) {
            int unsignedInt2 = Byte.toUnsignedInt(unsignedInt);
            if (unsignedInt2 <= 32 || unsignedInt2 >= 127) {
                stringBuffer.append('\\');
                sByteFormat.format((long) unsignedInt2, stringBuffer, sPos);
            } else if (unsignedInt2 == 34 || unsignedInt2 == 46 || unsignedInt2 == 59 || unsignedInt2 == 92 || unsignedInt2 == 40 || unsignedInt2 == 41 || unsignedInt2 == 64 || unsignedInt2 == 36) {
                stringBuffer.append('\\');
                stringBuffer.append((char) unsignedInt2);
            } else {
                stringBuffer.append((char) unsignedInt2);
            }
        }
        return stringBuffer.toString();
    }

    private NaptrDnsResolver() {
    }
}
