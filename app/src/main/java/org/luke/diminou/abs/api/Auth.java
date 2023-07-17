package org.luke.diminou.abs.api;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.api.json.Param;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.beans.User;


public class Auth {
	public static JSONObject netErr;

	static {
		try {
			netErr = new JSONObject("{\"err\":[{\"key\":\"global\",\"value\":\"net_err\"}]}");
		} catch (JSONException e) {
			ErrorHandler.handle(e, "create netErr");
		}
	}

	private static final String PASSWORD = "password";
	
	private Auth() {
		
	}

	public static void googleLogIn(String email, ObjectConsumer<JSONObject> onResult) {
		API.asyncJsonPost(API.Auth.GOOGLE_LOG_IN, "login with google account", onResult,
				new Param("email", email));
	}

	public static void googleSignUp(String email, String name,
									ObjectConsumer<JSONObject> onResult){
		API.asyncJsonPost(API.Auth.GOOGLE_SIGN_UP, "signing up with google account", onResult,
				new Param("email", email),
				new Param("name", name)
		);
	}

	public static String hashPassword(String password) {
		return DigestUtils.sha256Hex(password);
	}
}
