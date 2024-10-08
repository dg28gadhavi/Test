package com.sec.internal.helper.httpclient;

import android.net.Network;
import com.sec.internal.constants.Mno;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import okhttp3.Dns;
import org.xbill.DNS.Message;
import org.xbill.DNS.NAPTRRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;

public class DnsController implements Dns {
    private static final int BUF_SIZE = 2048;
    private static final int DNS_PORT = 53;
    private static final String TAG = "DnsController";
    public static final int TIMEOUT = 3000;
    public static final int TYPE_A = 1;
    public static final int TYPE_AAAA = 2;
    public static final int TYPE_AAAA_PREF = 6;
    public static final int TYPE_A_PREF = 5;
    public static final int TYPE_NAPTR = 3;
    static List<InetAddress> mListBsf = new ArrayList();
    static List<InetAddress> mListNaf = new ArrayList();
    private static String mPreBsfname = "";
    private static String mPreNafname = "";
    int bsfRetryCounter;
    boolean isNaf;
    private String mBsfHostname = "";
    InetAddress mDnsAddress;
    List<InetAddress> mDnsAddresses;
    private DnsCache mDnsCache = new DnsCache();
    int mDnsType;
    private boolean mIsLookAhead = false;
    Mno mMno;
    private String mNafHostname = "";
    Network mNetwork;
    List<SRVRecord> mSrvRecord = new ArrayList();
    int retryCounter;

    public DnsController(int i, int i2, Network network, List<InetAddress> list, int i3, boolean z, Mno mno) {
        this.retryCounter = i;
        this.bsfRetryCounter = i2;
        this.mNetwork = network;
        this.mDnsAddresses = list;
        this.isNaf = z;
        this.mDnsType = i3;
        this.mMno = mno;
    }

    public DnsController(int i, int i2, Network network, List<InetAddress> list, int i3, boolean z, Mno mno, String str, String str2, boolean z2) {
        this.retryCounter = i;
        this.bsfRetryCounter = i2;
        this.mNetwork = network;
        this.mDnsAddresses = list;
        this.isNaf = z;
        this.mDnsType = i3;
        this.mMno = mno;
        this.mNafHostname = str;
        this.mBsfHostname = str2;
        this.mIsLookAhead = z2;
    }

    public List<InetAddress> lookup(String str) throws UnknownHostException {
        if (this.mIsLookAhead) {
            List<InetAddress> query = this.mDnsCache.query(str);
            if (query != null) {
                return query;
            }
            dnsLookAhead();
            List<InetAddress> query2 = this.mDnsCache.query(str);
            if (query2 != null) {
                return query2;
            }
            throw new UnknownHostException("There is no valid group.");
        }
        ArrayList arrayList = new ArrayList();
        IMSLog.d(TAG, "lookup: send DNS with hostname: " + str + ",mPreNafname:" + mPreNafname + ",mPreBsfname:" + mPreBsfname);
        if ((mListNaf.size() == 0 || !str.equals(mPreNafname)) && this.isNaf) {
            mListNaf.clear();
            sendDns(str);
            mPreNafname = str;
        } else if ((mListBsf.size() == 0 || !str.equals(mPreBsfname)) && !this.isNaf) {
            mListBsf.clear();
            sendDns(str);
            mPreBsfname = str;
        }
        if (!this.isNaf || mListNaf.size() <= 0) {
            return mListBsf.size() > 0 ? Collections.singletonList(mListBsf.get(this.bsfRetryCounter)) : arrayList;
        }
        return Collections.singletonList(mListNaf.get(this.retryCounter));
    }

    private void dnsLookAhead() {
        Record[] dnsQuery;
        int i = this.mDnsType == 2 ? 28 : 1;
        for (InetAddress inetAddress : this.mDnsAddresses) {
            this.mDnsAddress = inetAddress;
            Record[] dnsQuery2 = getDnsQuery(this.mNafHostname, i);
            if (dnsQuery2 != null && dnsQuery2.length != 0 && (dnsQuery = getDnsQuery(this.mBsfHostname, i)) != null && dnsQuery.length != 0) {
                this.mDnsCache.store(new DnsGroup(new DnsResponse(this.mNafHostname, dnsQuery2, i), new DnsResponse(this.mBsfHostname, dnsQuery, i)));
                return;
            }
        }
    }

    public int getRetryCounter() {
        return this.retryCounter;
    }

    public void setNaf(boolean z) {
        this.isNaf = z;
    }

