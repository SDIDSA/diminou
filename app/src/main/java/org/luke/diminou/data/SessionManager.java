package org.luke.diminou.data;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.data.beans.Bean;
import org.luke.diminou.data.beans.User;

import java.net.URISyntaxException;

import io.socket.client.Socket;

public class SessionManager {

    //	private static HashMap<String, String> data = new HashMap<>();
    private SessionManager() {

    }

    public static void registerSocket(Socket socket, String token, String uid) {
        Runnable register = () -> socket.emit("register",
                JsonUtils.make("socket", socket.id(), "token", token, "user_id", uid));

        System.out.println("listening for reconnect...");
        socket.io().off("reconnect");
        socket.io().on("reconnect", data -> new Thread(() -> {
            Platform.waitWhile(() -> socket.id() == null);
            System.out.println("reconnecting");
            register.run();
            Bean.refresh();
        }).start());
        register.run();
    }

    public static void storeSession(String token, App owner, String uid) throws URISyntaxException {
        Store.setAccessToken(token, null);
        Socket socket = owner.getMainSocket();
        registerSocket(socket, token, uid);
    }

    public static String getSession() {
        return Store.getAccessToken();
    }

    public static void clearSession() {
        Store.removeAccessToken(null);
    }
}
