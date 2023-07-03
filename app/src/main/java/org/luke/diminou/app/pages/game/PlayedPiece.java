package org.luke.diminou.app.pages.game;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;

public class PlayedPiece {
    private static final String PIECE = "piece";
    private static final String ROTATION = "rotation";
    private final Piece piece;
    private final PieceRotation rotation;

    public PlayedPiece(Piece piece, PieceRotation rotation) {
        this.piece = piece;
        this.rotation = rotation;
    }

    public Piece getPiece() {
        return piece;
    }

    public boolean isFlipped() {
        return rotation == PieceRotation.FLIPPED;
    }

    public boolean isMiddle() {
        return rotation == PieceRotation.BOTH;
    }

    public int getEnd() {
        return isFlipped() ? piece.getN1() : piece.getN0();
    }

    public int getOtherEnd() {
        return isFlipped() ? piece.getN0() : piece.getN1();
    }

    public JSONObject serialize() {
        try {
            JSONObject obj = new JSONObject();
            obj.put(PIECE, piece.name());
            obj.put(ROTATION, rotation.name());
            return obj;
        }catch(JSONException x) {
            ErrorHandler.handle(x, "serializing piece");
            return null;
        }
    }

    public static PlayedPiece deserialize(String data) {
        try {
            JSONObject obj = new JSONObject(data);
            return new PlayedPiece(
                    Piece.valueOf(obj.getString(PIECE)),
                    PieceRotation.valueOf(obj.getString(ROTATION)));
        }catch(JSONException x) {
            ErrorHandler.handle(x, "deserializing piece");
            return null;
        }
    }
}
