package org.luke.diminou.app.pages.game.online;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.app.pages.game.offline.OfflineGame;
import org.luke.diminou.app.pages.game.offline.player.OfflinePieceHolder;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayer;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayerType;
import org.luke.diminou.app.pages.game.online.player.PieceHolder;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.settings.FourMode;

import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    private final App owner;
    private final Game game;

    public TurnManager(App owner, Game game) {
        this.owner = owner;
        this.game = game;
    }

    public void turn(int p) {
        //TODO turn
    }

    public void turn(PieceHolder holder) {
        turn(holder.getPlayer());
    }

    public void nextTurn(OfflinePieceHolder holder) {
        //TODO next turn
    }

    public void init() {
        //TODO init
    }
}
