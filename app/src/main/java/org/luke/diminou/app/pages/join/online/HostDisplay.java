package org.luke.diminou.app.pages.join.online;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.AvatarDisplay;
import org.luke.diminou.app.pages.home.online.friends.Username;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

public class HostDisplay extends HBox implements Styleable {
    private final Username name;
    private final AvatarDisplay avatar;
    public HostDisplay(App owner) {
        super(owner);
        setPadding(10);
        setCornerRadius(7);

        name = new Username(owner);
        name.setFont(new Font(20));
        avatar = new AvatarDisplay(owner, 64);

        VBox left = new VBox(owner);
        left.setLayoutParams(new LayoutParams(-2, ViewUtils.dipToPx(64, owner)));

        ColoredLabel cl = new ColoredLabel(owner, "Created by", Style::getTextNormal);

        left.addView(cl);
        left.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        left.addView(name);

        addView(left);
        addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(avatar);

        applyStyle(owner.getStyle());
    }

    public void setUser(User user) {
        name.setUser(user);
        avatar.setUser(user);
    }

    @Override
    public void applyStyle(Style style) {
        setBackground(style.getBackgroundPrimary());
        setBorderColor(style.getTextMuted());
        avatar.setOnlineBackground(style.getBackgroundPrimary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
