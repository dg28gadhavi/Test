package com.sec.internal.helper;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.Map;

public class JsonUtil {
    private static final String LOG_TAG = "JsonUtil";

    public static boolean isValidJsonElement(JsonElement jsonElement) {
        return jsonElement != null && !jsonElement.isJsonNull() && !jsonElement.toString().equals("{}") && !jsonElement.toString().equals("[]");
    }

    public static JsonElement merge(JsonElement jsonElement, JsonElement jsonElement2) {
        JsonObject jsonObject = JsonNull.INSTANCE;
        if (jsonElement.isJsonObject() && jsonElement2.isJsonObject()) {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            JsonObject asJsonObject2 = jsonElement2.getAsJsonObject();
            jsonObject = new JsonObject();
            for (Map.Entry entry : asJsonObject.entrySet()) {
                if (asJsonObject2.has((String) entry.getKey())) {
                    JsonNull merge = merge((JsonElement) entry.getValue(), asJsonObject2.get((String) entry.getKey()));
                    if (merge == JsonNull.INSTANCE) {
                        String str = LOG_TAG;
                        Log.e(str, "merge failed. key: " + ((String) entry.getKey()) + " value: " + entry.getValue() + " + " + asJsonObject2.get((String) entry.getKey()));
                    } else {
                        jsonObject.add((String) entry.getKey(), merge);
                    }
                } else {
                    jsonObject.add((String) entry.getKey(), (JsonElement) entry.getValue());
                }
            }
            for (Map.Entry entry2 : asJsonObject2.entrySet()) {
                if (!jsonObject.has((String) entry2.getKey())) {
                    jsonObject.add((String) entry2.getKey(), (JsonElement) entry2.getValue());
                }
            }
        } else if (jsonElement.isJsonNull()) {
            return jsonElement2;
        } else {
            if (jsonElement.isJsonPrimitive() && jsonElement2.isJsonPrimitive()) {
                return jsonElement2;
            }
            if (jsonElement.isJsonArray() && jsonElement2.isJsonArray()) {
                return jsonElement2;
            }
            Log.e(LOG_TAG, "merge: type mismatch.");
        }
        return jsonObject;
    }

    public static <T> T deepCopy(T t, Class<T> cls) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(gson.toJson(t, cls), cls);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
