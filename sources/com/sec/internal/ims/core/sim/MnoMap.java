package com.sec.internal.ims.core.sim;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.sim.CscNetParser;
import com.sec.internal.ims.core.sim.MnoMapJsonParser;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MnoMap {
    public static final String LOG_TAG = "MnoMap";
    private final Context mContext;
    private final CscNetParser mCscNetParser;
    protected SimpleEventLog mEventLog;
    private final MnoMapJsonParser mMnoMapJsonParser;
    private final int mPhoneId;
    private final Map<String, List<NetworkIdentifier>> mTable;

    public MnoMap(Context context, int i) {
        ArrayMap arrayMap = new ArrayMap();
        this.mTable = arrayMap;
        this.mContext = context;
        this.mPhoneId = i;
        this.mCscNetParser = new CscNetParser(i);
        this.mMnoMapJsonParser = new MnoMapJsonParser(context, i);
        this.mEventLog = new SimpleEventLog(context, i, LOG_TAG, 200);
        synchronized (arrayMap) {
            createTable();
        }
    }

    public void createTable() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "createTable: init");
        ImsAutoUpdate.getInstance(this.mContext, this.mPhoneId).loadCarrierFeature();
        List<NetworkIdentifier> parse = this.mMnoMapJsonParser.parse();
        applyAutoUpdates(parse);
        createTableFromMnoMap(parse);
        mergeTableFromCustomerXml();
    }

    private void applyAutoUpdates(List<NetworkIdentifier> list) {
        applyAutoUpdate(list, 0);
        applyAutoUpdate(list, 1);
        applyAutoUpdate(list, 4);
    }

    private void applyAutoUpdate(List<NetworkIdentifier> list, int i) {
        ImsAutoUpdate instance = ImsAutoUpdate.getInstance(this.mContext, this.mPhoneId);
        if (instance != null) {
            applyMnoMapRemove(list, i, instance);
            applyMnoMapAdd(list, i, instance);
            applyGcBlockMccList(i, instance);
        }
    }

    private void applyMnoMapAdd(List<NetworkIdentifier> list, int i, ImsAutoUpdate imsAutoUpdate) {
        int i2 = i;
        JsonElement mnomap = imsAutoUpdate.getMnomap(i2, ImsAutoUpdate.TAG_MNOMAP_ADD);
        if (!mnomap.isJsonNull() && mnomap.isJsonArray()) {
            JsonArray asJsonArray = mnomap.getAsJsonArray();
            if (!asJsonArray.isJsonNull() && asJsonArray.size() > 0) {
                Iterator it = asJsonArray.iterator();
                while (it.hasNext()) {
                    JsonObject asJsonObject = ((JsonElement) it.next()).getAsJsonObject();
                    if (!asJsonObject.has(MnoMapJsonParser.Param.MCCMNC) || !asJsonObject.has(MnoMapJsonParser.Param.SUBSET) || !asJsonObject.has(MnoMapJsonParser.Param.GID1) || !asJsonObject.has(MnoMapJsonParser.Param.GID2) || !asJsonObject.has("mnoname")) {
                        List<NetworkIdentifier> list2 = list;
                    } else {
                        String asString = asJsonObject.get(MnoMapJsonParser.Param.MCCMNC).getAsString();
                        String asString2 = asJsonObject.get(MnoMapJsonParser.Param.SUBSET).getAsString();
                        String upperCase = asJsonObject.get(MnoMapJsonParser.Param.GID1).getAsString().toUpperCase();
                        String upperCase2 = asJsonObject.get(MnoMapJsonParser.Param.GID2).getAsString().toUpperCase();
                        String asString3 = asJsonObject.get("mnoname").getAsString();
                        list.add(new NetworkIdentifier(asString, asString2, upperCase, upperCase2, asJsonObject.has(MnoMapJsonParser.Param.SPNAME) ? asJsonObject.get(MnoMapJsonParser.Param.SPNAME).getAsString() : "", asString3));
                        SimpleEventLog simpleEventLog = this.mEventLog;
                        simpleEventLog.logAndAdd("AutoUpdate : add MnoMap : " + asString3 + " by resource : " + i2);
                    }
                }
            }
        }
    }

    private void applyMnoMapRemove(List<NetworkIdentifier> list, int i, ImsAutoUpdate imsAutoUpdate) {
        int i2 = i;
        JsonElement mnomap = imsAutoUpdate.getMnomap(i2, ImsAutoUpdate.TAG_MNOMAP_REMOVE);
        if (!mnomap.isJsonNull() && mnomap.isJsonArray()) {
            JsonArray asJsonArray = mnomap.getAsJsonArray();
            if (!asJsonArray.isJsonNull() && asJsonArray.size() > 0) {
                Iterator it = asJsonArray.iterator();
                while (it.hasNext()) {
                    JsonObject asJsonObject = ((JsonElement) it.next()).getAsJsonObject();
                    if (!asJsonObject.has(MnoMapJsonParser.Param.MCCMNC) || !asJsonObject.has(MnoMapJsonParser.Param.SUBSET) || !asJsonObject.has(MnoMapJsonParser.Param.GID1) || !asJsonObject.has(MnoMapJsonParser.Param.GID2) || !asJsonObject.has("mnoname")) {
                        List<NetworkIdentifier> list2 = list;
                    } else {
                        String asString = asJsonObject.get(MnoMapJsonParser.Param.MCCMNC).getAsString();
                        String asString2 = asJsonObject.get(MnoMapJsonParser.Param.SUBSET).getAsString();
                        String upperCase = asJsonObject.get(MnoMapJsonParser.Param.GID1).getAsString().toUpperCase();
                        String upperCase2 = asJsonObject.get(MnoMapJsonParser.Param.GID2).getAsString().toUpperCase();
                        String asString3 = asJsonObject.has(MnoMapJsonParser.Param.SPNAME) ? asJsonObject.get(MnoMapJsonParser.Param.SPNAME).getAsString() : "";
                        String asString4 = asJsonObject.get("mnoname").getAsString();
                        list.remove(new NetworkIdentifier(asString, asString2, upperCase, upperCase2, asString3, asString4));
                        SimpleEventLog simpleEventLog = this.mEventLog;
                        simpleEventLog.logAndAdd("AutoUpdate : remove MnoMap : " + asString4 + " by resource : " + i2);
                    }
                }
            }
        }
    }

    private void createTableFromMnoMap(List<NetworkIdentifier> list) {
        for (NetworkIdentifier next : list) {
            String mccMnc = next.getMccMnc();
            List list2 = this.mTable.get(mccMnc);
            if (list2 == null) {
                list2 = new ArrayList();
            }
            list2.add(next);
            this.mTable.put(mccMnc, list2);
        }
    }

    private void mergeTableFromCustomerXml() {
        boolean z;
        Iterator<CscNetParser.CscNetwork> it = this.mCscNetParser.getCscNetworkInfo().iterator();
        while (it.hasNext()) {
            CscNetParser.CscNetwork next = it.next();
            Iterator<NetworkIdentifier> it2 = next.getIdentifiers().iterator();
            boolean z2 = false;
            while (it2.hasNext()) {
                NetworkIdentifier next2 = it2.next();
                List list = this.mTable.get(next2.getMccMnc());
                if (list != null) {
                    Iterator it3 = list.iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            break;
                        }
                        NetworkIdentifier networkIdentifier = (NetworkIdentifier) it3.next();
                        if (networkIdentifier.equalsWithoutMnoName(next2)) {
                            next.setMnoName(networkIdentifier.getMnoName());
                            z2 = true;
                            break;
                        }
                    }
                    if (z2) {
                        break;
                    }
                }
            }
            if (z2) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "createTable merge: " + next.getNetworkName());
                Iterator<NetworkIdentifier> it4 = next.getIdentifiers().iterator();
                while (it4.hasNext()) {
                    NetworkIdentifier next3 = it4.next();
                    List list2 = this.mTable.get(next3.getMccMnc());
                    if (list2 != null) {
                        Iterator it5 = list2.iterator();
                        while (true) {
                            if (it5.hasNext()) {
                                if (((NetworkIdentifier) it5.next()).contains(next3)) {
                                    z = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    z = false;
                    if (!z) {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "add new networkd identifier: " + next3.toString());
                        List list3 = this.mTable.get(next3.getMccMnc());
                        if (list3 == null) {
                            list3 = new ArrayList();
                        }
                        list3.add(next3);
                        this.mTable.put(next3.getMccMnc(), list3);
                    }
                }
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "not found Mno for " + next.getNetworkName());
            }
        }
    }

    private void applyGcBlockMccList(int i, ImsAutoUpdate imsAutoUpdate) {
        List<String> gcBlockList = this.mMnoMapJsonParser.getGcBlockList();
        JsonElement mnomap = imsAutoUpdate.getMnomap(i, ImsAutoUpdate.TAG_REPLACE_GC_BLOCK_MCC_LIST);
        if (!mnomap.isJsonNull() && mnomap.isJsonArray()) {
            JsonArray asJsonArray = mnomap.getAsJsonArray();
            if (!asJsonArray.isJsonNull() && asJsonArray.size() > 0) {
                gcBlockList.clear();
                Iterator it = asJsonArray.iterator();
                while (it.hasNext()) {
                    gcBlockList.add(((JsonElement) it.next()).getAsString());
                }
            }
        }
    }

    public boolean isGcBlockListContains(String str) {
        if (DeviceUtil.isTablet() || str == null || str.length() < 3 || this.mMnoMapJsonParser.getGcBlockList().contains("*")) {
            return true;
        }
        return this.mMnoMapJsonParser.getGcBlockList().contains(str.substring(0, 3));
    }

    public String changeMnoNameByIccid(String str, String str2, String str3, String str4) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "changeMnoNameByIccid(), simOp:" + str4);
        if ("CTC".equals(str4)) {
            return Mno.CTC.getName();
        }
        if ("CTCMO".equals(str4)) {
            return Mno.CTCMO.getName();
        }
        if ("CTG".equals(str4)) {
            return Mno.CTG.getName();
        }
        if ("APT".equals(str4)) {
            return Mno.APT.getName();
        }
        if (!TextUtils.equals(str, Mno.CORIOLIS.getName())) {
            return str;
        }
        if (!TextUtils.equals(str2, "20801") || TextUtils.isEmpty(str3) || str3.startsWith("893327")) {
            return Mno.CORIOLIS.getName();
        }
        Log.e(LOG_TAG, "CORIOLIS iccid is not match => Change Mno name to GoogleGC");
        return Mno.GOOGLEGC.getName();
    }

    public String getMnoName(String str, String str2, String str3, String str4, String str5) {
        synchronized (this.mTable) {
            List list = this.mTable.get(str);
            String name = (isGcBlockListContains(str) ? Mno.DEFAULT : Mno.GOOGLEGC).getName();
            if (list == null) {
                return name;
            }
            String substring = str2.substring(str.length());
            Iterator it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NetworkIdentifier networkIdentifier = (NetworkIdentifier) it.next();
                if (!TextUtils.isEmpty(networkIdentifier.getSubset())) {
                    int i = 0;
                    while (true) {
                        try {
                            if (networkIdentifier.getSubset().charAt(i) != 'x') {
                                if (networkIdentifier.getSubset().charAt(i) != 'X') {
                                    break;
                                }
                            }
                            i++;
                        } catch (StringIndexOutOfBoundsException e) {
                            Log.e(LOG_TAG, "invalid subset - mnomap:" + networkIdentifier.getSubset() + ", SIM:" + substring);
                            e.printStackTrace();
                        }
                    }
                    if (substring.startsWith(networkIdentifier.getSubset().substring(i), i)) {
                        name = networkIdentifier.getMnoName();
                        break;
                    }
                }
                if (TextUtils.isEmpty(networkIdentifier.getGid1()) || TextUtils.isEmpty(str3) || !str3.toUpperCase().startsWith(networkIdentifier.getGid1().toUpperCase())) {
                    if (!TextUtils.isEmpty(networkIdentifier.getGid2()) && !TextUtils.isEmpty(str4) && str4.toUpperCase().startsWith(networkIdentifier.getGid2().toUpperCase())) {
                        name = networkIdentifier.getMnoName();
                        break;
                    }
                    if (!TextUtils.isEmpty(networkIdentifier.getSpName()) && !TextUtils.isEmpty(str5)) {
                        networkIdentifier.setSpName(networkIdentifier.getSpName().trim());
                        str5 = str5.trim();
                        if (!TextUtils.isEmpty(networkIdentifier.getSpName()) && !TextUtils.isEmpty(str5) && str5.equalsIgnoreCase(networkIdentifier.getSpName())) {
                            name = networkIdentifier.getMnoName();
                            break;
                        }
                    }
                    if (TextUtils.isEmpty(networkIdentifier.getSubset()) && TextUtils.isEmpty(networkIdentifier.getGid1()) && TextUtils.isEmpty(networkIdentifier.getGid2()) && TextUtils.isEmpty(networkIdentifier.getSpName())) {
                        name = networkIdentifier.getMnoName();
                    }
                } else {
                    name = networkIdentifier.getMnoName();
                    break;
                }
            }
            int i2 = this.mPhoneId;
            IMSLog.i(LOG_TAG, i2, "getMnoName: (" + str + "," + str3 + "," + str4 + "," + str5 + ") => " + name);
            return name;
        }
    }

    public Set<String> getMnoNamesFromNetworkPlmn(String str) {
        Set<String> set;
        synchronized (this.mTable) {
            set = (Set) Optional.ofNullable(this.mTable.get(str)).map(new MnoMap$$ExternalSyntheticLambda4()).orElse(Collections.emptySet());
        }
        return set;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ Set lambda$getMnoNamesFromNetworkPlmn$3(List list) {
        return (Set) list.stream().map(new MnoMap$$ExternalSyntheticLambda0()).filter(new MnoMap$$ExternalSyntheticLambda1()).filter(new MnoMap$$ExternalSyntheticLambda2()).map(new MnoMap$$ExternalSyntheticLambda3()).collect(Collectors.toSet());
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getMnoNamesFromNetworkPlmn$0(String str) {
        return !str.startsWith("DEFAULT");
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getMnoNamesFromNetworkPlmn$1(String str) {
        return !str.startsWith("GoogleGC_ALL");
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, this.mPhoneId, "\nDump of MnoMap");
        this.mEventLog.dump();
    }
}
