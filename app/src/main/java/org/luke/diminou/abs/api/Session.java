package org.luke.diminou.abs.api;

import org.json.JSONObject;
import org.luke.diminou.abs.api.json.Param;
import org.luke.diminou.abs.api.multipart.Part;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.SessionManager;

public class Session {

	private Session() {

	}

	private static void call(String path, String action, ObjectConsumer<JSONObject> onResult, Param... params) {
		API.asyncJsonPost(path, action, onResult, SessionManager.getSession(), params);
	}

	private static void callMulti(String path, String action, ObjectConsumer<JSONObject> onResult, Part... parts) {
		API.asyncMultiPost(path, action, onResult, SessionManager.getSession(), parts);
	}

	public static void logout(ObjectConsumer<JSONObject> onResult) {
		call(API.Session.LOGOUT, "logout", onResult);
	}
}
