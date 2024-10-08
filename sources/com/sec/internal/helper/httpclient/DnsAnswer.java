package com.sec.internal.helper.httpclient;

import java.net.InetAddress;

public class DnsAnswer {
    private long mExpiryTime;
    private InetAddress mIp;

    public DnsAnswer(InetAddress inetAddress, long j) {
        this.mIp = inetAddress;
        this.mExpiryTime = j;
    }

    public InetAddress getIp() {
        return this.mIp;
    }

    public boolean isExpired() {
        return this.mExpiryTime <= System.nanoTime();
    }
}
