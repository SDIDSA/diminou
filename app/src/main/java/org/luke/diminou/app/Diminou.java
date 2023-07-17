package org.luke.diminou.app;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.API;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ImageProxy;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.app.pages.login.Login;
import org.luke.diminou.data.SessionManager;
import org.luke.diminou.data.beans.Bean;
import org.luke.diminou.data.beans.User;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.socket.client.IO;
import io.socket.client.Socket;

public class Diminou extends App {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postCreate();
    }

    protected void postCreate() {
        new Thread(() -> {
            Page.clearCache();
            ImageProxy.init(this);
            Piece.initAll(this);
            Platform.runAfter(() -> {
                startAmbient();
                initializeSocket();

                String token = Store.getAccessToken();
                if(token.isBlank()) {
                    loadPage(Login.class);
                } else {
                    Session.getUser(result -> {
                        if (result.has("user")) {
                            int userId = result.getInt("user");
                            User.getForId(userId, user -> {
                                putUser(user);
                                SessionManager.registerSocket(getMainSocket(), token, String.valueOf(user.getId()));
                                loadPage(Home.class);
                            });


                        } else {
                            SessionManager.clearSession();
                            loadPage(Login.class);
                        }
                    });
                }
            }, 2000);
        }, "post_create_thread").start();
    }
    private void initializeSocket() {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.reconnectionDelay = 2000;
        options.reconnectionDelayMax = 5000;
        try {
            Socket mSocket = IO.socket(API.BASE);
            mSocket.on(Socket.EVENT_CONNECT, d -> {
                putMainSocket(mSocket);
            });
            mSocket.on(Socket.EVENT_CONNECT_ERROR, d -> {
                for (Object o : d) {
                    if(o instanceof Throwable t) {
                        t.printStackTrace();
                    }else
                        Log.i("error", String.valueOf(o));
                }
            });
            if(!mSocket.connected()){
                mSocket.connect();
            }
        } catch (URISyntaxException e) {
            ErrorHandler.handle(e, "initializing socket");
        }

    }
}