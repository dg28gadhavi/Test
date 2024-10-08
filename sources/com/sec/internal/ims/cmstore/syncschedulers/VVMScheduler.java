package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Looper;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmFolders;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUriParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.AttributeList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.mail.MessagingException;

public class VVMScheduler extends BaseMessagingScheduler {
    private static final String CONFIDENTIAL_SENSITIVITY = "CONFIDENTIAL";
    private static final String HIGH_IMPORTANCE = "HIGH";
    private static final String NORMAL_IMPORTANCE = "NORMAL";
    private static final String PERSONAL_SENSITIVITY = "PERSONAL";
    private static final String PRIVATE_SENSITIVITY = "PRIVATE";
    private String TAG = VVMScheduler.class.getSimpleName();
    private final VVMQueryBuilder mBufferDbQuery;

    public VVMScheduler(MessageStoreClient messageStoreClient, CloudMessageBufferDBEventSchedulingRule cloudMessageBufferDBEventSchedulingRule, SummaryQueryBuilder summaryQueryBuilder, IDeviceDataChangeListener iDeviceDataChangeListener, IBufferDBEventListener iBufferDBEventListener, Looper looper) {
        super(messageStoreClient, cloudMessageBufferDBEventSchedulingRule, iDeviceDataChangeListener, iBufferDBEventListener, looper, summaryQueryBuilder);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mBufferDbQuery = new VVMQueryBuilder(messageStoreClient, iBufferDBEventListener);
        this.mDbTableContractIndex = 17;
    }

    public void resetImsi() {
        Log.i(this.TAG, "resetImsi");
        this.mBufferDbQuery.resetImsi();
    }

