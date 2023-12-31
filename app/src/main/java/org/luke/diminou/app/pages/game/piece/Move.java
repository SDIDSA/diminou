package org.luke.diminou.app.pages.game.piece;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.app.pages.game.player.Side;

public record Move(PlayedPiece played, Side side) {
    private static final String PLAYED = "played";
    private static final String SIDE = "side";

    public JSONObject serialize() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(PLAYED, played.serialize());
            obj.put(SIDE, side.name());
            return obj;
        } catch (JSONException x) {
            ErrorHandler.handle(x, "serializing piece");
            return null;
        }
    }

    public static Move deserialize(JSONObject obj) {
        try {
            return new Move(
                    PlayedPiece.deserialize(obj.getString(PLAYED)),
                    Side.valueOf(obj.getString(SIDE)));
        } catch (JSONException x) {
            ErrorHandler.handle(x, "deserializing piece");
            return null;
        }
    }
}
