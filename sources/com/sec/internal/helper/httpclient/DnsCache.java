package com.sec.internal.helper.httpclient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DnsCache {
    private static final String LOG_TAG = "DnsController";
    private List<DnsGroup> mDnsGroups = new ArrayList();

    public List<InetAddress> query(String str) {
        removeInvalidResult();
        Iterator<DnsGroup> it = this.mDnsGroups.iterator();
        if (it.hasNext()) {
            return it.next().query(str);
        }
        return null;
    }

    public void store(DnsGroup dnsGroup) {
        this.mDnsGroups.add(dnsGroup);
    }

    private void removeInvalidResult() {
        for (int size = this.mDnsGroups.size() - 1; size >= 0; size--) {
            DnsGroup dnsGroup = this.mDnsGroups.get(size);
            if (dnsGroup.isInvalid()) {
                this.mDnsGroups.remove(dnsGroup);
            }
        }
    }
}
