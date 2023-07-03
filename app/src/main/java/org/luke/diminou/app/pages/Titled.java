package org.luke.diminou.app.pages;

import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.controls.text.transformationMethods.AllCaps;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

public abstract class Titled extends Page {
    private final VBox root;

    protected final VBox content;

    private final ColorIcon back;
    private final Label title;

    private final Animation hideBack;

    private final HBox top;

    private final Animation showBack;
    public Titled(App owner, String text) {
        super(owner);

        root = new VBox(owner);
        root.setGravity(Gravity.TOP | Gravity.CENTER);
        root.setClipChildren(false);

        top = new HBox(owner);
        top.setPadding(15);
        top.setGravity(Gravity.CENTER);
        top.setMinimumHeight(ViewUtils.dipToPx(66, owner));

        back = new ColorIcon(owner, R.drawable.arrow);
        back.setSize(36);
        ViewUtils.setMarginRight(back, owner, 15);
        title = new Label(owner, text);
        title.setFont(new Font(22));
        title.setTransformationMethod(new AllCaps());

        back.setOnClick(this::onBack);

        top.addView(back);
        top.addView(title);
        top.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));

        content = new VBox(owner);
        content.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        content.setPadding(15);
        content.setSpacing(15);

        content.setClipChildren(false);
        content.setClipToPadding(false);
        content.setClipToOutline(false);

        ViewUtils.spacer(content);

        root.addView(top);
        root.addView(content);

        addView(root);

        int by = ViewUtils.dipToPx(15, owner);

        hideBack = new ParallelAnimation(400)
                .addAnimation(new ValueAnimation(0, 0) {
                    @Override
                    public void updateValue(float v) {
                        back.setWidth(ViewUtils.pxToDip(v, owner));
                    }
                }.setLateFromInt(back::getWidth))
                .addAnimation(new TranslateXAnimation(title, 0).setLateTo(() -> (float) (ViewCompat.getLayoutDirection(top) == ViewCompat.LAYOUT_DIRECTION_LTR ? -by : by)))
                .setInterpolator(Interpolator.EASE_OUT)
                .setOnFinished(() -> {
                    top.removeView(back);
                    title.setTranslationX(0);
                });

        showBack = new ParallelAnimation(400)
                .addAnimation(new ValueAnimation(0, 36) {
                    @Override
                    public void updateValue(float v) {
                        back.setWidth(v);
                    }
                })
                .addAnimation(new TranslateXAnimation(title, 0))
                .setInterpolator(Interpolator.EASE_OUT);

        applyStyle(owner.getStyle());
    }

    public Animation getHideBack() {
        return hideBack;
    }

    public Animation getShowBack() {
        title.setTranslationX(ViewUtils.dipToPx(15, owner) * (ViewCompat.getLayoutDirection(top) == ViewCompat.LAYOUT_DIRECTION_LTR ? -1 : 1));
        back.setWidth(0);
        top.removeView(back);
        top.addView(back, 0);
        return showBack;
    }

    public HBox getPreTitle() {
        return top;
    }

    @Override
    public void setup() {
        super.setup();

        setAlpha(0);
        setTranslationY(ViewUtils.dipToPx(-30, owner));

        top.removeView(back);
        top.addView(back, 0);

        ParallelAnimation show = new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(this, 1))
                .addAnimation(new TranslateYAnimation(this, 0))
                .setInterpolator(Interpolator.EASE_OUT);

        show.start();
    }

    @Override
    public void applyInsets(Insets insets) {
        root.setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    @Override
    public void applyStyle(Style style) {
        back.setFill(style.getTextNormal());
        title.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
