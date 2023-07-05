package org.luke.diminou.app;

import android.os.Bundle;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.app.pages.home.Home;

public class Diminou extends App {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postCreate();
    }

    protected void postCreate() {
        new Thread(() -> {
            Page.clearCache();
            Piece.initAll(this);
            Platform.runAfter(() -> loadPage(Home.class), 2000);
        }, "post_create_thread").start();
    }
}