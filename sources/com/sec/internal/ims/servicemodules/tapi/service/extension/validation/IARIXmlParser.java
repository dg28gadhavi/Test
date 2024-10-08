package com.sec.internal.ims.servicemodules.tapi.service.extension.validation;

import android.util.Log;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.SignatureInfo;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IARIXmlParser {
    private static final String LOG_TAG = "IARIXmlParser";
    private Element iariNode;
    private final AuthType mAuthType = AuthType.SELF_SIGNED;
    private String mIari;
    private String mPackageName;
    private String mPackageSigner;
    private SignatureInfo mSignature;
    private Element packageNameNode;
    private Element packageSignerNode;
    private Element signatureNode;

    public enum AuthType {
        SELF_SIGNED
    }

    public Element getIariNode() {
        return this.iariNode;
    }

    public Element getPackageNameNode() {
        return this.packageNameNode;
    }

    public Element getPackageSignerNode() {
        return this.packageSignerNode;
    }

    public Element getSignatureNode() {
        return this.signatureNode;
    }

    public String getIari() {
        return this.mIari;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getPackageSigner() {
        return this.mPackageSigner;
    }

    public SignatureInfo getSignature() {
        return this.mSignature;
    }

    public void setSignature(SignatureInfo signatureInfo) {
        this.mSignature = signatureInfo;
    }

    public boolean parse(InputStream inputStream) {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        newInstance.setNamespaceAware(true);
        try {
            Document parse = newInstance.newDocumentBuilder().parse(inputStream);
            String xmlEncoding = parse.getXmlEncoding();
            if (xmlEncoding != null && !xmlEncoding.equalsIgnoreCase("UTF-8")) {
                return printErrorMessage("Invalid IARI xml: iari-authorization is not encoded with UTF-8");
            }
            NodeList elementsByTagNameNS = parse.getElementsByTagNameNS(Constants.IARI_AUTH_NS, Constants.IARI_AUTH_ELT);
            if (elementsByTagNameNS.getLength() != 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of iari-authorization elements");
            }
            Element element = (Element) elementsByTagNameNS.item(0);
            if (element != parse.getDocumentElement()) {
                return printErrorMessage("Invalid IARI xml: iari-authorization is not the root element");
            }
            NodeList elementsByTagNameNS2 = parse.getElementsByTagNameNS(Constants.IARI_AUTH_NS, Constants.IARI_ELT);
            if (elementsByTagNameNS2.getLength() != 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of iari elements");
            }
            Element element2 = (Element) elementsByTagNameNS2.item(0);
            this.iariNode = element2;
            if (element2.getParentNode() != element) {
                return printErrorMessage("Invalid IARI xml: iari must be a child of iari-authorization");
            }
            this.iariNode.setIdAttribute(Constants.ID, true);
            this.mIari = this.iariNode.getTextContent();
            NodeList elementsByTagNameNS3 = parse.getElementsByTagNameNS(Constants.IARI_AUTH_NS, Constants.PACKAGE_NAME_ELT);
            int length = elementsByTagNameNS3.getLength();
            if (length > 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of package-name elements");
            }
            if (length == 1) {
                Element element3 = (Element) elementsByTagNameNS3.item(0);
                this.packageNameNode = element3;
                if (element3.getParentNode() != element) {
                    return printErrorMessage("Invalid IARI xml: package-name must be a child of iari-authorization");
                }
                this.packageNameNode.setIdAttribute(Constants.ID, true);
                this.mPackageName = this.packageNameNode.getTextContent();
            }
            NodeList elementsByTagNameNS4 = parse.getElementsByTagNameNS(Constants.IARI_AUTH_NS, Constants.PACKAGE_SIGNER_ELT);
            if (elementsByTagNameNS4.getLength() != 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of package-signer elements");
            }
            Element element4 = (Element) elementsByTagNameNS4.item(0);
            this.packageSignerNode = element4;
            if (element4.getParentNode() != element) {
                return printErrorMessage("Invalid IARI xml: package-signer must be a child of iari-authorization");
            }
            this.packageSignerNode.setIdAttribute(Constants.ID, true);
            this.mPackageSigner = this.packageSignerNode.getTextContent();
            NodeList elementsByTagNameNS5 = parse.getElementsByTagNameNS(Constants.DIGITAL_SIGN_NS, Constants.SIGNATURE_ELT);
            if (elementsByTagNameNS5.getLength() != 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of ds:Signature elements");
            }
            Element element5 = (Element) elementsByTagNameNS5.item(0);
            this.signatureNode = element5;
            if (element5.getParentNode() == element || printErrorMessage("Invalid IARI xml: signature node must be a child of iari-authorization")) {
                return true;
            }
            return false;
        } catch (ParserConfigurationException | SAXException e) {
            return printErrorMessage("Unexpected exception parsing IARI xml:" + e.getLocalizedMessage());
        } catch (IOException e2) {
            return printErrorMessage("Unexpected exception reading IARI xml: " + e2.getLocalizedMessage());
        }
    }

    private boolean printErrorMessage(String str) {
        String str2 = LOG_TAG;
        Log.d(str2, "iari xml parse error : " + str);
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("authType=");
        sb.append(this.mAuthType.name());
        sb.append(10);
        if (this.mIari != null) {
            sb.append("iari=");
            sb.append(this.mIari);
            sb.append(10);
        }
        if (this.mPackageName != null) {
            sb.append("packageName=");
            sb.append(this.mPackageName);
            sb.append(10);
        }
        if (this.mPackageSigner != null) {
            sb.append("packageSigner=");
            sb.append(this.mPackageSigner);
            sb.append(10);
        }
        SignatureInfo signatureInfo = this.mSignature;
        if (signatureInfo != null) {
            sb.append(signatureInfo);
            sb.append(10);
        }
        return sb.toString();
    }
}
