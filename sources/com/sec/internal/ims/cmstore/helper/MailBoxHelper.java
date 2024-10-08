package com.sec.internal.ims.cmstore.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MailBoxHelper {
    public static boolean isMailBoxReset(String str) {
        JSONObject jSONObject;
        JSONObject jSONObject2;
        JSONArray jSONArray;
        JSONObject jSONObject3;
        try {
            JSONArray jSONArray2 = new JSONObject(str).getJSONArray("notificationList");
            if (jSONArray2 == null || (jSONObject = (JSONObject) jSONArray2.opt(0)) == null || (jSONObject2 = jSONObject.getJSONObject("nmsEventList")) == null || (jSONArray = jSONObject2.getJSONArray("nmsEvent")) == null || (jSONObject3 = (JSONObject) jSONArray.opt(0)) == null || !jSONObject3.has("resetBox")) {
                return false;
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
