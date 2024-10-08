package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.ims.core.cmc.CmcConstants;
import java.io.ByteArrayInputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ChatbotXmlUtils {
    private static ChatbotXmlUtils sInstance;
    private XPathExpression mCommandIdPath;
    private DocumentBuilder mDocumentBuilder;
    private XPath mXpath;

    private ChatbotXmlUtils() {
    }

    public static synchronized ChatbotXmlUtils getInstance() {
        ChatbotXmlUtils chatbotXmlUtils;
        synchronized (ChatbotXmlUtils.class) {
            if (sInstance == null) {
                sInstance = new ChatbotXmlUtils();
            }
            chatbotXmlUtils = sInstance;
        }
        return chatbotXmlUtils;
    }

    public String composeAnonymizeXml(String str, String str2) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<AM" + " xmlns=\"urn:gsma:params:xml:ns:rcs:rcs:aliasmgmt\"" + ">\n" + "\t<Command-ID>" + str2 + "</Command-ID>\n" + "\t<action>" + str + "</action>\n" + "</AM>\n";
    }

    public String composeSpamXml(String str, List<String> list, String str2, String str3) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("\t<SR");
        sb.append(" xmlns=\"urn:gsma:params:xml:ns:rcs:rcs:spamreport\"");
        sb.append(">\n");
        sb.append("\t\t<Chatbot>");
        sb.append(str);
        sb.append("</Chatbot>\n");
        int i = 0;
        for (String next : list) {
            if (next != null && !next.isEmpty()) {
                if (i >= 10) {
                    break;
                }
                sb.append("\t\t<Message-ID>");
                sb.append(next);
                sb.append("</Message-ID>\n");
                i++;
            }
        }
        if (str2 != null) {
            sb.append("\t\t<spam-type>");
            sb.append(str2);
            sb.append("</spam-type>\n");
        }
        if (str3 != null) {
            String replace = str3.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;").replace(CmcConstants.E_NUM_STR_QUOTE, "&quot;").replace("'", "&apos;");
            sb.append("\t\t<free-text>");
            sb.append(replace);
            sb.append("</free-text>\n");
        }
        sb.append("</SR>\n");
        return sb.toString();
    }

    public String parseXml(String str, String str2) throws Exception {
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        this.mXpath = XPathFactory.newInstance().newXPath();
        try {
            this.mDocumentBuilder = newInstance.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        this.mCommandIdPath = createXpathLazy(str2);
        return extractString(this.mCommandIdPath, this.mDocumentBuilder.parse(new ByteArrayInputStream(str.getBytes("utf-8"))));
    }

    private XPathExpression createXpathLazy(String str) {
        try {
            return this.mXpath.compile(str);
        } catch (XPathExpressionException e) {
            throw new Error(e);
        }
    }

    private static String extractString(XPathExpression xPathExpression, Document document) throws XPathExpressionException {
        Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            return node.getTextContent();
        }
        return null;
    }
}
