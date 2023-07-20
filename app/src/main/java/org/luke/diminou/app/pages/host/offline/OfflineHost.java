package org.luke.diminou.app.pages.host.offline;

import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.scratches.ColoredSeparator;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.net.SocketConnection;
import org.luke.diminou.abs.net.LocalHost;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.app.avatar.Avatar;
import org.luke.diminou.app.cards.offline.OfflineDisplayCards;
import org.luke.diminou.app.cards.offline.OfflineMirorredCards;
import org.luke.diminou.app.cards.offline.OfflinePlayerCard;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.game.Game;
import org.luke.diminou.app.pages.game.player.Player;
import org.luke.diminou.app.pages.game.player.PlayerType;
import org.luke.diminou.app.pages.home.offline.OfflineHome;
import org.luke.diminou.app.pages.settings.FourMode;

import java.util.ArrayList;
import java.util.Objects;

public class OfflineHost extends Titled {
    private final OfflineDisplayCards cards;
    private final OfflineMirorredCards offlineMirorredCards;

    private final Button start;

    private final Animation showStart, hideStart;

    public OfflineHost(App owner) {
        super(owner, "create_party");

        cards = new OfflineDisplayCards(owner, true);

        cards.forEach(card -> card.setOnClickListener(e -> {
            if(!card.isLoaded()) {
                owner.playMenuSound(R.raw.joined);
                card.loadPlayer(cards.botName(), Avatar.randomBot().name(), OfflinePlayerCard.Type.BOT);
                updateCards();
            }
        }));

        offlineMirorredCards = new OfflineMirorredCards(owner);

        start = new Button(owner, "start_game");
        start.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        start.setFont(new Font(18));

        TeamModeOverlay teamModeOverlay = new TeamModeOverlay(owner);

        Runnable begin = () -> {
            ArrayList<Player> players = new ArrayList<>();

            cards.forEach(pc -> {
                if(pc.isLoaded())
                    players.add(
                            new Player(
                                    pc.getType() == OfflinePlayerCard.Type.BOT ?
                                            PlayerType.BOT : PlayerType.PLAYER,
                                    pc.getUsername(),
                                    pc.getAvatar(),
                                    pc.getConnection() != null ?
                                            pc.getConnection().getIp() : ""));
            });

            owner.putData("host", true);
            owner.putData("sockets" , cards.broadcast());
            owner.putData("players", players);

            owner.putData("timer", Store.getTimer());

            cards.broadcast().forEach(socket -> {
                JSONArray arr = new JSONArray();
                players.forEach(p -> arr.put(p.serialize()));

                try {
                    JSONObject game = new JSONObject();
                    game.put("players", arr);
                    game.put("mode", owner.getFourMode().getText());
                    game.put("timer", owner.getTimer().getText());
                    socket.emit("begin", game);
                } catch (Exception x) {
                    ErrorHandler.handle(x, "starting game");
                }
            });

            owner.putData("to_game", true);

            owner.loadPage(Game.class);
        };

        teamModeOverlay.setOnDone(begin);

        start.setOnClick(() -> {
            if(cards.size() == 4) {
                FourMode mode = FourMode.byText(Store.getFourMode());
                if(mode == FourMode.ASK_EVERYTIME) {
                    teamModeOverlay.show();
                }else {
                    owner.putString("mode", Store.getFourMode());
                    begin.run();
                }
            }else {
                owner.putString("mode", FourMode.NORMAL_MODE.getText());
                begin.run();
            }
        });

        content.addView(cards);
        content.addView(new ColoredSeparator(owner, Orientation.HORIZONTAL, 0, Style::getTextMuted));
        content.addView(offlineMirorredCards);
        content.addView(start);

        hideStart = new ParallelAnimation(300)
                .addAnimation(new TranslateYAnimation(start, 40))
                .addAnimation(new AlphaAnimation(start, 0))
                .addAnimation(new ScaleXYAnimation(start, .7f))
                .setInterpolator(Interpolator.EASE_OUT);

        showStart = new ParallelAnimation(300)
                .addAnimation(new TranslateYAnimation(start, 0))
                .addAnimation(new AlphaAnimation(start, 1))
                .addAnimation(new ScaleXYAnimation(start, 1))
                .setInterpolator(Interpolator.OVERSHOOT);

        applyStyle(owner.getStyle());
    }

