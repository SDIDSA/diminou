package org.luke.diminou.abs.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.api.json.Param;
import org.luke.diminou.abs.api.multipart.FilePart;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.data.beans.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Session extends AuthRoute {

	Session() {

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

	public static void getFriends(ObjectConsumer<List<Integer>> onResult) {
		call(API.Session.GET_FRIENDS, "get friends", res -> {
			JSONArray friends = res.getJSONArray("friends");
			List<Integer> ids = IntStream.range(0, friends.length()).boxed().map(index -> {
				try {
					return friends.getInt(index);
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			}).collect(Collectors.toList());

			Platform.runBack(() -> {
				ArrayList<User> friendList = new ArrayList<>();
				ids.forEach(id -> friendList.add(User.getForIdSync(id)));
				friendList.sort((u0, u1) -> {
					int o = -Boolean.compare(u0.isOnline(), u1.isOnline());
					if(o == 0) {
						o = u0.getUsername().compareTo(u1.getUsername());
					}
					return o;
				});
				Platform.runLater(() -> {
					try {
						onResult.accept(friendList.stream().map(User::getId).collect(Collectors.toList()));
					} catch (Exception e) {
						ErrorHandler.handle(e, "handling results of [get friends]");
					}
				});
			});
		});
	}

	public static void getRequests(ObjectConsumer<JSONObject> onResult) {
		call(API.Session.GET_REQUESTS, "get friends", onResult);
	}

	public static void getForUsername(String searchFor, ObjectConsumer<JSONObject> onResult) {
		call(API.Session.GET_FOR_USERNAME, "search by username", onResult,
				new Param("username", searchFor));
	}

	public static void sendRequest(int otherId, ObjectConsumer<JSONObject> onResult) {
		call(API.Session.SEND_REQUEST, "send friend request", onResult,
				new Param("user_id", otherId));
	}

	public static void cancelRequest(int otherId, ObjectConsumer<JSONObject> onResult) {
		call(API.Session.CANCEL_REQUEST, "cancel friend request", onResult,
				new Param("user_id", otherId));
	}

	public static void acceptRequest(int otherId, ObjectConsumer<JSONObject> onResult) {
		call(API.Session.ACCEPT_REQUEST, "accept friend request", onResult,
				new Param("user_id", otherId));
	}

	public static void createGame(ObjectConsumer<JSONObject> onResult) {
		call(API.Session.CREATE_GAME, "create game", onResult);
	}

	public static void endGame(String roomId, ObjectConsumer<JSONObject> onResult) {
		call(API.Session.END_GAME, "create game", onResult,
				new Param("room_id", roomId));
	}

	public static void invite(int who, String roomId,ObjectConsumer<JSONObject> onResult) {
		call(API.Session.INVITE, "invite player", onResult,
				new Param("who", who),
				new Param("room_id", roomId));
	}

	public static void join(String roomId,ObjectConsumer<JSONObject> onResult) {
		call(API.Session.JOIN, "join room", onResult,
				new Param("room_id", roomId));
	}

	public static void leave(int user, String roomId,ObjectConsumer<JSONObject> onResult) {
		call(API.Session.LEAVE, "leave room", onResult,
				new Param("user_id", user),
				new Param("room_id", roomId));
	}

	public static void swap(String roomId, int i1, int i2,ObjectConsumer<JSONObject> onResult) {
		call(API.Session.SWAP, "swap players", onResult,
				new Param("room_id", roomId),
				new Param("i1", i1),
				new Param("i2", i2));
	}

	public static void begin(String roomId, String mode,ObjectConsumer<JSONObject> onResult) {
		call(API.Session.BEGIN, "begin game", onResult,
				new Param("room_id", roomId),
				new Param("mode", mode));
	}
}
