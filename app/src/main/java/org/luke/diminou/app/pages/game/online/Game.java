package org.luke.diminou.app.pages.game.online;

import android.graphics.Color;
import android.view.Gravity;

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
import org.luke.diminou.abs.components.layout.StackPane;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.app.pages.game.pause.GamePause;
import org.luke.diminou.app.pages.game.offline.score.OfflineScoreBoard;
import org.luke.diminou.app.pages.game.online.player.PieceHolder;
import org.luke.diminou.app.pages.game.online.table.Table;
import org.luke.diminou.app.pages.game.piece.Move;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.game.player.Side;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.data.beans.Room;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;

public class Game extends Page {
    private final VBox root;
    private final ArrayList<PieceHolder> holders;

    private final Table table;

    private final HBox center;
    private final Cherrat cherat;

    private final TurnManager turn;

    private final OfflineScoreBoard scoreBoard;

    private final GamePause gamePause;

    private final Label leftInStock;

    private final StackPane preRoot;

    private final Rectangle background;

    private final ColoredIcon menu;
    public Game(App owner) {
        super(owner);
        setLayoutDirection(LAYOUT_DIRECTION_LTR);

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

        scoreBoard = new OfflineScoreBoard(owner);
        scoreBoard.addOnShowing(() -> owner.playMenuSound(R.raw.end));
        scoreBoard.addOnShowing(() -> {
            for (PieceHolder holder : holders) {
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

        turn = new TurnManager(owner, this);

        gamePause = new GamePause(owner);

        menu.setOnClick(this::onBack);

        ViewUtils.alignInFrame(leftInStock, Gravity.TOP | Gravity.END);

        applyStyle(owner.getStyle());
    }

    public Cherrat getCherrat() {
        return cherat;
    }

    public PassInit getPassInit() {
        return cherat.getPassInit();
    }

    public PieceHolder getForPlayer(int player) {
        for (PieceHolder holder : holders) {
            if (holder.getPlayer() == player) return holder;
        }
        ErrorHandler.handle(new IllegalStateException("player not found"), "getting pieceHolder for player " + player);
        return null;
    }

    public synchronized void updateStock(int val) {
        Platform.runLater(() -> leftInStock.addParam(0, String.valueOf(val)));
    }

    public ArrayList<PieceHolder> getHolders() {
        return holders;
    }

    public OfflineScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    public int getScoreOf(int player) {
        Room room = owner.getRoom();
        return 0;
    }

    public int index(int player) {
        return owner.getRoom().indexOf(player);
    }

    public int otherPlayer(int player) {
        Room room = owner.getRoom();
        return room.playerAt((index(player) + 2) % 4);
    }

    public TurnManager getTurn() {
        return turn;
    }

    public Table getTable() {
        return table;
    }

    public void victory(JSONArray data) {
        Platform.runAfter(scoreBoard::show, 200);
    }

    private void on(String event, ObjectConsumer<JSONObject> handler) {
        owner.getMainSocket().off(event);
        owner.getMainSocket().on(event,
                data -> Platform.runLater(() -> {
                    try {
                        handler.accept(new JSONObject(data[0].toString()));
                    } catch (Exception e) {
                        ErrorHandler.handle(e, "handling socket event " + event);
                    }
                }));
    }

    @Override
    public void setup() {
        super.setup();
        ended = false;
        root.setBackground(Color.TRANSPARENT);
        leftInStock.setAlpha(0);
        table.clear();
        holders.forEach(h -> {
            root.removeView(h);
            center.removeView(h);
        });
        holders.clear();

        cherat.setAlpha(0);
        cherat.setTranslationY(ViewUtils.dipToPx(80, owner));

        Room room = owner.getRoom();

        int index = 0;
        for (int i = 0; i < room.count(); i++) {
            int ati = room.playerAt(i);

            if (ati == owner.getUser().getId())
                index = i;
        }

        int pos = 0;
        while (pos < room.count()) {
            int p = room.playerAt(index);

            switch (pos) {
                case 0 -> {
                    PieceHolder ph = new PieceHolder(owner, this, p, Side.BOTTOM, true);
                    root.addView(ph);
                    holders.add(ph);
                }
                case 1 -> {
                    if (room.count() == 2) {
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

            index = (index + 1) % room.count();
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
                        holders.stream().map(PieceHolder::setup).toArray(Animation[]::new))
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

        leftInStock.addParam(0, String.valueOf(28));

        //TODO error

        on("deal", data -> {
            updateStock(data.getInt("stock"));
            int player = data.getInt("player");
            JSONArray arr = data.getJSONArray("toAdd");
            Piece[] pieces = new Piece[arr.length()];
            for(int i = 0; i < arr.length(); i++) {
                pieces[i] = Piece.valueOf(arr.getString(i));
            }

            getForPlayer(player).add(pieces);
        });

        on("turn", data -> {
           int turn = data.getInt("turn");
           this.turn.turn(turn);
        });

        //TODO saket

        //TODO khabet

        on("play", data -> {
            int player = data.getInt("player");
            Move move = Move.deserialize(data.getJSONObject("move"));
            getForPlayer(player).play(move);
        });

        //TODO winner

        on("pass", data -> {
            int player = data.getInt("player");
            PieceHolder passed = getForPlayer(player);
            if(passed.isSelf()) {
                owner.toast("pass");
            }
            owner.playGameSound(R.raw.pass);
        });

        //TODO skip

        //TODO end

        //TODO leave
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
        gamePause.setOnExit(() -> {
            //TODO send leave

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
                    owner.loadPage(Home.class);
                }).start();
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
