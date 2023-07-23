package org.luke.diminou.app;

import android.os.Bundle;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.API;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ImageProxy;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.app.pages.login.Login;
import org.luke.diminou.data.SessionManager;
import org.luke.diminou.data.beans.User;

import java.net.URISyntaxException;

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
            putOnline(false);
            Page.clearCache();
            ImageProxy.init(this);
            Piece.initAll(this);
            Platform.runAfter(() -> {
                startAmbient();
                initializeSocket(() -> {
                    String token = SessionManager.getSession();
                    if(token == null || token.isBlank()) {
                        loadPage(Login.class);
                    } else {
                        Session.getUser(result -> {
                            if (result.has("user")) {
                                int userId = result.getInt("user");
                                User.getForId(userId, user -> {
                                    putUser(user);
                                    SessionManager.registerSocket(getMainSocket(), token, user.getId());
                                    loadPage(Home.class);
                                });
                            } else {
                                SessionManager.clearSession(this);
                                loadPage(Login.class);
                            }
                        });
                    }
                }, () -> loadPage(Login.class));
            }, 2000);
        }, "post_create_thread").start();
    }
    private void initializeSocket(Runnable onConnect, Runnable onError) {
        try {
            Socket mSocket = IO.socket(API.BASE);
            Runnable off = () -> {
                mSocket.off(Socket.EVENT_CONNECT);
                mSocket.off(Socket.EVENT_CONNECT_ERROR);
            };
            mSocket.on(Socket.EVENT_CONNECT, d -> {
                putMainSocket(mSocket);
                onConnect.run();
                off.run();
            });
            mSocket.on(Socket.EVENT_CONNECT_ERROR, d -> {
                onError.run();
                off.run();
            });
            if(!mSocket.connected()){
                mSocket.connect();
            }
        } catch (URISyntaxException e) {
            ErrorHandler.handle(e, "initializing socket");
        }

    }
}