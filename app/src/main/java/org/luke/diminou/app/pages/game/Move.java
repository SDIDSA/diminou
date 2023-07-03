package org.luke.diminou.app.pages.game;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;

public class Move {
    private static final String PLAYED = "played";
    private static final String SIDE = "side";

    private final PlayedPiece played;
    private final Side side;

    public Move(PlayedPiece played, Side side) {
        this.played = played;
        this.side = side;
    }

    public PlayedPiece getPlayed() {
        return played;
    }

    public Side getSide() {
        return side;
    }

    public JSONObject serialize() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(PLAYED, played.serialize());
            obj.put(SIDE, side.name());
            return obj;
        }catch(JSONException x) {
            ErrorHandler.handle(x, "serializing piece");
            return null;
        }
    }

    public static Move deserialize(JSONObject obj) {
        try {
            return new Move(
                    PlayedPiece.deserialize(obj.getString(PLAYED)),
                    Side.valueOf(obj.getString(SIDE)));
        }catch(JSONException x) {
            ErrorHandler.handle(x, "deserializing piece");
            return null;
        }
    }
}
