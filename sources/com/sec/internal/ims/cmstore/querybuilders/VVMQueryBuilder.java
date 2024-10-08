package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import java.net.URL;

public class VVMQueryBuilder extends QueryBuilderBase {
    private String TAG = VVMQueryBuilder.class.getSimpleName();

    public VVMQueryBuilder(MessageStoreClient messageStoreClient, IBufferDBEventListener iBufferDBEventListener) {
        super(messageStoreClient, iBufferDBEventListener);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
    }

    public long insertVvmMessageUsingObject(ParamOMAObject paramOMAObject, String str, boolean z) {
        ContentValues createDataVvm = createDataVvm(paramOMAObject, str, z);
        createDataVvm.put("messageId", paramOMAObject.MESSAGE_ID);
        createDataVvm.put(CloudMessageProviderContract.VVMMessageColumns.FLAG_READ, Integer.valueOf(getIfSeenValueUsingFlag(paramOMAObject.mFlagList)));
        createDataVvm.put(CloudMessageProviderContract.VVMMessageColumns.SENDER, paramOMAObject.FROM);
        createDataVvm.put(CloudMessageProviderContract.VVMMessageColumns.RECIPIENT, paramOMAObject.TO.get(0));
        createDataVvm.put(CloudMessageProviderContract.VVMMessageColumns.TIMESTAMP, Long.valueOf(getDateFromDateString(paramOMAObject.DATE)));
        createDataVvm.put("importance", paramOMAObject.IMPORTANCE);
        createDataVvm.put(CloudMessageProviderContract.VVMMessageColumns.SENSITIVITY, paramOMAObject.SENSITIVITY);
        createDataVvm.put(CloudMessageProviderContract.BufferDBExtensionBase.FILE_URI, "");
        return this.mBufferDB.insertTable(17, createDataVvm);
    }

