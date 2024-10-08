package com.sec.internal.ims.aec.persist;

import android.content.Context;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderSettings {
    private static final String MNO_NAME = "mnoname";
    private static final String PROVIDER_SETTINGS = "ProviderSettings";

    private static JsonElement getResource(InputStream inputStream) {
        JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
        JsonElement parse = new JsonParser().parse(jsonReader);
        try {
            jsonReader.close();
            inputStream.close();
            if (parse.getAsJsonObject().has(PROVIDER_SETTINGS)) {
                return parse.getAsJsonObject().get(PROVIDER_SETTINGS);
            }
            return JsonNull.INSTANCE;
        } catch (IOException unused) {
            return JsonNull.INSTANCE;
        }
    }

    private static Map<String, String> getSettingMap(String str, JsonElement jsonElement) {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        if (jsonElement != JsonNull.INSTANCE) {
            Iterator it = jsonElement.getAsJsonArray().iterator();
            while (it.hasNext()) {
                JsonObject asJsonObject = ((JsonElement) it.next()).getAsJsonObject();
                if (str.equalsIgnoreCase(asJsonObject.get("mnoname").getAsString())) {
                    for (Map.Entry entry : asJsonObject.entrySet()) {
                        concurrentHashMap.put((String) entry.getKey(), asJsonObject.get((String) entry.getKey()).getAsString());
                    }
                }
            }
        }
        return concurrentHashMap;
    }

    private static JsonElement getImsUpdate(Context context, int i) {
        return ImsAutoUpdate.getInstance(context, i).getProviderSettings(1, PROVIDER_SETTINGS);
    }

    private static Map<String, String> mergeSettingMap(Map<String, String> map, Map<String, String> map2) {
        if (map2 != null && !map2.isEmpty()) {
            map.putAll(map2);
        }
        return map;
    }

    public static Map<String, String> getSettingMap(Context context, int i, String str) {
        return mergeSettingMap(getSettingMap(str, getResource(context.getResources().openRawResource(R.raw.providersettings))), getSettingMap(str, getImsUpdate(context, i)));
    }
}