    private void sendDns(String str) {
        IMSLog.d(TAG, "Requst dns query with " + this.mDnsType);
        int i = this.mDnsType;
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    getNaptrRecord(str);
                    return;
                } else if (i != 5) {
                    if (i != 6) {
                        return;
                    }
                }
            }
            getDnsManualAAAA(str);
            return;
        }
        getDnsManualA(str);
    }

    private void getNaptrRecord(String str) {
        String str2;
        Record[] dnsNAPTR = getDnsNAPTR(str);
        if (dnsNAPTR == null || dnsNAPTR.length <= 0) {
            IMSLog.e(TAG, "sendDns: NAPTR is null");
            if (str.startsWith("_http.")) {
                str2 = str;
            } else {
                str2 = "_http._tcp." + str;
            }
            Record[] dnsSRV = getDnsSRV(str2);
            if (dnsSRV == null || dnsSRV.length <= 0) {
                IMSLog.e(TAG, "sendDns: SRV direct error");
                getDnsA(str);
                return;
            }
            sortSRV(dnsSRV);
            for (SRVRecord target : this.mSrvRecord) {
                getDnsA(target.getTarget().toString());
            }
            return;
        }
        for (Record record : dnsNAPTR) {
            if (record != null && record.getType() == 35) {
                NAPTRRecord nAPTRRecord = (NAPTRRecord) record;
                if (nAPTRRecord.getService().equalsIgnoreCase("HTTP+D2T")) {
                    Record[] dnsSRV2 = getDnsSRV(nAPTRRecord.getReplacement().toString());
                    if (dnsSRV2 == null || dnsSRV2.length <= 0) {
                        getDnsA(str);
                    } else {
                        sortSRV(dnsSRV2);
                        for (SRVRecord target2 : this.mSrvRecord) {
                            getDnsA(target2.getTarget().toString());
                        }
                    }
                }
            }
        }
    }

    private Record[] getDnsQuery(String str, int i) {
        DatagramSocket datagramSocket;
        try {
            datagramSocket = new DatagramSocket();
            if (!str.endsWith(".")) {
                str = str + ".";
            }
            Message newQuery = Message.newQuery(Record.newRecord(Name.fromString(str), i, 1));
            this.mNetwork.bindSocket(datagramSocket);
            byte[] wire = newQuery.toWire();
            datagramSocket.send(new DatagramPacket(wire, wire.length, this.mDnsAddress, 53));
            DatagramPacket datagramPacket = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
            datagramSocket.setSoTimeout(3000);
            datagramSocket.receive(datagramPacket);
            Message message = new Message(datagramPacket.getData());
            int rcode = message.getRcode();
            IMSLog.d(TAG, "result is " + Rcode.string(rcode));
            Record[] sectionArray = rcode == 0 ? message.getSectionArray(1) : null;
            datagramSocket.close();
            return sectionArray;
        } catch (IOException unused) {
            IMSLog.e(TAG, "DNS query timeout, try next type or IP");
            return null;
        } catch (NullPointerException e) {
            IMSLog.e(TAG, e.getMessage());
            return null;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    private Record[] getDnsNAPTR(String str) {
        IMSLog.d(TAG, "getDnsNAPTR() called with: hostname = [" + str + "]");
        this.mDnsAddress = this.mDnsAddresses.get(0);
        return getDnsQuery(str, 35);
    }

    private Record[] getDnsSRV(String str) {
        IMSLog.d(TAG, "getDnsSRV() called with: hostname = [" + str + "]");
        this.mDnsAddress = this.mDnsAddresses.get(0);
        return getDnsQuery(str, 33);
    }

    private Record[] getDnsManualAAAA(String str) {
        IMSLog.d(TAG, "getDnsManualAAAA() called with: hostname = [" + str + "]");
        Record[] recordArr = null;
        for (InetAddress inetAddress : this.mDnsAddresses) {
            this.mDnsAddress = inetAddress;
            recordArr = getManualDnsQuery(str, 28);
            if (recordArr == null || recordArr.length <= 0) {
                if (this.mDnsType == 6) {
                    recordArr = getManualDnsQuery(str, 1);
                    if (recordArr != null && recordArr.length > 0) {
                        break;
                    } else if (recordArr == null) {
                        IMSLog.d(TAG, "AAAA and A type query failed,try next IP");
                    }
                }
            } else {
                break;
            }
        }
        return recordArr;
    }

    private Record[] getDnsManualA(String str) {
        IMSLog.d(TAG, "getDnsManualA() called with: hostname = [" + str + "]");
        Record[] recordArr = null;
        for (InetAddress inetAddress : this.mDnsAddresses) {
            this.mDnsAddress = inetAddress;
            recordArr = getManualDnsQuery(str, 1);
            if (recordArr == null || recordArr.length <= 0) {
                if (this.mDnsType == 5) {
                    recordArr = getManualDnsQuery(str, 28);
                    if (recordArr != null && recordArr.length > 0) {
                        break;
                    } else if (recordArr == null) {
                        IMSLog.d(TAG, "A and AAAA type query failed,try next IP");
                    }
                }
            } else {
                break;
            }
        }
        return recordArr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x00f7 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private org.xbill.DNS.Record[] getManualDnsQuery(java.lang.String r11, int r12) {
        /*
            r10 = this;
            org.xbill.DNS.Record[] r11 = r10.getDnsQuery(r11, r12)
            java.lang.String r0 = "DnsController"
            r1 = 0
            if (r11 == 0) goto L_0x00fb
            int r2 = r11.length
            if (r2 <= 0) goto L_0x00fb
            int r2 = r11.length
            r3 = 0
            r5 = r1
            r4 = r3
        L_0x0010:
            if (r4 >= r2) goto L_0x00fb
            r6 = r11[r4]
            if (r6 != 0) goto L_0x0018
            goto L_0x00f7
        L_0x0018:
            int r7 = r6.getType()
            r8 = 5
            if (r7 != r8) goto L_0x0027
            org.xbill.DNS.CNAMERecord r6 = (org.xbill.DNS.CNAMERecord) r6
            org.xbill.DNS.Name r5 = r6.getTarget()
            goto L_0x00f7
        L_0x0027:
            r7 = 28
            r8 = 1
            if (r5 == 0) goto L_0x007d
            int r9 = r6.getType()
            if (r9 != r8) goto L_0x0070
            org.xbill.DNS.ARecord r6 = (org.xbill.DNS.ARecord) r6
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r9 = "target Name "
            r7.append(r9)
            java.lang.String r9 = r5.toString()
            r7.append(r9)
            java.lang.String r9 = " ARecord name "
            r7.append(r9)
            java.lang.String r9 = r6.toString()
            r7.append(r9)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.i(r0, r7)
            org.xbill.DNS.Name r7 = r6.getName()
            java.lang.String r7 = r7.toString()
            java.lang.String r9 = r5.toString()
            boolean r7 = r7.equalsIgnoreCase(r9)
            if (r7 == 0) goto L_0x0093
            java.net.InetAddress r6 = r6.getAddress()
            goto L_0x0094
        L_0x0070:
            int r9 = r6.getType()
            if (r9 != r7) goto L_0x0093
            org.xbill.DNS.AAAARecord r6 = (org.xbill.DNS.AAAARecord) r6
            java.net.InetAddress r6 = r6.getAddress()
            goto L_0x0094
        L_0x007d:
            if (r12 != r8) goto L_0x0086
            org.xbill.DNS.ARecord r6 = (org.xbill.DNS.ARecord) r6
            java.net.InetAddress r6 = r6.getAddress()
            goto L_0x0094
        L_0x0086:
            int r9 = r6.getType()
            if (r9 != r7) goto L_0x0093
            org.xbill.DNS.AAAARecord r6 = (org.xbill.DNS.AAAARecord) r6
            java.net.InetAddress r6 = r6.getAddress()
            goto L_0x0094
        L_0x0093:
            r6 = r1
        L_0x0094:
            if (r6 == 0) goto L_0x00f7
            com.sec.internal.constants.Mno r7 = r10.mMno
            boolean r7 = r7.isChn()
            if (r7 == 0) goto L_0x00b0
            java.lang.String r7 = r6.getHostAddress()
            java.lang.String r9 = "::"
            boolean r7 = r7.startsWith(r9)
            if (r7 == 0) goto L_0x00b0
            java.lang.String r6 = "chn not supported IPv6 addr"
            com.sec.internal.log.IMSLog.d(r0, r6)
            goto L_0x00f7
        L_0x00b0:
            boolean r7 = r10.isNaf
            if (r7 == 0) goto L_0x00d6
            java.util.List<java.net.InetAddress> r7 = mListNaf
            java.util.Iterator r7 = r7.iterator()
        L_0x00ba:
            boolean r9 = r7.hasNext()
            if (r9 == 0) goto L_0x00cd
            java.lang.Object r9 = r7.next()
            java.net.InetAddress r9 = (java.net.InetAddress) r9
            boolean r9 = r9.equals(r6)
            if (r9 == 0) goto L_0x00ba
            goto L_0x00ce
        L_0x00cd:
            r8 = r3
        L_0x00ce:
            if (r8 != 0) goto L_0x00f7
            java.util.List<java.net.InetAddress> r7 = mListNaf
            r7.add(r6)
            goto L_0x00f7
        L_0x00d6:
            java.util.List<java.net.InetAddress> r7 = mListBsf
            java.util.Iterator r7 = r7.iterator()
        L_0x00dc:
            boolean r9 = r7.hasNext()
            if (r9 == 0) goto L_0x00ef
            java.lang.Object r9 = r7.next()
            java.net.InetAddress r9 = (java.net.InetAddress) r9
            boolean r9 = r9.equals(r6)
            if (r9 == 0) goto L_0x00dc
            goto L_0x00f0
        L_0x00ef:
            r8 = r3
        L_0x00f0:
            if (r8 != 0) goto L_0x00f7
            java.util.List<java.net.InetAddress> r7 = mListBsf
            r7.add(r6)
        L_0x00f7:
            int r4 = r4 + 1
            goto L_0x0010
        L_0x00fb:
            com.sec.internal.constants.Mno r12 = r10.mMno
            boolean r12 = r12.isChn()
            if (r12 == 0) goto L_0x0121
            boolean r12 = r10.isNaf
            if (r12 == 0) goto L_0x010f
            java.util.List<java.net.InetAddress> r12 = mListNaf
            int r12 = r12.size()
            if (r12 == 0) goto L_0x011b
        L_0x010f:
            boolean r10 = r10.isNaf
            if (r10 != 0) goto L_0x0121
            java.util.List<java.net.InetAddress> r10 = mListBsf
            int r10 = r10.size()
            if (r10 != 0) goto L_0x0121
        L_0x011b:
            java.lang.String r10 = "chn find no valid addr, return null"
            com.sec.internal.log.IMSLog.d(r0, r10)
            return r1
        L_0x0121:
            return r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.DnsController.getManualDnsQuery(java.lang.String, int):org.xbill.DNS.Record[]");
    }

    private void getDnsA(String str) {
        try {
            InetAddress byName = this.mNetwork.getByName(str);
            IMSLog.d(TAG, "getDnsA: " + byName);
            boolean z = true;
            if (this.isNaf) {
                Iterator<InetAddress> it = mListNaf.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (it.next().equals(byName)) {
                            break;
                        }
                    } else {
                        z = false;
                        break;
                    }
                }
                if (!z) {
                    mListNaf.add(byName);
                    return;
                }
                return;
            }
            Iterator<InetAddress> it2 = mListBsf.iterator();
            while (true) {
                if (it2.hasNext()) {
                    if (it2.next().equals(byName)) {
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
            if (!z) {
                mListBsf.add(byName);
            }
        } catch (NullPointerException | UnknownHostException unused) {
            IMSLog.e(TAG, "getDnsA: error with hostname: " + str);
        }
    }

    private void sortSRV(Record[] recordArr) {
        this.mSrvRecord.clear();
        for (SRVRecord sRVRecord : recordArr) {
            if (this.mSrvRecord.size() == 0) {
                IMSLog.d(TAG, "sortSRV: 1st Record");
                this.mSrvRecord.add(sRVRecord);
            } else {
                boolean z = false;
                for (int i = 0; i < this.mSrvRecord.size() && !z; i++) {
                    SRVRecord sRVRecord2 = this.mSrvRecord.get(i);
                    if (sRVRecord.getPriority() < sRVRecord2.getPriority()) {
                        IMSLog.d(TAG, "sortSRV: Update SRV better, lower priority");
                        this.mSrvRecord.add(i, sRVRecord);
                    } else if (sRVRecord.getWeight() > sRVRecord2.getWeight()) {
                        IMSLog.d(TAG, "sortSRV: Update SRV better, higher weight");
                        this.mSrvRecord.add(i, sRVRecord);
                    }
                    z = true;
                }
                if (!z) {
                    this.mSrvRecord.add(sRVRecord);
                }
            }
        }
    }

    public static int getNafAddrSize() {
        return mListNaf.size();
    }

    public static int getBsfAddrSize() {
        return mListBsf.size();
    }

    public static void correctServerAddr(int i, int i2) {
        if (i > 0 && i < mListNaf.size()) {
            mListNaf.remove(i);
            mListNaf.add(0, mListNaf.get(i));
        }
        if (i2 > 0 && i2 < mListBsf.size()) {
            mListBsf.remove(i2);
            mListBsf.add(0, mListBsf.get(i2));
        }
    }
}
