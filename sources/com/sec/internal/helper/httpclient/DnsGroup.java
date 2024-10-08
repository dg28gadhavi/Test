package com.sec.internal.helper.httpclient;

import java.net.InetAddress;
import java.util.List;

public class DnsGroup {
    DnsResponse mBsfResponse;
    DnsResponse mNafResponse;

    public DnsGroup(DnsResponse dnsResponse, DnsResponse dnsResponse2) {
        this.mNafResponse = dnsResponse;
        this.mBsfResponse = dnsResponse2;
    }

    public List<InetAddress> query(String str) {
        if (str.equals(this.mNafResponse.getHostname())) {
            return this.mNafResponse.getIp();
        }
        if (str.equals(this.mBsfResponse.getHostname())) {
            return this.mBsfResponse.getIp();
        }
        return null;
    }

    public boolean isInvalid() {
        return this.mNafResponse.isInvalid() || this.mBsfResponse.isInvalid();
    }
}
