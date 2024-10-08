package com.sec.internal.ims.servicemodules.tapi.service.extension.utils;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

public class CertificateInfo {
    private final X509Certificate mCert;
    private String mUriIdentity;

    public X509Certificate getX509Certificate() {
        return this.mCert;
    }

    public String getURIIdentity() {
        return this.mUriIdentity;
    }

    public CertificateInfo(X509Certificate x509Certificate) {
        this.mCert = x509Certificate;
        loadSanData();
    }

    private void loadSanData() {
        try {
            Collection<List<?>> subjectAlternativeNames = this.mCert.getSubjectAlternativeNames();
            if (subjectAlternativeNames != null && !subjectAlternativeNames.isEmpty() && subjectAlternativeNames.iterator().next().get(0) != null && subjectAlternativeNames.iterator().next().get(1) != null) {
                this.mUriIdentity = subjectAlternativeNames.iterator().next().get(1).toString();
            }
        } catch (CertificateParsingException e) {
            e.printStackTrace();
        }
    }
}
