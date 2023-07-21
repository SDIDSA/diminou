package org.luke.diminou.data;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;

public class JsonUtils {
    private JsonUtils() {

    }

    public static JSONObject make(Object... strings) {
        if (strings.length % 2 == 1) {
            throw new IllegalArgumentException("must be called with an even number of args");
        }

        JSONObject obj = new JSONObject();

        for (int i = 0; i < strings.length; i += 2) {
            try {
                obj.put((String) strings[i], strings[i + 1]);
            } catch (JSONException e) {
                ErrorHandler.handle(e, "making json");
            }
        }

        return obj;
    }
}