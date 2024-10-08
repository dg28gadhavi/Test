package com.sec.internal.ims.settings;

import android.content.Context;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipMsg$$ExternalSyntheticLambda23;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

public class ImsSimMobilityUpdate {
    private static final String ALLOW_LIST = "simmobility_allowlist";
    private static final String BLOCK_LIST = "simmobility_blocklist";
    private static final String MOBILITY_PROFILE_UPDATE = "mobilityprofile_update";
    private static final String RCS_ALLOW_LIST = "simmobility_allowlist_rcs";
    private static final String RCS_BLOCK_LIST = "simmobility_blocklist_rcs";
    private static final String SIMMOBILITY_UPDATE = "simmobility_update";
    private static final String TAG = "ImsSimMobilityUpdate";
    private static ImsSimMobilityUpdate mInstance;
    private Context mContext;
    protected SimpleEventLog mEventLog;

    protected ImsSimMobilityUpdate(Context context) {
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, TAG, 200);
    }

    public static ImsSimMobilityUpdate getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ImsSimMobilityUpdate.class) {
                if (mInstance == null) {
                    mInstance = new ImsSimMobilityUpdate(context);
                }
            }
        }
        return mInstance;
    }

    private ImsProfile makeUpdatedProfile(ImsProfile imsProfile, JsonObject jsonObject) {
        JsonElement jsonElement = JsonNull.INSTANCE;
        try {
            jsonElement = new JsonParser().parse(imsProfile.toJson());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "profile cannot parse result");
            e.printStackTrace();
        }
        JsonElement jsonElement2 = (JsonElement) JsonUtil.deepCopy(jsonElement, JsonElement.class);
        if (jsonObject == null) {
            Log.d(TAG, "object profiles is null.");
            return imsProfile;
        }
        JsonArray asJsonArray = jsonObject.getAsJsonArray("profile");
        if (asJsonArray == null) {
            Log.d(TAG, "updates is null.");
            return imsProfile;
        }
        JsonElement jsonElement3 = JsonNull.INSTANCE;
        if (!jsonElement.isJsonNull() && !asJsonArray.isJsonNull()) {
            Iterator it = asJsonArray.iterator();
            while (it.hasNext()) {
                JsonElement jsonElement4 = (JsonElement) it.next();
                JsonObject asJsonObject = jsonElement4.getAsJsonObject();
                if (asJsonObject.has("name") && asJsonObject.has("mnoname")) {
                    String asString = asJsonObject.get("name").getAsString();
                    String asString2 = asJsonObject.get("mnoname").getAsString();
                    if (jsonElement2 != null && checkProfileWithNames(jsonElement2, asString, asString2)) {
                        String str = TAG;
                        Log.d(str, "sim mobility imsprofile update : " + asString2);
                        jsonElement3 = JsonUtil.merge(jsonElement2, jsonElement4);
                    }
                }
            }
        }
        if (!jsonElement3.isJsonNull()) {
            return new ImsProfile(jsonElement3.toString());
        }
        Log.d(TAG, "updatedProf is null");
        return imsProfile;
    }

    public ImsProfile overrideImsProfileForSimMobilityUpdateOnDemand(ImsProfile imsProfile, int i) {
        boolean z;
        boolean z2;
        String str = TAG;
        IMSLog.i(str, i, "overrideImsProfileForSimMobilityUpdateOnDemand");
        try {
            boolean z3 = false;
            if (SimUtil.isSimMobilityAvailable(this.mContext, i, Mno.fromName(imsProfile.getMnoName()))) {
                if (!ImsProfile.hasVolteService(imsProfile) || imsProfile.hasEmergencySupport()) {
                    z2 = false;
                } else {
                    z2 = isAllowSimMobilityByImsprofile(imsProfile, i);
                    SlotBasedConfig.getInstance(i).activeSimMobility(z2);
                }
                if (ImsProfile.hasRcsService(imsProfile)) {
                    z3 = isAllowSimMobilityByImsprofileForRcs(imsProfile, i);
                    SlotBasedConfig.getInstance(i).activeSimMobilityForRcs(z3);
                }
                boolean z4 = z3;
                z3 = z2;
                z = z4;
            } else {
                z = false;
            }
            imsProfile.setSimMobility(z3);
            imsProfile.setSimMobilityForRcs(z);
            if (!z3) {
                Log.d(str, "Not support SimMobility for " + imsProfile.getName());
                return imsProfile;
            } else if (imsProfile.getSimMobilityUpdate() == null || imsProfile.getSimMobilityUpdate().length() <= 0) {
                return imsProfile;
            } else {
                JsonObject asJsonObject = new JsonParser().parse(imsProfile.getSimMobilityUpdate().toString()).getAsJsonObject();
                this.mEventLog.logAndAdd(i, "update SimMobility ImsProfile: " + asJsonObject.entrySet().stream().map(new SipMsg$$ExternalSyntheticLambda23()).collect(Collectors.toSet()));
                return mergeProfiles(asJsonObject, imsProfile);
            }
        } catch (JsonParseException | IllegalStateException | NullPointerException e) {
            Log.e(TAG, "Failed to overrideImsProfileForSimMobilityUpdateOnDemand : " + e);
            return imsProfile;
        }
    }

    public JsonElement overrideGlobalSettingsForSimMobilityUpdateOnDemand(JsonElement jsonElement, int i) {
        IMSLog.i(TAG, i, "overrideGlobalSettingsForSimMobilityUpdateOnDemand");
        try {
            if (!jsonElement.getAsJsonObject().has(SIMMOBILITY_UPDATE)) {
                return jsonElement;
            }
            JsonObject asJsonObject = jsonElement.getAsJsonObject().getAsJsonObject(SIMMOBILITY_UPDATE);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(i, "update SimMobility GlobalSettings: " + asJsonObject.entrySet().stream().map(new SipMsg$$ExternalSyntheticLambda23()).collect(Collectors.toSet()));
            return JsonUtil.merge(jsonElement, asJsonObject);
        } catch (JsonParseException | IllegalStateException | NullPointerException e) {
            String str = TAG;
            Log.e(str, "Failed to overrideGlobalSettingsForSimMobilityUpdateOnDemand : " + e);
            return jsonElement;
        }
    }

    public static boolean isAllowSimMobilityByImsprofile(ImsProfile imsProfile, int i) {
        String[] split = imsProfile.getAsString(ALLOW_LIST).split(",");
        String[] split2 = imsProfile.getAsString(BLOCK_LIST).split(",");
        String str = TAG;
        IMSLog.i(str, i, "isAllowSimMobilityByImsprofile : Profile Name [" + imsProfile.getName() + "]");
        return checkAllowListForSimMobility(split, split2, i);
    }

    public static boolean isAllowSimMobilityByImsprofileForRcs(ImsProfile imsProfile, int i) {
        String[] split = imsProfile.getAsString(RCS_ALLOW_LIST).split(",");
        String[] split2 = imsProfile.getAsString(RCS_BLOCK_LIST).split(",");
        String str = TAG;
        IMSLog.i(str, i, "isAllowSimMobilityByImsprofileForRcs : Profile Name [" + imsProfile.getName() + "]");
        return checkAllowListForSimMobility(split, split2, i);
    }

    public static boolean checkAllowListForSimMobility(String[] strArr, String[] strArr2, int i) {
        String str = OmcCode.get();
        String str2 = Mno.getRegionOfDevice().toString();
        String str3 = TAG;
        IMSLog.i(str3, i, "checkAllowListForSimMobility : deviceRegion [" + str2 + "], OMC_CODE [" + str + "]");
        if (Arrays.stream(strArr2).anyMatch(new ImsSimMobilityUpdate$$ExternalSyntheticLambda0(str2, str))) {
            IMSLog.i(str3, i, "No mobility condition by blockList");
        } else if (Arrays.stream(strArr).anyMatch(new ImsSimMobilityUpdate$$ExternalSyntheticLambda1(str2, str))) {
            IMSLog.i(str3, i, "SimMobility enabled by allowlist");
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$checkAllowListForSimMobility$0(String str, String str2, String str3) {
        return str3.equals("*") || str3.equals(str) || str3.equals(str2);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$checkAllowListForSimMobility$1(String str, String str2, String str3) {
        return str3.equals("*") || str3.equals(str) || str3.equals(str2);
    }

    public boolean isSimMobilityAllowedByCarrier(ImsProfile imsProfile) {
        String str = OmcCode.get();
        String asString = imsProfile.getAsString(ALLOW_LIST);
        String asString2 = imsProfile.getAsString(BLOCK_LIST);
        if (str.isEmpty()) {
            return true;
        }
        if (SemSystemProperties.getInt(ImsConstants.SystemProperties.SIMMOBILITY_ENABLE, -1) == 1) {
            IMSLog.d(TAG, "SimMobility Feature is Enabled");
            return true;
        }
        if (asString2.contains("*") || asString2.contains(str)) {
            IMSLog.d(TAG, "No mobility condition");
        } else if (asString.contains("*") || asString.contains(str)) {
            IMSLog.d(TAG, "SimMobility enabled by allowlist");
            return true;
        }
        return false;
    }

    private ImsProfile mergeProfiles(JsonObject jsonObject, ImsProfile imsProfile) {
        JsonObject asJsonObject;
        if (!(jsonObject == null || (asJsonObject = jsonObject.getAsJsonObject()) == null)) {
            try {
                return makeUpdatedProfile(imsProfile, asJsonObject);
            } catch (Exception e) {
                String str = TAG;
                Log.e(str, "Updating mobility profile failed.return original profile " + e.toString());
            }
        }
        return imsProfile;
    }

    private static boolean checkProfileWithNames(JsonElement jsonElement, String str, String str2) {
        try {
            if (jsonElement.isJsonNull()) {
                return false;
            }
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            JsonElement jsonElement2 = asJsonObject.get("name");
            JsonElement jsonElement3 = asJsonObject.get("mnoname");
            if (jsonElement2 == null || jsonElement3 == null || asJsonObject.isJsonNull() || !TextUtils.equals(jsonElement2.getAsString(), str) || !TextUtils.equals(jsonElement3.getAsString(), str2)) {
                return false;
            }
            return true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void dump() {
        IMSLog.dump(TAG, "\nDump of ImsSimMobilityUpdate:");
        this.mEventLog.dump();
    }
}
