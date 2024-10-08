package com.sec.internal.ims.core.handler.secims;

import com.sec.ims.util.SipError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RegInfoChanged;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.X509CertVerifyRequest;
import java.util.List;

public abstract class StackEventListener {
    public void onContactActivated(int i) {
    }

    public void onDeregistered(int i, SipError sipError, long j, boolean z) {
    }

    public void onDnsResponse(String str, List<String> list, int i, int i2) {
    }

    public void onISIMAuthRequested(int i, String str, int i2) {
    }

    public void onRefreshRegNotification(int i) {
    }

    public void onRegEventContactUriNotification(int i, List<String> list, int i2, String str, String str2) {
    }

    public void onRegImpuNotification(int i, String str) {
    }

    public void onRegInfoNotification(int i, RegInfoChanged regInfoChanged) {
    }

    public void onRegistered(int i, List<String> list, List<String> list2, SipError sipError, long j, int i2, String str) {
    }

    public void onSubscribed(int i, SipError sipError) {
    }

    public void onUpdatePani() {
    }

    public void onUpdateRouteTableRequested(int i, int i2, String str) {
    }

    public void onX509CertVerifyRequested(X509CertVerifyRequest x509CertVerifyRequest) {
    }
}
