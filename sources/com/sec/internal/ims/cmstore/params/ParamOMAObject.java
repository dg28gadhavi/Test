package com.sec.internal.ims.cmstore.params;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.RcsContent;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.RcsData;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.TmoGcmMessage;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.TmoPushNotificationRecipients;
import com.sec.internal.ims.cmstore.strategy.KorCmStrategy;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IMessageAttributeInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.FlagList;
import com.sec.internal.omanetapi.nms.data.ImdnInfo;
import com.sec.internal.omanetapi.nms.data.ImdnList;
import com.sec.internal.omanetapi.nms.data.ImdnObject;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.PayloadPartInfo;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ParamOMAObject {
    public String CHIPLIST = null;
    public String CONTENT_DURATION = null;
    public String CONTENT_TYPE = null;
    public String CONTRIBUTION_ID = null;
    public String CONVERSATION_ID = null;
    public String DATE = null;
    public String DIRECTION = null;
    public String DISPOSITION_ORIGINAL_MESSAGEID = null;
    public String DISPOSITION_ORIGINAL_TO = null;
    public String DISPOSITION_STATUS = null;
    public String EXTENDEDRCS = null;
    public String FROM = null;
    public String IMPORTANCE = null;
    public boolean IS_CPM_GROUP = false;
    public boolean IS_OPEN_GROUP = false;
    public String MESSAGEBODY = null;
    public String MESSAGE_ID = null;
    public String MULTIPARTCONTENTTYPE = null;
    public String PARTICIPATING_DEVICE = null;
    public String P_ASSERTED_SERVICE = null;
    public int SAFE_MESSAGE = 0;
    public String SENSITIVITY = null;
    public String SUBJECT = null;
    private String TAG = ParamOMAObject.class.getSimpleName();
    public String TEXT_CONTENT = null;
    public ArrayList<String> TO = new ArrayList<>();
    public String TRAFFICTYPE = null;
    public String X_CNS_Greeting_Type = null;
    public String correlationId;
    public String correlationTag;
    private String countryCode;
    public Long lastModSeq;
    public CloudMessageBufferDBConstants.ActionStatusFlag mFlag;
    public FlagList mFlagList = null;
    public Map<String, NotificationStatus> mImdnList;
    public ImdnList mImdns;
    public boolean mIsFromChangedObj = false;
    public boolean mIsGoforwardSync;
    public String mLine = null;
    public Set<ImsUri> mNomalizedOtherParticipants;
    public int mObjectType;
    private String mRawFromString = null;
    public boolean mReassembled = false;
    private MessageStoreClient mStoreClient;
    public URL parentFolder;
    public String parentFolderPath;
    public String path;
    public PayloadPartInfo[] payloadPart;
    public URL payloadURL;
    public String protocol = "";
    public URL resourceURL = null;
    public String[] sFlags = null;

    private CloudMessageBufferDBConstants.ActionStatusFlag getCloudActionPerFlag(FlagList flagList) {
        CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag = CloudMessageBufferDBConstants.ActionStatusFlag.Insert;
        if (flagList == null || flagList.flag == null) {
            Log.i(this.TAG, "Null Flags");
            return actionStatusFlag;
        }
        String str = this.TAG;
        Log.i(str, "getCloudActionPerFlag: " + Arrays.toString(flagList.flag));
        int i = 0;
        if (!this.mIsGoforwardSync) {
            while (true) {
                String[] strArr = flagList.flag;
                if (i >= strArr.length) {
                    break;
                }
                CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.None;
                if (strArr[i].equalsIgnoreCase(FlagNames.Seen)) {
                    actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                } else if (flagList.flag[i].equalsIgnoreCase(FlagNames.Deleted)) {
                    actionStatusFlag2 = CloudMessageBufferDBConstants.ActionStatusFlag.Delete;
                }
                if (actionStatusFlag2.getId() > actionStatusFlag.getId()) {
                    actionStatusFlag = actionStatusFlag2;
                }
                i++;
            }
        } else {
            while (true) {
                String[] strArr2 = flagList.flag;
                if (i >= strArr2.length) {
                    break;
                }
                if (strArr2[i].equalsIgnoreCase(FlagNames.Seen)) {
                    CloudMessageBufferDBConstants.ActionStatusFlag actionStatusFlag3 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
                    if (actionStatusFlag3.getId() > actionStatusFlag.getId()) {
                        actionStatusFlag = actionStatusFlag3;
                    }
                }
                i++;
            }
        }
        return actionStatusFlag;
    }

    private Map<String, NotificationStatus> getCloudActionPerImdn(ImdnList imdnList) {
        ImdnObject[] imdnObjectArr;
        if (imdnList == null || (imdnObjectArr = imdnList.imdn) == null || imdnObjectArr.length == 0) {
            return null;
        }
        HashMap hashMap = new HashMap();
        for (ImdnObject imdnObject : imdnList.imdn) {
            String str = imdnObject.originalTo;
            ImdnInfo[] imdnInfoArr = imdnObject.imdnInfo;
            int length = imdnInfoArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if ("displayed".equalsIgnoreCase(imdnInfoArr[i].type)) {
                    hashMap.put(str, NotificationStatus.DISPLAYED);
                    break;
                } else {
                    hashMap.put(str, NotificationStatus.DELIVERED);
                    i++;
                }
            }
        }
        return hashMap;
    }

    public ParamOMAObject(Object object, boolean z, int i, ICloudMessageManagerHelper iCloudMessageManagerHelper, MessageStoreClient messageStoreClient) {
        int i2;
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.countryCode = Util.getSimCountryCode(messageStoreClient.getContext(), messageStoreClient.getClientID());
        this.mIsGoforwardSync = z;
        this.parentFolder = object.parentFolder;
        this.parentFolderPath = object.parentFolderPath;
        this.mFlag = getCloudActionPerFlag(object.flags);
        FlagList flagList = object.flags;
        this.mFlagList = flagList;
        if (flagList != null) {
            this.sFlags = flagList.flag;
        }
        ImdnList imdnList = object.imdns;
        this.mImdns = imdnList;
        if (imdnList != null) {
            this.mImdnList = getCloudActionPerImdn(imdnList);
        }
        this.resourceURL = object.resourceURL;
        if (TextUtils.isEmpty(object.path)) {
            this.path = "";
        } else {
            this.path = object.path;
        }
        this.payloadURL = object.payloadURL;
        this.payloadPart = object.payloadPart;
        this.lastModSeq = object.lastModSeq;
        this.correlationId = object.correlationId;
        this.correlationTag = object.correlationTag;
        if (!TextUtils.isEmpty(object.protocol)) {
            this.protocol = object.protocol;
        }
        this.mObjectType = i;
        URL url = this.resourceURL;
        if (url == null || TextUtils.isEmpty(url.toString())) {
            this.mLine = messageStoreClient.getPrerenceManager().getUserCtn();
        } else {
            this.mLine = Util.getLineTelUriFromObjUrl(this.resourceURL.toString());
        }
        ArrayList arrayList = new ArrayList();
        int i3 = 0;
        while (true) {
            Attribute[] attributeArr = object.attributes.attribute;
            if (i3 >= attributeArr.length) {
                break;
            }
            Attribute attribute = attributeArr[i3];
            String str = attribute.name;
            if (str != null && attribute.value != null) {
                if (str.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.DATE))) {
                    this.DATE = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.IS_CPM_GROUP))) {
                    this.IS_CPM_GROUP = "yes".equalsIgnoreCase(object.attributes.attribute[i3].value[0]);
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.IS_OPEN_GROUP))) {
                    this.IS_OPEN_GROUP = "yes".equalsIgnoreCase(object.attributes.attribute[i3].value[0]);
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.DIRECTION))) {
                    this.DIRECTION = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.FROM))) {
                    this.FROM = Util.getTelUri(object.attributes.attribute[i3].value[0], this.countryCode);
                    this.mRawFromString = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.TO))) {
                    if (object.attributes.attribute[i3].value != null) {
                        int i4 = 0;
                        while (true) {
                            String[] strArr = object.attributes.attribute[i3].value;
                            if (i4 >= strArr.length) {
                                break;
                            }
                            this.TO.add(Util.getTelUri(strArr[i4], this.countryCode));
                            i4++;
                        }
                    }
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.CC))) {
                    if (object.attributes.attribute[i3].value != null) {
                        int i5 = 0;
                        while (true) {
                            String[] strArr2 = object.attributes.attribute[i3].value;
                            if (i5 >= strArr2.length) {
                                break;
                            }
                            this.TO.add(Util.getTelUri(strArr2[i5], this.countryCode));
                            i5++;
                        }
                    }
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.BCC))) {
                    if (object.attributes.attribute[i3].value != null) {
                        int i6 = 0;
                        while (true) {
                            String[] strArr3 = object.attributes.attribute[i3].value;
                            if (i6 >= strArr3.length) {
                                break;
                            }
                            arrayList.add(Util.getTelUri(strArr3[i6], this.countryCode));
                            i6++;
                        }
                    }
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.TEXT_CONTENT))) {
                    this.TEXT_CONTENT = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.SUBJECT))) {
                    this.SUBJECT = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.CONVERSATION_ID))) {
                    this.CONVERSATION_ID = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.CONTRIBUTION_ID))) {
                    this.CONTRIBUTION_ID = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.DISPOSITION_STATUS))) {
                    this.DISPOSITION_STATUS = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.DISPOSITION_ORIGINAL_TO))) {
                    this.DISPOSITION_ORIGINAL_TO = Util.getTelUri(object.attributes.attribute[i3].value[0], this.countryCode);
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.DISPOSITION_ORIGINAL_MESSAGEID))) {
                    this.DISPOSITION_ORIGINAL_MESSAGEID = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.MULTIPARTCONTENTTYPE))) {
                    this.MULTIPARTCONTENTTYPE = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.CONTENT_TYPE))) {
                    this.CONTENT_TYPE = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.MESSAGE_ID))) {
                    this.MESSAGE_ID = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.CONTENT_DURATION))) {
                    this.CONTENT_DURATION = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.PARTICIPATING_DEVICE))) {
                    this.PARTICIPATING_DEVICE = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.X_CNS_GREETING_TYPE))) {
                    this.X_CNS_Greeting_Type = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.MESSAGE_CONTEXT))) {
                    this.mObjectType = getMessageContextType(object.attributes.attribute[i3].value[0]);
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.IMPORTANCE))) {
                    String str2 = object.attributes.attribute[i3].value[0];
                    object.importance = str2;
                    this.IMPORTANCE = str2;
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.SENSITIVITY))) {
                    String str3 = object.attributes.attribute[i3].value[0];
                    object.sensitivity = str3;
                    this.SENSITIVITY = str3;
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.P_ASSERTED_SERVICE))) {
                    this.P_ASSERTED_SERVICE = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.MESSAGEBODY))) {
                    this.MESSAGEBODY = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.EXTENDED_RCS))) {
                    this.EXTENDEDRCS = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.CHIPLIST))) {
                    this.CHIPLIST = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.TRAFFICTYPE))) {
                    this.TRAFFICTYPE = object.attributes.attribute[i3].value[0];
                } else if (object.attributes.attribute[i3].name.equalsIgnoreCase(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMessageAttributeRegistration().get(IMessageAttributeInterface.SAFETY))) {
                    this.SAFE_MESSAGE = CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(object.attributes.attribute[i3].value[0]) ? 1 : 0;
                }
            }
            i3++;
        }
        if ("Out".equalsIgnoreCase(this.DIRECTION) && !arrayList.isEmpty()) {
            this.TO.addAll(arrayList);
        }
        if (this.mStoreClient.getCloudMessageStrategyManager().getStrategy().shouldCorrectShortCode()) {
            recalculateCorrelationTag();
        }
        if (!(this.mStoreClient.getCloudMessageStrategyManager().getStrategy() instanceof KorCmStrategy) && (11 == (i2 = this.mObjectType) || 14 == i2)) {
            updatePartcipantContentEmail();
        }
        this.mNomalizedOtherParticipants = getNormalizedParticipantsExcludeOwn();
    }

    private Set<ImsUri> getNormalizedParticipantsExcludeOwn() {
        HashSet hashSet = new HashSet();
        Iterator<String> it = this.TO.iterator();
        while (it.hasNext()) {
            String next = it.next();
            ImsUri parse = ImsUri.parse(next);
            String str = this.TAG;
            IMSLog.i(str, "getNormalizedParticipantsExcludeOwn uri: " + IMSLog.numberChecker(next) + ", value: " + IMSLog.numberChecker(parse));
            if (parse != null) {
                hashSet.add(parse);
            }
        }
        if (ImsUri.parse(this.FROM) != null) {
            hashSet.add(ImsUri.parse(this.FROM));
        }
        hashSet.remove(ImsUri.parse(this.mLine));
        return hashSet;
    }

    private int getMessageContextType(String str) {
        String str2 = this.TAG;
        Log.d(str2, "getMessageContextType: " + str);
        return this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getTypeUsingMessageContext(str);
    }

    public ParamOMAObject(ChangedObject changedObject, boolean z, int i, String str, MessageStoreClient messageStoreClient) {
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.countryCode = Util.getSimCountryCode(messageStoreClient.getContext(), messageStoreClient.getClientID());
        boolean z2 = true;
        this.mIsFromChangedObj = true;
        this.mIsGoforwardSync = z;
        this.parentFolder = changedObject.parentFolder;
        this.mFlag = getCloudActionPerFlag(changedObject.flags);
        FlagList flagList = changedObject.flags;
        this.mFlagList = flagList;
        if (flagList != null) {
            this.sFlags = flagList.flag;
        }
        this.resourceURL = changedObject.resourceURL;
        this.payloadURL = null;
        this.payloadPart = null;
        this.lastModSeq = changedObject.lastModSeq;
        this.correlationId = changedObject.correlationId;
        this.correlationTag = changedObject.correlationTag;
        if ("FT".equalsIgnoreCase(str)) {
            this.mObjectType = 12;
        } else if (CloudMessageProviderContract.DataTypes.CHAT.equalsIgnoreCase(str)) {
            this.mObjectType = 11;
        } else if (CloudMessageProviderContract.DataTypes.GSO.equalsIgnoreCase(str)) {
            this.mObjectType = 34;
        } else {
            this.mObjectType = i;
        }
        this.mLine = Util.getLineTelUriFromObjUrl(this.resourceURL.toString());
        TmoGcmMessage tmoGcmMessage = changedObject.extendedMessage;
        this.DATE = tmoGcmMessage.message_time;
        this.DIRECTION = tmoGcmMessage.direction;
        String str2 = tmoGcmMessage.reassembled;
        this.mReassembled = (str2 == null || !CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(str2)) ? false : z2;
        this.mRawFromString = changedObject.extendedMessage.sender;
        if ("In".equalsIgnoreCase(this.DIRECTION)) {
            this.FROM = Util.getTelUri(changedObject.extendedMessage.sender, this.countryCode);
            for (TmoPushNotificationRecipients tmoPushNotificationRecipients : changedObject.extendedMessage.recipients) {
                this.TO.add(Util.getTelUri(tmoPushNotificationRecipients.uri, this.countryCode));
            }
        } else {
            this.FROM = this.mLine;
            for (TmoPushNotificationRecipients tmoPushNotificationRecipients2 : changedObject.extendedMessage.recipients) {
                this.TO.add(Util.getTelUri(tmoPushNotificationRecipients2.uri, this.countryCode));
            }
        }
        RcsContent rcsContent = changedObject.extendedMessage.content[0];
        this.TEXT_CONTENT = rcsContent.content;
        this.CONTENT_TYPE = rcsContent.content_type;
        RcsData rcsData = rcsContent.rcsdata;
        this.CONVERSATION_ID = rcsData.conversation_id;
        this.CONTRIBUTION_ID = rcsData.contribution_id;
        int i2 = this.mObjectType;
        if (11 == i2 || 14 == i2) {
            updatePartcipantContentEmail();
        }
        this.mNomalizedOtherParticipants = getNormalizedParticipantsExcludeOwn();
    }

    private void updatePartcipantContentEmail() {
        String[] parseEmailOverSlm;
        if (this.TEXT_CONTENT != null && (parseEmailOverSlm = Util.parseEmailOverSlm(ImsUri.parse(this.mRawFromString), this.TEXT_CONTENT)) != null) {
            ImsUri parse = ImsUri.parse("sip:" + parseEmailOverSlm[0]);
            if (parse != null) {
                this.FROM = parse.toString();
            }
            this.TEXT_CONTENT = parseEmailOverSlm[1];
        }
    }

    private void recalculateCorrelationTag() {
        if ("IN".equalsIgnoreCase(this.DIRECTION) && AmbsUtils.isInvalidShortCode(this.mRawFromString) && !TextUtils.isEmpty(this.correlationTag)) {
            String substring = this.mRawFromString.substring(1);
            this.mRawFromString = substring;
            this.FROM = Util.getTelUri(substring, this.countryCode);
            this.correlationTag = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getSmsHashTagOrCorrelationTag(this.mRawFromString, 1, this.TEXT_CONTENT);
            Log.d(this.TAG, "recalculateCorrelationTag: ");
        }
    }

    public String toString() {
        return "OMAConvertedObjectParam [mObjectType=" + this.mObjectType + ", mLine= " + IMSLog.checker(this.mLine) + ", correlationId= " + this.correlationId + ", correlationTag=" + this.correlationTag + ", resourceURL=" + IMSLog.checker(this.resourceURL) + ", mFlag=" + this.mFlag + " , mFlagList = " + Arrays.toString(this.sFlags) + ", DISPOSITION_STATUS=" + this.DISPOSITION_STATUS + ", mIsGoforwardSync=" + this.mIsGoforwardSync + ", TEXT_CONTENT=" + IMSLog.checker(this.TEXT_CONTENT) + ", DIRECTION=" + this.DIRECTION + ", DATE=" + this.DATE + ", MESSAGE_ID=" + this.MESSAGE_ID + ", CONTENT_TYPE=" + this.CONTENT_TYPE + ", X_CNS_Greeting_Type=" + this.X_CNS_Greeting_Type + ", mReassembled=" + this.mReassembled + ", mIsFromChangeObj=" + this.mIsFromChangedObj + " mNomalizedOtherParticipants=" + IMSLog.numberChecker((Collection<ImsUri>) this.mNomalizedOtherParticipants) + ", SUBJECT=" + this.SUBJECT + ", IS_CPM_GROUP=" + this.IS_CPM_GROUP + ", IS_OPEN_GROUP=" + this.IS_OPEN_GROUP + ", Importance= " + this.IMPORTANCE + ", SENSITIVITY=" + this.SENSITIVITY + ", protocol=" + this.protocol + "]";
    }
}
