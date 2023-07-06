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
import org.luke.diminou.abs.animation.view.padding.PaddingAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
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
import org.luke.diminou.app.pages.game.piece.Move;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.game.piece.Stock;
import org.luke.diminou.app.pages.game.player.PieceHolder;
import org.luke.diminou.app.pages.game.player.Player;
import org.luke.diminou.app.pages.game.player.Side;
import org.luke.diminou.app.pages.game.score.ScoreBoard;
import org.luke.diminou.app.pages.game.table.Table;
import org.luke.diminou.app.pages.home.Home;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Game extends Page {
    private final boolean host;
    private final VBox root;
    private final ArrayList<PieceHolder> holders;

    private final Table table;

    private final HBox center;
    private final Cherrat cherat;
    private Stock stock;

    private final ScoreBoard scoreBoard;

    private final Label leftInStock;

    private final FrameLayout preRoot;

    private final Rectangle background;

    private final TurnManager turn;

    public Game(App owner) {
        super(owner);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);

        host = owner.isHost();

        root = new VBox(owner);
        root.setGravity(Gravity.BOTTOM | Gravity.CENTER);
        root.setClipChildren(false);

        table = new Table(owner);

        holders = new ArrayList<>();

        center = new HBox(owner);
        center.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        center.setClipChildren(false);

        cherat = new Cherrat(owner);

        root.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        root.addView(center);
        root.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        root.addView(cherat);

        scoreBoard = new ScoreBoard(owner);
        scoreBoard.addOnShowing(() -> owner.playMenuSound(R.raw.end));
        scoreBoard.addOnShowing(() -> {
            for (PieceHolder holder : holders) {
                holder.setEnabled(false);
            }
        });

        preRoot = new FrameLayout(owner);
        background = new Rectangle(owner);
        background.setRadius(10);
        background.setLayoutParams(new LayoutParams(-1, -1));
        preRoot.addView(background);
        preRoot.addView(table);
        preRoot.addView(root);
        preRoot.setClipChildren(false);

        leftInStock = new Label(owner, "stock");
        addView(preRoot);
        addView(leftInStock);

        turn = new TurnManager(owner, this);

        ViewUtils.alignInFrame(leftInStock, Gravity.TOP | Gravity.END);

        applyStyle(owner.getStyle());
    }

    public Cherrat getCherrat() {
        return cherat;
    }

    public PassInit getPassInit() {
        return cherat.getPassInit();
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

    public ArrayList<PieceHolder> getHolders() {
        return holders;
    }

    public Stock getStock() {
        return stock;
    }

    public ScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    public int getScoreOf(Player player) {
        ConcurrentHashMap<Player, Integer> score = owner.getScore();

        Integer i = score.get(player);
        if(i != null)
            return i;

        score.put(player, 0);
        return 0;
    }

    private void addScoreOf(Player player, int add) {
        ConcurrentHashMap<Player, Integer> score = owner.getScore();

        int oldScore = getScoreOf(player);

        score.put(player, oldScore + add);
    }

    public void setScoreOf(Player player, int val) {
        ConcurrentHashMap<Player, Integer> score = owner.getScore();

        score.put(player, val);
    }

    public int index(Player player) {
        List<Player> players = owner.getPlayers();
        for(int i = 0; i < players.size(); i++) {
            if(players.get(i).equals(player)) {
                return i;
            }
        }
        return -1;
    }

    public Player otherPlayer(Player player) {
        List<Player> players = owner.getPlayers();
        return players.get((index(player) + 2) % players.size());
    }

    private int scoreToAdd(Player winner) {
        int sum = 0;

        ArrayList<Player> winners = new ArrayList<>();
        winners.add(winner);

        if(owner.getFourMode() == FourMode.TEAM_MODE) {
            winners.add(otherPlayer(winner));
        }

        for(PieceHolder holder : holders) {
            if(!winners.contains(holder.getPlayer())) {
                sum += holder.sum();
            }
        }
        return sum;
    }

    public void pass(PieceHolder holder) {
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

    public TurnManager getTurn() {
        return turn;
    }

    public Table getTable() {
        return table;
    }

    public List<Piece> deal() {
        return stock.deal();
    }

    public Player checkForWinner() {
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

    private JSONArray scoreBoard(Player winner) {
        JSONArray arr = new JSONArray();
        try {
            List<Player> players = owner.getPlayers();
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
        ended = false;
        root.setBackground(Color.TRANSPARENT);
        leftInStock.setAlpha(0);
        stock = new Stock();
        table.clear();
        holders.forEach(h -> {
            root.removeView(h);
            center.removeView(h);
        });
        holders.clear();

        cherat.setAlpha(0);
        cherat.setTranslationY(ViewUtils.dipToPx(80, owner));

        List<Player> players = owner.getPlayers();

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
        root.setCornerRadius(10);

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

                    owner.getSockets().forEach(s -> s.emit("move", data));
                    Platform.runLater(() -> holder.play(move));
                });
                socket.on("khabet", data -> {
                    owner.getSockets().forEach(s -> s.emit("khabet", data));
                    Player player = Player.deserialize(new JSONObject(data));
                    getForPlayer(player).cherra(R.drawable.khabet, R.raw.khabet);
                });
                socket.on("saket", data -> {
                    owner.getSockets().forEach(s -> s.emit("saket", data));
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
                    turn.turn(p);
                });
            });
        } else {
            SocketConnection socket = owner.getSocket();
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
                turn.turn(p);
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
                        owner.toast("pass");
                    owner.playGameSound(R.raw.pass);
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

            Platform.runAfter(turn::init, 300);
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
        endGame();
        return true;
    }

    private boolean ended = false;

    public boolean isEnded() {
        return ended;
    }

    private void endGame() {
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
        ended = true;
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
        background.setFill(style.getBackgroundTertiary());
        root.setBorderColor(style.getBackgroundTertiary());
        leftInStock.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }

}
