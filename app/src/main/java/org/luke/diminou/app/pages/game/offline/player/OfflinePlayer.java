package org.luke.diminou.app.pages.game.offline.player;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.net.Local;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.app.pages.game.offline.OfflineGame;

import java.util.Objects;

public class OfflinePlayer {
    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String AVATAR = "avatar";
    private static final String IP = "ip";
    private OfflinePlayerType type;
    private final String name;
    private final String avatar;
    private String ip;

    public OfflinePlayer(OfflinePlayerType type, String name, String avatar, String ip) {
        this.type = type;
        this.name = name;
        this.avatar = avatar;
        this.ip = ip;
    }

    public OfflinePlayerType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isSelf(boolean host) {
        return host ? (ip.isBlank() && type != OfflinePlayerType.BOT) : Local.getMyIp().contains(ip);
    }

    public boolean isWinner(App owner) {
        OfflineGame game = (OfflineGame) Page.getInstance(owner, OfflineGame.class);

        OfflinePlayer winner = owner.getWinner();
        if (equals(winner)) return true;
        assert game != null;
        return game.otherPlayer(this).equals(winner);
    }

    public JSONObject serialize() {
        JSONObject obj = new JSONObject();
        try {
            obj.put(TYPE, type.name());
            obj.put(NAME, name);
            obj.put(AVATAR, avatar);
            obj.put(IP, ip);
        }catch(JSONException x) {
            ErrorHandler.handle(x, "serializing player");
        }
        return obj;
    }

    public static OfflinePlayer deserialize(JSONObject obj) {
        try {
            return new OfflinePlayer(
              OfflinePlayerType.valueOf(obj.getString(TYPE)),
              obj.getString(NAME),
              obj.getString(AVATAR),
              obj.getString(IP)
            );
        } catch (JSONException e) {
            ErrorHandler.handle(e, "deserializing player " + obj);
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OfflinePlayer player = (OfflinePlayer) o;

        if (type != player.type) return false;
        if (!Objects.equals(name, player.name)) return false;
        if (!Objects.equals(avatar, player.avatar)) return false;
        return Objects.equals(ip, player.ip);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        return result;
    }

    public String getIp() {
        return ip;
    }

    public void makeBot() {
        type = OfflinePlayerType.BOT;
        ip = "";
    }
}
