package org.luke.diminou.app.pages.join.offline;

import org.luke.diminou.abs.net.SocketConnection;

import java.util.Objects;

public class Party {
    private final SocketConnection connection;
    private final String username;
    private final String avatar;

    private int players = 0;

    public Party(SocketConnection connection, String username, String avatar) {
        this.connection = connection;
        this.username = username;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public int getPlayers() {
        return players;
    }

    public SocketConnection getConnection() {
        return connection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Party party = (Party) o;

        if (!Objects.equals(username, party.username))
            return false;
        return Objects.equals(avatar, party.avatar);
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        return result;
    }
}
