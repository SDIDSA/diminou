package org.luke.diminou.app.pages.game.offline;

import android.graphics.Color;
import android.view.Gravity;
import org.luke.diminou.abs.components.layout.StackPane;

import androidx.core.graphics.Insets;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ColorAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.padding.PaddingAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.shape.Rectangle;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.net.SocketConnection;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.game.pause.GamePause;
import org.luke.diminou.app.pages.game.piece.Move;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.game.offline.piece.OfflineStock;
import org.luke.diminou.app.pages.game.offline.player.OfflinePieceHolder;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayer;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayerType;
import org.luke.diminou.app.pages.game.player.Side;
import org.luke.diminou.app.pages.game.offline.score.OfflineScoreBoard;
import org.luke.diminou.app.pages.game.offline.table.OfflineTable;
import org.luke.diminou.app.pages.home.offline.OfflineHome;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class OfflineGame extends Page {
    private final boolean host;
    private final VBox root;
    private final ArrayList<OfflinePieceHolder> holders;

    private final OfflineTable table;

    private final HBox center;
    private final OfflineCherrat cherat;
    private OfflineStock stock;

    private final OfflineTurnManager turn;

    private final OfflineScoreBoard scoreBoard;

    private final GamePause gamePause;

    private final Label leftInStock;

    private final StackPane preRoot;

    private final Rectangle background;

    private final ColoredIcon menu;
    public OfflineGame(App owner) {
        super(owner);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);

        host = owner.isHost();

        root = new VBox(owner);
        root.setGravity(Gravity.BOTTOM | Gravity.CENTER);
        root.setClipChildren(false);

        table = new OfflineTable(owner);

        holders = new ArrayList<>();

        center = new HBox(owner);
        center.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        center.setClipChildren(false);

        cherat = new OfflineCherrat(owner);

        root.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        root.addView(center);
        root.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        root.addView(cherat);

        scoreBoard = new OfflineScoreBoard(owner);
        scoreBoard.addOnShowing(() -> owner.playMenuSound(R.raw.end));
        scoreBoard.addOnShowing(() -> {
            for (OfflinePieceHolder holder : holders) {
                holder.setEnabled(false);
            }
        });

        menu = new ColoredIcon(owner, Style::getTextNormal, R.drawable.leave);
        menu.setRotation(180);
        ViewUtils.alignInFrame(menu, Gravity.TOP | Gravity.START);
        menu.setSize(40);
        ViewUtils.setPaddingUnified(menu, 7, owner);


        preRoot = new StackPane(owner);
        background = new Rectangle(owner);
        background.setRadius(10);
        background.setLayoutParams(new LayoutParams(-1, -1));
        preRoot.addView(background);
        preRoot.addView(table);
        preRoot.addView(root);
        preRoot.addView(menu);

        preRoot.setClipChildren(false);

        leftInStock = new Label(owner, "stock");
        addView(preRoot);
        addView(leftInStock);

        turn = new OfflineTurnManager(owner, this);

        gamePause = new GamePause(owner);

        menu.setOnClick(this::onBack);

        ViewUtils.alignInFrame(leftInStock, Gravity.TOP | Gravity.END);

        applyStyle(owner.getStyle());
    }

    public OfflineCherrat getCherrat() {
        return cherat;
    }

    public OfflinePassInit getPassInit() {
        return cherat.getPassInit();
    }

    public OfflinePieceHolder getForPlayer(OfflinePlayer player) {
        for (OfflinePieceHolder holder : holders) {
            if (holder.getPlayer().equals(player)) return holder;
        }
        ErrorHandler.handle(new IllegalStateException("player not found"), "getting pieceHolder for player " + player.serialize());
        return null;
    }

    public OfflinePlayer getForSocket(SocketConnection socket) {
        if(owner.getPlayers() == null) return null;
        for(OfflinePlayer p : owner.getPlayers()){
            if(!p.getIp().isBlank() && p.getIp().equals(socket.getIp())) {
                return p;
            }
        }
        return null;
    }

    public synchronized void updateStock() {
        int stock = 28 - holders.stream().mapToInt(OfflinePieceHolder::getDisplayedCount).sum() - table.count();
        Platform.runLater(() -> leftInStock.addParam(0, String.valueOf(stock)));
    }

    public ArrayList<OfflinePieceHolder> getHolders() {
        return holders;
    }

    public OfflineStock getStock() {
        return stock;
    }

    public OfflineScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    public int getScoreOf(OfflinePlayer player) {
        ConcurrentHashMap<OfflinePlayer, Integer> score = owner.getScore();

        Integer i = score.get(player);
        if(i != null)
            return i;

        score.put(player, 0);
        return 0;
    }

    private void addScoreOf(OfflinePlayer player, int add) {
        ConcurrentHashMap<OfflinePlayer, Integer> score = owner.getScore();

        int oldScore = getScoreOf(player);

        score.put(player, oldScore + add);
    }

    public void setScoreOf(OfflinePlayer player, int val) {
        ConcurrentHashMap<OfflinePlayer, Integer> score = owner.getScore();

        score.put(player, val);
    }

    public int index(OfflinePlayer player) {
        List<OfflinePlayer> players = owner.getPlayers();
        for(int i = 0; i < players.size(); i++) {
            if(players.get(i).equals(player)) {
                return i;
            }
        }
        return -1;
    }

    public OfflinePlayer otherPlayer(OfflinePlayer player) {
        List<OfflinePlayer> players = owner.getPlayers();
        return players.get((index(player) + 2) % players.size());
    }

    private int scoreToAdd(OfflinePlayer winner) {
        int sum = 0;

        ArrayList<OfflinePlayer> winners = new ArrayList<>();
        winners.add(winner);

        if(owner.getFourMode() == FourMode.TEAM_MODE) {
            winners.add(otherPlayer(winner));
        }

        for(OfflinePieceHolder holder : holders) {
            if(!winners.contains(holder.getPlayer())) {
                sum += holder.sum();
            }
        }
        return sum;
    }

    public void pass(OfflinePieceHolder holder) {
        if(host) {
            owner.getSockets().forEach(socket -> socket.emit("pass", holder.getPlayer().serialize()));
        }
        Platform.runAfter(() -> {
            if(holder.getPlayer().isSelf(host)) {
                owner.toast("pass");
            }
            owner.playGameSound(R.raw.pass);
        }, 300);
        Platform.runAfter(() -> turn.nextTurn(holder), 1000);
    }

    public OfflineTurnManager getTurn() {
        return turn;
    }

    public OfflineTable getTable() {
        return table;
    }

    public List<Piece> deal() {
        return stock.deal();
    }

    public OfflinePlayer checkForWinner() {
        AtomicReference<OfflinePlayer> winner = new AtomicReference<>();
        holders.forEach(h -> {
            if(h.getPieces().isEmpty()) {
                winner.set(h.getPlayer());
            }
        });
        return winner.get();
    }

    public void emitWin(OfflinePlayer winner) {
        if(host) {
            int scoreToAdd = scoreToAdd(winner);

            addScoreOf(winner, scoreToAdd);
            if(owner.getFourMode() == FourMode.TEAM_MODE) {
                addScoreOf(otherPlayer(winner), scoreToAdd);
            }

            owner.getSockets().forEach(s -> s.emit("winner", scoreBoard(winner).toString()));
            owner.putData("winner", winner);
            victory(scoreBoard(winner));
        }
    }

    private JSONArray scoreBoard(OfflinePlayer winner) {
        JSONArray arr = new JSONArray();
        try {
            List<OfflinePlayer> players = owner.getPlayers();
            for(OfflinePlayer player : players) {
                JSONObject obj = new JSONObject();
                if(player.equals(winner)) {
                    obj.put("winner", true);
                }
                obj.put("player", player.serialize());
                obj.put("score", getScoreOf(player));
                arr.put(obj);
            }
        }catch(Exception x) {
            ErrorHandler.handle(x, "sending victory");
        }
        return arr;
    }

    public void emitDraw() {
        if(host) {
            owner.getSockets().forEach(s -> s.emit("winner", scoreBoard(null).toString()));
            owner.putData("winner", null);
        }
        victory(scoreBoard(null));
    }

    public void victory(JSONArray data) {
        if(!host) {
            try {
                for(int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    OfflinePlayer player = OfflinePlayer.deserialize(obj.getJSONObject("player"));
                    int score = obj.getInt("score");
                    if(obj.has("winner")) owner.putData("winner", player);
                    setScoreOf(player, score);
                }
            }catch(Exception x) {
                ErrorHandler.handle(x, "receiving scoreBoard");
            }
        }
        Platform.runAfter(scoreBoard::show, 200);
    }

    private void playerLeft(OfflinePlayer left, SocketConnection socket) {
        if(host) {
            owner.getSockets().forEach(s -> s.emit("leave", left.serialize()));
            owner.getSockets().remove(socket);
            Platform.runLater(() -> {
                owner.toast("player_left", left.getName());
                owner.playMenuSound(R.raw.left);
                getForPlayer(left).makeBot();
            });
        }else if(!left.equals(getBottomHolder().getPlayer())){
            Platform.runLater(() -> {
                owner.toast("player_left", left.getName());
                owner.playMenuSound(R.raw.left);
            });
        }
    }

    @Override
    public void setup() {
        super.setup();
        ended = false;
        root.setBackground(Color.TRANSPARENT);
        leftInStock.setAlpha(0);
        stock = new OfflineStock();
        table.clear();
        holders.forEach(h -> {
            root.removeView(h);
            center.removeView(h);
        });
        holders.clear();

        cherat.setAlpha(0);
        cherat.setTranslationY(ViewUtils.dipToPx(80, owner));

        List<OfflinePlayer> players = owner.getPlayers();

        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            OfflinePlayer ati = players.get(i);

            if (ati.isSelf(host))
                index = i;
        }

        int pos = 0;
        while (pos < players.size()) {
            OfflinePlayer p = players.get(index);

            switch (pos) {
                case 0 -> {
                    OfflinePieceHolder ph = new OfflinePieceHolder(owner, this, p, Side.BOTTOM, true);
                    root.addView(ph);
                    holders.add(ph);
                }
                case 1 -> {
                    if (players.size() == 2) {
                        OfflinePieceHolder ph = new OfflinePieceHolder(owner, this, p, Side.TOP, false);
                        root.addView(ph, 0);
                        holders.add(ph);
                    } else {
                        OfflinePieceHolder ph = new OfflinePieceHolder(owner, this, p, Side.RIGHT, false);
                        center.addView(ph);
                        holders.add(ph);
                    }
                }
                case 2 -> {
                    OfflinePieceHolder ph = new OfflinePieceHolder(owner, this, p, Side.TOP, false);
                    root.addView(ph, 0);
                    holders.add(ph);
                }
                case 3 -> {
                    OfflinePieceHolder ph = new OfflinePieceHolder(owner, this, p, Side.LEFT, false);
                    center.addView(ph, 0);
                    holders.add(ph);
                }
            }

            index = (index + 1) % players.size();
            pos++;
        }

        Insets insets = owner.getSystemInsets();

        int add = ViewUtils.dipToPx(8, owner);

        preRoot.setPadding(0,0,0,0);
        root.setCornerRadius(10);

        ParallelAnimation show = new ParallelAnimation(400)
                .addAnimation(new PaddingAnimation(400, preRoot,
                        insets.left + add,
                        insets.top + add + ViewUtils.dipToPx(17, owner),
                        insets.right + add,
                        insets.bottom + add))
                .addAnimation(new ColorAnimation(owner.getStyle().get().getBackgroundTertiary(), owner.getStyle().get().getTextMuted()) {
                    @Override
                    public void updateValue(int color) {
                        root.setBorderColor(color);
                    }
                })
                .addAnimations(
                        holders.stream().map(OfflinePieceHolder::setup).toArray(Animation[]::new))
                .addAnimation(new TranslateYAnimation(menu, -ViewUtils.by(owner), 0))
                .addAnimation(new AlphaAnimation(menu, 0, 1))
                .setOnUpdate(v -> {
                    leftInStock.setTranslationY(owner.getSystemInsets().top * v);
                    leftInStock.setTranslationX(-preRoot.getPaddingRight());
                    leftInStock.setAlpha(v);
                })
                .setOnFinished(table::adjustBoard)
                .setInterpolator(Interpolator.EASE_OUT);

        if(owner.getFourMode() == FourMode.TEAM_MODE) {
            show.addAnimation(cherat.show());
        }else {
            root.removeView(cherat);
        }
        show.start();

        if (host) {
            owner.getSockets().forEach(socket -> {
                socket.setOnError(() -> {
                    OfflinePlayer client = getForSocket(socket);
                    if(client == null || client.getType() == OfflinePlayerType.BOT) return;
                    playerLeft(client, socket);
                });

                socket.on("deal", data -> {
                    JSONArray arr = new JSONArray();
                    OfflinePlayer player = OfflinePlayer.deserialize(new JSONObject(data));
                    OfflinePieceHolder holder = getForPlayer(player);

                    assert holder != null;
                    assert player != null;

                    while (holder.getPieces().isEmpty()) Platform.sleep(50);

                    holder.getPieces().forEach(p -> arr.put(p.name()));
                    JSONObject obj = new JSONObject();
                    obj.put("player", player.serialize());
                    obj.put("pieces", arr);
                    socket.emit("deal", obj);
                });
                socket.on("move", data -> {
                    JSONObject obj = new JSONObject(data);
                    OfflinePlayer player = OfflinePlayer.deserialize(obj.getJSONObject("player"));
                    Move move = Move.deserialize(obj.getJSONObject("move"));
                    OfflinePieceHolder holder = getForPlayer(player);

                    assert holder != null;
                    assert move != null;

                    owner.getSockets().forEach(s -> s.emit("move", data));
                    Platform.runLater(() -> holder.play(move));
                });
                socket.on("khabet", data -> {
                    owner.getSockets().forEach(s -> s.emit("khabet", data));
                    OfflinePlayer player = OfflinePlayer.deserialize(new JSONObject(data));
                    getForPlayer(player).cherra(R.drawable.khabet, R.raw.khabet);
                });
                socket.on("saket", data -> {
                    owner.getSockets().forEach(s -> s.emit("saket", data));
                    OfflinePlayer player = OfflinePlayer.deserialize(new JSONObject(data));
                    getForPlayer(player).cherra(R.drawable.saket, R.raw.saket);
                });
                socket.on("turn", data -> {
                    OfflinePlayer winner = checkForWinner();
                    if(winner != null) {
                        emitWin(winner);
                        return;
                    }
                    OfflinePlayer p = OfflinePlayer.deserialize(new JSONObject(data));
                    turn.turn(p);
                });
                socket.on("leave", data ->
                        playerLeft(OfflinePlayer.deserialize(new JSONObject(data)), socket));
            });
        } else {
            SocketConnection socket = owner.getSocket();
            socket.setOnError(() -> Platform.runLater(() -> {
                if(ended)
                    return;
                owner.toast("host_ended");
                endGame();
            }));
            socket.on("deal", data -> {
                JSONObject all = new JSONObject(data);
                OfflinePlayer player = OfflinePlayer.deserialize(all.getJSONObject("player"));
                OfflinePieceHolder holder = getForPlayer(player);

                assert holder != null;
                assert player != null;

                JSONArray arr = all.getJSONArray("pieces");
                ArrayList<Piece> pieces = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    pieces.add(Piece.valueOf(arr.getString(i)));
                }
                holder.add(pieces.toArray(new Piece[0]));
            });
            socket.on("turn", data -> {
                OfflinePlayer p = OfflinePlayer.deserialize(new JSONObject(data));
                turn.turn(p);
            });
            socket.on("saket", data -> {
                OfflinePlayer player = OfflinePlayer.deserialize(new JSONObject(data));
                assert player != null;
                if(player.equals(getBottomHolder().getPlayer())) return;
                getForPlayer(player).cherra(R.drawable.saket, R.raw.saket);
            });
            socket.on("khabet", data -> {
                OfflinePlayer player = OfflinePlayer.deserialize(new JSONObject(data));
                assert player != null;
                if(player.equals(getBottomHolder().getPlayer())) return;
                getForPlayer(player).cherra(R.drawable.khabet, R.raw.khabet);
            });
            socket.on("move", data -> {
                JSONObject obj = new JSONObject(data);
                OfflinePlayer player = OfflinePlayer.deserialize(obj.getJSONObject("player"));
                Move move = Move.deserialize(obj.getJSONObject("move"));
                OfflinePieceHolder holder = getForPlayer(player);

                assert holder != null;
                assert move != null;
                assert player != null;
                if(player.isSelf(false)) return;
                Platform.runLater(() -> holder.play(move));
            });
            socket.on("winner", data -> victory(new JSONArray(data)));
            socket.on("pass", data -> {
                OfflinePlayer p = OfflinePlayer.deserialize(new JSONObject(data));
                Platform.runAfter(() -> {
                    assert p != null;
                    if(p.isSelf(false))
                        owner.toast("pass");
                    owner.playGameSound(R.raw.pass);
                }, 300);
            });
            socket.on("skip", data -> Platform.runLater(() -> {
                scoreBoard.hide();
                setup();
            }));
            socket.on("end_game", data -> Platform.runLater(() -> {
                if(ended)
                    return;
                owner.toast("host_ended");
                endGame();
            }));
            socket.on("leave", data ->
                    playerLeft(OfflinePlayer.deserialize(new JSONObject(data)), socket));
        }

        Platform.runBack(() -> {
            boolean empty = true;
            while (empty) {
                empty = false;
                for (OfflinePieceHolder holder : holders) {
                    if (holder.getPieces().isEmpty()) {
                        empty = true;
                        break;
                    }
                }
                Platform.sleep(50);
            }

            Platform.runAfter(turn::init, 300);
        });
    }

    public OfflinePieceHolder getTopHolder() {
        return getHolderAt(Side.TOP);
    }

    public OfflinePieceHolder getBottomHolder() {
        return getHolderAt(Side.BOTTOM);
    }

    public OfflinePieceHolder getRightHolder() {
        return getHolderAt(Side.RIGHT);
    }

    public OfflinePieceHolder getLeftHolder() {
        return getHolderAt(Side.LEFT);
    }

    public OfflinePieceHolder getHolderAt(Side side) {
        for(OfflinePieceHolder holder : holders) {
            if(holder.getSide() == side) {
                return holder;
            }
        }
        return null;
    }

    @Override
    public boolean onBack() {
        gamePause.setOnExit(() -> {
            if(host) {
                owner.getSockets().forEach(s -> s.emit("end_game", ""));
            }else {
                owner.getSocket().emit("leave", getBottomHolder().getPlayer().serialize());
            }
            endGame();
        });
        gamePause.show();
        return true;
    }

    private boolean ended = false;

    public boolean isEnded() {
        return ended;
    }

    private void endGame() {
        ended = true;
        gamePause.hide();
        owner.putData("score", null);
        owner.putData("winner", null);
        owner.putData("players", null);
        holders.forEach(OfflinePieceHolder::deselect);
        holders.forEach(h -> h.setEnabled(false));
        Style style = owner.getStyle().get();
        new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(table, 0))
                .addAnimation(new ScaleXYAnimation(table, .5f))
                .addAnimations(
                        holders.stream().map(OfflinePieceHolder::hideAnimation).toArray(Animation[]::new))
                .addAnimation(new PaddingAnimation(400, preRoot,
                        0,
                        0,
                        0,
                        0))
                .addAnimation(cherat.hide())
                .addAnimation(new ColorAnimation(style.getTextMuted(), style.getBackgroundTertiary()) {
                    @Override
                    public void updateValue(int color) {
                        root.setBorderColor(color);
                    }
                })
                .addAnimation(new ColorAnimation(Color.TRANSPARENT, style.getBackgroundTertiary()) {
                    @Override
                    public void updateValue(int color) {
                        root.setBackground(color);
                    }
                })
                .addAnimation(new TranslateYAnimation(menu, 0, -ViewUtils.by(owner)))
                .addAnimation(new AlphaAnimation(menu, 1, 0))
                .setOnUpdate(v -> {
                    leftInStock.setTranslationY(owner.getSystemInsets().top * (1 - v));
                    leftInStock.setAlpha(1 - v);
                })
                .setInterpolator(Interpolator.EASE_OUT)
                .setOnFinished(() -> {
                    holders.forEach(h -> {
                        root.removeView(h);
                        center.removeView(h);
                    });
                    holders.clear();
                    owner.removeLoaded();
                    owner.loadPage(OfflineHome.class);
                    if(host) {
                        owner.getSockets().forEach(s -> s.setOnError(null));
                        owner.getSockets().forEach(SocketConnection::stop);
                        owner.getSockets().clear();
                    }else {
                        owner.getSocket().setOnError(null);
                        owner.getSocket().stop();
                        owner.putData("socket", null);
                    }
                }).start();
    }

    public boolean isHost() {
        return host;
    }

    @Override
    public void applyInsets(Insets insets) {
        //ignore
    }

    @Override
    public void destroy(Page newPage) {
        super.destroy(newPage);
        preRoot.setPadding(0,0,0,0);
        root.setCornerRadius(0);
    }

    @Override
    public void applyStyle(Style style) {
        preRoot.setBackgroundColor(style.getBackgroundPrimary());
        background.setFill(style.getBackgroundTertiary());
        root.setBorderColor(style.getTextMuted());
        leftInStock.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }

}
