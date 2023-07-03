package org.luke.diminou.app.pages.game.piece;

import android.view.ViewGroup;

import androidx.annotation.DrawableRes;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.Collections;

public enum Piece implements Styleable {
    ZERO_ZERO(R.drawable._0_0, R.drawable._0_0_h, 0, 0),
    ZERO_ONE(R.drawable._0_1, R.drawable._0_1_h, 0, 1),
    ZERO_TWO(R.drawable._0_2, R.drawable._0_2_h, 0, 2),
    ZERO_THREE(R.drawable._0_3, R.drawable._0_3_h, 0, 3),
    ZERO_FOUR(R.drawable._0_4, R.drawable._0_4_h, 0, 4),
    ZERO_FIVE(R.drawable._0_5, R.drawable._0_5_h, 0, 5),
    ZERO_SIX(R.drawable._0_6, R.drawable._0_6_h, 0, 6),
    ONE_ONE(R.drawable._1_1, R.drawable._1_1_h, 1, 1),
    ONE_TWO(R.drawable._1_2, R.drawable._1_2_h, 1, 2),
    ONE_THREE(R.drawable._1_3, R.drawable._1_3_h, 1, 3),
    ONE_FOUR(R.drawable._1_4, R.drawable._1_4_h, 1, 4),
    ONE_FIVE(R.drawable._1_5, R.drawable._1_5_h, 1, 5),
    ONE_SIX(R.drawable._1_6, R.drawable._1_6_h, 1, 6),
    TWO_TWO(R.drawable._2_2, R.drawable._2_2_h, 2, 2),
    TWO_THREE(R.drawable._2_3, R.drawable._2_3_h, 2, 3),
    TWO_FOUR(R.drawable._2_4, R.drawable._2_4_h, 2, 4),
    TWO_FIVE(R.drawable._2_5, R.drawable._2_5_h, 2, 5),
    TWO_SIX(R.drawable._2_6, R.drawable._2_6_h, 2, 6),
    THREE_THREE(R.drawable._3_3, R.drawable._3_3_h, 3, 3),
    THREE_FOUR(R.drawable._3_4, R.drawable._3_4_h, 3, 4),
    THREE_FIVE(R.drawable._3_5, R.drawable._3_5_h, 3, 5),
    THREE_SIX(R.drawable._3_6, R.drawable._3_6_h, 3, 6),
    FOUR_FOUR(R.drawable._4_4, R.drawable._4_4_h, 4, 4),
    FOUR_FIVE(R.drawable._4_5, R.drawable._4_5_h, 4, 5),
    FOUR_SIX(R.drawable._4_6, R.drawable._4_6_h, 4, 6),
    FIVE_FIVE(R.drawable._5_5, R.drawable._5_5_h, 5, 5),
    FIVE_SIX(R.drawable._5_6, R.drawable._5_6_h, 5, 6),
    SIX_SIX(R.drawable._6_6, R.drawable._6_6_h, 6, 6),
    HIDDEN(R.drawable._hidden, R.drawable._hidden_h, -1, -1);

    public static ArrayList<Piece> pack() {
        ArrayList<Piece> res = new ArrayList<>();

        for(Piece p : values()) {
            if(p != HIDDEN) res.add(p);
        }

        Collections.shuffle(res);

        return res;
    }

    public int sum() {
        return n0 + n1;
    }

    private static ArrayList<Piece> priority;
    public static ArrayList<Piece> priority() {
        if(priority == null) {
            priority = pack();

            priority().sort((p1, p2) -> {
                if ((p1.isDouble() && p2.isDouble()) || (!p1.isDouble() && !p2.isDouble()))
                    return -Integer.compare(p1.sum(), p2.sum());
                else if (p1.isDouble()) return -1;
                else return 1;
            });
        }

        return priority;
    }

    private int n0, n1;
    public static void initAll(App owner) {
        for(Piece piece : Piece.values()) {
            piece.init(owner);
        }
    }

    public static Piece random() {
        return values()[(int) (Math.random() * values().length)];
    }
    private final @DrawableRes int res;
    private @DrawableRes int resHor;
    Piece(@DrawableRes int res, @DrawableRes int resHor, int n0, int n1) {
        this.res = res;
        this.resHor = resHor;
        this.n0 = n0;
        this.n1 = n1;
    }

    public int getN0() {
        return n0;
    }

    public int getN1() {
        return n1;
    }

    public boolean isDouble() {
        return n0 == n1;
    }

    public boolean has(int number) {
        return number == -1 ? isDouble() : number == n1 || number == n0;
    }

    private final ArrayList<ColorIcon> images = new ArrayList<>();
    public ColorIcon getImage(App owner, float size, Orientation orientation) {
        ColorIcon i = new ColorIcon(owner, orientation == Orientation.VERTICAL ? res : resHor);
        if(orientation == Orientation.VERTICAL) {
            i.setWidth(size);
            i.setHeight(size * 2);
        } else {
            i.setHeight(size);
            i.setWidth(size * 2);
        }

        images.add(i);
        applyStyle(owner.getStyle().get(), i);
        return i;
    }

    public ColorIcon getInParent(ViewGroup parent) {
        for(ColorIcon i : images) {
            if(parent.indexOfChild(i) != -1) return i;
        }

        return null;
    }

    public ColorIcon getImage(App owner, float size) {
        return getImage(owner, size, Orientation.VERTICAL);
    }

    private void init(App owner) {
        applyStyle(owner.getStyle());
    }

    private void applyStyle(Style style, ColorIcon image) {
        image.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Style style) {
        images.removeIf(i -> !i.isAttachedToWindow());
        for(ColorIcon image : images) {
            applyStyle(style, image);
        }
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
