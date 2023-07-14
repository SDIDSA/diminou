package org.luke.diminou.app;

import android.os.Bundle;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.API;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ImageProxy;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.app.pages.login.Login;

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
            Page.clearCache();
            ImageProxy.init(this);
            Piece.initAll(this);
            Platform.runAfter(() -> {
                try {
                    Socket socket = IO.socket(API.BASE);
                    socket.connect();

                    putMainSocket(socket);
                } catch (URISyntaxException x) {
                    ErrorHandler.handle(x, "init socket");
                }

                String token = Store.getAccessToken();
                if(token == null) {
                    loadPage(Login.class);
                } else {
                    loadPage(Login.class);
                }
            }, 2000);
        }, "post_create_thread").start();
    }
}