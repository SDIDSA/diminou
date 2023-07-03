package org.luke.diminou.app.pages.game;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ColorAnimation;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.padding.UnifiedPaddingAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.controls.image.AnimatedColorIcon;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.shape.Rectangle;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.layout.linear.LinearBox;
import org.luke.diminou.abs.local.SocketConnection;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Threaded;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.app.pages.settings.Timer;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PieceHolder extends FrameLayout implements Styleable {
    private final App owner;
    private final ArrayList<Piece> pieces;
    private final HashMap<Piece, ColorIcon> piecesDisplay;
    private final ArrayList<ColorIcon> adding;
    private final int size;
    private final boolean mine;
    private final Side side;
    private final int padding;
    private final Game game;
    private final Table gameTable;
    private final LinearBox root;
    private final Player player;
    private final Label name;
    private final Label score;
    private ColorIcon removing = null;
    private Piece selected = null;
    private ParallelAnimation resizing;

    private final Rectangle timer;

    private ObjectConsumer<Float> setTimer = null;

    private Animation yellow, red;

    private final Rect timerClip;
    @SuppressLint("RtlHardcoded")
    public PieceHolder(App owner, Game game, Player player, Side side, boolean mine) {
        super(owner);
        this.owner = owner;
        this.game = game;
        gameTable = game.getTable();
        this.size = mine ? 32 : 18;
        this.mine = mine;
        this.side = side;
        int radius = size / 3;
        padding = size / 3;
        this.player = player;

        root = new LinearBox(owner);
        root.setOrientation(side.isHorizontal() ? LinearBox.HORIZONTAL : LinearBox.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setClipToPadding(false);
        root.setZ(5);
        setClipChildren(false);

        ViewUtils.setPadding(root,
                side == Side.LEFT ? 0 : padding,
                side == Side.TOP ? 0 : padding,
                side == Side.RIGHT ? 0 : padding,
                side == Side.BOTTOM ? 0 : padding,
                owner);
        switch (side) {
            case TOP -> root.setCornerRadiusBottom(radius);
            case BOTTOM -> root.setCornerRadiusTop(radius);
            case RIGHT -> root.setCornerRadiusLeft(radius);
            case LEFT -> root.setCornerRadiusRight(radius);
        }
        int large = size * 7 + 3 + padding * 2;
        ViewGroup.LayoutParams p;
        if (side.isHorizontal()) {
            p = new ViewGroup.LayoutParams(ViewUtils.dipToPx(large, owner), ViewUtils.dipToPx(3 + padding + size * 2, owner));
        } else {
            p = new ViewGroup.LayoutParams(ViewUtils.dipToPx(3 + padding + size * 2, owner), ViewUtils.dipToPx(large, owner));
        }
        root.setLayoutParams(p);
        setLayoutParams(p);


        pieces = new ArrayList<>();
        piecesDisplay = new HashMap<>();
        adding = new ArrayList<>();

        name = new Label(owner, "");
        name.setText(player.getName());
        name.setLayoutParams(new ViewGroup.LayoutParams(500, -2));
        ViewUtils.alignInFrame(name, side.getNameGravity());
        name.setMaxLines(1);

        score = new Label(owner, "");
        score.setText("0");
        ViewUtils.alignInFrame(score, side.getScoreGravity());

        int fullPx = ViewUtils.dipToPx(large, owner);
        int thick = size;
        int thickPx = ViewUtils.dipToPx(thick, owner);
        int timerBy = 0;
        int timerShow = ViewUtils.dipToPx(3, owner);
        timer = new Rectangle(owner);
        timer.setZ(4);
        timer.setRadius(radius);
        timerClip = new Rect();
        timer.setClipBounds(timerClip);
        switch (side) {
            case TOP -> {
                timer.setWidth(large);
                timer.setHeight(thick);
                timer.setTranslationX(-timerBy);
                timer.setTranslationY(timerShow);
                ViewUtils.alignInFrame(timer, Gravity.BOTTOM);
                timerClip.bottom = thickPx;
                timerClip.right = fullPx;
                setTimer = i -> timerClip.left = (int) (timer.getWidth() * (1 - i));
            }
            case LEFT -> {
                timer.setHeight(large);
                timer.setWidth(thick);
                timer.setTranslationY(timerBy);
                timer.setTranslationX(timerShow);
                ViewUtils.alignInFrame(timer, Gravity.RIGHT);
                timerClip.bottom = fullPx;
                timerClip.right = thickPx;
                setTimer = i -> timerClip.bottom = (int) (timer.getHeight() * i);
            }
            case BOTTOM -> {
                timer.setWidth(large);
                timer.setHeight(thick);
                timer.setTranslationX(timerBy);
                timer.setTranslationY(-timerShow);
                ViewUtils.alignInFrame(timer, Gravity.TOP);
                timerClip.bottom = thickPx;
                timerClip.right = fullPx;
                setTimer = i -> timerClip.right = (int) (timer.getWidth() * i);
            }
            case RIGHT -> {
                timer.setHeight(large);
                timer.setWidth(thick);
                timer.setTranslationY(-timerBy);
                timer.setTranslationX(-timerShow);
                ViewUtils.alignInFrame(timer, Gravity.LEFT);
                timerClip.bottom = fullPx;
                timerClip.right = thickPx;
                setTimer = i -> timerClip.top = (int) (timer.getHeight() * (1 - i));
            }
        }

        addView(timer);
        addView(root);
        addView(name);
        addView(score);

        if(side.isVertical()) {
            ViewUtils.setMarginTop(this, owner, 30);
        }

        setEnabled(false);

        applyStyle(owner.getStyle());
    }

    public void cherra(int drawable, int audio) {
        Platform.runLater(() -> {
            AnimatedColorIcon icon = new AnimatedColorIcon(owner, drawable, audio);
            icon.setSize(3 + padding + size * 2);

            Style s = owner.getStyle().get();
            icon.setBackgroundColor(s.getBackgroundPrimary());
            icon.setFill(s.getTextNormal());
            icon.setCornerRadius(padding);
            icon.setAlpha(0f);

            icon.setZ(2);

            addView(icon);

            icon.start();

            ViewUtils.alignInFrame(icon, side.getScoreGravity());
            int add = ViewUtils.dipToPx(7, owner);
            ParallelAnimation show = new ParallelAnimation(300)
                    .addAnimation(new AlphaAnimation(icon, 1))
                    .setInterpolator(Interpolator.EASE_OUT);
            Animation hide = new ParallelAnimation(300)
                    .addAnimation(new TranslateYAnimation(icon, 0))
                    .addAnimation(new TranslateXAnimation(icon, 0))
                    .addAnimation(new AlphaAnimation(icon, 0))
                    .setInterpolator(Interpolator.EASE_OUT);

            hide.setOnFinished(() -> removeView(icon));

            icon.setOnFinished(() -> {
                Platform.runAfter(hide::start, 300);
            });
            switch (side) {
                case TOP -> {
                    show.addAnimation(new TranslateYAnimation(icon, icon.height() + add));
                }
                case BOTTOM -> show.addAnimation(new TranslateYAnimation(icon, - icon.height() - add));
                case LEFT -> {
                    show.addAnimation(new TranslateXAnimation(icon, icon.width() + add));
                }
                case RIGHT -> {
                    show.addAnimation(new TranslateXAnimation(icon, - icon.width() - add));
                }
            }

            show.start();
        });
    }

    public int getDisplayedCount() {
        return piecesDisplay.size();
    }

    public void select(Piece p) {
        if(gameTable.isPlaying() != null) return;
        if (selected == p) {
            deselect(p);
            selected = null;
            return;
        }

        selected = p;
        ColorIcon piece = piecesDisplay.get(p);

        gameTable.getPossiblePlays(p, this::applyMove);

        if (piece != null) {
            ParallelAnimation sa = new ParallelAnimation(200)
                    .addAnimation(new AlphaAnimation(piece, 1))
                    .addAnimation(new ScaleXYAnimation(piece, Math.max(1, (float) ViewUtils.dipToPx(size, owner) / (side.isHorizontal() ? piece.getWidth() : piece.getHeight()))))
                    .setInterpolator(Interpolator.EASE_OUT);

            switch (side) {
                case TOP ->
                        sa.addAnimation(new TranslateYAnimation(piece, ViewUtils.dipToPx(size / 2, owner)));
                case BOTTOM ->
                        sa.addAnimation(new TranslateYAnimation(piece, -ViewUtils.dipToPx(size / 2, owner)));
                case RIGHT ->
                        sa.addAnimation(new TranslateXAnimation(piece, -ViewUtils.dipToPx(size / 2, owner)));
                case LEFT ->
                        sa.addAnimation(new TranslateXAnimation(piece, ViewUtils.dipToPx(size / 2, owner)));
            }

            for (ColorIcon other : piecesDisplay.values()) {
                if (other != piece) {
                    sa.addAnimation(new AlphaAnimation(other, .5f));
                    sa.addAnimation(new TranslateYAnimation(other, 0f));
                    sa.addAnimation(new TranslateXAnimation(other, 0f));
                    sa.addAnimation(new ScaleXYAnimation(other, 1));
                }
            }

            sa.start();
        }
    }

    private void applyMove(Move m) {
        play(m);
        Platform.runAfter(() -> {
            if (pieces.isEmpty()) {
                game.emitWin(player);
                if (game.isHost()) return;
            }
            game.nextTurn(this);
        }, 500);
        try {
            JSONObject obj = new JSONObject();
            obj.put("player", player.serialize());
            obj.put("move", m.serialize());

            if (game.isHost()) {
                List<SocketConnection> sockets = owner.getTypedData("sockets");
                sockets.forEach(socket -> socket.emit("move", obj));
            } else {
                SocketConnection socket = owner.getTypedData("socket");
                socket.emit("move", obj);
            }
        } catch (Exception x) {
            ErrorHandler.handle(x, "sending move");
        }
    }

    public void play(Move move) {
        gameTable.play(move, piecesDisplay.get(move.getPlayed().getPiece()), player);
        remove(move.getPlayed().getPiece());
        gameTable.removePossiblePlays();
    }

    public void deselect(Piece p) {
        gameTable.removePossiblePlays();
        ColorIcon piece = piecesDisplay.get(p);
        if (piece != null) {
            ParallelAnimation sa = new ParallelAnimation(200)
                    .addAnimation(new TranslateYAnimation(piece, 0))
                    .addAnimation(new TranslateXAnimation(piece, 0))
                    .addAnimation(new ScaleXYAnimation(piece, 1))
                    .setInterpolator(Interpolator.EASE_OUT);

            for (ColorIcon other : piecesDisplay.values()) {
                if (other != piece) {
                    sa.addAnimation(new AlphaAnimation(other, 1));
                }
            }
            sa.start();
        }
    }

    public void deselect() {
        if (selected != null) deselect(selected);
        selected = null;
    }

    public boolean add(Piece... toAdd) {
        if (!adding.isEmpty()) return false;
        ArrayList<Piece> rToAdd = new ArrayList<>(Arrays.asList(toAdd));
        rToAdd.removeIf(p -> pieces.contains(p) || p == Piece.HIDDEN || gameTable.contains(p));
        if (rToAdd.isEmpty()) return false;
        pieces.addAll(rToAdd);
        resizePieces();

        Threaded.runBack(() -> {
            for (Piece p : rToAdd) {
                ColorIcon piece = (mine ? p : Piece.HIDDEN).getImage(owner, size, side.getOrientation().other());
                if (side.isHorizontal())
                    piece.setWidth(0);
                else
                    piece.setHeight(0);
                piece.setAlpha(0f);
                switch (side) {
                    case TOP -> piece.setTranslationY(-ViewUtils.dipToPx(size, owner));
                    case BOTTOM -> piece.setTranslationY(ViewUtils.dipToPx(size, owner));
                    case RIGHT -> piece.setTranslationX(ViewUtils.dipToPx(size, owner));
                    case LEFT -> piece.setTranslationX(-ViewUtils.dipToPx(size, owner));
                }
                piece.setElevation(-1);
                if (mine) Platform.runLater(() -> {
                    piece.setOnDoubleClick(() -> {
                        deselect();
                        if (isEnabled() && gameTable.isPlaying() == null) {
                            Player winner = owner.getTypedData("winner");
                            if ((winner != null && winner.equals(player)) ||
                                    gameTable.getPossiblePlays(pieces).contains(p)) {
                                List<Move> moves = gameTable.getPossiblePlays(p, null);
                                if (moves.size() == 1 || p.isDouble() || pieces.size() == 1 || gameTable.isMirror()) {
                                    applyMove(moves.get(0));
                                }
                            }
                        }
                    });
                    piece.setOnClick(() -> {
                        if (isEnabled()) {
                            Player winner = owner.getTypedData("winner");
                            if ((winner != null && winner.equals(player)) ||
                                    gameTable.getPossiblePlays(pieces).contains(p)) {
                                select(p);
                            }
                        }
                    });
                });
                Platform.runLater(() -> root.addView(piece, side == Side.TOP || side == Side.RIGHT ? 0 : root.getChildCount()));
                adding.add(piece);
                piecesDisplay.put(p, piece);
                game.updateStock();

                //if(side == Side.BOTTOM)
                owner.playSound(PlaySound.SOUND_16.getRes());
                new ParallelAnimation(300)
                        .addAnimation(new ValueAnimation(0, calcPieceSize()) {
                            @Override
                            public void updateValue(float v) {
                                if (side.isHorizontal())
                                    piece.setWidth(v);
                                else
                                    piece.setHeight(v);
                            }
                        })
                        .addAnimation(new TranslateYAnimation(piece, 0))
                        .addAnimation(new TranslateXAnimation(piece, 0))
                        .addAnimation(new AlphaAnimation(piece, 1))
                        .addAnimation(new UnifiedPaddingAnimation(piece, ViewUtils.dipToPx(1.5f, owner)))
                        .setOnFinished(() -> adding.remove(piece))
                        .setInterpolator(Interpolator.EASE_OUT)
                        .start();

                Threaded.sleep(100);
            }
        });
        return true;
    }

    public void remove(Piece piece) {
        ColorIcon disp = piecesDisplay.get(piece);
        if (removing == disp || disp == null) return;

        removing = disp;

        pieces.remove(piece);

        resizePieces();

        new ParallelAnimation(300)
                .addAnimation(new ValueAnimation(ViewUtils.pxToDip(side.isHorizontal() ? disp.getWidth() : disp.getHeight(), owner), 0) {
                    @Override
                    public void updateValue(float v) {
                        if (side.isHorizontal()) disp.setWidth(v);
                        else disp.setHeight(v);
                    }
                })
                .addAnimation(new AlphaAnimation(disp, 0))
                .addAnimation(new UnifiedPaddingAnimation(disp, 0))
                .setInterpolator(Interpolator.EASE_OUT)
                .setOnFinished(() -> {
                    removing = null;
                    piecesDisplay.remove(piece);
                    root.removeView(disp);
                }).start();
    }

    public void resizePieces() {
        if (pieces.isEmpty()) return;

        float pieceSize = calcPieceSize();

        ColorIcon ref = null;

        for (ColorIcon p : piecesDisplay.values()) {
            if (p != removing) {
                ref = p;
                break;
            }
        }

        if (ref == null) return;

        if (resizing != null) resizing.stop();
        resizing = new ParallelAnimation(300)
                .addAnimation(new ValueAnimation(ViewUtils.pxToDip(side.isHorizontal() ? ref.getWidth() : ref.getHeight(), owner), pieceSize) {
                    @Override
                    public void updateValue(float v) {
                        try {
                            piecesDisplay.values().forEach(p -> {
                                if (p != removing && !adding.contains(p))
                                    if (side.isHorizontal()) p.setWidth(v);
                                    else p.setHeight(v);
                            });
                        } catch (ConcurrentModificationException x) {
                            ErrorHandler.handle(x, "resizing pieces");
                        }
                    }
                })
                .addAnimation(new ValueAnimation(ref.getAlpha(), 1) {
                    @Override
                    public void updateValue(float v) {
                        try {
                            piecesDisplay.values().forEach(p -> {
                                if (p != removing && !adding.contains(p)) p.setAlpha(v);
                            });
                        } catch (ConcurrentModificationException x) {
                            ErrorHandler.handle(x, "resizing pieces");
                        }
                    }
                })
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    public int sum() {
        return pieces.stream().mapToInt(Piece::sum).sum();
    }

    private float calcPieceSize() {
        int size = (side.isHorizontal() ? root.getWidth() : root.getHeight()) - (side == Side.RIGHT ? root.getPaddingLeft() : root.getPaddingRight()) * 2;
        float sizeDp = ViewUtils.pxToDip(size, owner);

        return sizeDp / pieces.size();
    }

    public Animation setup() {
        pieces.clear();
        piecesDisplay.clear();
        root.removeAllViews();

        name.setAlpha(0);
        score.setAlpha(0);
        score.setElevation(-1);

        score.setText(String.valueOf(game.getScoreOf(player)));

        ParallelAnimation res = new ParallelAnimation();

        switch (side) {
            case TOP -> {
                setTranslationY(-ViewUtils.dipToPx(padding + 3 + size * 2, owner));
                res.addAnimation(new TranslateYAnimation(this, ViewUtils.dipToPx(-1.5, owner)));
            }
            case BOTTOM -> {
                setTranslationY(ViewUtils.dipToPx(padding + 3 + size * 2, owner));
                res.addAnimation(new TranslateYAnimation(this, ViewUtils.dipToPx(1.5, owner)));
            }
            case RIGHT -> {
                setTranslationX(ViewUtils.dipToPx(padding + 3 + size * 2, owner));
                res.addAnimation(new TranslateXAnimation(this, ViewUtils.dipToPx(1.5, owner)));
            }
            case LEFT -> {
                setTranslationX(-ViewUtils.dipToPx(padding + 3 + size * 2, owner));
                res.addAnimation(new TranslateXAnimation(this, ViewUtils.dipToPx(-1.5, owner)));
            }
        }

        removing = null;


        res.addAnimation(new ColorAnimation(owner.getStyle().get().getBackgroundTertiary(), owner.getStyle().get().getTextMuted()) {
                    @Override
                    public void updateValue(int color) {
                        root.setBorderColor(color);
                    }
                })
                .setOnFinished(() -> {
                    side.namePos(name);
                    side.scorePos(score);
                    new ParallelAnimation(400)
                            .addAnimation(new AlphaAnimation(name, 0, 1))
                            .addAnimation(new ScaleXYAnimation(name, 0.5f, 1))
                            .addAnimation(new AlphaAnimation(score, 0, 1))
                            .addAnimation(new ScaleXYAnimation(score, 0.5f, 1))
                            .setInterpolator(Interpolator.EASE_OUT)
                            .start();
                    Platform.runLater(() -> {
                        if (game.isHost()) {
                            add(game.deal().toArray(new Piece[0]));
                        } else {
                            SocketConnection socket = owner.getTypedData("socket");
                            socket.emit("deal", player.serialize());
                        }
                    });
                });

        return res;
    }

    public ParallelAnimation hideAnimation() {
        ParallelAnimation res = new ParallelAnimation();

        switch (side) {
            case TOP ->
                    res.addAnimation(new TranslateYAnimation(this, -ViewUtils.dipToPx(padding + 3 + size * 2, owner)));
            case BOTTOM ->
                    res.addAnimation(new TranslateYAnimation(this, ViewUtils.dipToPx(padding + 3 + size * 2, owner)));
            case RIGHT ->
                    res.addAnimation(new TranslateXAnimation(this, ViewUtils.dipToPx(padding + 3 + size * 2, owner)));
            case LEFT ->
                    res.addAnimation(new TranslateXAnimation(this, -ViewUtils.dipToPx(padding + 3 + size * 2, owner)));
        }

        res.addAnimation(new ColorAnimation(owner.getStyle().get().getTextMuted(), owner.getStyle().get().getBackgroundTertiary()) {
            @Override
            public void updateValue(int color) {
                root.setBorderColor(color);
            }
        });

        res.addAnimation(new AlphaAnimation(name, 0));
        return res;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void setEnabled(boolean enabled) {
        Platform.runLater(() -> {
            super.setEnabled(enabled);
            name.setFill(App.adjustAlpha(owner.getStyle().get().getTextNormal(), enabled ? 1 : .5f));
            score.setFill(App.adjustAlpha(owner.getStyle().get().getTextNormal(), enabled ? 1 : .5f));

            new ColorAnimation(300, root.getForegroundColor(), App.adjustAlpha(owner.getStyle().get().getBackgroundPrimary(), enabled ? 0 : .6f)) {
                @Override
                public void updateValue(int color) {
                    root.setForeground(color);
                }
            }.setOnFinished(this::deselect).setInterpolator(Interpolator.EASE_OUT)
                    .start();

            if(enabled) {
                timer.setFill(owner.getStyle().get().getTextPositive());
                int time = Objects.requireNonNull(Timer.byText(owner.getString("timer"))).getDuration();
                Threaded.runBack(() -> {
                    long start = System.currentTimeMillis();
                    boolean yellow = false;
                    boolean red = false;
                    while(isEnabled() && isAttachedToWindow()) {
                        long passed = System.currentTimeMillis() - start;
                        float factor = Math.max(0, 1 - passed / (time * 1000f));

                        if(factor == 0 && player.isSelf(game.isHost())) {
                            Platform.runLater(() -> {
                                if(gameTable.isPlaying() != null) return;
                                Piece p = gameTable.getPossiblePlays(pieces).get(0);
                                Move m = gameTable.getPossiblePlays(p, null).get(0);
                                applyMove(m);
                            });
                            break;
                        }

                        if(factor < .5 && !yellow) {
                            yellow = true;
                            this.yellow.start();
                        }

                        if(factor < .25 && !red) {
                            red = true;
                            this.red.start();
                        }

                        Platform.runLater(() -> {
                            try {
                                setTimer.accept(factor);
                                timer.setClipBounds(timerClip);
                            } catch (Exception e) {
                                //IGNORE
                            }
                        });
                        Threaded.sleep(20);
                    }
                    Platform.runLater(() -> {
                        try {
                            setTimer.accept(0f);
                            timer.setClipBounds(timerClip);
                        } catch (Exception e) {
                            //IGNORE
                        }
                    });
                });
            }
        });
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public Side getSide() {
        return side;
    }

    @Override
    public void applyStyle(Style style) {
        root.setBackground(style.getBackgroundPrimary());
        root.setBorderColor(style.getBackgroundTertiary());
        name.setFill(style.getTextNormal());
        name.setFill(style.getTextNormal());

        timer.setFill(style.getTextNormal());

        yellow = new ColorAnimation(400, style.getTextPositive(), style.getTextWarning()) {
            @Override
            public void updateValue(int color) {
                timer.setFill(color);
            }
        }.setInterpolator(Interpolator.EASE_OUT);

        red = new ColorAnimation(400, style.getTextWarning(), style.getTextDanger()) {
            @Override
            public void updateValue(int color) {
                timer.setFill(color);
            }
        }.setInterpolator(Interpolator.EASE_OUT);
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
