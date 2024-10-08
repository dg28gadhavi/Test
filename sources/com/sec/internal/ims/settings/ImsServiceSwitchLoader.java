package com.sec.internal.ims.settings;

import android.content.Context;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ImsServiceSwitchLoader {
    private static final String LOG_TAG = "ImsServiceSwitchLoader";
    protected static final String SP_KEY_MNONAME = "mnoname";

    protected static JsonElement getImsSwitchFromJson(Context context, String str, int i) {
        InputStream openRawResource;
        InputStream inputStream = null;
        try {
            if (DeviceUtil.isTablet()) {
                IMSLog.d(LOG_TAG, i, " getResources : imsswitch_tablet.json");
                openRawResource = context.getResources().openRawResource(R.raw.imsswitch_tablet);
            } else {
                Mno fromName = Mno.fromName(str);
                if (!DeviceUtil.isUSOpenDevice() || (!fromName.isUSA() && !fromName.isCanada())) {
                    IMSLog.d(LOG_TAG, i, " getResources : imsswitch.json");
                    openRawResource = context.getResources().openRawResource(R.raw.imsswitch);
                } else {
                    IMSLog.d(LOG_TAG, i, " getResources : imsswitch_open.json");
                    openRawResource = context.getResources().openRawResource(R.raw.imsswitch_open);
                }
            }
            InputStream inputStream2 = openRawResource;
            JsonParser jsonParser = new JsonParser();
            JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream2)));
            JsonElement parse = jsonParser.parse(jsonReader);
            jsonReader.close();
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return parse;
        } catch (IOException e2) {
            e2.printStackTrace();
            JsonNull jsonNull = JsonNull.INSTANCE;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            return jsonNull;
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        com.sec.internal.log.IMSLog.d(LOG_TAG, r14, "loadImsSwitchFromJson - mvnoname on json:" + r5 + " found");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        com.sec.internal.log.IMSLog.d(LOG_TAG, r14, "loadImsSwitchFromJson - mnoname on json:" + r5 + " found");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.google.gson.JsonElement getMatchedJsonElement(android.content.Context r10, com.google.gson.JsonObject r11, java.lang.String r12, java.lang.String r13, int r14) {
        /*
            com.google.gson.JsonNull r0 = com.google.gson.JsonNull.INSTANCE
            java.lang.String r1 = "imsswitch"
            com.google.gson.JsonArray r11 = r11.getAsJsonArray(r1)
            boolean r1 = com.sec.internal.helper.JsonUtil.isValidJsonElement(r11)
            java.lang.String r2 = "ImsServiceSwitchLoader"
            if (r1 != 0) goto L_0x0016
            java.lang.String r10 = "load: parse failed."
            com.sec.internal.log.IMSLog.e(r2, r14, r10)
            return r0
        L_0x0016:
            boolean r1 = android.text.TextUtils.isEmpty(r13)
            r3 = 1
            if (r1 != 0) goto L_0x0033
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r12)
            char r4 = com.sec.internal.constants.Mno.MVNO_DELIMITER
            r1.append(r4)
            r1.append(r13)
            java.lang.String r13 = r1.toString()
            r1 = r3
            goto L_0x0036
        L_0x0033:
            r13 = 0
            r1 = r13
            r13 = r12
        L_0x0036:
            com.google.gson.JsonNull r4 = com.google.gson.JsonNull.INSTANCE
            java.util.Iterator r11 = r11.iterator()     // Catch:{ Exception -> 0x0103 }
        L_0x003c:
            boolean r5 = r11.hasNext()     // Catch:{ Exception -> 0x0103 }
            java.lang.String r6 = "mnoname"
            java.lang.String r7 = " found"
            if (r5 == 0) goto L_0x00a5
            java.lang.Object r5 = r11.next()     // Catch:{ Exception -> 0x0103 }
            com.google.gson.JsonElement r5 = (com.google.gson.JsonElement) r5     // Catch:{ Exception -> 0x0103 }
            com.google.gson.JsonObject r8 = r5.getAsJsonObject()     // Catch:{ Exception -> 0x0103 }
            com.google.gson.JsonObject r5 = r5.getAsJsonObject()     // Catch:{ Exception -> 0x0103 }
            com.google.gson.JsonElement r5 = r5.get(r6)     // Catch:{ Exception -> 0x0103 }
            java.lang.String r5 = r5.getAsString()     // Catch:{ Exception -> 0x0103 }
            if (r1 == 0) goto L_0x0084
            boolean r9 = r5.equalsIgnoreCase(r13)     // Catch:{ Exception -> 0x0103 }
            if (r9 == 0) goto L_0x007c
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00a3 }
            r11.<init>()     // Catch:{ Exception -> 0x00a3 }
            java.lang.String r0 = "loadImsSwitchFromJson - mvnoname on json:"
            r11.append(r0)     // Catch:{ Exception -> 0x00a3 }
            r11.append(r5)     // Catch:{ Exception -> 0x00a3 }
            r11.append(r7)     // Catch:{ Exception -> 0x00a3 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x00a3 }
            com.sec.internal.log.IMSLog.d(r2, r14, r11)     // Catch:{ Exception -> 0x00a3 }
            goto L_0x00a1
        L_0x007c:
            boolean r5 = r5.equalsIgnoreCase(r12)     // Catch:{ Exception -> 0x0103 }
            if (r5 == 0) goto L_0x003c
            r4 = r8
            goto L_0x003c
        L_0x0084:
            boolean r9 = r5.equalsIgnoreCase(r12)     // Catch:{ Exception -> 0x0103 }
            if (r9 == 0) goto L_0x003c
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00a3 }
            r11.<init>()     // Catch:{ Exception -> 0x00a3 }
            java.lang.String r0 = "loadImsSwitchFromJson - mnoname on json:"
            r11.append(r0)     // Catch:{ Exception -> 0x00a3 }
            r11.append(r5)     // Catch:{ Exception -> 0x00a3 }
            r11.append(r7)     // Catch:{ Exception -> 0x00a3 }
            java.lang.String r11 = r11.toString()     // Catch:{ Exception -> 0x00a3 }
            com.sec.internal.log.IMSLog.d(r2, r14, r11)     // Catch:{ Exception -> 0x00a3 }
        L_0x00a1:
            r0 = r8
            goto L_0x00a5
        L_0x00a3:
            r0 = r8
            goto L_0x0103
        L_0x00a5:
            com.sec.internal.ims.settings.ImsAutoUpdate r10 = com.sec.internal.ims.settings.ImsAutoUpdate.getInstance(r10, r14)
            boolean r11 = r0.isJsonNull()
            if (r11 == 0) goto L_0x00fd
            com.google.gson.JsonObject r11 = new com.google.gson.JsonObject
            r11.<init>()
            r11.addProperty(r6, r13)
            if (r10 == 0) goto L_0x00bd
            com.google.gson.JsonElement r11 = r10.getUpdatedImsSwitch(r11)
        L_0x00bd:
            com.google.gson.JsonObject r13 = r11.getAsJsonObject()
            int r13 = r13.size()
            if (r13 > r3) goto L_0x00fa
            java.lang.String r11 = "loadImsSwitchFromJson - not matched"
            com.sec.internal.log.IMSLog.d(r2, r14, r11)
            if (r1 == 0) goto L_0x00f2
            boolean r11 = r4.isJsonNull()
            if (r11 != 0) goto L_0x00f2
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r13 = "loadImsSwitchFromJson - primary mnoname on json:"
            r11.append(r13)
            r11.append(r12)
            r11.append(r7)
            java.lang.String r11 = r11.toString()
            com.sec.internal.log.IMSLog.d(r2, r14, r11)
            if (r10 == 0) goto L_0x00fb
            com.google.gson.JsonElement r4 = r10.getUpdatedImsSwitch(r4)
            goto L_0x00fb
        L_0x00f2:
            java.lang.String r10 = "loadImsSwitchFromJson - No matched imsswitch"
            com.sec.internal.log.IMSLog.d(r2, r14, r10)
            com.google.gson.JsonNull r4 = com.google.gson.JsonNull.INSTANCE
            goto L_0x00fb
        L_0x00fa:
            r4 = r11
        L_0x00fb:
            r0 = r4
            goto L_0x0103
        L_0x00fd:
            if (r10 == 0) goto L_0x0103
            com.google.gson.JsonElement r0 = r10.getUpdatedImsSwitch(r0)
        L_0x0103:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsServiceSwitchLoader.getMatchedJsonElement(android.content.Context, com.google.gson.JsonObject, java.lang.String, java.lang.String, int):com.google.gson.JsonElement");
    }
}
