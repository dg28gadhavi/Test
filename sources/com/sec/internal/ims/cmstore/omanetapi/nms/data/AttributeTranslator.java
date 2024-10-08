package com.sec.internal.ims.cmstore.omanetapi.nms.data;

import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.AttributeList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeTranslator implements IMessageAttributeInterface {
    private final List<Attribute> mAttributeList = new ArrayList();
    private final Map<String, String> mMessageAttributeRegistration;

    public AttributeTranslator(MessageStoreClient messageStoreClient) {
        this.mMessageAttributeRegistration = messageStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration();
    }

    public AttributeList getAttributeList() {
        List<Attribute> list = this.mAttributeList;
        AttributeList attributeList = new AttributeList();
        attributeList.attribute = (Attribute[]) list.toArray(new Attribute[this.mAttributeList.size()]);
        return attributeList;
    }

    public void setDate(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DATE)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DATE);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setFrom(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.FROM)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.FROM);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setTo(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.TO)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.TO);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setBCC(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.BCC)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.BCC);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setCC(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CC)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CC);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setDirection(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DIRECTION)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DIRECTION);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setMessageContext(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.MESSAGE_CONTEXT)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.MESSAGE_CONTEXT);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setSafetyMessage(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.SAFETY)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.SAFETY);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setSubject(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.SUBJECT)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.SUBJECT);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setOpenGroup(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.IS_OPEN_GROUP)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.IS_OPEN_GROUP);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setMessageId(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.MESSAGE_ID)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.MESSAGE_ID);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setMessageBody(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.MESSAGEBODY)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.MESSAGEBODY);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setMimeVersion(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.MIME_VERSION)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.MIME_VERSION);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setCpmGroup(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.IS_CPM_GROUP)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.IS_CPM_GROUP);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setContentType(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CONTENT_TYPE)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CONTENT_TYPE);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setPAssertedService(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.P_ASSERTED_SERVICE)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.P_ASSERTED_SERVICE);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setConversationId(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CONVERSATION_ID)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CONVERSATION_ID);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setDispositionStatus(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DISPOSITION_STATUS)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DISPOSITION_STATUS);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setDispositionType(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DISPOSITION_TYPE)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DISPOSITION_TYPE);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setDispositionOriginalMessageID(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setDispositionOriginalTo(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setPwd(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.PWD)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.PWD);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setOldPwd(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.OLD_PWD)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.OLD_PWD);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setGreetingType(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.X_CNS_GREETING_TYPE)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.X_CNS_GREETING_TYPE);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setContentDuration(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.CONTENT_DURATION)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CONTENT_DURATION);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setEmailAddress(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.EMAILADDRESS)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.EMAILADDRESS);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setVVMOn(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.VVMOn)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.VVMOn);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setNUT(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.NUT)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.NUT);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setTextContent(String str) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.TEXT_CONTENT)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.TEXT_CONTENT);
            attribute.value = new String[]{str};
            this.mAttributeList.add(attribute);
        }
    }

    public void setExtendedRCS(String[] strArr) {
        this.mMessageAttributeRegistration.containsValue(IMessageAttributeInterface.EXTENDED_RCS);
        Attribute attribute = new Attribute();
        attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.EXTENDED_RCS);
        attribute.value = strArr;
        this.mAttributeList.add(attribute);
    }

    public void setChipList(String[] strArr) {
        this.mMessageAttributeRegistration.containsValue(IMessageAttributeInterface.CHIPLIST);
        Attribute attribute = new Attribute();
        attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.CHIPLIST);
        attribute.value = strArr;
        this.mAttributeList.add(attribute);
    }

    public void setTrafficType(String[] strArr) {
        this.mMessageAttributeRegistration.containsValue(IMessageAttributeInterface.TRAFFICTYPE);
        Attribute attribute = new Attribute();
        attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.TRAFFICTYPE);
        attribute.value = strArr;
        this.mAttributeList.add(attribute);
    }

    public void setV2tLanguage(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.V2T_LANGUAGE)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.V2T_LANGUAGE);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setV2tResourceURL(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.V2T_RES)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.V2T_RES);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setV2tSMS(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.V2T_SMS)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.V2T_SMS);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }

    public void setV2tEmail(String[] strArr) {
        if (this.mMessageAttributeRegistration.containsKey(IMessageAttributeInterface.V2T_EMAIL)) {
            Attribute attribute = new Attribute();
            attribute.name = this.mMessageAttributeRegistration.get(IMessageAttributeInterface.V2T_EMAIL);
            attribute.value = strArr;
            this.mAttributeList.add(attribute);
        }
    }
}