    public void handleVvmProfileDownloaded(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        VvmProfileAttributes parseDownloadedVvmAttributes;
        this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(false);
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null && paramOMAresponseforBufDB.getVvmServiceProfile() != null && (parseDownloadedVvmAttributes = parseDownloadedVvmAttributes(paramOMAresponseforBufDB.getVvmServiceProfile())) != null) {
            String[] strArr = {String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)};
            String str = this.TAG;
            int clientID = this.mStoreClient.getClientID();
            EventLogHelper.add(str, clientID, "handleVvmProfileDownloaded  nut value: " + parseDownloadedVvmAttributes.NUT);
            ContentValues contentValues = new ContentValues();
            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.VVMON, parseDownloadedVvmAttributes.VVMOn);
            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.ISBLOCKED, parseDownloadedVvmAttributes.IsBlocked);
            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.COS, parseDownloadedVvmAttributes.COSName);
            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.LANGUAGE, parseDownloadedVvmAttributes.Language);
            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.NUT, parseDownloadedVvmAttributes.NUT);
            if (parseDownloadedVvmAttributes.EmailAddress.size() == 1) {
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1, parseDownloadedVvmAttributes.EmailAddress.get(0));
            } else if (parseDownloadedVvmAttributes.EmailAddress.size() == 2) {
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1, parseDownloadedVvmAttributes.EmailAddress.get(0));
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR2, parseDownloadedVvmAttributes.EmailAddress.get(1));
            }
            String str2 = this.TAG;
            Log.i(str2, "handleVvmProfileDownloaded nut value: " + parseDownloadedVvmAttributes.NUT + ", COSName value: " + parseDownloadedVvmAttributes.COSName + ", V2t_Language: " + parseDownloadedVvmAttributes.V2t_Language + ", EmailAddress count: " + parseDownloadedVvmAttributes.EmailAddress.size() + ", VVMOn: " + parseDownloadedVvmAttributes.VVMOn + ", IsBlocked: " + parseDownloadedVvmAttributes.IsBlocked + ", v2t_sms: " + parseDownloadedVvmAttributes.v2t_sms + ", v2t_email: " + parseDownloadedVvmAttributes.v2t_email);
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
            contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.V2T_LANGUAGE, parseDownloadedVvmAttributes.V2t_Language);
            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.V2T_SMS, parseDownloadedVvmAttributes.v2t_sms);
            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.V2T_EMAIL, parseDownloadedVvmAttributes.v2t_email);
            this.mBufferDbQuery.updateTable(20, contentValues, "_bufferdbid=?", strArr);
            this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPROFILE, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
        }
    }

    public void handleVvmQuotaInfo(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        VvmQuotaAttributes parseDownloadedVvmQuotaAttributes;
        if (paramOMAresponseforBufDB.getBufferDBChangeParam() != null && paramOMAresponseforBufDB.getVvmFolders() != null && (parseDownloadedVvmQuotaAttributes = parseDownloadedVvmQuotaAttributes(paramOMAresponseforBufDB.getVvmFolders())) != null) {
            String[] strArr = {String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)};
            ContentValues contentValues = new ContentValues();
            contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.TOTAL_STORAGE, Long.valueOf(parseDownloadedVvmQuotaAttributes.TotalStorage));
            contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.OCCUPIED_STORAGE, Long.valueOf(parseDownloadedVvmQuotaAttributes.OccupiedStorage));
            contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.VMMESSAGES_QUOTA, Integer.valueOf(parseDownloadedVvmQuotaAttributes.VMMessagesQuota));
            contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.VMOCUPPIED_MESSAGES, Integer.valueOf(parseDownloadedVvmQuotaAttributes.VMOccupiedMessages));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
            contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.LAST_UPDATED, Long.valueOf(System.currentTimeMillis()));
            contentValues.put("linenum", paramOMAresponseforBufDB.getBufferDBChangeParam().mLine);
            contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
            try {
                this.mBufferDbQuery.updateTable(36, contentValues, "_bufferdbid=?", strArr);
                this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMQUOTA, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
            } catch (SQLException e) {
                Log.e(this.TAG, e.getMessage());
            }
        }
    }

    private static class VvmProfileAttributes {
        String COSName;
        ArrayList<String> EmailAddress;
        String IsBlocked;
        String Language;
        String NUT;
        String V2t_Language;
        String VVMOn;
        String v2t_email;
        String v2t_sms;

        VvmProfileAttributes() {
            this.VVMOn = null;
            this.IsBlocked = null;
            this.COSName = null;
            this.Language = "eng";
            this.NUT = null;
            this.EmailAddress = null;
            this.V2t_Language = "None";
            this.v2t_email = null;
            this.v2t_sms = null;
            this.EmailAddress = new ArrayList<>();
        }
    }

    private static class VvmQuotaAttributes {
        long OccupiedStorage;
        long TotalStorage;
        int VMMessagesQuota;
        int VMOccupiedMessages;

        private VvmQuotaAttributes() {
            this.TotalStorage = 0;
            this.OccupiedStorage = 0;
            this.VMMessagesQuota = 0;
            this.VMOccupiedMessages = 0;
        }
    }

    public enum VvmMessageSensitivity {
        INVALID(-1),
        PERSONAL(0),
        PRIVATE(1),
        CONFIDENTIAL(2);
        
        private final int mId;

        private VvmMessageSensitivity(int i) {
            this.mId = i;
        }

        public String toString() {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageSensitivity[ordinal()];
            if (i != 2) {
                return i != 3 ? "Personal" : "Confidential";
            }
            return "Private";
        }
    }

    public enum VvmMessageImportance {
        INVALID(-1),
        NORMAL(0),
        HIGH(1);
        
        private final int mId;

        private VvmMessageImportance(int i) {
            this.mId = i;
        }

        public String toString() {
            return AnonymousClass1.$SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageImportance[ordinal()] != 2 ? "Normal" : "High";
        }
    }

    private VvmProfileAttributes parseDownloadedVvmAttributes(VvmServiceProfile vvmServiceProfile) {
        AttributeList attributeList = vvmServiceProfile.attributes;
        if (attributeList == null || attributeList.attribute == null) {
            Log.i(this.TAG, "parseDownloadedVvmAttributes: invalid profile");
            return null;
        }
        VvmProfileAttributes vvmProfileAttributes = new VvmProfileAttributes();
        int i = 0;
        while (true) {
            Attribute[] attributeArr = vvmServiceProfile.attributes.attribute;
            if (i >= attributeArr.length) {
                return vvmProfileAttributes;
            }
            Attribute attribute = attributeArr[i];
            String str = attribute.name;
            if (str != null && attribute.value[0] != null) {
                if ("cosname".equalsIgnoreCase(str)) {
                    vvmProfileAttributes.COSName = vvmServiceProfile.attributes.attribute[i].value[0];
                } else if (CloudMessageProviderContract.VVMAccountInfoColumns.ISBLOCKED.equalsIgnoreCase(vvmServiceProfile.attributes.attribute[i].name)) {
                    vvmProfileAttributes.IsBlocked = vvmServiceProfile.attributes.attribute[i].value[0];
                } else if (CloudMessageProviderContract.VVMAccountInfoColumns.LANGUAGE.equalsIgnoreCase(vvmServiceProfile.attributes.attribute[i].name)) {
                    vvmProfileAttributes.Language = vvmServiceProfile.attributes.attribute[i].value[0];
                } else if (CloudMessageProviderContract.VVMAccountInfoColumns.NUT.equalsIgnoreCase(vvmServiceProfile.attributes.attribute[i].name)) {
                    vvmProfileAttributes.NUT = vvmServiceProfile.attributes.attribute[i].value[0];
                } else if (CloudMessageProviderContract.VVMAccountInfoColumns.VVMON.equalsIgnoreCase(vvmServiceProfile.attributes.attribute[i].name)) {
                    vvmProfileAttributes.VVMOn = vvmServiceProfile.attributes.attribute[i].value[0];
                } else if ("EmailAddress".equalsIgnoreCase(vvmServiceProfile.attributes.attribute[i].name)) {
                    int i2 = 0;
                    while (true) {
                        String[] strArr = vvmServiceProfile.attributes.attribute[i].value;
                        if (i2 >= strArr.length) {
                            break;
                        }
                        vvmProfileAttributes.EmailAddress.add(strArr[i2]);
                        i2++;
                    }
                } else if ("V2t_Language".equalsIgnoreCase(vvmServiceProfile.attributes.attribute[i].name)) {
                    vvmProfileAttributes.V2t_Language = vvmServiceProfile.attributes.attribute[i].value[0];
                } else if ("V2E_ON".equalsIgnoreCase(vvmServiceProfile.attributes.attribute[i].name)) {
                    vvmProfileAttributes.v2t_email = vvmServiceProfile.attributes.attribute[i].value[0];
                } else if ("SMSDirectLink".equalsIgnoreCase(vvmServiceProfile.attributes.attribute[i].name)) {
                    vvmProfileAttributes.v2t_sms = vvmServiceProfile.attributes.attribute[i].value[0];
                }
            }
            i++;
        }
    }

    private VvmQuotaAttributes parseDownloadedVvmQuotaAttributes(VvmFolders vvmFolders) {
        AttributeList attributeList = vvmFolders.attributes;
        if (attributeList == null || attributeList.attribute == null) {
            Log.i(this.TAG, "parseDownloadedVvmQuotaAttributes: invalid profile");
            return null;
        }
        VvmQuotaAttributes vvmQuotaAttributes = new VvmQuotaAttributes();
        int i = 0;
        while (true) {
            Attribute[] attributeArr = vvmFolders.attributes.attribute;
            if (i < attributeArr.length) {
                Attribute attribute = attributeArr[i];
                String str = attribute.name;
                if (!(str == null || attribute.value[0] == null)) {
                    if (CloudMessageProviderContract.VVMQuotaColumns.TOTAL_STORAGE.equalsIgnoreCase(str)) {
                        vvmQuotaAttributes.TotalStorage = Long.valueOf(vvmFolders.attributes.attribute[i].value[0]).longValue();
                    } else if (CloudMessageProviderContract.VVMQuotaColumns.OCCUPIED_STORAGE.equalsIgnoreCase(vvmFolders.attributes.attribute[i].name)) {
                        vvmQuotaAttributes.OccupiedStorage = Long.valueOf(vvmFolders.attributes.attribute[i].value[0]).longValue();
                    } else if (CloudMessageProviderContract.VVMQuotaColumns.VMMESSAGES_QUOTA.equalsIgnoreCase(vvmFolders.attributes.attribute[i].name)) {
                        vvmQuotaAttributes.VMMessagesQuota = Integer.valueOf(vvmFolders.attributes.attribute[i].value[0]).intValue();
                    } else if (CloudMessageProviderContract.VVMQuotaColumns.VMOCUPPIED_MESSAGES.equalsIgnoreCase(vvmFolders.attributes.attribute[i].name)) {
                        vvmQuotaAttributes.VMOccupiedMessages = Integer.valueOf(vvmFolders.attributes.attribute[i].value[0]).intValue();
                    }
                }
                i++;
            } else {
                Log.i(this.TAG, "Total Storage " + vvmQuotaAttributes.TotalStorage + " OccupiedStorage " + vvmQuotaAttributes.OccupiedStorage + " VMMessagesQuota " + vvmQuotaAttributes.VMMessagesQuota + " VMOccupiedMessages " + vvmQuotaAttributes.VMOccupiedMessages);
                return vvmQuotaAttributes;
            }
        }
    }

    public void handleUpdateVVMResponse(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        String str = this.TAG;
        Log.i(str, "handleUpdateVVMResponse: " + paramOMAresponseforBufDB + ", isSuccess: " + z);
        this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(false);
        if (z) {
            switch (paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex) {
                case 17:
                    onAdhocV2tUpdateSuccess(paramOMAresponseforBufDB);
                    return;
                case 18:
                    onVvmGreetingUpdateSuccess(paramOMAresponseforBufDB);
                    return;
                case 19:
                    onVvmPINUpdateSuccess(paramOMAresponseforBufDB);
                    return;
                case 20:
                    onVvmProfileUpdateSuccess(paramOMAresponseforBufDB);
                    return;
                default:
                    return;
            }
        } else {
            switch (paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex) {
                case 17:
                    onAdhocV2tUpdateFailure(paramOMAresponseforBufDB);
                    return;
                case 18:
                    onVvmGreetingUpdateFailure(paramOMAresponseforBufDB);
                    return;
                case 19:
                    onVvmPINUpdateFailure(paramOMAresponseforBufDB);
                    return;
                case 20:
                    onVvmProfileUpdateFailure(paramOMAresponseforBufDB);
                    return;
                default:
                    return;
            }
        }
    }

    private void setVVMImportanceSensitivity(ParamOMAObject paramOMAObject, ContentValues contentValues) {
        VvmMessageImportance vvmMessageImportance = VvmMessageImportance.INVALID;
        VvmMessageSensitivity vvmMessageSensitivity = VvmMessageSensitivity.INVALID;
        String str = paramOMAObject.SENSITIVITY;
        if (str != null) {
            if (str.equalsIgnoreCase(PERSONAL_SENSITIVITY)) {
                vvmMessageSensitivity = VvmMessageSensitivity.PERSONAL;
            } else if (paramOMAObject.SENSITIVITY.equalsIgnoreCase(PRIVATE_SENSITIVITY)) {
                vvmMessageSensitivity = VvmMessageSensitivity.PRIVATE;
            } else if (paramOMAObject.SENSITIVITY.equalsIgnoreCase(CONFIDENTIAL_SENSITIVITY)) {
                vvmMessageSensitivity = VvmMessageSensitivity.CONFIDENTIAL;
            }
        }
        String str2 = paramOMAObject.IMPORTANCE;
        if (str2 != null) {
            if (str2.equalsIgnoreCase("NORMAL")) {
                vvmMessageImportance = VvmMessageImportance.NORMAL;
            } else if (paramOMAObject.IMPORTANCE.equalsIgnoreCase(HIGH_IMPORTANCE)) {
                vvmMessageImportance = VvmMessageImportance.HIGH;
            }
        }
        contentValues.put("importance", vvmMessageImportance.toString());
        contentValues.put(CloudMessageProviderContract.VVMMessageColumns.SENSITIVITY, vvmMessageSensitivity.toString());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00fb, code lost:
        if (r14.equals(com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud) != false) goto L_0x0103;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ff, code lost:
        if (r10 >= r0) goto L_0x0102;
     */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0109 A[SYNTHETIC, Splitter:B:41:0x0109] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0121  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0166 A[Catch:{ all -> 0x01a8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x019a A[Catch:{ all -> 0x01a4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01d9 A[SYNTHETIC, Splitter:B:72:0x01d9] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectVvmMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r23, boolean r24) {
        /*
            r22 = this;
            r11 = r22
            r0 = r23
            java.lang.String r1 = "flagRead"
            java.lang.String r2 = "syncdirection"
            java.lang.String r3 = "syncaction"
            java.lang.String r4 = r11.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "handleObjectVvmMessageCloudSearch: "
            r5.append(r6)
            r5.append(r0)
            java.lang.String r6 = ", mIsGoforwardSync: "
            r5.append(r6)
            r7 = r24
            r5.append(r7)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            r4 = -1
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r6 = r11.mBufferDbQuery     // Catch:{ NullPointerException -> 0x01e3 }
            java.net.URL r8 = r0.resourceURL     // Catch:{ NullPointerException -> 0x01e3 }
            java.lang.String r8 = r8.toString()     // Catch:{ NullPointerException -> 0x01e3 }
            r9 = 17
            android.database.Cursor r12 = r6.queryBufferDBWithResUrl(r9, r8)     // Catch:{ NullPointerException -> 0x01e3 }
            java.net.URL r6 = r0.resourceURL     // Catch:{ all -> 0x01d3 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x01d3 }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r6)     // Catch:{ all -> 0x01d3 }
            r6 = 1
            if (r12 == 0) goto L_0x01af
            boolean r10 = r12.moveToFirst()     // Catch:{ all -> 0x01d3 }
            if (r10 == 0) goto L_0x01af
            java.lang.String r10 = "_bufferdbid"
            int r10 = r12.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x01d3 }
            int r4 = r12.getInt(r10)     // Catch:{ all -> 0x01d3 }
            long r4 = (long) r4
            java.lang.String r10 = "_id"
            int r10 = r12.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x01aa }
            int r10 = r12.getInt(r10)     // Catch:{ all -> 0x01aa }
            int r13 = r12.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x01aa }
            int r13 = r12.getInt(r13)     // Catch:{ all -> 0x01aa }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r15 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r13)     // Catch:{ all -> 0x01aa }
            int r13 = r12.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x01aa }
            int r13 = r12.getInt(r13)     // Catch:{ all -> 0x01aa }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r14 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r13)     // Catch:{ all -> 0x01aa }
            java.lang.String r13 = "_bufferdbid=?"
            java.lang.String[] r9 = new java.lang.String[r6]     // Catch:{ all -> 0x01aa }
            java.lang.String r16 = java.lang.String.valueOf(r4)     // Catch:{ all -> 0x01aa }
            r6 = 0
            r9[r6] = r16     // Catch:{ all -> 0x01aa }
            android.content.ContentValues r6 = new android.content.ContentValues     // Catch:{ all -> 0x01aa }
            r6.<init>()     // Catch:{ all -> 0x01aa }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r7 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x01aa }
            r18 = r13
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r13 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x01aa }
            r20 = r8
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x01aa }
            r7.<init>(r13, r8)     // Catch:{ all -> 0x01aa }
            r13 = 0
            r7.mIsChanged = r13     // Catch:{ all -> 0x01aa }
            r11.setVVMImportanceSensitivity(r0, r6)     // Catch:{ all -> 0x01aa }
            int r13 = r12.getColumnIndex(r1)     // Catch:{ all -> 0x01aa }
            int r13 = r12.getInt(r13)     // Catch:{ all -> 0x01aa }
            r16 = r7
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x01aa }
            r21 = r12
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r12 = r0.mFlag     // Catch:{ all -> 0x01a8 }
            boolean r12 = r7.equals(r12)     // Catch:{ all -> 0x01a8 }
            if (r12 == 0) goto L_0x00b7
        L_0x00b5:
            r1 = r7
            goto L_0x0103
        L_0x00b7:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad     // Catch:{ all -> 0x01a8 }
            boolean r7 = r15.equals(r7)     // Catch:{ all -> 0x01a8 }
            if (r7 != 0) goto L_0x0102
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri     // Catch:{ all -> 0x01d1 }
            boolean r7 = r15.equals(r7)     // Catch:{ all -> 0x01d1 }
            if (r7 != 0) goto L_0x0102
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r7 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x01d1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r12 = r0.mFlag     // Catch:{ all -> 0x01d1 }
            boolean r12 = r7.equals(r12)     // Catch:{ all -> 0x01d1 }
            if (r12 == 0) goto L_0x00e8
            if (r13 != 0) goto L_0x00b5
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud     // Catch:{ all -> 0x01d1 }
            boolean r0 = r14.equals(r0)     // Catch:{ all -> 0x01d1 }
            if (r0 == 0) goto L_0x00df
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x01d1 }
            r1 = r0
            goto L_0x0103
        L_0x00df:
            r0 = 1
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x01d1 }
            r6.put(r1, r0)     // Catch:{ all -> 0x01d1 }
            goto L_0x00b5
        L_0x00e8:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x01d1 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = r0.mFlag     // Catch:{ all -> 0x01d1 }
            boolean r0 = r1.equals(r0)     // Catch:{ all -> 0x01d1 }
            if (r0 == 0) goto L_0x00fe
            r0 = 1
            if (r13 != r0) goto L_0x00ff
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud     // Catch:{ all -> 0x01d1 }
            boolean r0 = r14.equals(r0)     // Catch:{ all -> 0x01d1 }
            if (r0 == 0) goto L_0x00fe
            goto L_0x0103
        L_0x00fe:
            r0 = 1
        L_0x00ff:
            if (r10 >= r0) goto L_0x0102
            goto L_0x0103
        L_0x0102:
            r1 = r8
        L_0x0103:
            boolean r0 = r8.equals(r1)     // Catch:{ all -> 0x01a8 }
            if (r0 != 0) goto L_0x0121
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r0 = r11.mScheduleRule     // Catch:{ all -> 0x01d1 }
            int r7 = r11.mDbTableContractIndex     // Catch:{ all -> 0x01d1 }
            r10 = r13
            r8 = r18
            r13 = r0
            r0 = r14
            r14 = r7
            r7 = r15
            r15 = r4
            r17 = r0
            r18 = r7
            r19 = r1
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r1 = r13.getSetFlagsForCldOperation(r14, r15, r17, r18, r19)     // Catch:{ all -> 0x01d1 }
            r12 = r1
            goto L_0x0128
        L_0x0121:
            r10 = r13
            r0 = r14
            r7 = r15
            r8 = r18
            r12 = r16
        L_0x0128:
            java.lang.String r1 = r11.TAG     // Catch:{ all -> 0x01a8 }
            java.lang.StringBuilder r13 = new java.lang.StringBuilder     // Catch:{ all -> 0x01a8 }
            r13.<init>()     // Catch:{ all -> 0x01a8 }
            java.lang.String r14 = "flagSet.mIsChanged : "
            r13.append(r14)     // Catch:{ all -> 0x01a8 }
            boolean r14 = r12.mIsChanged     // Catch:{ all -> 0x01a8 }
            r13.append(r14)     // Catch:{ all -> 0x01a8 }
            java.lang.String r14 = " flagSet.mAction: "
            r13.append(r14)     // Catch:{ all -> 0x01a8 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r14 = r12.mAction     // Catch:{ all -> 0x01a8 }
            r13.append(r14)     // Catch:{ all -> 0x01a8 }
            java.lang.String r14 = " local db read: "
            r13.append(r14)     // Catch:{ all -> 0x01a8 }
            r13.append(r10)     // Catch:{ all -> 0x01a8 }
            java.lang.String r10 = ", origDir: "
            r13.append(r10)     // Catch:{ all -> 0x01a8 }
            r13.append(r0)     // Catch:{ all -> 0x01a8 }
            java.lang.String r0 = " origAction: "
            r13.append(r0)     // Catch:{ all -> 0x01a8 }
            r13.append(r7)     // Catch:{ all -> 0x01a8 }
            java.lang.String r0 = r13.toString()     // Catch:{ all -> 0x01a8 }
            android.util.Log.i(r1, r0)     // Catch:{ all -> 0x01a8 }
            boolean r0 = r12.mIsChanged     // Catch:{ all -> 0x01a8 }
            if (r0 == 0) goto L_0x019a
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = r12.mAction     // Catch:{ all -> 0x01a8 }
            int r0 = r0.getId()     // Catch:{ all -> 0x01a8 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x01a8 }
            r6.put(r3, r0)     // Catch:{ all -> 0x01a8 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r0 = r12.mDirection     // Catch:{ all -> 0x01a8 }
            int r0 = r0.getId()     // Catch:{ all -> 0x01a8 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x01a8 }
            r6.put(r2, r0)     // Catch:{ all -> 0x01a8 }
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r0 = r11.mBufferDbQuery     // Catch:{ all -> 0x01a8 }
            r1 = 17
            r0.updateTable(r1, r6, r8, r9)     // Catch:{ all -> 0x01a8 }
            r0 = 17
            r6 = 0
            r9 = 0
            r10 = 0
            r1 = r22
            r2 = r12
            r12 = r4
            r3 = r12
            r5 = r0
            r7 = r24
            r8 = r20
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9, r10)     // Catch:{ all -> 0x01a4 }
            goto L_0x01a2
        L_0x019a:
            r12 = r4
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r0 = r11.mBufferDbQuery     // Catch:{ all -> 0x01a4 }
            r1 = 17
            r0.updateTable(r1, r6, r8, r9)     // Catch:{ all -> 0x01a4 }
        L_0x01a2:
            r4 = r12
            goto L_0x01cb
        L_0x01a4:
            r0 = move-exception
            r1 = r0
            r4 = r12
            goto L_0x01d7
        L_0x01a8:
            r0 = move-exception
            goto L_0x01ad
        L_0x01aa:
            r0 = move-exception
            r21 = r12
        L_0x01ad:
            r12 = r4
            goto L_0x01d6
        L_0x01af:
            r20 = r8
            r21 = r12
            java.lang.String r1 = r11.TAG     // Catch:{ all -> 0x01d1 }
            java.lang.String r2 = "handleObjectVvmMessageCloudSearch: vvm not found: "
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x01d1 }
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r11.mSummaryDB     // Catch:{ all -> 0x01d1 }
            r2 = 17
            r1.insertSummaryDbUsingObjectIfNonExist(r0, r2)     // Catch:{ all -> 0x01d1 }
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r1 = r11.mBufferDbQuery     // Catch:{ all -> 0x01d1 }
            r2 = r20
            r3 = 1
            long r0 = r1.insertVvmMessageUsingObject(r0, r2, r3)     // Catch:{ all -> 0x01d1 }
            r4 = r0
        L_0x01cb:
            if (r21 == 0) goto L_0x01ed
            r21.close()     // Catch:{ NullPointerException -> 0x01e3 }
            goto L_0x01ed
        L_0x01d1:
            r0 = move-exception
            goto L_0x01d6
        L_0x01d3:
            r0 = move-exception
            r21 = r12
        L_0x01d6:
            r1 = r0
        L_0x01d7:
            if (r21 == 0) goto L_0x01e2
            r21.close()     // Catch:{ all -> 0x01dd }
            goto L_0x01e2
        L_0x01dd:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x01e3 }
        L_0x01e2:
            throw r1     // Catch:{ NullPointerException -> 0x01e3 }
        L_0x01e3:
            r0 = move-exception
            java.lang.String r1 = r11.TAG
            java.lang.String r0 = r0.toString()
            android.util.Log.e(r1, r0)
        L_0x01ed:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.handleObjectVvmMessageCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject, boolean):long");
    }

    public long handleObjectVvmGreetingCloudSearch(ParamOMAObject paramOMAObject) {
        String str = this.TAG;
        Log.i(str, "handleObjectVvmGreetingCloudSearch: " + paramOMAObject);
        try {
            String lineTelUriFromObjUrl = Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString());
            this.mSummaryDB.insertSummaryDbUsingObjectIfNonExist(paramOMAObject, 18);
            return this.mBufferDbQuery.insertVvmGreetingUsingObject(paramOMAObject, lineTelUriFromObjUrl, true);
        } catch (NullPointerException e) {
            Log.e(this.TAG, e.toString());
            return -1;
        }
    }

    public void handleNormalSyncDownloadedVVMGreeting(ParamOMAObject paramOMAObject) {
        String str = this.TAG;
        Log.i(str, "handleNormalSyncDownloadedVVMGreeting: " + paramOMAObject);
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        Cursor queryBufferDBWithResUrl = this.mBufferDbQuery.queryBufferDBWithResUrl(18, paramOMAObject.resourceURL.toString());
        try {
            String lineTelUriFromObjUrl = Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString());
            if (queryBufferDBWithResUrl == null || !queryBufferDBWithResUrl.moveToFirst()) {
                bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(18, this.mBufferDbQuery.insertVvmGreetingUsingObject(paramOMAObject, lineTelUriFromObjUrl, true), false, lineTelUriFromObjUrl, this.mStoreClient));
            } else {
                long j = queryBufferDBWithResUrl.getLong(queryBufferDBWithResUrl.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                ContentValues contentValues = new ContentValues();
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
                contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
                this.mBufferDbQuery.updateTable(18, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
                this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, j);
            }
            if (queryBufferDBWithResUrl != null) {
                queryBufferDBWithResUrl.close();
            }
            if (bufferDBChangeParamList.mChangelst.size() > 0) {
                this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(bufferDBChangeParamList);
                return;
            }
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public int queryPendingVVMUrlFetch(int i) {
        Cursor queryMessageBySyncAction = this.mBufferDbQuery.queryMessageBySyncAction(i, CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId());
        if (queryMessageBySyncAction != null) {
            try {
                String str = this.TAG;
                Log.i(str, " count : " + queryMessageBySyncAction.getCount());
                int count = queryMessageBySyncAction.getCount();
                queryMessageBySyncAction.close();
                return count;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else if (queryMessageBySyncAction == null) {
            return 0;
        } else {
            queryMessageBySyncAction.close();
            return 0;
        }
        throw th;
    }

    public Cursor queryVVMwithResUrl(String str) {
        return this.mBufferDbQuery.queryBufferDBWithResUrl(17, str);
    }

    public void handleNormalSyncDownloadedVVMMessage(ParamOMAObject paramOMAObject) {
        String str = this.TAG;
        Log.i(str, "handleNormalSyncDownloadedVVMMessage: " + paramOMAObject);
        BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
        Cursor queryVVMwithResUrl = queryVVMwithResUrl(paramOMAObject.resourceURL.toString());
        if (queryVVMwithResUrl != null) {
            try {
                if (queryVVMwithResUrl.moveToFirst()) {
                    int i = queryVVMwithResUrl.getInt(queryVVMwithResUrl.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
                    URL url = paramOMAObject.payloadURL;
                    if (url != null) {
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, url.toString());
                    }
                    String[] strArr = {String.valueOf(i)};
                    String lineTelUriFromObjUrl = Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString());
                    this.mBufferDbQuery.updateTable(17, contentValues, "_bufferdbid=?", strArr);
                    bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(17, (long) i, false, lineTelUriFromObjUrl, this.mStoreClient));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryVVMwithResUrl != null) {
            queryVVMwithResUrl.close();
        }
        if (bufferDBChangeParamList.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(bufferDBChangeParamList);
            return;
        }
        return;
        throw th;
    }

    public void onUpdateFromDeviceFtUriFetch(DeviceMsgAppFetchUriParam deviceMsgAppFetchUriParam) {
        onUpdateFromDeviceFtUriFetch(deviceMsgAppFetchUriParam, this.mBufferDbQuery);
    }

    public void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam deviceMsgAppFetchUpdateParam, boolean z) {
        onUpdateFromDeviceMsgAppFetch(deviceMsgAppFetchUpdateParam, z, this.mBufferDbQuery);
    }

    /* JADX INFO: finally extract failed */
    public void onVvmAllPayloadDownloaded(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        Throwable th;
        if (paramOMAresponseforBufDB != null && paramOMAresponseforBufDB.getAllPayloads() != null && paramOMAresponseforBufDB.getAllPayloads().size() >= 1) {
            try {
                Cursor queryTablewithBufferDbId = this.mBufferDbQuery.queryTablewithBufferDbId(paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
                if (queryTablewithBufferDbId != null) {
                    try {
                        if (queryTablewithBufferDbId.moveToFirst()) {
                            int i = queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                            String string = queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow("linenum"));
                            String[] strArr = {String.valueOf(i)};
                            ContentValues contentValues = new ContentValues();
                            for (int i2 = 0; i2 < paramOMAresponseforBufDB.getAllPayloads().size(); i2++) {
                                String contentType = paramOMAresponseforBufDB.getAllPayloads().get(i2).getContentType();
                                InputStream inputStream = null;
                                try {
                                    if (contentType.contains("text")) {
                                        String[] header = paramOMAresponseforBufDB.getAllPayloads().get(i2).getHeader("X-Transcription-Language");
                                        if (header != null && header.length > 0) {
                                            Log.i(this.TAG, "onVvmAllPayloadDownloaded adhocV2tReceived value: " + header[0]);
                                            contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.V2T_LANGUAGE, header[0]);
                                        }
                                        inputStream = paramOMAresponseforBufDB.getAllPayloads().get(i2).getInputStream();
                                        Log.i(this.TAG, "onVvmAllPayloadDownloaded VM transcription present");
                                        contentValues.put("text", getTextDatafromInputStream(inputStream));
                                    } else if (contentType.contains("audio") && !paramOMAresponseforBufDB.getBufferDBChangeParam().mIsAdhocV2t) {
                                        String string2 = queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.FILE_URI));
                                        if (string2 != null) {
                                            inputStream = paramOMAresponseforBufDB.getAllPayloads().get(i2).getInputStream();
                                            contentValues.put("size", Long.valueOf(Util.saveInputStreamtoAppUri(this.mContext, inputStream, string2)));
                                        }
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } catch (Throwable th2) {
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                    throw th2;
                                }
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            }
                            ParamSyncFlagsSet paramSyncFlagsSet = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice, CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload);
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(paramSyncFlagsSet.mDirection.getId()));
                            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(paramSyncFlagsSet.mAction.getId()));
                            this.mBufferDbQuery.updateTable(17, contentValues, "_bufferdbid= ?", strArr);
                            if (paramOMAresponseforBufDB.getBufferDBChangeParam().mIsAdhocV2t) {
                                this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.ADHOC_V2TLANGUAGE, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
                                this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(false);
                            } else {
                                handleOutPutParamSyncFlagSet(paramSyncFlagsSet, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId, 17, false, z, string, (BufferDBChangeParamList) null, false);
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        queryTablewithBufferDbId.close();
                        throw th;
                    }
                }
                if (queryTablewithBufferDbId != null) {
                    queryTablewithBufferDbId.close();
                }
            } catch (IOException | NullPointerException | MessagingException e2) {
                e2.printStackTrace();
            } catch (Throwable th4) {
                th.addSuppressed(th4);
            }
        }
    }

    private String getTextDatafromInputStream(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (inputStream != null) {
            try {
                byte[] bArr = new byte[256];
                int read = inputStream.read(bArr);
                while (read >= 0) {
                    byteArrayOutputStream.write(bArr, 0, read);
                    read = inputStream.read(bArr);
                }
            } catch (IOException e) {
                String str = this.TAG;
                Log.e(str, "getTextDatafromInputStream error: " + e);
                try {
                    inputStream.close();
                    return null;
                } catch (IOException e2) {
                    String str2 = this.TAG;
                    Log.e(str2, "getTextDatafromInputStream: error:" + e2);
                    return null;
                }
            } catch (Throwable th) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    String str3 = this.TAG;
                    Log.e(str3, "getTextDatafromInputStream: error:" + e3);
                }
                throw th;
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e4) {
                String str4 = this.TAG;
                Log.e(str4, "getTextDatafromInputStream: error:" + e4);
            }
        }
        String str5 = this.TAG;
        Log.i(str5, "getTextDatafromInputStream size: " + byteArrayOutputStream.size() + ", value: " + IMSLog.checker(byteArrayOutputStream.toString()));
        return byteArrayOutputStream.toString();
    }

    public void onGreetingAllPayloadDownloaded(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        Throwable th;
        Throwable th2;
        if (paramOMAresponseforBufDB != null && paramOMAresponseforBufDB.getAllPayloads() != null && paramOMAresponseforBufDB.getAllPayloads().size() >= 1) {
            try {
                Cursor queryTablewithBufferDbId = this.mBufferDbQuery.queryTablewithBufferDbId(paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
                try {
                    InputStream inputStream = paramOMAresponseforBufDB.getAllPayloads().get(0).getInputStream();
                    if (queryTablewithBufferDbId != null) {
                        try {
                            if (queryTablewithBufferDbId.moveToFirst()) {
                                int i = queryTablewithBufferDbId.getInt(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                                String string = queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow("linenum"));
                                String[] strArr = {String.valueOf(i)};
                                ContentValues contentValues = new ContentValues();
                                long j = 0;
                                String string2 = queryTablewithBufferDbId.getString(queryTablewithBufferDbId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.FILE_URI));
                                if (string2 != null) {
                                    j = Util.saveInputStreamtoAppUri(this.mContext, inputStream, string2);
                                }
                                ParamSyncFlagsSet paramSyncFlagsSet = new ParamSyncFlagsSet(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice, CloudMessageBufferDBConstants.ActionStatusFlag.UpdatePayload);
                                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(paramSyncFlagsSet.mDirection.getId()));
                                contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(paramSyncFlagsSet.mAction.getId()));
                                contentValues.put("size", Long.valueOf(j));
                                this.mBufferDbQuery.updateTable(18, contentValues, "_bufferdbid= ?", strArr);
                                handleOutPutParamSyncFlagSet(paramSyncFlagsSet, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId, 18, false, z, string, (BufferDBChangeParamList) null, false);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (Throwable th3) {
                            th2 = th3;
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            throw th2;
                        }
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (queryTablewithBufferDbId != null) {
                        queryTablewithBufferDbId.close();
                    }
                } catch (Throwable th4) {
                    th = th4;
                    if (queryTablewithBufferDbId != null) {
                        queryTablewithBufferDbId.close();
                    }
                    throw th;
                }
            } catch (IOException | NullPointerException | MessagingException e2) {
                e2.printStackTrace();
            } catch (Throwable th5) {
                th.addSuppressed(th5);
            }
        }
    }

    public void handleDownLoadMessageResponse(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        String str = this.TAG;
        Log.d(str, "handleDownLoadMessageResponse: " + paramOMAresponseforBufDB + ", isSuccess: " + z);
        if (!z && ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND.equals(paramOMAresponseforBufDB.getActionType())) {
            this.mBufferDbQuery.setMsgDeleted(paramOMAresponseforBufDB.getBufferDBChangeParam().mDBIndex, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
        }
    }

    public void onAppOperationReceived(ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        String str = this.TAG;
        Log.i(str, "onAppOperationReceived: " + paramAppJsonValue);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[paramAppJsonValue.mOperation.ordinal()];
        if (i == 1) {
            handleUploadVvm(paramAppJsonValue);
        } else if (i == 2) {
            handleReadVvm(paramAppJsonValue, bufferDBChangeParamList);
        } else if (i == 3) {
            handleUnReadVvm(paramAppJsonValue, bufferDBChangeParamList);
        } else if (i == 4) {
            handledeleteVvm(paramAppJsonValue, bufferDBChangeParamList);
        } else if (i == 5) {
            onDownloadRequestFromApp(paramAppJsonValue);
        }
    }

    private void onDownloadRequestFromApp(ParamAppJsonValue paramAppJsonValue) {
        long j = 0;
        if (paramAppJsonValue != null && ParamVvmUpdate.VvmTypeChange.FULLPROFILE.equals(paramAppJsonValue.mVvmUpdate.mVvmChange)) {
            long insertDownloadNewProfileRequest = this.mBufferDbQuery.insertDownloadNewProfileRequest(paramAppJsonValue.mVvmUpdate);
            if (insertDownloadNewProfileRequest > 0) {
                this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(true);
                BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
                bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(20, insertDownloadNewProfileRequest, false, paramAppJsonValue.mLine, this.mStoreClient));
                this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(bufferDBChangeParamList);
                return;
            }
            return;
        } else if (paramAppJsonValue != null && paramAppJsonValue.mDataContractType == 17) {
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("onDownloadRequestFromApp ADHOCV2T present: ");
            ParamVvmUpdate.VvmTypeChange vvmTypeChange = ParamVvmUpdate.VvmTypeChange.ADHOC_V2T;
            sb.append(vvmTypeChange.equals(paramAppJsonValue.mVvmUpdate.mVvmChange));
            Log.i(str, sb.toString());
            Cursor queryVvmMessageBufferDBwithAppId = this.mBufferDbQuery.queryVvmMessageBufferDBwithAppId((long) paramAppJsonValue.mRowId);
            if (queryVvmMessageBufferDBwithAppId != null) {
                try {
                    if (queryVvmMessageBufferDBwithAppId.moveToFirst()) {
                        j = queryVvmMessageBufferDBwithAppId.getLong(queryVvmMessageBufferDBwithAppId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            String[] strArr = {String.valueOf(j)};
            ContentValues contentValues = new ContentValues();
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
            this.mBufferDbQuery.updateTable(17, contentValues, "_bufferdbid=?", strArr);
            if (queryVvmMessageBufferDBwithAppId != null) {
                queryVvmMessageBufferDBwithAppId.close();
            }
            this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(true);
            BufferDBChangeParamList bufferDBChangeParamList2 = new BufferDBChangeParamList();
            bufferDBChangeParamList2.mChangelst.add(new BufferDBChangeParam(paramAppJsonValue.mDataContractType, j, false, paramAppJsonValue.mLine, this.mStoreClient, vvmTypeChange.equals(paramAppJsonValue.mVvmUpdate.mVvmChange)));
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(bufferDBChangeParamList2);
            return;
        } else {
            return;
        }
        throw th;
    }

    public void handleSyncSummaryComplete(String str) {
        long validVVMQuotaRowID = this.mBufferDbQuery.getValidVVMQuotaRowID();
        if (validVVMQuotaRowID > 0) {
            BufferDBChangeParamList bufferDBChangeParamList = new BufferDBChangeParamList();
            bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(36, validVVMQuotaRowID, false, str, this.mStoreClient));
            this.mDeviceDataChangeListener.sendGetVVMQuotaInfo(bufferDBChangeParamList);
        }
    }

    private void handleReadVvm(ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        Cursor queryVvmMessageBufferDBwithAppId = this.mBufferDbQuery.queryVvmMessageBufferDBwithAppId((long) paramAppJsonValue.mRowId);
        if (queryVvmMessageBufferDBwithAppId != null) {
            try {
                if (queryVvmMessageBufferDBwithAppId.moveToFirst()) {
                    this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(true);
                    ContentValues contentValues = new ContentValues();
                    String string = queryVvmMessageBufferDBwithAppId.getString(queryVvmMessageBufferDBwithAppId.getColumnIndexOrThrow("linenum"));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                    contentValues.put(CloudMessageProviderContract.VVMMessageColumns.FLAG_READ, 1);
                    long j = queryVvmMessageBufferDBwithAppId.getLong(queryVvmMessageBufferDBwithAppId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    this.mBufferDbQuery.updateTable(paramAppJsonValue.mDataContractType, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
                    bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(paramAppJsonValue.mDataContractType, j, false, string, this.mStoreClient));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryVvmMessageBufferDBwithAppId != null) {
            queryVvmMessageBufferDBwithAppId.close();
            return;
        }
        return;
        throw th;
    }

    private void handleUnReadVvm(ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        Cursor queryVvmMessageBufferDBwithAppId = this.mBufferDbQuery.queryVvmMessageBufferDBwithAppId((long) paramAppJsonValue.mRowId);
        if (queryVvmMessageBufferDBwithAppId != null) {
            try {
                if (queryVvmMessageBufferDBwithAppId.moveToFirst()) {
                    this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(true);
                    ContentValues contentValues = new ContentValues();
                    String string = queryVvmMessageBufferDBwithAppId.getString(queryVvmMessageBufferDBwithAppId.getColumnIndexOrThrow("linenum"));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                    contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                    contentValues.put(CloudMessageProviderContract.VVMMessageColumns.FLAG_READ, 0);
                    long j = queryVvmMessageBufferDBwithAppId.getLong(queryVvmMessageBufferDBwithAppId.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                    this.mBufferDbQuery.updateTable(paramAppJsonValue.mDataContractType, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
                    bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(paramAppJsonValue.mDataContractType, j, false, string, this.mStoreClient));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (queryVvmMessageBufferDBwithAppId != null) {
            queryVvmMessageBufferDBwithAppId.close();
            return;
        }
        return;
        throw th;
    }

    private void handledeleteVvm(ParamAppJsonValue paramAppJsonValue, BufferDBChangeParamList bufferDBChangeParamList) {
        Cursor queryVvmGreetingBufferDBwithAppId;
        ParamAppJsonValue paramAppJsonValue2 = paramAppJsonValue;
        Cursor cursor = null;
        try {
            int i = paramAppJsonValue2.mDataContractType;
            if (i == 17) {
                queryVvmGreetingBufferDBwithAppId = this.mBufferDbQuery.queryVvmMessageBufferDBwithAppId((long) paramAppJsonValue2.mRowId);
            } else if (i == 18) {
                queryVvmGreetingBufferDBwithAppId = this.mBufferDbQuery.queryVvmGreetingBufferDBwithAppId((long) paramAppJsonValue2.mRowId);
            } else {
                String str = this.TAG;
                Log.e(str, "handledeleteVvm, unrecognized datatype: " + paramAppJsonValue2.mDataContractType);
                return;
            }
            Cursor cursor2 = queryVvmGreetingBufferDBwithAppId;
            if (cursor2 != null) {
                try {
                    if (cursor2.moveToFirst()) {
                        ContentValues contentValues = new ContentValues();
                        long j = cursor2.getLong(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                        CloudMessageBufferDBConstants.ActionStatusFlag valueOf = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
                        CloudMessageBufferDBConstants.DirectionFlag valueOf2 = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor2.getInt(cursor2.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
                        String string = cursor2.getString(cursor2.getColumnIndexOrThrow("linenum"));
                        ParamSyncFlagsSet setFlagsForMsgOperation = this.mScheduleRule.getSetFlagsForMsgOperation(paramAppJsonValue2.mDataContractType, j, valueOf2, valueOf, CloudMessageBufferDBConstants.MsgOperationFlag.Delete);
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(setFlagsForMsgOperation.mDirection.getId()));
                        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(setFlagsForMsgOperation.mAction.getId()));
                        this.mBufferDbQuery.updateTable(paramAppJsonValue2.mDataContractType, contentValues, "_bufferdbid=?", new String[]{String.valueOf(j)});
                        if (setFlagsForMsgOperation.mIsChanged) {
                            this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(true);
                            handleOutPutParamSyncFlagSet(setFlagsForMsgOperation, j, paramAppJsonValue2.mDataContractType, false, false, string, bufferDBChangeParamList, false);
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    cursor = cursor2;
                    cursor.close();
                    throw th;
                }
            }
            if (cursor2 != null && !cursor2.isClosed()) {
                cursor2.close();
            }
        } catch (Throwable th2) {
            th = th2;
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            throw th;
        }
    }

    private void onVvmPINUpdateSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        this.mBufferDbQuery.updateTable(19, contentValues, "_bufferdbid=?", new String[]{String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)});
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPIN, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }

    private void onVvmPINUpdateFailure(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("error_message", paramOMAresponseforBufDB.getReasonPhrase());
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        this.mBufferDbQuery.updateTable(19, contentValues, "_bufferdbid=?", new String[]{String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)});
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPIN, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }

    private void onVvmGreetingUpdateSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        String[] strArr = {String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)};
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        if (!(paramOMAresponseforBufDB.getReference() == null || paramOMAresponseforBufDB.getReference().resourceURL == null)) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAresponseforBufDB.getReference().resourceURL.toString()));
        }
        this.mBufferDbQuery.updateTable(18, contentValues, "_bufferdbid=?", strArr);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }

    private void onVvmGreetingUpdateFailure(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        this.mBufferDbQuery.updateTable(18, contentValues, "_bufferdbid=?", new String[]{String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)});
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMGREETING, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }

    private void onVvmProfileUpdateSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        this.mBufferDbQuery.updateTable(20, contentValues, "_bufferdbid=?", new String[]{String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)});
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPROFILE, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }

    private void onVvmProfileUpdateFailure(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        this.mBufferDbQuery.updateTable(20, contentValues, "_bufferdbid=?", new String[]{String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)});
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.VVMPROFILE, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }

    private void onAdhocV2tUpdateSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        this.mBufferDbQuery.updateTable(17, contentValues, "_bufferdbid=?", new String[]{String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)});
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.ADHOC_V2TLANGUAGE, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }

    private void onAdhocV2tUpdateFailure(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        this.mBufferDbQuery.updateTable(17, contentValues, "_bufferdbid=?", new String[]{String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)});
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.ADHOC_V2TLANGUAGE, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x007d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleUploadVvm(com.sec.internal.ims.cmstore.params.ParamAppJsonValue r15) {
        /*
            r14 = this;
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r0 = r15.mVvmUpdate
            if (r0 == 0) goto L_0x010d
            java.lang.String r0 = r0.mLine
            if (r0 != 0) goto L_0x000a
            goto L_0x010d
        L_0x000a:
            com.sec.internal.ims.cmstore.MessageStoreClient r0 = r14.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r0 = r0.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = r0.getStrategy()
            r1 = 1
            r0.setVVMPendingRequestCounts(r1)
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList
            r0.<init>()
            int[] r2 = com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.AnonymousClass1.$SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r3 = r15.mVvmUpdate
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r3 = r3.mVvmChange
            int r3 = r3.ordinal()
            r2 = r2[r3]
            r3 = 0
            switch(r2) {
                case 1: goto L_0x00ec;
                case 2: goto L_0x00ca;
                case 3: goto L_0x00a8;
                case 4: goto L_0x00a8;
                case 5: goto L_0x00a8;
                case 6: goto L_0x00a8;
                case 7: goto L_0x00a8;
                case 8: goto L_0x00a8;
                case 9: goto L_0x00a8;
                case 10: goto L_0x00a8;
                case 11: goto L_0x003c;
                default: goto L_0x002d;
            }
        L_0x002d:
            com.sec.internal.ims.cmstore.MessageStoreClient r14 = r14.mStoreClient
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r14 = r14.getCloudMessageStrategyManager()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r14 = r14.getStrategy()
            r14.setVVMPendingRequestCounts(r3)
            goto L_0x010d
        L_0x003c:
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r2 = r14.mBufferDbQuery
            int r4 = r15.mRowId
            long r4 = (long) r4
            android.database.Cursor r2 = r2.queryVvmMessageBufferDBwithAppId(r4)
            if (r2 == 0) goto L_0x0058
            boolean r4 = r2.moveToFirst()     // Catch:{ all -> 0x009c }
            if (r4 == 0) goto L_0x0058
            java.lang.String r4 = "_bufferdbid"
            int r4 = r2.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x009c }
            long r4 = r2.getLong(r4)     // Catch:{ all -> 0x009c }
            goto L_0x005a
        L_0x0058:
            r4 = 0
        L_0x005a:
            r8 = r4
            java.lang.String r4 = "_bufferdbid=?"
            java.lang.String[] r1 = new java.lang.String[r1]     // Catch:{ all -> 0x009c }
            java.lang.String r5 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x009c }
            r1[r3] = r5     // Catch:{ all -> 0x009c }
            android.content.ContentValues r3 = new android.content.ContentValues     // Catch:{ all -> 0x009c }
            r3.<init>()     // Catch:{ all -> 0x009c }
            java.lang.String r5 = "v2t_language"
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r6 = r15.mVvmUpdate     // Catch:{ all -> 0x009c }
            java.lang.String r6 = r6.mV2tLang     // Catch:{ all -> 0x009c }
            r3.put(r5, r6)     // Catch:{ all -> 0x009c }
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r5 = r14.mBufferDbQuery     // Catch:{ all -> 0x009c }
            r6 = 17
            r5.updateTable(r6, r3, r4, r1)     // Catch:{ all -> 0x009c }
            if (r2 == 0) goto L_0x0080
            r2.close()
        L_0x0080:
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r1 = r0.mChangelst
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam
            r7 = 17
            r10 = 0
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r15 = r15.mVvmUpdate
            java.lang.String r11 = r15.mLine
            com.sec.internal.ims.cmstore.MessageStoreClient r12 = r14.mStoreClient
            r13 = 1
            r6 = r2
            r6.<init>((int) r7, (long) r8, (boolean) r10, (java.lang.String) r11, (com.sec.internal.ims.cmstore.MessageStoreClient) r12, (boolean) r13)
            r1.add(r2)
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r14 = r14.mDeviceDataChangeListener
            r14.sendDeviceUpdate(r0)
            goto L_0x010d
        L_0x009c:
            r14 = move-exception
            if (r2 == 0) goto L_0x00a7
            r2.close()     // Catch:{ all -> 0x00a3 }
            goto L_0x00a7
        L_0x00a3:
            r15 = move-exception
            r14.addSuppressed(r15)
        L_0x00a7:
            throw r14
        L_0x00a8:
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r1 = r14.mBufferDbQuery
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r2 = r15.mVvmUpdate
            long r5 = r1.insertVvmNewProfileDeviceUpdate(r2)
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r1 = r0.mChangelst
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam
            r4 = 20
            r7 = 0
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r15 = r15.mVvmUpdate
            java.lang.String r8 = r15.mLine
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r14.mStoreClient
            r3 = r2
            r3.<init>(r4, r5, r7, r8, r9)
            r1.add(r2)
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r14 = r14.mDeviceDataChangeListener
            r14.sendDeviceUpdate(r0)
            goto L_0x010d
        L_0x00ca:
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r1 = r14.mBufferDbQuery
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r2 = r15.mVvmUpdate
            long r5 = r1.insertVvmNewPinDeviceUpdate(r2)
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r1 = r0.mChangelst
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam
            r4 = 19
            r7 = 0
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r15 = r15.mVvmUpdate
            java.lang.String r8 = r15.mLine
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r14.mStoreClient
            r3 = r2
            r3.<init>(r4, r5, r7, r8, r9)
            r1.add(r2)
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r14 = r14.mDeviceDataChangeListener
            r14.sendDeviceUpdate(r0)
            goto L_0x010d
        L_0x00ec:
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r1 = r14.mBufferDbQuery
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r2 = r15.mVvmUpdate
            long r5 = r1.insertVvmNewGreetingDeviceUpdate(r2)
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r1 = r0.mChangelst
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r2 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam
            r4 = 18
            r7 = 0
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r15 = r15.mVvmUpdate
            java.lang.String r8 = r15.mLine
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r14.mStoreClient
            r3 = r2
            r3.<init>(r4, r5, r7, r8, r9)
            r1.add(r2)
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r14 = r14.mDeviceDataChangeListener
            r14.sendDeviceUpdate(r0)
        L_0x010d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.handleUploadVvm(com.sec.internal.ims.cmstore.params.ParamAppJsonValue):void");
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageImportance;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageSensitivity;

        /* JADX WARNING: Can't wrap try/catch for region: R(42:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|(2:29|30)|31|33|34|35|36|37|38|39|40|(2:41|42)|43|45|46|(2:47|48)|49|51|52|53|54|(3:55|56|58)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(43:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|(2:29|30)|31|33|34|35|36|37|38|39|40|(2:41|42)|43|45|46|(2:47|48)|49|51|52|53|54|(3:55|56|58)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(44:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|33|34|35|36|37|38|39|40|(2:41|42)|43|45|46|(2:47|48)|49|51|52|53|54|(3:55|56|58)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(49:0|(2:1|2)|3|5|6|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|33|34|35|36|37|38|39|40|41|42|43|45|46|47|48|49|51|52|53|54|55|56|58) */
        /* JADX WARNING: Can't wrap try/catch for region: R(50:0|1|2|3|5|6|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|33|34|35|36|37|38|39|40|41|42|43|45|46|47|48|49|51|52|53|54|55|56|58) */
        /* JADX WARNING: Can't wrap try/catch for region: R(51:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|33|34|35|36|37|38|39|40|41|42|43|45|46|47|48|49|51|52|53|54|55|56|58) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x0095 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x009f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00a9 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00b3 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x00ce */
        /* JADX WARNING: Missing exception handler attribute for start block: B:53:0x00e9 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:55:0x00f3 */
        static {
            /*
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange[] r0 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange = r0
                r1 = 1
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r2 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.GREETING     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r3 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.PIN     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r4 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r5 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.ACTIVATE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r6 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.DEACTIVATE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r5 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r6 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.NUTOFF     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r7 = 6
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r5 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r6 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.NUTON     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r7 = 7
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r5 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r6 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2TLANGUAGE     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r7 = 8
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r5 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r6 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2T_SMS     // Catch:{ NoSuchFieldError -> 0x006c }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r7 = 9
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r5 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r6 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2T_EMAIL     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r7 = 10
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r5 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r6 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.ADHOC_V2T     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r7 = 11
                r5[r6] = r7     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag[] r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.values()
                int r5 = r5.length
                int[] r5 = new int[r5]
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = r5
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Upload     // Catch:{ NoSuchFieldError -> 0x0095 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0095 }
                r5[r6] = r1     // Catch:{ NoSuchFieldError -> 0x0095 }
            L_0x0095:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x009f }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Read     // Catch:{ NoSuchFieldError -> 0x009f }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x009f }
                r5[r6] = r0     // Catch:{ NoSuchFieldError -> 0x009f }
            L_0x009f:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00a9 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.UnRead     // Catch:{ NoSuchFieldError -> 0x00a9 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a9 }
                r5[r6] = r2     // Catch:{ NoSuchFieldError -> 0x00a9 }
            L_0x00a9:
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00b3 }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Delete     // Catch:{ NoSuchFieldError -> 0x00b3 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b3 }
                r5[r6] = r3     // Catch:{ NoSuchFieldError -> 0x00b3 }
            L_0x00b3:
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag     // Catch:{ NoSuchFieldError -> 0x00bd }
                com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r5 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.Download     // Catch:{ NoSuchFieldError -> 0x00bd }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x00bd }
                r3[r5] = r4     // Catch:{ NoSuchFieldError -> 0x00bd }
            L_0x00bd:
                com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$VvmMessageImportance[] r3 = com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.VvmMessageImportance.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                $SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageImportance = r3
                com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$VvmMessageImportance r4 = com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.VvmMessageImportance.NORMAL     // Catch:{ NoSuchFieldError -> 0x00ce }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x00ce }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x00ce }
            L_0x00ce:
                int[] r3 = $SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageImportance     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$VvmMessageImportance r4 = com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.VvmMessageImportance.HIGH     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r3[r4] = r0     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$VvmMessageSensitivity[] r3 = com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.VvmMessageSensitivity.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                $SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageSensitivity = r3
                com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$VvmMessageSensitivity r4 = com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.VvmMessageSensitivity.PERSONAL     // Catch:{ NoSuchFieldError -> 0x00e9 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e9 }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x00e9 }
            L_0x00e9:
                int[] r1 = $SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageSensitivity     // Catch:{ NoSuchFieldError -> 0x00f3 }
                com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$VvmMessageSensitivity r3 = com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.VvmMessageSensitivity.PRIVATE     // Catch:{ NoSuchFieldError -> 0x00f3 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f3 }
                r1[r3] = r0     // Catch:{ NoSuchFieldError -> 0x00f3 }
            L_0x00f3:
                int[] r0 = $SwitchMap$com$sec$internal$ims$cmstore$syncschedulers$VVMScheduler$VvmMessageSensitivity     // Catch:{ NoSuchFieldError -> 0x00fd }
                com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler$VvmMessageSensitivity r1 = com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.VvmMessageSensitivity.CONFIDENTIAL     // Catch:{ NoSuchFieldError -> 0x00fd }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fd }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00fd }
            L_0x00fd:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.AnonymousClass1.<clinit>():void");
        }
    }

    public Cursor queryToDeviceUnDownloadedVvm(String str, int i) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedVvm(str, i);
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0029  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int queryVVMPDUActionStatus(long r2) {
        /*
            r1 = this;
            com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder r1 = r1.mBufferDbQuery
            r0 = 17
            android.database.Cursor r1 = r1.queryTablewithBufferDbId(r0, r2)
            if (r1 == 0) goto L_0x0026
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x001c }
            if (r2 == 0) goto L_0x0026
            java.lang.String r2 = "syncaction"
            int r2 = r1.getColumnIndex(r2)     // Catch:{ all -> 0x001c }
            int r2 = r1.getInt(r2)     // Catch:{ all -> 0x001c }
            goto L_0x0027
        L_0x001c:
            r2 = move-exception
            r1.close()     // Catch:{ all -> 0x0021 }
            goto L_0x0025
        L_0x0021:
            r1 = move-exception
            r2.addSuppressed(r1)
        L_0x0025:
            throw r2
        L_0x0026:
            r2 = -1
        L_0x0027:
            if (r1 == 0) goto L_0x002c
            r1.close()
        L_0x002c:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler.queryVVMPDUActionStatus(long):int");
    }

    public Cursor queryToDeviceUnDownloadedGreeting(String str, int i) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedGreeting(str, i);
    }

    public void notifyMsgAppDeleteFail(int i, long j, String str) {
        String str2 = this.TAG;
        Log.i(str2, "notifyMsgAppDeleteFail, dbIndex: " + i + " bufferDbId: " + j + " line: " + IMSLog.checker(str));
        this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(false);
        if (i == 17) {
            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", String.valueOf(j));
            jsonArray.add(jsonObject);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail("VVMDATA", "VVMDATA", jsonArray.toString());
        }
    }

    public void wipeOutData(int i, String str) {
        wipeOutData(i, str, this.mBufferDbQuery);
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB paramOMAresponseforBufDB, boolean z) {
        this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(false);
        onCloudUpdateFlagSuccess(paramOMAresponseforBufDB, z, this.mBufferDbQuery);
    }

    public void onAdhocV2tPayloadDownloadFailure(ParamOMAresponseforBufDB paramOMAresponseforBufDB) {
        Log.i(this.TAG, "onAdhocV2tPayloadDownloadFailure");
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.FAILURE.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        this.mBufferDbQuery.updateTable(17, contentValues, "_bufferdbid=?", new String[]{String.valueOf(paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId)});
        this.mStoreClient.getCloudMessageStrategyManager().getStrategy().setVVMPendingRequestCounts(false);
        this.mBufferDbQuery.notifyApplication("VVMDATA", CloudMessageProviderContract.DataTypes.ADHOC_V2TLANGUAGE, paramOMAresponseforBufDB.getBufferDBChangeParam().mRowId);
    }
}
