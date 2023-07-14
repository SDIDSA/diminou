package org.luke.diminou.abs.api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.api.basic.BasicApiGet;
import org.luke.diminou.abs.api.json.JsonApiCall;
import org.luke.diminou.abs.api.json.Param;
import org.luke.diminou.abs.api.multipart.MultiPartApiCall;
import org.luke.diminou.abs.api.multipart.Part;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.abs.utils.functional.StringConsumer;

public class API {
    public static final String VERSION = "1.0.0";
    public static final String DEV_BASE = "http://10.0.2.2:4000/";
    public static final String TEST_BASE = "http://192.168.187.161:4000/";
    public static final String REL_BASE = "https://mesa69.herokuapp.com/";
    public static final String BASE = TEST_BASE;
    public static final String INVITE_BASE = "https://mesa-invite.tk/";
    public static final String INVITE_CODE = "hTKzmak";
    public static JSONObject netErr;

    static {
        try {
            netErr = new JSONObject("{\"err\":[{\"key\":\"global\",\"value\":\"net_err\"}]}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private API() {

    }

    public static void asyncBasicGet(String path, String action, StringConsumer onResult,
                                     Param... params) {
        asyncExec(new BasicApiGet(path, params), action, res -> {
            try {
                onResult.accept(res.getString("body"));
            } catch (JSONException x) {
                x.printStackTrace();
                ErrorHandler.handle(x, action);
            }
        }, null);
    }

    public static void asyncJsonPost(String path, String action, ObjectConsumer<JSONObject> onResult, String session,
                                     Param... params) {
        asyncExec(new JsonApiCall(path, params), action, onResult, session);
    }

    public static void asyncMultiPost(String path, String action, ObjectConsumer<JSONObject> onResult, String session,
                                      Part... parts) {
        asyncExec(new MultiPartApiCall(path, parts), action, onResult, session);
    }

    private static void asyncExec(ApiCall call, String action, ObjectConsumer<JSONObject> onResult, String session) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.i("api call", call.toString());
                    call.execute(result -> {
                        try {
                            Log.i("api response", result.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (onResult != null)
                            onResult.accept(result);
                    }, session);
                } catch (Exception x) {
                    ErrorHandler.handle(x, action);
                    Platform.runLater(() -> {
                        try {
                            onResult.accept(netErr);
                        } catch (Exception e) {
                            x.printStackTrace();
                            ErrorHandler.handle(x, "report exception for [ " + action + " ] action");
                        }
                    });
                }
            }
        }.start();
    }

    public static void asyncJsonPost(String path, String action, ObjectConsumer<JSONObject> onResult, Param... params) {
        asyncJsonPost(path, action, onResult, null, params);
    }

    public static class Auth {
        private static final String PREFIX = BASE + "auth/";

        public static final String GOOGLE_LOG_IN = PREFIX + "googleLogIn";

        public static final String GOOGLE_SIGN_UP = PREFIX + "googleSignUp";

        private Auth() {

        }
    }

    public static class Session {
        private static final String PREFIX = BASE + "session/";

        public static final String LOGOUT = PREFIX + "logout";

        public static final String GET_USER = PREFIX + "getUser";

        public static final String GET_USER_FOR_ID = PREFIX + "getUserForId";

        public static final String CREATE_SERVER = PREFIX + "createServer";

        public static final String GET_SERVERS = PREFIX + "getServers";

        public static final String GET_SERVER = PREFIX + "getServer";

        public static final String CREATE_INVITE = PREFIX + "createInvite";

        public static final String JOIN_WITH_INVITE = PREFIX + "joinWithInvite";

        public static final String SEND_MESSAGE = PREFIX + "sendMessage";

        public static final String GET_MESSAGES = PREFIX + "getMessages";

        public static final String SEEN = PREFIX + "seen";

        public static final String DELETE_CHANNEL = PREFIX + "deleteChannel";

        public static final String CREATE_CHANNEL = PREFIX + "createChannel";

        private Session() {

        }
    }
}
