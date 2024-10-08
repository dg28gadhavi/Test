package com.sec.internal.ims.servicemodules.tapi.service.extension.validation;

import android.util.Log;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.CertificateInfo;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.ValidationHelper;
import java.io.InputStream;
import java.util.HashSet;
import org.w3c.dom.Element;

class IARIXmlProcessor {
    private static final String LOG_TAG = "IARIXmlProcessor";
    private IARIXmlParser authDocument;
    private int status = -1;

    IARIXmlProcessor() {
    }

    public void parseAuthDoc(InputStream inputStream) {
        IARIXmlParser iARIXmlParser = new IARIXmlParser();
        this.authDocument = iARIXmlParser;
        if (iARIXmlParser.parse(inputStream)) {
            this.status = 0;
        }
    }

    public void process() {
        if (this.authDocument == null) {
            Log.d(LOG_TAG, "process: auth doc is null");
            this.status = -1;
            return;
        }
        HashSet hashSet = new HashSet();
        hashSet.add(this.authDocument.getIariNode().getAttribute(Constants.ID));
        hashSet.add(this.authDocument.getPackageSignerNode().getAttribute(Constants.ID));
        Element packageNameNode = this.authDocument.getPackageNameNode();
        if (packageNameNode != null) {
            hashSet.add(packageNameNode.getAttribute(Constants.ID));
        }
        int validateCertificateOtherProperties = validateCertificateOtherProperties();
        this.status = validateCertificateOtherProperties;
        if (validateCertificateOtherProperties == 0) {
            this.status = 0;
        }
    }

    public int getStatus() {
        return this.status;
    }

    public IARIXmlParser getAuthDocument() {
        return this.authDocument;
    }

    private int validateCertificateOtherProperties() {
        String str = LOG_TAG;
        Log.d(str, "validateCertificateOtherProperties");
        CertificateInfo entityCertificate = this.authDocument.getSignature().getEntityCertificate();
        if (!this.authDocument.getIari().equals(entityCertificate.getURIIdentity())) {
            this.status = 1;
            return 1;
        } else if (!this.authDocument.getIari().startsWith(Constants.SELF_SIGNED_IARI_PREFIX)) {
            this.status = 1;
            return 1;
        } else {
            if (!this.authDocument.getIari().substring(47).equals(ValidationHelper.hash(entityCertificate.getX509Certificate().getPublicKey().getEncoded()))) {
                Log.d(str, "Requested IARI key-specific part does not match signing key");
                this.status = 1;
                return 1;
            }
            this.status = 0;
            return 0;
        }
    }
}
