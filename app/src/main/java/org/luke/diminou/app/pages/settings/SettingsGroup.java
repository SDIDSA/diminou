package org.luke.diminou.app.pages.settings;

import android.view.Gravity;
import android.view.View;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.ElevationAnimation;
import org.luke.diminou.abs.animation.view.LinearHeightAnimation;
import org.luke.diminou.abs.animation.view.RotateAnimation;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;

public class SettingsGroup extends VBox implements Styleable {
    private final Label title;
    private final ColoredIcon arrow;

    private final VBox settingsBox;
    private final ArrayList<Setting> settings;
    public SettingsGroup(App owner, String key) {
        super(owner);
        setCornerRadius(10);
        setLayoutParams(new LayoutParams(-1, -2));

        settings = new ArrayList<>();

        HBox top = new HBox(owner);
        top.setGravity(Gravity.CENTER);

        title = new Label(owner, key);
        title.setFont(new Font(16));

        arrow = new ColoredIcon(owner, Style::getTextNormal, R.drawable.right_arrow);
        arrow.setSize(18);
        arrow.setRotation(90);

        settingsBox = new VBox(owner);
        settingsBox.setPadding(10);
        settingsBox.setLayoutParams(new LayoutParams(-1, 0));

        top.addView(title);
        top.setPadding(15);
        top.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        top.addView(arrow);

        setOnClickListener(e -> open());

        addView(top);

        addView(settingsBox);

        applyStyle(owner.getStyle());
    }

    private int settingsHeight() {
        return settingsBox.getPaddingTop() + settingsBox.getPaddingBottom()
                + settings.stream().mapToInt(View::getHeight).sum();
    }

    public void addSetting(Setting setting) {
        settings.add(setting);
        settingsBox.addView(setting);
    }

    static SettingsGroup openGroup = null;
    private boolean open = false;

    public void open() {
        if(openGroup != null) {
            if(openGroup == this) {
                close();
                return;
            } else {
                openGroup.close();
            }
        }

        open = true;
        openGroup = this;
        new ParallelAnimation(400)
                .addAnimation(new RotateAnimation(arrow, 270))
                .addAnimation(new LinearHeightAnimation(settingsBox, 0)
                        .setLateToInt(this::settingsHeight))
                .addAnimation(new ElevationAnimation(this, ViewUtils.dipToPx(20, getOwner())))
                .setInterpolator(Interpolator.EASE_OUT).start();

        getOwner().putData("open_cat", getKey());
    }

    public String getKey() {
        return title.getKey();
    }

    public void close() {
        if(!open) return;
        open = false;
        new ParallelAnimation(400)
                .addAnimation(new RotateAnimation(arrow, 90))
                .addAnimation(new LinearHeightAnimation(settingsBox, 0))
                .addAnimation(new ElevationAnimation(this, 0))
                .setInterpolator(Interpolator.EASE_OUT).start();
        openGroup = null;
        getOwner().putData("open_cat", null);
    }

    @Override
    public void applyStyle(Style style) {
        title.setFill(style.getTextNormal());
        setBackground(style.getBackgroundPrimary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
