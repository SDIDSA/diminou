package org.luke.diminou.data.beans;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.diminou.abs.utils.ErrorHandler;

import java.util.Arrays;

public class Room {
    private String id;
    private String state;
    private int host;
    private int[] players;

    public Room(JSONObject data) {
        try {
            id = data.getString("id");
            state = data.getString("state");
            host = data.getInt("host");
            players = new int[4];
            JSONArray arr = data.getJSONArray("players");
            for(int i = 0; i < 4; i++){
                players[i] = arr.getInt(i);
            }
        }catch(Exception x) {
            ErrorHandler.handle(x, "initializing room object");
        }
    }

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public int getHost() {
        return host;
    }

    public int[] getPlayers() {
        return players;
    }

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
