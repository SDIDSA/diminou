package org.luke.diminou.abs.api;

import org.json.JSONObject;
import org.luke.diminou.abs.api.json.Param;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.app.pages.game.piece.Move;

public class GameRoute extends AuthRoute {
    public static void deal(String roomId, ObjectConsumer<JSONObject> onResult) {
        call(API.Game.DEAL, "request deal from server", onResult,
                new Param("room_id", roomId));
    }

    public static void play(String roomId, Move move, ObjectConsumer<JSONObject> onResult) {
        call(API.Game.PLAY, "play move", onResult,
                new Param("room_id", roomId),
                new Param("move", move.serialize()));
    }
}
