package org.luke.diminou.app.pages.game;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.FrameLayout;

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
import org.luke.diminou.abs.animation.view.corner_radii.CornerRadiiAnimation;
import org.luke.diminou.abs.animation.view.padding.PaddingAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.local.SocketConnection;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.game.score.ScoreBoard;
import org.luke.diminou.app.pages.home.Home;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class Game extends Page {
    private final boolean host;
    private final VBox root;
    private final ArrayList<PieceHolder> holders;

    private final Table table;

    private final HBox center;
    private final HBox cherat;
    private final Semaphore stockMutex = new Semaphore(1);
    private ArrayList<Piece> stock;

    private final ScoreBoard scoreBoard;

    private final Label leftInStock;

    private final FrameLayout preRoot;

    public Game(App owner) {
        super(owner);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);

        host = owner.getTypedData("host");

        root = new VBox(owner);
        root.setGravity(Gravity.BOTTOM | Gravity.CENTER);
        root.setClipChildren(false);

        table = new Table(owner);

        holders = new ArrayList<>();

        center = new HBox(owner);
        center.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        center.setClipChildren(false);

        cherat = new HBox(owner);
        cherat.setPadding(7);

        ColoredIcon khabt = new ColoredIcon(owner, Style::getTextNormal, R.drawable.khabet_static);
        khabt.setSize(38);

        ColoredIcon sakt = new ColoredIcon(owner, Style::getTextNormal, R.drawable.saket_static);
        sakt.setSize(38);

        khabt.setOnClick(() -> {
            getBottomHolder().cherra(R.drawable.khabet, R.raw.khabet);
            if(host) {
                broadCast().forEach(s -> s.emit("khabet", getBottomHolder().getPlayer().serialize()));
            } else {
                SocketConnection host = owner.getTypedData("socket");
                host.emit("khabet", getBottomHolder().getPlayer().serialize());
            }
        });

        sakt.setOnClick(() -> {
            getBottomHolder().cherra(R.drawable.saket, R.raw.saket);
            if(host) {
                broadCast().forEach(s -> s.emit("saket", getBottomHolder().getPlayer().serialize()));
            } else {
                SocketConnection host = owner.getTypedData("socket");
                host.emit("saket", getBottomHolder().getPlayer().serialize());
            }
        });

        cherat.addView(sakt);
        cherat.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        cherat.addView(khabt);

        ViewUtils.setMarginBottom(cherat, owner, 20);

        root.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        root.addView(center);
        root.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        root.addView(cherat);

        scoreBoard = new ScoreBoard(owner);
        scoreBoard.addOnShowing(() -> {
            for (PieceHolder holder : holders) {
                holder.setEnabled(false);
            }
        });

        preRoot = new FrameLayout(owner);
        preRoot.addView(table);
        preRoot.addView(root);
        preRoot.setClipChildren(false);

        leftInStock = new Label(owner, "stock");
        addView(preRoot);
        addView(leftInStock);

        ViewUtils.alignInFrame(leftInStock, Gravity.TOP | Gravity.END);

        applyStyle(owner.getStyle());
    }

    public PieceHolder getForPlayer(Player player) {
        for (PieceHolder holder : holders) {
            if (holder.getPlayer().equals(player)) return holder;
        }
        ErrorHandler.handle(new IllegalStateException("player not found"), "getting pieceHolder for player " + player.serialize());
        return null;
    }

    public synchronized void updateStock() {
        int stock = 28 - holders.stream().mapToInt(PieceHolder::getDisplayedCount).sum() - table.count();
        Platform.runLater(() -> leftInStock.addParam(0, String.valueOf(stock)));
    }

    public void turn(Player p) {
        holders.forEach(h -> h.setEnabled(p.equals(h.getPlayer())));
        if(host) {
            broadCast().forEach(socket -> socket.emit("turn", p.serialize()));
            PieceHolder holder = getForPlayer(p);
            assert holder != null;
            ArrayList<Piece> toAdd = new ArrayList<>(holder.getPieces());
            boolean lostTurn = false;
            while(table.getPossiblePlays(toAdd).isEmpty()) {
                if(stock.isEmpty()) {
                    pass(holder);
                    lostTurn = true;
                    break;
                }
                stockMutex.acquireUninterruptibly();
                toAdd.add(stock.get(0));
                stock.remove(0);
                stockMutex.release();
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
                        broadCast().forEach(socket -> socket.emit("deal", obj));
                        holder.add(toAdd.toArray(new Piece[0]));
                    }, 300);
                }catch(Exception x) {
                    ErrorHandler.handle(x, "dealing pieces");
                }
            }
            if(p.getType() == PlayerType.BOT && !lostTurn && checkForWinner() == null) {
                Platform.runAfter(() -> {
                    if(owner.getLoaded() != this) return;
                    List<Piece> possible = table.getPossiblePlays(holder.getPieces());
                    if(!possible.isEmpty()) {
                        Piece piece = possible.get(0);
                        Move m = table.getPossiblePlays(piece, null).get(0);

                        holder.play(m);

                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("player", p.serialize());
                            obj.put("move", m.serialize());
                            broadCast().forEach(socket -> socket.emit("move", obj));
                        }catch (Exception x) {
                            ErrorHandler.handle(x, "playing bot");
                        }

                        if(holder.getPieces().isEmpty()) {
                            emitWin(p);
                        }else {
                            nextTurn(holder);
                        }
                    } else {
                        pass(holder);
                    }
                }, 2000);
            }
        }
    }

    public void turn(PieceHolder holder) {
        turn(holder.getPlayer());
    }

    public void nextTurn(PieceHolder holder) {
        if(holders.isEmpty()) return;
        PieceHolder next = holders.get((holders.indexOf(holder) + 1) % holders.size());
        if(!host) {
            SocketConnection socket = owner.getTypedData("socket");
            socket.emit("turn", next.getPlayer().serialize());
        }
        if(host) {
            boolean m9foul = stock.isEmpty();
            if(m9foul) {
                for(PieceHolder h : holders) {
                    if(!table.getPossiblePlays(h.getPieces()).isEmpty()) {
                        m9foul = false;
                    }
                }
            }
            if(m9foul) {
                ArrayList<Player> winner = new ArrayList<>();
                int min = Integer.MAX_VALUE;

                for(PieceHolder h : holders) {
                    int sum = h.sum();
                    if(sum <= min) {
                        if(sum < min) {
                            winner.clear();
                            min = sum;
                        }
                        winner.add(h.getPlayer());
                    }
                }

                FourMode mode = FourMode.byText(owner.getString("mode"));
                if(winner.size() == 1 ||
                        (mode == FourMode.TEAM_MODE &&
                                winner.size() == 2 &&
                                index(winner.get(0)) % 2 == index(winner.get(1)) % 2)) {
                    emitWin(winner.get(0));
                } else {
                    emitDraw();
                }
            }else {
                turn(next);
            }
        }else {
            turn(next);
        }
    }

    private HashMap<Player, Integer> getScore() {
        HashMap<Player, Integer> score = owner.getTypedData("score");
        if(score == null) {
            score = new HashMap<>();
            owner.putData("score", score);
        }
        return score;
    }

    int getScoreOf(Player player) {
        HashMap<Player, Integer> score = getScore();

        Integer i = score.get(player);
        if(i != null)
            return i;

        score.put(player, 0);
        return 0;
    }

    private void addScoreOf(Player player, int add) {
        HashMap<Player, Integer> score = getScore();

        int oldScore = getScoreOf(player);

        score.put(player, oldScore + add);
    }

    public void setScoreOf(Player player, int val) {
        HashMap<Player, Integer> score = getScore();

        score.put(player, val);
    }

    private int index(Player player) {
        ArrayList<Player> players = owner.getTypedData("players");
        for(int i = 0; i < players.size(); i++) {
            if(players.get(i).equals(player)) {
                return i;
            }
        }
        return -1;
    }

    public Player otherPlayer(Player player) {
        ArrayList<Player> players = owner.getTypedData("players");
        return players.get((index(player) + 2) % players.size());
    }

    private int scoreToAdd(Player winner) {
        int sum = 0;
        FourMode mode = FourMode.byText(owner.getString("mode"));

        ArrayList<Player> winners = new ArrayList<>();
        winners.add(winner);

        if(mode == FourMode.TEAM_MODE) {
            winners.add(otherPlayer(winner));
        }

        for(PieceHolder holder : holders) {
            if(!winners.contains(holder.getPlayer())) {
                sum += holder.sum();
            }
        }
        return sum;
    }

    private void pass(PieceHolder holder) {
        if(host) {
            broadCast().forEach(socket -> socket.emit("pass", holder.getPlayer().serialize()));
        }
        Platform.runAfter(() -> {
            if(holder.getPlayer().isSelf(host)) {
                owner.toast("Pass");
            }
            owner.playSound(R.raw.pass);
        }, 300);
        Platform.runAfter(() -> nextTurn(holder), 1000);
    }

    public Table getTable() {
        return table;
    }

    public List<SocketConnection> broadCast() {
        return owner.getTypedData("sockets");
    }

    public List<Piece> deal() {
        stockMutex.acquireUninterruptibly();
        ArrayList<Piece> res = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            res.add(stock.get(i));
        }
        stock.removeAll(res);
        stockMutex.release();
        return res;
    }

    private Player checkForWinner() {
        AtomicReference<Player> winner = new AtomicReference<>();
        holders.forEach(h -> {
            if(h.getPieces().isEmpty()) {
                winner.set(h.getPlayer());
            }
        });
        return winner.get();
    }

    public void emitWin(Player winner) {
        if(host) {
            FourMode mode = FourMode.byText(owner.getString("mode"));

            int scoreToAdd = scoreToAdd(winner);

            addScoreOf(winner, scoreToAdd);
            if(mode == FourMode.TEAM_MODE) {
                addScoreOf(otherPlayer(winner), scoreToAdd);
            }

            broadCast().forEach(s -> s.emit("winner", scoreBoard(winner).toString()));
            owner.putData("winner", winner);
            victory(scoreBoard(winner));
        }
    }

    private JSONArray scoreBoard(Player winner) {
        JSONArray arr = new JSONArray();
        try {
            ArrayList<Player> players = owner.getTypedData("players");
            for(Player player : players) {
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
            broadCast().forEach(s -> s.emit("winner", scoreBoard(null).toString()));
            owner.putData("winner", null);
        }
        victory(scoreBoard(null));
    }

    public void victory(JSONArray data) {
        if(!host) {
            try {
                for(int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    Player player = Player.deserialize(obj.getJSONObject("player"));
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

    @Override
    public void setup() {
        super.setup();
        root.setBackground(Color.TRANSPARENT);
        leftInStock.setAlpha(0);
        stock = Piece.pack();
        table.clear();
        holders.forEach(h -> {
            root.removeView(h);
            center.removeView(h);
        });
        holders.clear();

        cherat.setAlpha(0);
        cherat.setTranslationY(ViewUtils.dipToPx(80, owner));

        ArrayList<Player> players = owner.getTypedData("players");

        int index = 0;
        for (int i = 0; i < players.size(); i++) {
            Player ati = players.get(i);

            if (ati.isSelf(host))
                index = i;
        }

        int pos = 0;
        while (pos < players.size()) {
            Player p = players.get(index);

            switch (pos) {
                case 0 -> {
                    PieceHolder ph = new PieceHolder(owner, this, p, Side.BOTTOM, true);
                    root.addView(ph);
                    holders.add(ph);
                }
                case 1 -> {
                    if (players.size() == 2) {
                        PieceHolder ph = new PieceHolder(owner, this, p, Side.TOP, false);
                        root.addView(ph, 0);
                        holders.add(ph);
                    } else {
                        PieceHolder ph = new PieceHolder(owner, this, p, Side.RIGHT, false);
                        center.addView(ph);
                        holders.add(ph);
                    }
                }
                case 2 -> {
                    PieceHolder ph = new PieceHolder(owner, this, p, Side.TOP, false);
                    root.addView(ph, 0);
                    holders.add(ph);
                }
                case 3 -> {
                    PieceHolder ph = new PieceHolder(owner, this, p, Side.LEFT, false);
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
        root.setCornerRadius(0);

        ParallelAnimation show = new ParallelAnimation(400)
                .addAnimation(new PaddingAnimation(400, preRoot,
                        insets.left + add,
                        insets.top + add + ViewUtils.dipToPx(15, owner),
                        insets.right + add,
                        insets.bottom + add))
                .addAnimation(new ColorAnimation(owner.getStyle().get().getBackgroundTertiary(), owner.getStyle().get().getTextMuted()) {
                    @Override
                    public void updateValue(int color) {
                        root.setBorderColor(color);
                    }
                })
                .addAnimations(
                        holders.stream().map(PieceHolder::setup).toArray(Animation[]::new))
                .setOnUpdate(v -> {
                    leftInStock.setTranslationY(owner.getSystemInsets().top * v);
                    leftInStock.setTranslationX(-preRoot.getPaddingRight());
                    leftInStock.setAlpha(v);
                })
                .setInterpolator(Interpolator.EASE_OUT);
        FourMode mode = FourMode.byText(owner.getString("mode"));
        if(mode == FourMode.TEAM_MODE) {
            show.addAnimation(new AlphaAnimation(cherat, 1))
                    .addAnimation(new TranslateYAnimation(cherat, 0));
        }
        show.start();



        if (host) {
            broadCast().forEach(socket -> {
                socket.on("deal", data -> {
                    JSONArray arr = new JSONArray();
                    Player player = Player.deserialize(new JSONObject(data));
                    PieceHolder holder = getForPlayer(player);

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
                    Player player = Player.deserialize(obj.getJSONObject("player"));
                    Move move = Move.deserialize(obj.getJSONObject("move"));
                    PieceHolder holder = getForPlayer(player);

                    assert holder != null;
                    assert move != null;

                    broadCast().forEach(s -> s.emit("move", data));
                    Platform.runLater(() -> holder.play(move));
                });
                socket.on("khabet", data -> {
                    broadCast().forEach(s -> s.emit("khabet", data));
                    Player player = Player.deserialize(new JSONObject(data));
                    getForPlayer(player).cherra(R.drawable.khabet, R.raw.khabet);
                });
                socket.on("saket", data -> {
                    broadCast().forEach(s -> s.emit("saket", data));
                    Player player = Player.deserialize(new JSONObject(data));
                    getForPlayer(player).cherra(R.drawable.saket, R.raw.saket);
                });
                socket.on("turn", data -> {
                    Player winner = checkForWinner();
                    if(winner != null) {
                        emitWin(winner);
                        return;
                    }
                    Player p = Player.deserialize(new JSONObject(data));
                    turn(p);
                });
            });
        } else {
            SocketConnection socket = owner.getTypedData("socket");
            socket.on("deal", data -> {
                JSONObject all = new JSONObject(data);
                Player player = Player.deserialize(all.getJSONObject("player"));
                PieceHolder holder = getForPlayer(player);

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
                Player p = Player.deserialize(new JSONObject(data));
                turn(p);
            });
            socket.on("saket", data -> {
                Player player = Player.deserialize(new JSONObject(data));
                assert player != null;
                if(player.equals(getBottomHolder().getPlayer())) return;
                getForPlayer(player).cherra(R.drawable.saket, R.raw.saket);
            });
            socket.on("khabet", data -> {
                Player player = Player.deserialize(new JSONObject(data));
                assert player != null;
                if(player.equals(getBottomHolder().getPlayer())) return;
                getForPlayer(player).cherra(R.drawable.khabet, R.raw.khabet);
            });
            socket.on("move", data -> {
                JSONObject obj = new JSONObject(data);
                Player player = Player.deserialize(obj.getJSONObject("player"));
                Move move = Move.deserialize(obj.getJSONObject("move"));
                PieceHolder holder = getForPlayer(player);

                assert holder != null;
                assert move != null;
                assert player != null;
                if(player.isSelf(false)) return;
                Platform.runLater(() -> holder.play(move));
            });
            socket.on("winner", data -> victory(new JSONArray(data)));
            socket.on("pass", data -> {
                Player p = Player.deserialize(new JSONObject(data));
                Platform.runAfter(() -> {
                    assert p != null;
                    if(p.isSelf(false))
                        owner.toast("Pass");
                    owner.playSound(R.raw.pass);
                }, 300);
            });
            socket.on("skip", data -> Platform.runLater(() -> {
                scoreBoard.hide();
                setup();
            }));
        }

        Platform.runBack(() -> {
            boolean empty = true;
            while (empty) {
                empty = false;
                for (PieceHolder holder : holders) {
                    if (holder.getPieces().isEmpty()) {
                        empty = true;
                        break;
                    }
                }
                Platform.sleep(50);
            }

            Platform.runAfter(() -> {
                if (host) {
                    Player winner = owner.getTypedData("winner");
                    if (winner != null)
                        turn(winner);
                    else {
                        List<Piece> priority = Piece.priority();
                        for (Piece piece : priority) {
                            boolean found = false;
                            for (PieceHolder holder : holders) {
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
            }, 300);
        });
    }

    public PieceHolder getTopHolder() {
        return getHolderAt(Side.TOP);
    }

    public PieceHolder getBottomHolder() {
        return getHolderAt(Side.BOTTOM);
    }

    public PieceHolder getRightHolder() {
        return getHolderAt(Side.RIGHT);
    }

    public PieceHolder getLeftHolder() {
        return getHolderAt(Side.LEFT);
    }

    public PieceHolder getHolderAt(Side side) {
        for(PieceHolder holder : holders) {
            if(holder.getSide() == side) {
                return holder;
            }
        }
        return null;
    }

    @Override
    public boolean onBack() {
        owner.putData("score", null);
        owner.putData("winner", null);
        owner.putData("players", null);
        holders.forEach(PieceHolder::deselect);
        holders.forEach(h -> h.setEnabled(false));
        Style style = owner.getStyle().get();
        new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(table, 0))
                .addAnimation(new ScaleXYAnimation(table, .5f))
                .addAnimations(
                        holders.stream().map(PieceHolder::hideAnimation).toArray(Animation[]::new))
                .addAnimation(new PaddingAnimation(400, preRoot,
                        0,
                        0,
                        0,
                        0))
                .addAnimation(new AlphaAnimation(cherat, 0))
                .addAnimation(new TranslateYAnimation(cherat, ViewUtils.dipToPx(80, owner)))
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
                    owner.loadPage(Home.class);
                }).start();
        return true;
    }

    public boolean isHost() {
        return host;
    }

    @Override
    public void applyInsets(Insets insets) {
        //ignore
    }

    @Override
    public void destroy() {
        super.destroy();
        preRoot.setPadding(0,0,0,0);
        root.setCornerRadius(0);
    }

    @Override
    public void applyStyle(Style style) {
        preRoot.setBackgroundColor(style.getBackgroundPrimary());
        table.setBackgroundColor(style.getBackgroundTertiary());
        root.setBorderColor(style.getBackgroundTertiary());
        leftInStock.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }

}
