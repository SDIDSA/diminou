package org.luke.diminou.app.pages.join.offline;

import android.view.Gravity;
import android.widget.LinearLayout;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.image.Image;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.Avatar;
import org.luke.diminou.data.property.Property;

import java.util.Locale;
import java.util.Objects;

public class PartyDisplay extends HBox implements Styleable {
    private final Party data;
    private final Image avatar;
    private final Label username;
    private final Label players;

    public PartyDisplay(App owner, Party data) {
        super(owner);
        this.data = data;

        setGravity(Gravity.CENTER);
        setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewUtils.dipToPx(32, owner)));

        avatar = new Image(owner);
        avatar.setSize(32);
        avatar.setCornerRadius(7);
        ViewUtils.setMarginRight(avatar, owner, 10);

        username = new Label(owner, "");
        username.setFont(new Font(18));

        players = new Label(owner, "");
        players.setFont(new Font(18));

        addView(avatar);
        addView(username);
        addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(players);

        avatar.setImageResource(Avatar.valueOf(data.getAvatar()).getRes());
        username.setText(data.getUsername());
        players.setText(String.format(Locale.getDefault(),"%d/4", data.getPlayers()));

        applyStyle(owner.getStyle());
    }

    public Party getData() {
        return data;
    }

    public void setCount(int count) {
        data.setPlayers(count);
        players.setText(String.valueOf(data.getPlayers()).concat("/4"));
    }

    public void decrementCount() {
        data.setPlayers(data.getPlayers() - 1);
        players.setText(String.valueOf(data.getPlayers()).concat("/4"));
    }

    @Override
    public void applyStyle(Style style) {
        username.setFill(style.getTextNormal());
        players.setFill(style.getTextNormal());
        avatar.setBackgroundColor(style.getTextMuted());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PartyDisplay display = (PartyDisplay) o;

        return Objects.equals(data, display.data);
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
