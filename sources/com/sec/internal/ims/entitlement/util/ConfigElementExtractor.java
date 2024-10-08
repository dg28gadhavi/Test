package com.sec.internal.ims.entitlement.util;

import android.text.TextUtils;
import com.sec.internal.log.IMSLog;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ConfigElementExtractor {
    private static final String LOG_TAG = "ConfigElementExtractor";

    public static Map<String, String> getAllElements(String str, String str2) {
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        try {
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath().evaluate(str2, new InputSource(new ByteArrayInputStream(str.getBytes())), XPathConstants.NODESET);
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                addChildNodes("", nodeList.item(i), hashMap, hashMap2);
            }
        } catch (IllegalArgumentException | XPathExpressionException e) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "XPath expression failed or Source became null" + e.getMessage());
        }
        return hashMap;
    }

    private static void addChildNodes(String str, Node node, Map<String, String> map, Map<String, Integer> map2) {
        String deriveKeyName = deriveKeyName(str, node, map2);
        if (node.getChildNodes() != null && node.getChildNodes().getLength() == 1) {
            map.put(deriveKeyName, node.getTextContent().trim());
        }
        if (!node.hasAttributes() && !node.hasChildNodes() && node.getNodeType() == 2 && node.getNodeValue() != null) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "addChildNodes: keyName = " + deriveKeyName + " value = " + node.getNodeValue());
            map.put(deriveKeyName, node.getNodeValue().trim());
        }
        addAllAttributeNodes(deriveKeyName, node, map);
        addAllChildsToMap(node, deriveKeyName, map, map2);
    }

    private static String deriveKeyName(String str, Node node, Map<String, Integer> map) {
        String nodeName = node.getNodeName();
        if (!TextUtils.isEmpty(str)) {
            nodeName = str + "/" + node.getNodeName();
        }
        if (map.get(nodeName) == null) {
            map.put(nodeName, 1);
            return nodeName;
        }
        int intValue = map.get(nodeName).intValue() + 1;
        map.put(nodeName, Integer.valueOf(intValue));
        return nodeName + "[" + intValue + "]";
    }

    private static void addAllChildsToMap(Node node, String str, Map<String, String> map, Map<String, Integer> map2) {
        NodeList childNodes = node.getChildNodes();
        int length = childNodes.getLength();
        for (int i = 0; i < length; i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == 1) {
                String deriveKeyName = deriveKeyName(str, item, map2);
                if (item.getChildNodes() != null && item.getChildNodes().getLength() == 1) {
                    map.put(deriveKeyName, item.getTextContent().trim());
                }
                addAllAttributeNodes(deriveKeyName, item, map);
                addAllChildsToMap(item, deriveKeyName, map, map2);
            }
        }
    }

    private static void addAllAttributeNodes(String str, Node node, Map<String, String> map) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null && attributes.getLength() > 0) {
            Node item = attributes.item(0);
            String nodeName = item.getNodeName();
            String textContent = item.getTextContent();
            map.put(str + "." + nodeName, textContent);
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item2 = attributes.item(i);
                String nodeName2 = item2.getNodeName();
                String textContent2 = item2.getTextContent();
                map.put(str + "." + nodeName2, textContent2);
            }
        }
    }
}
