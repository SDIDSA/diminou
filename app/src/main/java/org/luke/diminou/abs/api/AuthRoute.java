package org.luke.diminou.abs.api;

import android.util.Log;

import org.json.JSONObject;
import org.luke.diminou.abs.api.json.Param;
import org.luke.diminou.abs.api.multipart.Part;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.SessionManager;

public class AuthRoute {

    static void call(String path, String action, ObjectConsumer<JSONObject> onResult, Param... params) {
        String session = SessionManager.getSession();
        Log.i("session", session);
        API.asyncJsonPost(path, action, onResult, session, params);
    }

    static void callMulti(String path, String action, ObjectConsumer<JSONObject> onResult, Part... parts) {
        API.asyncMultiPost(path, action, onResult, SessionManager.getSession(), parts);
    }
}
