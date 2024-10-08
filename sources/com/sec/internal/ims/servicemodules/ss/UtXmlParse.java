package com.sec.internal.ims.servicemodules.ss;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.ims.servicemodules.ss.CallBarringData;
import com.sec.internal.ims.servicemodules.ss.CallForwardingData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UtXmlParse {
    public static final String LOG_TAG = "UtXmlParse";
    private XPathExpression mDefaultBehavior;
    private DocumentBuilder mDocumenetbuilder;
    private XPathExpression mErrorPath;
    private XPathExpression mReplyTimer;
    private XPathExpression mRootActiviationPath;
    private XPathExpression mRootBarringElement;
    private XPathExpression mRulePath;
    private XPath mXpath;

    public UtXmlParse() {
        try {
            this.mDocumenetbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        this.mXpath = XPathFactory.newInstance().newXPath();
        this.mReplyTimer = createXPathNode(UtElement.PARSE_NO_REPLY_TIMER);
        this.mRootActiviationPath = createXPathNode(UtElement.PARSE_ROOT_ACTIVATION);
        this.mRulePath = createXPathNode(UtElement.PARSE_RULE);
        this.mRootBarringElement = createXPathNode(UtElement.PARSE_ROOT_BARRING);
        this.mErrorPath = createXPathNode(UtElement.PARSE_ERROR);
        this.mDefaultBehavior = createXPathNode(UtElement.PARSE_BEHAVIOUR);
    }

    public boolean parseCallWaitingOrClip(String str) {
        try {
            return extractBoolean(this.mRootActiviationPath, this.mDocumenetbuilder.parse(new ByteArrayInputStream(str.getBytes("utf-8"))));
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CallBarringData parseCallBarring(String str) {
        CallBarringData callBarringData = new CallBarringData();
        try {
            Document parse = this.mDocumenetbuilder.parse(new ByteArrayInputStream(str.getBytes("utf-8")));
            int extractCbType = extractCbType(parse);
            callBarringData.active = extractBoolean(this.mRootActiviationPath, parse);
            NodeList extractNodeList = extractNodeList(this.mRulePath, parse);
            if (extractNodeList != null) {
                for (int i = 0; i < extractNodeList.getLength(); i++) {
                    CallBarringData.Rule rule = new CallBarringData.Rule();
                    rule.ruleId = extractNodeList.item(i).getAttributes().getNamedItem("id").getTextContent();
                    for (int i2 = 0; i2 < extractNodeList.item(i).getChildNodes().getLength(); i2++) {
                        NodeList childNodes = extractNodeList.item(i).getChildNodes();
                        if (childNodes.item(i2).getNodeName().contains("conditions")) {
                            Condition conditions = getConditions(childNodes.item(i2).getChildNodes());
                            rule.conditions = conditions;
                            if (conditions.condition == 16) {
                                NodeList childNodes2 = childNodes.item(i2).getChildNodes();
                                for (int i3 = 0; i3 < childNodes2.getLength(); i3++) {
                                    if (childNodes2.item(i3).getNodeName().contains("cp:identity")) {
                                        NodeList childNodes3 = childNodes2.item(i3).getChildNodes();
                                        for (int i4 = 0; i4 < childNodes3.getLength(); i4++) {
                                            if (childNodes3.item(i4).getNodeName().contains(UtElement.ELEMENT_ONE)) {
                                                rule.target.add(childNodes3.item(i4).getAttributes().getNamedItem("id").getTextContent());
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (childNodes.item(i2).getNodeName().contains(SoftphoneNamespaces.SoftphoneCallHandling.ACTIONS)) {
                            for (int i5 = 0; i5 < childNodes.item(i2).getChildNodes().getLength(); i5++) {
                                NodeList childNodes4 = childNodes.item(i2).getChildNodes();
                                if (childNodes4.item(i5).getNodeName().contains("allow")) {
                                    rule.allow = Boolean.parseBoolean(childNodes.item(i2).getTextContent());
                                } else if (childNodes4.item(i5).getNodeType() == 1) {
                                    ActionElm actionElm = new ActionElm();
                                    actionElm.name = childNodes4.item(i5).getNodeName();
                                    actionElm.value = childNodes4.item(i5).getTextContent();
                                    if (!(childNodes4.item(i5).getAttributes() == null || childNodes4.item(i5).getAttributes().getLength() == 0)) {
                                        actionElm.attribute = childNodes4.item(i5).getAttributes().item(0).toString();
                                    }
                                    rule.actions.add(actionElm);
                                }
                            }
                        }
                    }
                    if (rule.conditions.condition == -1) {
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(MEDIA.ALL);
                        rule.conditions.media = arrayList;
                    }
                    combineCbType(rule, extractCbType);
                    callBarringData.rules.add(rule);
                    Log.i(LOG_TAG, "ruleId = " + rule.ruleId + " conditions = " + rule.conditions + " allow = " + rule.allow);
                }
            }
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }
        return callBarringData;
    }

    public int parseClir(String str) {
        try {
            Document parse = this.mDocumenetbuilder.parse(new ByteArrayInputStream(str.getBytes("utf-8")));
            if (!extractBoolean(this.mRootActiviationPath, parse)) {
                return 0;
            }
            String extractString = extractString(this.mDefaultBehavior, parse);
            if (TextUtils.isEmpty(extractString)) {
                return 2;
            }
            if (extractString.contains(UtElement.ELEMENT_CLI_RESTRICTED)) {
                return 1;
            }
            extractString.contains(UtElement.ELEMENT_CLI_NOT_RESTRICTED);
            return 2;
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            return 2;
        }
    }

    private void combineCbType(CallBarringData.Rule rule, int i) {
        Condition condition = rule.conditions;
        int i2 = condition.condition;
        if (i2 == 0) {
            if (i == 102) {
                condition.condition = 1;
            } else {
                condition.condition = 2;
            }
        } else if (i2 != 12) {
            if (i2 == 14) {
                condition.condition = 5;
            } else if (i2 == 10) {
                condition.condition = 3;
            } else if (i2 == 11) {
                condition.condition = 4;
            } else if (i2 != 13) {
                if (i2 == 15) {
                    condition.condition = 6;
                } else if (i2 == 16) {
                    condition.condition = 10;
                }
            }
        }
    }

    public CallForwardingData parseCallForwarding(String str, Mno mno) {
        CallForwardingData callForwardingData = new CallForwardingData();
        try {
            Document parse = this.mDocumenetbuilder.parse(new ByteArrayInputStream(str.getBytes("utf-8")));
            callForwardingData.active = extractBoolean(this.mRootActiviationPath, parse);
            callForwardingData.replyTimer = extractInt(this.mReplyTimer, parse);
            NodeList extractNodeList = extractNodeList(this.mRulePath, parse);
            if (extractNodeList != null) {
                int i = 0;
                while (i < extractNodeList.getLength()) {
                    String textContent = extractNodeList.item(i).getAttributes().getNamedItem("id").getTextContent();
                    if (!mno.isChn()) {
                        if (!textContent.contains("rule2") && !textContent.contains("rule3") && !textContent.contains("-vm")) {
                            if (textContent.contains("-default")) {
                            }
                        }
                        i++;
                    }
                    CallForwardingData.Rule rule = new CallForwardingData.Rule();
                    rule.ruleId = textContent;
                    int i2 = 0;
                    for (int i3 = 0; i3 < extractNodeList.item(i).getChildNodes().getLength(); i3++) {
                        NodeList childNodes = extractNodeList.item(i).getChildNodes();
                        if (childNodes.item(i3).getNodeName().contains("conditions")) {
                            rule.conditions = getConditions(childNodes.item(i3).getChildNodes());
                        } else if (childNodes.item(i3).getNodeName().contains(SoftphoneNamespaces.SoftphoneCallHandling.ACTIONS)) {
                            rule.fwdElm = getForwardData(childNodes.item(i3).getChildNodes());
                        }
                        i2++;
                    }
                    if (!mno.isChn() || rule.conditions.condition != 0 || !callForwardingData.isExist(0) || i2 >= 2) {
                        if (rule.conditions.condition == -1) {
                            Condition condition = new Condition();
                            rule.conditions = condition;
                            condition.condition = 0;
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(MEDIA.ALL);
                            rule.conditions.media = arrayList;
                        }
                        callForwardingData.rules.add(rule);
                        i++;
                    } else {
                        Log.i(LOG_TAG, "Ignore the repeated CFU rule with low priority. id: " + textContent);
                        i++;
                    }
                }
            }
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }
        return callForwardingData;
    }

    public String parseError(String str) {
        String str2 = null;
        try {
            NodeList extractNodeList = extractNodeList(this.mErrorPath, this.mDocumenetbuilder.parse(new ByteArrayInputStream(str.getBytes("utf-8"))));
            if (extractNodeList != null) {
                for (int i = 0; i < extractNodeList.getLength(); i++) {
                    if ("constraint-failure".equals(extractNodeList.item(i).getNodeName())) {
                        if (extractNodeList.item(i).getAttributes().getNamedItem("phrase") != null) {
                            str2 = extractNodeList.item(i).getAttributes().getNamedItem("phrase").getTextContent();
                        }
                    }
                }
            }
        } catch (IOException | DOMException | SAXException e) {
            e.printStackTrace();
        }
        return str2;
    }

    private XPathExpression createXPathNode(String str) {
        try {
            return this.mXpath.compile(str);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractNodeName(XPathExpression xPathExpression, Document document) {
        try {
            Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
            if (node != null) {
                return node.getNodeName();
            }
            return null;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractString(XPathExpression xPathExpression, Document document) {
        try {
            Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
            if (node != null) {
                return node.getTextContent();
            }
            return null;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean extractBoolean(XPathExpression xPathExpression, Document document) {
        try {
            if (CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase((String) xPathExpression.evaluate(document, XPathConstants.STRING))) {
                return true;
            }
            return false;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int extractInt(XPathExpression xPathExpression, Document document) {
        try {
            String str = (String) xPathExpression.evaluate(document, XPathConstants.STRING);
            if (!TextUtils.isEmpty(str)) {
                return Integer.parseInt(str.trim());
            }
            return 20;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return 20;
        } catch (NumberFormatException unused) {
            Log.e(LOG_TAG, "Invalid integer");
            return 20;
        }
    }

    private static NodeList extractNodeList(XPathExpression xPathExpression, Document document) {
        try {
            return (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ForwardTo getForwardData(NodeList nodeList) {
        ForwardTo forwardTo = new ForwardTo();
        int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            for (int i2 = 0; i2 < nodeList.item(i).getChildNodes().getLength(); i2++) {
                String nodeName = nodeList.item(i).getChildNodes().item(i2).getNodeName();
                String textContent = nodeList.item(i).getChildNodes().item(i2).getTextContent();
                if (!nodeName.equals("#text")) {
                    if (nodeName.contains("to-target")) {
                        ForwardElm forwardElm = new ForwardElm();
                        forwardElm.id = nodeName;
                        forwardElm.value = textContent;
                        forwardTo.fwdElm.add(forwardElm);
                    } else if (!nodeName.contains(SoftphoneNamespaces.SoftphoneCallHandling.TARGET)) {
                        ForwardElm forwardElm2 = new ForwardElm();
                        forwardElm2.id = nodeName;
                        forwardElm2.value = textContent;
                        Node item = nodeList.item(i).getChildNodes().item(i2);
                        if (!(item.getAttributes() == null || item.getAttributes().getLength() == 0)) {
                            forwardElm2.attribute = item.getAttributes().item(0).toString();
                        }
                        forwardTo.fwdElm.add(forwardElm2);
                    } else {
                        forwardTo.target = textContent;
                    }
                }
            }
        }
        return forwardTo;
    }

    private List<MEDIA> getMediaTypes(NodeList nodeList) {
        int length = nodeList.getLength();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < length; i++) {
            if (nodeList.item(i).getTextContent().contains("audio")) {
                arrayList.add(MEDIA.AUDIO);
            } else if (nodeList.item(i).getTextContent().contains(SipMsg.FEATURE_TAG_MMTEL_VIDEO)) {
                arrayList.add(MEDIA.VIDEO);
            }
        }
        if (arrayList.isEmpty()) {
            arrayList.add(MEDIA.ALL);
        }
        return arrayList;
    }

    private int getConditionFromName(String str) {
        if (str.contains("busy")) {
            return 1;
        }
        if (str.contains("no-answer")) {
            return 2;
        }
        if (str.contains("not-reachable")) {
            return 3;
        }
        if (str.contains("not-logged") || str.contains("not-registered")) {
            return 6;
        }
        if (str.contains("international-exHC")) {
            return 11;
        }
        if (str.contains("international")) {
            return 10;
        }
        if (str.contains("roaming")) {
            return 14;
        }
        if (str.contains("external-list")) {
            return 12;
        }
        if (str.contains("other-identity")) {
            return 13;
        }
        if (str.contains("anonymous")) {
            return 15;
        }
        return str.contains("cp:identity") ? 16 : 0;
    }

    private Condition getConditions(NodeList nodeList) {
        int length = nodeList.getLength();
        Condition condition = new Condition();
        condition.media = new ArrayList();
        for (int i = 0; i < length; i++) {
            String nodeName = nodeList.item(i).getNodeName();
            if (!nodeName.contains("text")) {
                if (nodeName.contains("rule-deactivated")) {
                    condition.state = false;
                    condition.action = 0;
                } else if (!nodeName.contains("media")) {
                    condition.condition = getConditionFromName(nodeName);
                } else if (nodeList.item(i).getChildNodes().getLength() > 0 && condition.media.size() == 0) {
                    condition.media = getMediaTypes(nodeList.item(i).getChildNodes());
                }
            }
        }
        if (length == 0 || condition.condition == -1) {
            condition.condition = 0;
        }
        if (condition.media.size() == 0) {
            condition.media.add(MEDIA.ALL);
        }
        return condition;
    }

    private int extractCbType(Document document) {
        String extractNodeName = extractNodeName(this.mRootBarringElement, document);
        if (extractNodeName != null && !extractNodeName.isEmpty()) {
            if (extractNodeName.contains(UtElement.ELEMENT_ICB)) {
                return 102;
            }
            if (extractNodeName.contains(UtElement.ELEMENT_OCB)) {
                return 104;
            }
        }
        return 0;
    }
}
