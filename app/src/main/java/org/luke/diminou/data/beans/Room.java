package org.luke.diminou.data.beans;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.app.pages.game.piece.Move;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.game.piece.PlayedPiece;

import java.util.Arrays;

public class Room {
    private String id;
    private String state;
    private String mode;
    private String boza;
    private int host;
    private int turn;
    private PlayedPiece[] table;
    private Piece[][] hands;
    private int winner;
    private Piece[] stock;
    private int[] players;

    public Room(JSONObject data) {
        try {
            id = data.getString("id");
            state = data.getString("state");
            mode = data.getString("mode");
            boza = data.getString("boza");
            turn = data.getInt("turn");
            host = data.getInt("host");
            winner = data.getInt("winner");
            players = new int[4];
            hands = new Piece[4][];
            JSONArray arr = data.getJSONArray("players");
            JSONArray harrs = data.getJSONArray("hands");
            for(int i = 0; i < 4; i++){
                players[i] = arr.getInt(i);
                JSONArray harr = harrs.getJSONArray(i);
                hands[i] = new Piece[harr.length()];
                for(int j = 0; j < harr.length(); j++) {
                    hands[i][j] = Piece.valueOf(harr.getString(j));
                }
            }

            JSONArray tarr = data.getJSONArray("table");
            table = new PlayedPiece[tarr.length()];
            for(int i = 0; i < tarr.length(); i++) {
                table[i] = PlayedPiece.deserialize(tarr.getJSONObject(i).toString());
            }

            JSONArray sarr = data.getJSONArray("stock");
            stock = new Piece[sarr.length()];
            for(int i = 0; i < sarr.length(); i++) {
                stock[i] = Piece.valueOf(sarr.getString(i));
            }
        }catch(Exception x) {
            ErrorHandler.handle(x, "initializing room object");
        }
    }

    public String getBoza() {
        return boza;
    }

    public PlayedPiece[] getTable() {
        return table;
    }

    public int getTurn() {
        return turn;
    }

    public Piece[] gatHandFor(int user) {
        for(int i = 0; i < 4; i++) {
            if(players[i] == user) {
                return hands[i];
            }
        }
        return new Piece[0];
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

    public String getMode() {
        return mode;
    }

    public Piece[] getStock() {
        return stock;
    }

    public String getState() {
        return state;
    }

    public String getId() {
        return id;
    }

    public int getWinner() {
        return winner;
    }

    public int count() {
        int count = 0;
        for(int player : players) {
            count += player == -1 ? 0 : 1;
        }
        return count;
    }

    public int getHost() {
        return host;
    }

    public int[] getPlayers() {
        return players;
    }

    public int indexOf(int player) {
        for(int i = 0; i < players.length; i++) {
            if(players[i] == player) {
                return i;
            }
        }
        return -1;
    }

    public int playerAt(int index) {
        return players[index];
    }

    @NonNull
    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", state='" + state + '\'' +
                ", host=" + host +
                ", players=" + Arrays.toString(players) +
                '}';
    }
}
