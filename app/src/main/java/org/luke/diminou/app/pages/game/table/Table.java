package org.luke.diminou.app.pages.game.table;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.PivotXAnimation;
import org.luke.diminou.abs.animation.view.scale.PivotYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.app.pages.game.Game;
import org.luke.diminou.app.pages.game.PlaySound;
import org.luke.diminou.app.pages.game.piece.Move;
import org.luke.diminou.app.pages.game.piece.Piece;
import org.luke.diminou.app.pages.game.piece.PieceRotation;
import org.luke.diminou.app.pages.game.piece.PlayedPiece;
import org.luke.diminou.app.pages.game.player.PieceHolder;
import org.luke.diminou.app.pages.game.player.Player;
import org.luke.diminou.app.pages.game.player.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Table extends FrameLayout {
    private static final int SPACING = 2;
    private static final int CENTER_SIZE = 9;
    private final App owner;
    private final ArrayList<PlayedPiece> onTable = new ArrayList<>();
    private final ArrayList<PlayedPiece> center = new ArrayList<>();
    private final ArrayList<PlayedPiece> left = new ArrayList<>();
    private final ArrayList<PlayedPiece> left_down = new ArrayList<>();
    private final ArrayList<PlayedPiece> right = new ArrayList<>();
    private final ArrayList<PlayedPiece> right_up = new ArrayList<>();
    public Table(App owner) {
        super(owner);
        this.owner = owner;

        setLayoutParams(new LayoutParams(owner.getScreenWidth() * 3, owner.getScreenHeight() * 3));
        ViewUtils.alignInFrame(this, Gravity.CENTER);

        setOnTouchListener((v, e) -> {
            float rawX = e.getRawX();
            float rawY = e.getRawY();

            Rect bounds = tableBounds();
            if(rawX < bounds.left || rawX > bounds.right || rawY < bounds.top || rawY > bounds.bottom) {
                return false;
            }

            float x = e.getX() - getWidth() / 2f;
            float y = e.getY() - getHeight() / 2f;

            double minDistance = ViewUtils.dipToPx(150, owner);
            ColorIcon closest = null;
            for(ColorIcon possible : possiblePlays) {
                float px = possible.getTranslationX();
                float py = possible.getTranslationY();

                float dx = px - x;
                float dy = py - y;
                double unscaled = Math.sqrt(dx * dx + dy * dy);
                double distance = unscaled * getScaleY();

                if(distance < minDistance) {
                    closest = possible;
                    minDistance = distance;
                }
            }
            if(closest != null) {
                closest.fire();
                performClick();
            }
            return false;
        });
    }

    public boolean contains(Piece p) {
        for(PlayedPiece pp : onTable) {
            if(pp.getPiece() == p) return true;
        }
        return false;
    }

    public boolean isMirror() {
        return getTopEnd() == getBottomEnd();
    }

    public List<Piece> getPossiblePlays(List<Piece> pieces) {
        if(onTable.isEmpty()) {
            ArrayList<Piece> res = new ArrayList<>();
            List<Piece> priority = Piece.priority();
            for(Piece piece : priority) {
                if(pieces.contains(piece)) {
                    res.add(piece);
                    return res;
                }
            }
            return res;
        }else {
            int topEnd = getTopEnd();
            int bottomEnd = getBottomEnd();
            return pieces.stream().filter(
                    p ->
                            p.has(topEnd) || p.has(bottomEnd)
            ).collect(Collectors.toList());
        }
    }

    public ArrayList<Move> getPossiblePlays(Piece p, ObjectConsumer<Move> onMove) {
        ArrayList<Move> res = new ArrayList<>();

        if(playing != null) {
            return res;
        }
        if(onTable.isEmpty()) {
            res.add(new Move(new PlayedPiece(p, PieceRotation.BOTH), Side.TOP));
        }else {
            int topEnd = getTopEnd();
            int bottomEnd = getBottomEnd();
            if(p.isDouble()) {
                if(p.getN0() == topEnd) {
                    res.add(new Move(new PlayedPiece(p, PieceRotation.NOT_FLIPPED), Side.TOP));
                }

                if(p.getN0() == bottomEnd) {
                    res.add(new Move(new PlayedPiece(p, PieceRotation.NOT_FLIPPED), Side.BOTTOM));
                }
            } else {
                if(p.getN1() == topEnd) {
                    res.add(new Move(new PlayedPiece(p, PieceRotation.NOT_FLIPPED), Side.TOP));
                }

                if(p.getN0() == topEnd) {
                    res.add(new Move(new PlayedPiece(p, PieceRotation.FLIPPED), Side.TOP));
                }

                if(p.getN1() == bottomEnd) {
                    res.add(new Move(new PlayedPiece(p, PieceRotation.NOT_FLIPPED), Side.BOTTOM));
                }

                if(p.getN0() == bottomEnd) {
                    res.add(new Move(new PlayedPiece(p, PieceRotation.FLIPPED), Side.BOTTOM));
                }
            }
        }


        removePossiblePlays();
        if(onMove != null)
            res.forEach(move -> displayOption(move, onMove));
        return res;
    }

    private final ArrayList<ColorIcon> possiblePlays = new ArrayList<>();

    private ColorIcon getPlayPositionCenter(Move move) {
        ColorIcon icon = move.getPlayed().getPiece().getImage(owner, 32, move.getPlayed().getPiece().isDouble() ? Orientation.HORIZONTAL : Orientation.VERTICAL);

        if((move.getSide() == Side.TOP && move.getPlayed().isFlipped()) ||
                (move.getSide() == Side.BOTTOM &&  !move.getPlayed().isFlipped())) {
            icon.setRotation(180);
        }

        if(!onTable.isEmpty()) {
            if(move.getSide() == Side.TOP) {
                icon.setTranslationY(getTopEndY() - ViewUtils.dipToPx(move.getPlayed().getPiece().isDouble() ? (16 + SPACING) : (32 + SPACING), owner));
            }else {
                icon.setTranslationY(getBottomEndY() + ViewUtils.dipToPx(move.getPlayed().getPiece().isDouble() ? (16 + SPACING) : (32 + SPACING), owner));
            }
        }

        ViewUtils.alignInFrame(icon, Gravity.CENTER);
        addView(icon);
        return icon;
    }

    private ColorIcon getPlayPositionLeft(Move move) {
        ColorIcon icon = move.getPlayed().getPiece().getImage(owner, 32,
                move.getPlayed().getPiece().isDouble() && !left.isEmpty()
                        ? Orientation.VERTICAL : Orientation.HORIZONTAL);

        if(!move.getPlayed().isFlipped()) {
            icon.setRotation(180);
        }

        icon.setTranslationY(getTopEndY());
        icon.setTranslationX(getTopEndX() - ViewUtils.dipToPx(move.getPlayed().getPiece().isDouble() && !left.isEmpty() ? (16 + SPACING) : (32 + SPACING), owner));

        ViewUtils.alignInFrame(icon, Gravity.CENTER);
        addView(icon);
        return icon;
    }

    private ColorIcon getPlayPositionRight(Move move) {
        ColorIcon icon = move.getPlayed().getPiece().getImage(owner, 32,
                move.getPlayed().getPiece().isDouble() && !right.isEmpty()
                        ? Orientation.VERTICAL : Orientation.HORIZONTAL);

        if(move.getPlayed().isFlipped()) {
            icon.setRotation(180);
        }

        icon.setTranslationY(getBottomEndY());
        icon.setTranslationX(getBottomEndX() + ViewUtils.dipToPx(move.getPlayed().getPiece().isDouble() && !right.isEmpty() ? (16 + SPACING) : (32 + SPACING), owner));

        ViewUtils.alignInFrame(icon, Gravity.CENTER);
        addView(icon);
        return icon;
    }

    private ColorIcon getPlayPositionLeftDown(Move move) {
        ColorIcon icon = move.getPlayed().getPiece().getImage(owner, 32,
                move.getPlayed().getPiece().isDouble() && !left_down.isEmpty() ? Orientation.HORIZONTAL : Orientation.VERTICAL);


        if(!move.getPlayed().isFlipped()) {
            icon.setRotation(180);
        }

        icon.setTranslationY(getTopEndY() + ViewUtils.dipToPx(move.getPlayed().getPiece().isDouble() && !left_down.isEmpty() ? (16 + SPACING) : (32 + SPACING), owner));
        icon.setTranslationX(getTopEndX() + ViewUtils.dipToPx(16, owner));

        ViewUtils.alignInFrame(icon, Gravity.CENTER);
        addView(icon);
        return icon;
    }

    private ColorIcon getPlayPositionRightUp(Move move) {
        ColorIcon icon = move.getPlayed().getPiece().getImage(owner, 32,
                move.getPlayed().getPiece().isDouble() && !right_up.isEmpty() ? Orientation.HORIZONTAL : Orientation.VERTICAL);


        if(move.getPlayed().isFlipped()) {
            icon.setRotation(180);
        }

        icon.setTranslationY(getBottomEndY() - ViewUtils.dipToPx(move.getPlayed().getPiece().isDouble() && !right_up.isEmpty() ? (16 + SPACING) : (32 + SPACING), owner));
        icon.setTranslationX(getBottomEndX() - ViewUtils.dipToPx(16, owner));

        ViewUtils.alignInFrame(icon, Gravity.CENTER);
        addView(icon);
        return icon;
    }

    private ColorIcon getPlayPosition(Move move) {
        if(center.size() < CENTER_SIZE) {
            return getPlayPositionCenter(move);
        }else {
            if(move.getSide() == Side.TOP) {
                if(left.size() < 2) {
                    return getPlayPositionLeft(move);
                }else {
                    return getPlayPositionLeftDown(move);
                }
            }else {
                if(right.size() < 2) {
                    return getPlayPositionRight(move);
                } else {
                    return getPlayPositionRightUp(move);
                }
            }
        }
    }

    private void displayOption(Move move, ObjectConsumer<Move> onMove) {
        ColorIcon icon = getPlayPosition(move);
        possiblePlays.add(icon);
        icon.setAlpha(.2f);
        icon.setOnClick(() -> {
            try {
                if(onMove != null)
                    onMove.accept(move);
            } catch (Exception e) {
                ErrorHandler.handle(e, "playing piece");
            }
        });
    }

    private volatile Player playing = null;
    public void play(Move move, ColorIcon source, Player player) {
        playing = player;
        owner.putData("winner", null);
        ColorIcon target = getPlayPosition(move);
        target.setAlpha(0f);

        if(move.getSide() == Side.TOP)
            onTable.add(0, move.getPlayed());
        else
            onTable.add(move.getPlayed());

        if(center.size() < CENTER_SIZE) {
            if(move.getSide() == Side.TOP)
                center.add(0, move.getPlayed());
            else
                center.add(move.getPlayed());
        }else if(move.getSide() == Side.TOP) {
            if(left.size() < 2) {
                left.add(0, move.getPlayed());
            }else {
                left_down.add(0, move.getPlayed());
            }
        }else if(move.getSide() == Side.BOTTOM) {
            if(right.size() < 2) {
                right.add(move.getPlayed());
            }else {
                right_up.add(move.getPlayed());
            }
        }

        owner.playSound(PlaySound.random().getRes());

        Platform.runAfter(() -> {
            float oldX = target.getTranslationX();
            float oldY = target.getTranslationY();

            int thisX = getXInParent(target);
            int thisY = getYInParent(target);

            int otherX = getXInParent(source);
            int otherY = getYInParent(source);

            target.setTranslationX(otherX - thisX);
            target.setTranslationY(otherY - thisY);

            ParallelAnimation anim = new ParallelAnimation(400)
                    .addAnimation(new TranslateXAnimation(target, oldX))
                    .addAnimation(new TranslateYAnimation(target, oldY))
                    .addAnimation(new AlphaAnimation(target, 1))
                    .setInterpolator(Interpolator.EASE_OUT)
                    .setOnFinished(this::adjustBoard);
            anim.start();
        }, 50);
    }

    public int count() {
        return onTable.size();
    }

    public void adjustBoard() {
        if(!isAttachedToWindow()) return;

        int top = getMaxTop() + getHeight() / 2;
        int bottom = getMaxBottom() + getHeight() / 2;
        int right = getMaxRight() + getWidth() / 2;
        int left = getMaxLeft() + getWidth() / 2;

        Rect pbounds = new Rect(left, top, right, bottom);

        Rect bounds = tableBounds();

        float height = pbounds.height();
        float width = pbounds.width();

        float maxHeight = bounds.height();
        float maxWidth = bounds.width();

        float factorY = maxHeight / height;
        float factorX = maxWidth / width;

        float factor = Math.min(1, Math.min(factorX, factorY));

        int pcenterX = pbounds.centerX() - getWidth() / 2;
        int pcenterY = pbounds.centerY() - getHeight() / 2;

        int dcx = (bounds.centerX() - owner.getScreenWidth() / 2);
        int dcy = (bounds.centerY() - owner.getScreenHeight() / 2);

        FrameLayout par = ((FrameLayout) getParent());

        int centerX = -pcenterX + dcx - par.getPaddingLeft() / 2 + par.getPaddingRight() / 2;
        int centerY = -pcenterY + dcy - par.getPaddingTop() / 2 + par.getPaddingBottom() / 2;

        ParallelAnimation anim = new ParallelAnimation(400)
                .addAnimation(new ScaleXYAnimation(this, factor))
                .addAnimation(new PivotYAnimation(this, getHeight() / 2f + pcenterY))
                .addAnimation(new PivotXAnimation(this, getWidth() / 2f + pcenterX))
                .addAnimation(new TranslateXAnimation(this, centerX))
                .addAnimation(new TranslateYAnimation(this, centerY))
                .setOnFinished(() -> playing = null)
                .setInterpolator(Interpolator.EASE_OUT);
        anim.start();

    }

    public Player isPlaying() {
        return playing;
    }

    public void removePossiblePlays() {
        possiblePlays.forEach(this::removeView);
        possiblePlays.clear();
    }

    public int getTopEnd() {
        if(onTable.isEmpty()) return -1;
        return onTable.get(0).getEnd();
    }

    public int getBottomEnd() {
        if(onTable.isEmpty()) return -1;
        PlayedPiece lastPiece = onTable.get(onTable.size() - 1);
        return lastPiece.isMiddle() ? lastPiece.getOtherEnd() : lastPiece.getEnd();
    }

    public int getTopPx() {
        if(center.isEmpty()) return 0;

        ColorIcon topPiece = center.get(0).getPiece().getInParent(this);
        assert topPiece != null;
        return (int) topPiece.getTranslationY() - topPiece.height() / 2;
    }

    private int getMaxTop() {
        int min = Integer.MAX_VALUE;

        ColorIcon atTop = getPlayPosition(new Move(new PlayedPiece(Piece.ONE_SIX, PieceRotation.FLIPPED), Side.TOP));
        atTop.setAlpha(0f);
        int top = (int) (atTop.getTranslationY() - atTop.height() / 2);
        if(top < min) {
            min = top;
        }
        removeView(atTop);


        ColorIcon atBottom = getPlayPosition(new Move(new PlayedPiece(Piece.ONE_SIX, PieceRotation.FLIPPED), Side.BOTTOM));
        atBottom.setAlpha(0f);
        top = (int) (atBottom.getTranslationY() - atBottom.height() / 2);
        if(top < min) {
            min = top;
        }
        removeView(atBottom);


        for(PlayedPiece piece : onTable) {
            ColorIcon img = piece.getPiece().getInParent(this);
            assert img != null;
            top = (int) (img.getTranslationY() - img.height() / 2);
            if(top < min) {
                min = top;
            }
        }

        return min;
    }

    private int getMaxBottom() {
        int max = Integer.MIN_VALUE;

        ColorIcon atTop = getPlayPosition(new Move(new PlayedPiece(Piece.ONE_SIX, PieceRotation.FLIPPED), Side.TOP));
        atTop.setAlpha(0f);
        int bottom = (int) (atTop.getTranslationY() + atTop.height() / 2);
        if(bottom > max) {
            max = bottom;
        }
        removeView(atTop);

        ColorIcon atBottom = getPlayPosition(new Move(new PlayedPiece(Piece.ONE_SIX, PieceRotation.FLIPPED), Side.BOTTOM));
        atBottom.setAlpha(0f);
        bottom = (int) (atBottom.getTranslationY() + atBottom.height() / 2);
        if(bottom > max) {
            max = bottom;
        }
        removeView(atBottom);

        for(PlayedPiece piece : onTable) {
            ColorIcon img = piece.getPiece().getInParent(this);
            assert img != null;
            bottom = (int) (img.getTranslationY() + img.height() / 2);
            if(bottom > max) {
                max = bottom;
            }
        }

        return max;
    }

    public int getBottomPx() {
        if(center.isEmpty()) return 0;

        ColorIcon bottomPiece = center.get(center.size() - 1).getPiece().getInParent(this);
        assert bottomPiece != null;
        return (int) bottomPiece.getTranslationY() + bottomPiece.height() / 2;
    }

    private int getMaxRight() {
        int max = Integer.MIN_VALUE;

        ColorIcon atTop = getPlayPosition(new Move(new PlayedPiece(Piece.ONE_SIX, PieceRotation.FLIPPED), Side.TOP));
        atTop.setAlpha(0f);
        int right = (int) (atTop.getTranslationX() + atTop.width() / 2);
        if(right > max) {
            max = right;
        }
        removeView(atTop);


        ColorIcon atBottom = getPlayPosition(new Move(new PlayedPiece(Piece.ONE_SIX, PieceRotation.FLIPPED), Side.BOTTOM));
        atBottom.setAlpha(0f);
        right = (int) (atBottom.getTranslationX() + atBottom.width() / 2);
        if(right > max) {
            max = right;
        }
        removeView(atBottom);


        for(PlayedPiece piece : onTable) {
            ColorIcon img = piece.getPiece().getInParent(this);
            assert img != null;
            right = (int) (img.getTranslationX() + img.width() / 2);
            if(right > max) {
                max = right;
            }
        }

        return max;
    }

    private int getMaxLeft() {
        int min = Integer.MAX_VALUE;

        ColorIcon atTop = getPlayPosition(new Move(new PlayedPiece(Piece.ONE_SIX, PieceRotation.FLIPPED), Side.TOP));
        atTop.setAlpha(0f);
        int left = (int) (atTop.getTranslationX() - atTop.width() / 2);
        if(left < min) {
            min = left;
        }
        removeView(atTop);


        ColorIcon atBottom = getPlayPosition(new Move(new PlayedPiece(Piece.ONE_SIX, PieceRotation.FLIPPED), Side.BOTTOM));
        atBottom.setAlpha(0f);
        left = (int) (atBottom.getTranslationX() - atBottom.width() / 2);
        if(left < min) {
            min = left;
        }
        removeView(atBottom);


        for(PlayedPiece piece : onTable) {
            ColorIcon img = piece.getPiece().getInParent(this);
            assert img != null;
            left = (int) (img.getTranslationX() - img.width() / 2);
            if(left < min) {
                min = left;
            }
        }

        return min;
    }

    public int getTopEndX() {
        if(center.size() < CENTER_SIZE) {
            return 0;
        } else if(left.isEmpty()) {
            ColorIcon topPiece = center.get(0).getPiece().getInParent(this);
            assert topPiece != null;
            return -topPiece.width() / 2;
        } else {
            if(left.size() <= 2) {
                ColorIcon topPiece = left.get(0).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationX() - topPiece.width() / 2;
            }else {
                ColorIcon topPiece = left_down.get(0).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationX();
            }
        }
    }

    public int getTopEndY() {
        if(center.size() < CENTER_SIZE) {
            return getTopPx();
        } else if(left.isEmpty()) {
            Piece p = center.get(0).getPiece();
            ColorIcon topPiece = p.getInParent(this);
            assert topPiece != null;
            return getTopPx() + (p.isDouble() ? topPiece.height() : topPiece.width()) / 2;
        } else {
            if(left.size() < 2) {
                ColorIcon topPiece = left.get(0).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationY();
            } else if(left_down.isEmpty()) {
                ColorIcon topPiece = left.get(0).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationY() + topPiece.height() / 2;
            } else {
                ColorIcon topPiece = left_down.get(0).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationY() + topPiece.height() / 2;
            }
        }
    }

    public int getBottomEndX() {
        if(center.size() < CENTER_SIZE) {
            return 0;
        } else if(right.isEmpty()) {
            ColorIcon topPiece = center.get(center.size() - 1).getPiece().getInParent(this);
            assert topPiece != null;
            return topPiece.width() / 2;
        } else {
            if(right.size() <= 2) {
                ColorIcon topPiece = right.get(right.size() - 1).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationX() + topPiece.width() / 2;
            }else {
                ColorIcon topPiece = right_up.get(right_up.size() - 1).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationX();
            }
        }
    }

    public int getBottomEndY() {
        if(center.size() < CENTER_SIZE) {
            return getBottomPx();
        } else if(right.isEmpty()) {
            Piece p = center.get(center.size() - 1).getPiece();
            ColorIcon topPiece = p.getInParent(this);
            assert topPiece != null;
            return getBottomPx() - (p.isDouble() ? topPiece.height() : topPiece.width()) / 2;
        } else {
            if(right.size() < 2) {
                ColorIcon topPiece = right.get(0).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationY();
            } else if(right_up.isEmpty()) {
                ColorIcon topPiece = right.get(right.size() - 1).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationY() - topPiece.height() / 2;
            } else {
                ColorIcon topPiece = right_up.get(right_up.size() - 1).getPiece().getInParent(this);
                assert topPiece != null;
                return (int) topPiece.getTranslationY() - topPiece.height() / 2;
            }
        }
    }

    private int getXInParent(View view) {
        Rect offsetViewBounds = new Rect();
        view.getDrawingRect(offsetViewBounds);
        owner.getLoaded().offsetDescendantRectToMyCoords(view, offsetViewBounds);
        return offsetViewBounds.left;
    }

    private int getYInParent(View view) {
        Rect offsetViewBounds = new Rect();
        view.getDrawingRect(offsetViewBounds);
        owner.getLoaded().offsetDescendantRectToMyCoords(view, offsetViewBounds);
        return offsetViewBounds.top;
    }

    private Rect bounds = null;
    private Rect tableBounds() {
        if(bounds == null) {
            Game game = owner.getLoaded();
            PieceHolder leftHolder = game.getLeftHolder();
            PieceHolder rightHolder = game.getRightHolder();
            PieceHolder topHolder = game.getTopHolder();
            PieceHolder bottomHolder = game.getBottomHolder();
            int maxLeft = (leftHolder == null ? ViewUtils.dipToPx(5, owner) : getXInParent(leftHolder) + leftHolder.getWidth()) + ViewUtils.dipToPx(10, owner);
            int maxRight = (rightHolder == null ? owner.getScreenWidth() - ViewUtils.dipToPx(5, owner) : getXInParent(rightHolder)) - ViewUtils.dipToPx(10, owner);
            int maxTop = getYInParent(topHolder) + topHolder.getHeight() + ViewUtils.dipToPx(5, owner);
            int maxBottom = getYInParent(bottomHolder) - ViewUtils.dipToPx(30, owner);
            bounds = new Rect(maxLeft, maxTop, maxRight, maxBottom);
        }
        return bounds;
    }

    public void clear() {
        bounds = null;
        playing = null;
        removeAllViews();
        setScaleX(1);
        setScaleY(1);
        setTranslationX(0);
        setAlpha(1);
        onTable.clear();
        center.clear();
        left.clear();
        left_down.clear();
        right.clear();
        right_up.clear();
    }
}
