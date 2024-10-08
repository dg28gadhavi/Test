package com.sec.internal.ims.cmstore.omanetapi.nms.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.ims.cmstore.strategy.KorCmStrategy;
import com.sec.internal.omanetapi.nc.data.LongPollingData;
import com.sec.internal.omanetapi.nms.data.Attribute;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class GsonInterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    private final Type mType;

    public GsonInterfaceAdapter(Class<?> cls) {
        this.mType = cls;
    }

    public final JsonElement serialize(T t, Type type, JsonSerializationContext jsonSerializationContext) {
        if (this.mType.equals(LongPollingData.class)) {
            JsonObject asJsonObject = jsonSerializationContext.serialize(t).getAsJsonObject();
            asJsonObject.addProperty("type", t.getClass().getSimpleName());
            return asJsonObject;
        } else if (!this.mType.equals(Attribute.class)) {
            return jsonSerializationContext.serialize(t).getAsJsonObject();
        } else {
            JsonObject jsonObject = new JsonObject();
            Attribute attribute = (Attribute) t;
            jsonObject.addProperty("name", attribute.name);
            if ("TO".equalsIgnoreCase(attribute.name)) {
                JsonArray jsonArray = new JsonArray();
                for (String add : attribute.value) {
                    jsonArray.add(add);
                }
                jsonObject.add("value", jsonArray);
                return jsonObject;
            } else if (AttributeNames.message_body.equalsIgnoreCase(attribute.name) || KorCmStrategy.KorAttributeNames.extended_rcs.equalsIgnoreCase(attribute.name) || AttributeNames.chip_list.equalsIgnoreCase(attribute.name)) {
                jsonObject.add("value", new JsonParser().parse(attribute.value[0]));
                return jsonObject;
            } else {
                jsonObject.addProperty("value", attribute.value[0]);
                return jsonObject;
            }
        }
    }

    public final T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (!this.mType.equals(Attribute.class)) {
            return jsonDeserializationContext.deserialize(jsonElement, this.mType);
        }
        T attribute = new Attribute();
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        attribute.name = asJsonObject.get("name").getAsString();
        if (asJsonObject.get("value").isJsonArray()) {
            JsonArray asJsonArray = asJsonObject.get("value").getAsJsonArray();
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < asJsonArray.size(); i++) {
                arrayList.add(asJsonArray.get(i).getAsString());
            }
            attribute.value = (String[]) arrayList.toArray(new String[arrayList.size()]);
        } else if (!asJsonObject.get("value").isJsonPrimitive() || !asJsonObject.get("value").getAsJsonPrimitive().isString()) {
            attribute.value = new String[]{asJsonObject.get("value").toString()};
        } else {
            attribute.value = new String[]{asJsonObject.get("value").getAsString()};
        }
        return attribute;
    }
}
