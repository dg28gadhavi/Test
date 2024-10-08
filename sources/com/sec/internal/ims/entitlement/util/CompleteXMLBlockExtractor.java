package com.sec.internal.ims.entitlement.util;

import com.sec.internal.log.IMSLog;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class CompleteXMLBlockExtractor {
    private static final String LOG_TAG = "CompleteXMLBlockExtractor";

    public static String getXmlBlockForElement(String str, String str2) {
        try {
            return nodeToString((Node) XPathFactory.newInstance().newXPath().evaluate(str2, new InputSource(new ByteArrayInputStream(str.getBytes())), XPathConstants.NODE));
        } catch (XPathExpressionException e) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "XPath expression failed :" + e.getMessage());
            return null;
        }
    }

    private static String nodeToString(Node node) {
        StringWriter stringWriter = new StringWriter();
        try {
            Transformer newTransformer = TransformerFactory.newInstance().newTransformer();
            newTransformer.setOutputProperty("omit-xml-declaration", "yes");
            newTransformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        } catch (TransformerException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "TransformerException: could not transform to string:" + e.getMessage());
        }
        return stringWriter.toString();
    }
}
