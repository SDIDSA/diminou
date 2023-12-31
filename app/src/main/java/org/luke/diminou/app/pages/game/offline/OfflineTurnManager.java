package org.luke.diminou.app.pages.game.offline;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.game.offline.player.OfflinePieceHolder;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayer;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayerType;
import org.luke.diminou.app.pages.settings.FourMode;

import java.util.ArrayList;
import java.util.List;

public class OfflineTurnManager {
    private final App owner;
    private final OfflineGame game;

    public OfflineTurnManager(App owner, OfflineGame game) {
        this.owner = owner;
        this.game = game;
    }

    public void turn(OfflinePlayer p) {
        if(game.isEnded() || owner.isPaused()) return;
        game.getHolders().forEach(h -> h.setEnabled(p.equals(h.getPlayer())));
        if(game.isHost()) {
            owner.getSockets().forEach(socket -> socket.emit("turn", p.serialize()));
            OfflinePieceHolder holder = game.getForPlayer(p);
            assert holder != null;
            ArrayList<Piece> toAdd = new ArrayList<>(holder.getPieces());
            boolean lostTurn = false;
            while(game.getTable().getPossiblePlays(toAdd).isEmpty()) {
                if(game.getStock().isEmpty()) {
                    game.pass(holder);
                    lostTurn = true;
                    break;
                }
                toAdd.add(game.getStock().getOne());
            }
            toAdd.removeAll(holder.getPieces());
            if(!toAdd.isEmpty()) {
                JSONArray arr = new JSONArray();

                toAdd.forEach(piece -> arr.put(piece.name()));
                JSONObject obj = new JSONObject();
                try {
                    obj.put("player", p.serialize());
                    obj.put("pieces", arr);
                    Platform.runAfter(() -> {
                        owner.getSockets().forEach(socket -> socket.emit("deal", obj));
                        holder.add(toAdd.toArray(new Piece[0]));
                    }, 300);
                }catch(Exception x) {
                    ErrorHandler.handle(x, "dealing pieces");
                }
            }
            if(p.getType() == OfflinePlayerType.BOT && !lostTurn && game.checkForWinner() == null) {
                holder.playBot();
            }
        }
    }

    public void turn(OfflinePieceHolder holder) {
        turn(holder.getPlayer());
    }

    public void nextTurn(OfflinePieceHolder holder) {
        if(game.isEnded() || owner.isPaused()) return;
        if(game.getHolders().isEmpty()) return;
        OfflinePieceHolder next = game.getHolders().get((game.getHolders().indexOf(holder) + 1)
                % game.getHolders().size());
        if(!game.isHost()) {
            owner.getSocket().emit("turn", next.getPlayer().serialize());
        }
        if(game.isHost()) {
            boolean m9foul = game.getStock().isEmpty();
            if(m9foul) {
                for(OfflinePieceHolder h : game.getHolders()) {
                    if(!game.getTable().getPossiblePlays(h.getPieces()).isEmpty()) {
                        m9foul = false;
                    }
                }
            }
            if(m9foul) {
                ArrayList<OfflinePlayer> winner = new ArrayList<>();
                int min = Integer.MAX_VALUE;

                for(OfflinePieceHolder h : game.getHolders()) {
                    int sum = h.sum();
                    if(sum <= min) {
                        if(sum < min) {
                            winner.clear();
                            min = sum;
                        }
                        winner.add(h.getPlayer());
                    }
                }

                if(winner.size() == 1 ||
                        (owner.getFourMode() == FourMode.TEAM_MODE &&
                                winner.size() == 2 &&
                                game.index(winner.get(0)) % 2 == game.index(winner.get(1)) % 2)) {
                    game.emitWin(winner.get(0));
                } else {
                    game.emitDraw();
                }
            }else {
                turn(next);
            }
        }else {
            turn(next);
        }
    }

    public void init() {
        if (game.isHost()) {
            OfflinePlayer winner = owner.getWinner();
            if (winner != null)
                turn(winner);
            else {
                List<Piece> priority = Piece.priority();
                for (Piece piece : priority) {
                    boolean found = false;
                    for (OfflinePieceHolder holder : game.getHolders()) {
                        if (holder.getPieces().contains(piece)) {
                            turn(holder);
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }
        }
    }
}
