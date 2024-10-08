package com.sec.internal.ims.core.sim;

import android.content.Context;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class MnoMapJsonParser {
    private static final String LOG_TAG = "MnoMapJsonParser";
    private final Context mContext;
    private final ArrayList<String> mGcBlockMccList = new ArrayList<>();
    private final int mPhoneId;

    static class Param {
        public static final String GID1 = "gid1";
        public static final String GID2 = "gid2";
        public static final String MCCMNC = "mccmnc";
        public static final String MNOMAP = "mnomap";
        public static final String MNONAME = "mnoname";
        public static final String SPNAME = "spname";
        public static final String SUBSET = "subset";

        Param() {
        }
    }

    public MnoMapJsonParser(Context context, int i) {
        this.mContext = context;
        this.mPhoneId = i;
    }

    /* access modifiers changed from: protected */
    public List<NetworkIdentifier> parse() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "parseNetworkInfo: getResources from mnomap.json");
        JsonElement parseMnoMapFile = parseMnoMapFile();
        JsonObject asJsonObject = parseMnoMapFile != null ? parseMnoMapFile.getAsJsonObject() : null;
        storeGcBlockMccList(asJsonObject);
        return parseToNetworkIdentifiers(asJsonObject);
    }

    private JsonElement parseMnoMapFile() {
        JsonReader jsonReader;
        JsonElement jsonElement = null;
        try {
            InputStream openRawResource = this.mContext.getResources().openRawResource(R.raw.mnomap);
            try {
                jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(openRawResource, StandardCharsets.UTF_8)));
                jsonElement = new JsonParser().parse(jsonReader);
                jsonReader.close();
                if (openRawResource != null) {
                    openRawResource.close();
                }
            } catch (Throwable th) {
                if (openRawResource != null) {
                    openRawResource.close();
                }
                throw th;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
        return jsonElement;
        throw th;
    }

    private void storeGcBlockMccList(JsonObject jsonObject) {
        JsonArray asJsonArray;
        if (jsonObject != null && (asJsonArray = jsonObject.getAsJsonArray(ImsAutoUpdate.TAG_GC_BLOCK_MCC_LIST)) != null && !asJsonArray.isJsonNull() && asJsonArray.size() > 0) {
            this.mGcBlockMccList.clear();
            Iterator it = asJsonArray.iterator();
            while (it.hasNext()) {
                this.mGcBlockMccList.add(((JsonElement) it.next()).getAsString());
            }
        }
    }

    private List<NetworkIdentifier> parseToNetworkIdentifiers(JsonObject jsonObject) {
        ArrayList arrayList = new ArrayList();
        if (jsonObject != null) {
            JsonArray asJsonArray = jsonObject.getAsJsonArray(Param.MNOMAP);
            if (asJsonArray == null || !asJsonArray.isJsonArray()) {
                Log.e(LOG_TAG, "array is null. Check your mnomap.json.");
            } else {
                Iterator it = asJsonArray.iterator();
                while (it.hasNext()) {
                    JsonObject asJsonObject = ((JsonElement) it.next()).getAsJsonObject();
                    arrayList.add(new NetworkIdentifier(asJsonObject.get(Param.MCCMNC).getAsString(), asJsonObject.get(Param.SUBSET).getAsString(), asJsonObject.get(Param.GID1).getAsString().toUpperCase(), asJsonObject.get(Param.GID2).getAsString().toUpperCase(), asJsonObject.get(Param.SPNAME).getAsString(), asJsonObject.get("mnoname").getAsString()));
                }
            }
        }
        return arrayList;
    }

    public List<String> getGcBlockList() {
        return this.mGcBlockMccList;
    }
}