    private final ArrayList<SocketConnection> connections = new ArrayList<>();

    @Override
    public void setup() {
        super.setup();

        cards.unloadAll();
        offlineMirorredCards.bind(cards);

        start.setAlpha(0);
        start.setTranslationY(40);
        start.setScaleX(.7f);
        start.setScaleY(.7f);

        loadPlayer(Store.getUsername(), Store.getAvatar(), OfflinePlayerCard.Type.SELF);

        LocalHost.host();

        LocalHost.setOnConnected(socket -> {
            SocketConnection client = new SocketConnection(socket);

            cards.unloadPlayer(client);

            client.on("by", data -> {
                JSONObject obj = new JSONObject();
                obj.put("username", Store.getUsername());
                obj.put("avatar", Store.getAvatar());
                obj.put("count", cards.size());
                connections.add(client);
                client.emit("by", obj);

                client.setOnError(() -> {
                    Platform.runLater(() -> {
                        if(cards.unloadExact(client))
                            owner.playMenuSound(R.raw.left);
                        updateCards();
                    });
                    client.emit("leave", "");
                    client.stop();
                    connections.remove(client);
                });
            });
            client.on("join", data -> {
                JSONObject obj = new JSONObject(data);
                Platform.runLater(() -> {
                    try {
                        loadPlayer(obj.getString("username"), obj.getString("avatar"), client);
                    } catch (JSONException e) {
                        ErrorHandler.handle(e, "loading player");
                    }
                });
            });
            client.on("leave", data -> Platform.runLater(() -> unloadPlayer(client)));
            client.start();
        });
    }

    private void unloadPlayer(SocketConnection connection) {
        owner.playMenuSound(R.raw.left);
        cards.unloadPlayer(connection);
        Platform.runAfter(this::updateCards, 100);
    }

    private synchronized void loadPlayer(String username, String avatar, SocketConnection connection, OfflinePlayerCard.Type type) {
        if(isPresent(connection)) {
            connection.emit("already_in", "");
            updateCards();
            return;
        }

        OfflinePlayerCard card = cards.getLast();

        if(card == null) {
            connection.emit("full", "");
            return;
        }

        card.loadPlayer(username, avatar, connection, type);
        if(connection != null) {
            owner.playMenuSound(R.raw.joined);
            connection.emit("joined", "");
        }
        updateCards();
    }

    public void loadPlayer(String name, String avatar, SocketConnection connection) {
        loadPlayer(name, avatar, connection, OfflinePlayerCard.Type.PLAYER);
    }

    public void loadPlayer(String name, String avatar, OfflinePlayerCard.Type type) {
        loadPlayer(name, avatar, null, type);
    }

    public void swap(int a, int b) {
        try {
            JSONObject data = new JSONObject();
            data.put("a", a);
            data.put("b", b);
            for(SocketConnection client : cards.broadcast()) {
                client.emit("swap", data);
            }
        }catch(JSONException x) {
            ErrorHandler.handle(x, "swapping");
        }
    }

    public void updateCards() {
        cards.fix();
        String data = cards.serialize();
        for(SocketConnection client : cards.broadcast()) {
            client.emit("players", data);
        }
        updateCount();

        if(cards.size() > 1) {
            showStart.start();
        }else {
            hideStart.start();
        }
    }

    public void updateCount() {
        try {
            connections.forEach(con -> con.emit("count", cards.size()));
        }catch(Exception x) {
            ErrorHandler.handle(x, "updating count");
        }
    }

    private boolean isPresent(SocketConnection connection) {
        for(SocketConnection client : cards.broadcast()) {
            if(Objects.equals(
                            client.getIp(),
                            connection.getIp())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();

        offlineMirorredCards.unbind();

        Object pToGame = owner.getData("to_game");
        boolean toGame = pToGame != null && (boolean) pToGame;
        if(!toGame) {
            connections.forEach(e -> e.emit("leave", ""));
            connections.clear();
        }

        owner.putData("to_game", false);
        LocalHost.stop();
    }

    @Override
    public boolean onBack() {
        owner.loadPage(OfflineHome.class);
        return true;
    }

    @Override
    public void applyStyle(Style style) {
        if(start == null) return;
        super.applyStyle(style);

        start.setFill(style.getBackgroundPrimary());
        start.setTextFill(style.getTextNormal());
    }
}
