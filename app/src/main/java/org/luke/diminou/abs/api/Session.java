package org.luke.diminou.abs.api;

import org.json.JSONObject;
import org.luke.diminou.abs.api.json.Param;
import org.luke.diminou.abs.api.multipart.FilePart;
import org.luke.diminou.abs.api.multipart.Part;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.SessionManager;

import java.io.File;

public class Session {

	private Session() {

	}

	private static void call(String path, String action, ObjectConsumer<JSONObject> onResult, Param... params) {
		API.asyncJsonPost(path, action, onResult, SessionManager.getSession(), params);
	}

	private static void callMulti(String path, String action, ObjectConsumer<JSONObject> onResult, Part... parts) {
		API.asyncMultiPost(path, action, onResult, SessionManager.getSession(), parts);
	}

	public static void getForId(String type, int id, ObjectConsumer<JSONObject> onResult) {
		call(API.Session.GET_FOR_ID, "getting [" + type + "] for id=" + id, onResult,
				new Param("type", type),
				new Param("id", id));
	}

	public static void logout(ObjectConsumer<JSONObject> onResult) {
		call(API.Session.LOGOUT, "logout", onResult);
	}

	public static void getUser(ObjectConsumer<JSONObject> onResult) {
		call(API.Session.GET_USER, "get user data", onResult);
	}

	public static void changeAvatar(File avatar,
									 ObjectConsumer<JSONObject> onResult) {
		callMulti(API.Session.CHANGE_AVATAR, "change avatar", onResult,
				new FilePart("avatar", avatar));
	}

	public static void changeUsername(String username,
									  ObjectConsumer<JSONObject> onResult) {
		call(API.Session.CHANGE_USERNAME, "change username", onResult,
				new Param("username", username));
	}

	public static void getFriends(ObjectConsumer<JSONObject> onResult) {
		call(API.Session.GET_FRIENDS, "get friends", onResult);
	}
}
