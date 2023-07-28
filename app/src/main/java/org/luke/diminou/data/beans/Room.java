package org.luke.diminou.data.beans;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.app.pages.game.piece.Piece;

import java.util.Arrays;

public class Room {
    private String id;
    private String state;
    private String mode;
    private int host;
    private int winner;
    private Piece[] stock;
    private int[] players;

    public Room(JSONObject data) {
        try {
            id = data.getString("id");
            state = data.getString("state");
            mode = data.getString("mode");
            host = data.getInt("host");
            winner = data.getInt("winner");
            players = new int[4];
            JSONArray arr = data.getJSONArray("players");
            for(int i = 0; i < 4; i++){
                players[i] = arr.getInt(i);
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
