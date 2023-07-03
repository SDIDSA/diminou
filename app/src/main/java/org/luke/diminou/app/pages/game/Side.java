package org.luke.diminou.app.pages.game;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.utils.ViewUtils;

public enum Side {
    TOP, BOTTOM, RIGHT, LEFT;

    public Orientation getOrientation() {
        return isHorizontal() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
    }

    public boolean isHorizontal() {
        return this == TOP || this == BOTTOM;
    }

    public boolean isVertical() {
        return !isHorizontal();
    }

    public int getNameGravity() {
        switch (this) {
            case TOP -> {
                return Gravity.TOP | Gravity.END;
            }
            case BOTTOM, LEFT -> {
                return Gravity.TOP | Gravity.START;
            }
            case RIGHT -> {
                return Gravity.BOTTOM | Gravity.END;
            }
        }
        return 0;
    }

    @SuppressLint("RtlHardcoded")
    public void namePos(Label name) {
        App owner = name.getOwner();
        int s = ViewUtils.dipToPx(7, owner);
        switch (this) {
            case TOP -> {
                name.setGravity(Gravity.LEFT);
                name.setTranslationX(name.getWidth() + s);
                name.setTranslationY(s / 2f);
            }
            case BOTTOM, LEFT -> {
                name.setGravity(Gravity.LEFT);
                name.setTranslationY(-name.getHeight()-s / 2f);
                name.setTranslationX(s);
            }
            case RIGHT -> {
                name.setGravity(Gravity.RIGHT);
                name.setTranslationX(-s);
                name.setTranslationY(name.getHeight() + s / 2f);
            }
        }
    }

    public int getScoreGravity() {
        switch (this) {
            case TOP -> {
                return Gravity.TOP | Gravity.START;
            }
            case BOTTOM, RIGHT -> {
                return Gravity.TOP | Gravity.END;
            }
            case LEFT -> {
                return Gravity.BOTTOM | Gravity.START;
            }
        }
        return 0;
    }
    public void scorePos(Label name) {
        App owner = name.getOwner();
        int s = ViewUtils.dipToPx(7, owner);
        switch (this) {
            case TOP -> {
                name.setTranslationX(-name.getWidth() - s);
                name.setTranslationY(s / 2f);
            }
            case BOTTOM -> {
                name.setTranslationY(-name.getHeight()-s / 2f);
                name.setTranslationX(-s);
            }
            case LEFT -> {
                name.setTranslationX(s);
                name.setTranslationY(name.getHeight() + s / 2f);
            }
            case RIGHT -> {
                name.setTranslationX(-s);
                name.setTranslationY(-name.getHeight() - s / 2f);
            }
        }
    }
}
