package com.sec.internal.helper.httpclient;

import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Record;

public class DnsResponse {
    private static final String LOG_TAG = "DnsController";
    private static final long NANO = 1000000000;
    private List<DnsAnswer> mDnsAnswers = new ArrayList();
    private String mHostname;

    public DnsResponse(String str, Record[] recordArr, int i) {
        for (Record record : recordArr) {
            if (record != null && isRecordMatchingType(record, i)) {
                this.mDnsAnswers.add(new DnsAnswer(getAddress(record, i), System.nanoTime() + (record.getTTL() * NANO)));
            }
        }
        this.mHostname = str;
    }

    private boolean isRecordMatchingType(Record record, int i) {
        if (i == 1) {
            return record instanceof ARecord;
        }
        if (i != 28) {
            return false;
        }
        return record instanceof AAAARecord;
    }

    public String getHostname() {
        return this.mHostname;
    }

    private InetAddress getAddress(Record record, int i) {
        if (i == 1) {
            return ((ARecord) record).getAddress();
        }
        if (i == 28) {
            return ((AAAARecord) record).getAddress();
        }
        return null;
    }

    public boolean isInvalid() {
        removeInvalidAnswers();
        return this.mDnsAnswers.isEmpty();
    }

    private void removeInvalidAnswers() {
        for (int size = this.mDnsAnswers.size() - 1; size >= 0; size--) {
            DnsAnswer dnsAnswer = this.mDnsAnswers.get(size);
            if (dnsAnswer.isExpired()) {
                IMSLog.i(LOG_TAG, "isInvalid: expired result.");
                this.mDnsAnswers.remove(dnsAnswer);
            }
        }
    }

    public List<InetAddress> getIp() {
        ArrayList arrayList = new ArrayList();
        for (DnsAnswer ip : this.mDnsAnswers) {
            arrayList.add(ip.getIp());
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        return arrayList;
    }
}