    public long insertVvmGreetingUsingObject(ParamOMAObject paramOMAObject, String str, boolean z) {
        ContentValues createDataVvm = createDataVvm(paramOMAObject, str, z);
        createDataVvm.put("mimeType", paramOMAObject.CONTENT_TYPE);
        createDataVvm.put(CloudMessageProviderContract.VVMGreetingColumns.DURATION, paramOMAObject.CONTENT_DURATION);
        createDataVvm.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        createDataVvm.put(CloudMessageProviderContract.VVMGreetingColumns.ACCOUNT_NUMBER, str);
        createDataVvm.put("messageId", paramOMAObject.MESSAGE_ID);
        createDataVvm.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.nameOf(paramOMAObject.X_CNS_Greeting_Type)));
        createDataVvm.put("flags", Integer.valueOf(getIfisGreetingOnUsingFlag(paramOMAObject.mFlagList)));
        return this.mBufferDB.insertTable(18, createDataVvm);
    }

    private ContentValues createDataVvm(ParamOMAObject paramOMAObject, String str, boolean z) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, paramOMAObject.correlationId);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
        URL url = paramOMAObject.parentFolder;
        if (url != null) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(url.toString()));
        }
        contentValues.put("path", Util.decodeUrlFromServer(paramOMAObject.path));
        URL url2 = paramOMAObject.payloadURL;
        if (url2 != null) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, url2.toString());
        }
        contentValues.put("linenum", str);
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        if (z) {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.FetchUri.getId()));
        } else {
            contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.FetchIndividualUri.getId()));
        }
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
        return contentValues;
    }

    public Cursor queryBufferDBWithResUrl(int i, String str) {
        return this.mBufferDB.queryTablewithResUrl(i, str);
    }

    public Cursor queryVvmMessageBufferDBwithAppId(long j) {
        String str = this.TAG;
        Log.i(str, "queryVvmMessageBufferDBwithAppId: " + j);
        return this.mBufferDB.queryTable(17, (String[]) null, "_id=?", new String[]{String.valueOf(j)}, (String) null);
    }

    public Cursor queryVvmGreetingBufferDBwithAppId(long j) {
        String str = this.TAG;
        Log.i(str, "queryVvmGreetingBufferDBwithAppId: " + j);
        return this.mBufferDB.queryTable(18, (String[]) null, "_id=?", new String[]{String.valueOf(j)}, (String) null);
    }

    public long insertVvmNewPinDeviceUpdate(ParamVvmUpdate paramVvmUpdate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.VVMPin.OLDPWD, paramVvmUpdate.mOldPwd);
        contentValues.put(CloudMessageProviderContract.VVMPin.NEWPWD, paramVvmUpdate.mNewPwd);
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        contentValues.put("linenum", paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        return this.mBufferDB.insertTable(19, contentValues);
    }

    public long insertVvmNewGreetingDeviceUpdate(ParamVvmUpdate paramVvmUpdate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("filepath", paramVvmUpdate.mGreetingUri);
        contentValues.put(CloudMessageProviderContract.VVMGreetingColumns.ACCOUNT_NUMBER, paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.VVMGreetingColumns.DURATION, Integer.valueOf(paramVvmUpdate.mDuration));
        contentValues.put("mimeType", paramVvmUpdate.mMimeType);
        contentValues.put("fileName", paramVvmUpdate.mfileName);
        contentValues.put("_id", Integer.valueOf(paramVvmUpdate.mId));
        if ("name".equalsIgnoreCase(paramVvmUpdate.mGreetingType)) {
            contentValues.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.Name.getId()));
        } else if ("custom".equalsIgnoreCase(paramVvmUpdate.mGreetingType)) {
            contentValues.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.Custom.getId()));
        } else {
            contentValues.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.Default.getId()));
        }
        contentValues.put("linenum", paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        return this.mBufferDB.insertTable(18, contentValues);
    }

    public long insertDefaultGreetingValues(String str) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE, Integer.valueOf(ParamVvmUpdate.VvmGreetingType.Default.getId()));
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        contentValues.put(CloudMessageProviderContract.VVMGreetingColumns.ACCOUNT_NUMBER, str);
        contentValues.put("linenum", str);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        return this.mBufferDB.insertTable(18, contentValues);
    }

    /* renamed from: com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange;

        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|18) */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange[] r0 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange = r0
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r1 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.ACTIVATE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r1 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.DEACTIVATE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r1 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.NUTOFF     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r1 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.NUTON     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r1 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r1 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2TLANGUAGE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r1 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2T_SMS     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r1 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.V2T_EMAIL     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder.AnonymousClass1.<clinit>():void");
        }
    }

    public long insertVvmNewProfileDeviceUpdate(ParamVvmUpdate paramVvmUpdate) {
        ContentValues contentValues = new ContentValues();
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$cmstore$params$ParamVvmUpdate$VvmTypeChange[paramVvmUpdate.mVvmChange.ordinal()]) {
            case 1:
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.VVMON, Boolean.TRUE.toString());
                break;
            case 2:
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.VVMON, Boolean.FALSE.toString());
                break;
            case 3:
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.NUT, Boolean.FALSE.toString());
                break;
            case 4:
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.NUT, Boolean.TRUE.toString());
                break;
            case 5:
                if (!TextUtils.isEmpty(paramVvmUpdate.mEmail1)) {
                    contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1, paramVvmUpdate.mEmail1);
                }
                if (!TextUtils.isEmpty(paramVvmUpdate.mEmail2)) {
                    contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR2, paramVvmUpdate.mEmail2);
                    break;
                }
                break;
            case 6:
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.V2T_LANGUAGE, paramVvmUpdate.mV2tLang);
                break;
            case 7:
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.V2T_SMS, paramVvmUpdate.mv2t_sms);
                break;
            case 8:
                contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.V2T_EMAIL, paramVvmUpdate.mv2t_email);
                break;
        }
        contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.LINE_NUMBER, paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.PROFILE_CHANGETYPE, Integer.valueOf(paramVvmUpdate.mVvmChange.getId()));
        contentValues.put("linenum", paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        return this.mBufferDB.insertTable(20, contentValues);
    }

    public long insertDownloadNewProfileRequest(ParamVvmUpdate paramVvmUpdate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.PROFILE_CHANGETYPE, Integer.valueOf(ParamVvmUpdate.VvmTypeChange.FULLPROFILE.getId()));
        contentValues.put("linenum", paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        return this.mBufferDB.insertTable(20, contentValues);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0047, code lost:
        if (r9.isClosed() == false) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0049, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005f, code lost:
        if (r9.isClosed() == false) goto L_0x0049;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getValidVVMQuotaRowID() {
        /*
            r10 = this;
            java.lang.String r0 = "_bufferdbid"
            java.lang.String[] r3 = new java.lang.String[]{r0}
            java.lang.String r4 = "_bufferdbid>0"
            r7 = -1
            r9 = 0
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r1 = r10.mBufferDB     // Catch:{ NullPointerException -> 0x004f }
            r2 = 36
            r5 = 0
            r6 = 0
            android.database.Cursor r9 = r1.queryTable((int) r2, (java.lang.String[]) r3, (java.lang.String) r4, (java.lang.String[]) r5, (java.lang.String) r6)     // Catch:{ NullPointerException -> 0x004f }
            if (r9 == 0) goto L_0x003d
            boolean r1 = r9.moveToFirst()     // Catch:{ NullPointerException -> 0x004f }
            if (r1 == 0) goto L_0x003d
            int r0 = r9.getColumnIndex(r0)     // Catch:{ NullPointerException -> 0x004f }
            int r0 = r9.getInt(r0)     // Catch:{ NullPointerException -> 0x004f }
            long r7 = (long) r0     // Catch:{ NullPointerException -> 0x004f }
            java.lang.String r0 = r10.TAG     // Catch:{ NullPointerException -> 0x004f }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x004f }
            r1.<init>()     // Catch:{ NullPointerException -> 0x004f }
            java.lang.String r2 = " Assigning Already present row "
            r1.append(r2)     // Catch:{ NullPointerException -> 0x004f }
            r1.append(r7)     // Catch:{ NullPointerException -> 0x004f }
            java.lang.String r1 = r1.toString()     // Catch:{ NullPointerException -> 0x004f }
            android.util.Log.d(r0, r1)     // Catch:{ NullPointerException -> 0x004f }
            goto L_0x0041
        L_0x003d:
            long r7 = r10.insertVVMQuotaInfo()     // Catch:{ NullPointerException -> 0x004f }
        L_0x0041:
            if (r9 == 0) goto L_0x0062
            boolean r10 = r9.isClosed()
            if (r10 != 0) goto L_0x0062
        L_0x0049:
            r9.close()
            goto L_0x0062
        L_0x004d:
            r10 = move-exception
            goto L_0x0063
        L_0x004f:
            r0 = move-exception
            java.lang.String r10 = r10.TAG     // Catch:{ all -> 0x004d }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x004d }
            android.util.Log.e(r10, r0)     // Catch:{ all -> 0x004d }
            if (r9 == 0) goto L_0x0062
            boolean r10 = r9.isClosed()
            if (r10 != 0) goto L_0x0062
            goto L_0x0049
        L_0x0062:
            return r7
        L_0x0063:
            if (r9 == 0) goto L_0x006e
            boolean r0 = r9.isClosed()
            if (r0 != 0) goto L_0x006e
            r9.close()
        L_0x006e:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.VVMQueryBuilder.getValidVVMQuotaRowID():long");
    }

    public long insertVVMQuotaInfo() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.TOTAL_STORAGE, 0);
        contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.OCCUPIED_STORAGE, 0);
        contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.VMMESSAGES_QUOTA, 0);
        contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.VMOCUPPIED_MESSAGES, 0);
        contentValues.put(CloudMessageProviderContract.VVMQuotaColumns.LAST_UPDATED, 0);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        contentValues.put("linenum", this.mStoreClient.getPrerenceManager().getUserTelCtn());
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        return this.mBufferDB.insertTable(36, contentValues);
    }

    public long insertVvmNewEmailProfileCloudUpdate(ParamVvmUpdate paramVvmUpdate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1, paramVvmUpdate.mEmail1);
        contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.LINE_NUMBER, paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.VVMAccountInfoColumns.PROFILE_CHANGETYPE, Integer.valueOf(ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT.getId()));
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        contentValues.put("linenum", paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        return this.mBufferDB.insertTable(20, contentValues);
    }

    public long insertVvmNewPinCloudUpdate(ParamVvmUpdate paramVvmUpdate) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        contentValues.put(CloudMessageProviderContract.VVMPin.NEWPWD, paramVvmUpdate.mNewPwd);
        contentValues.put("linenum", paramVvmUpdate.mLine);
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        contentValues.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        contentValues.put("sim_imsi", this.mStoreClient.getCurrentIMSI());
        return this.mBufferDB.insertTable(19, contentValues);
    }

    public Cursor queryToDeviceUnDownloadedVvm(String str, int i) {
        return this.mBufferDB.queryTable(17, (String[]) null, "syncaction=? AND linenum=?", new String[]{String.valueOf(i), str}, (String) null);
    }

    public Cursor queryToDeviceUnDownloadedGreeting(String str, int i) {
        return this.mBufferDB.queryTable(18, (String[]) null, "syncaction=? AND linenum=?", new String[]{String.valueOf(i), str}, (String) null);
    }
}
