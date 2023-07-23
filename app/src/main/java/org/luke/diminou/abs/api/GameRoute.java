package org.luke.diminou.abs.api;

import org.json.JSONObject;
import org.luke.diminou.abs.api.json.Param;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;

public class GameRoute extends AuthRoute {
    public static void deal(String roomId, ObjectConsumer<JSONObject> onResult) {
        call(API.Game.DEAL, "request deal from server", onResult,
                new Param("room_id", roomId));
    }
}
