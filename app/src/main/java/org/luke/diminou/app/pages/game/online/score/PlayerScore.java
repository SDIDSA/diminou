package org.luke.diminou.app.pages.game.online.score;

import android.view.Gravity;

import androidx.core.graphics.ColorUtils;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.AvatarDisplay;
import org.luke.diminou.app.pages.game.online.Game;
import org.luke.diminou.app.pages.home.online.friends.Username;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

public class PlayerScore extends HBox implements Styleable {
    private final AvatarDisplay ad;
    private final Username name;
    private final Label score;

    private final int scoreVal;
    public PlayerScore(App owner, int player, int score) {
        super(owner);
        this.scoreVal = score;
        setGravity(Gravity.CENTER);
        setCornerRadius(7);

        ad = new AvatarDisplay(owner);
        ViewUtils.setMarginRight(ad, owner, 15);

        name = new Username(owner);
        name.setFont(new Font(18));

        this.score = new Label(owner, "");
        this.score.setText(String.valueOf(score));
        this.score.setFont(new Font(24));

        HBox pieces = new HBox(owner);
        ViewUtils.setMarginRight(pieces, owner, 7);
        Game game =  Page.getInstance(owner, Game.class);
        assert game != null;
        game.getForPlayer(player).getPieces().forEach(piece -> {
            ColorIcon image = piece.getImage(owner, 12);
            ViewUtils.setMarginRight(image, owner, 7);
            pieces.addView(image);
        });

        VBox data = new VBox(owner);
        ViewUtils.setMarginRight(data, owner, 15);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewUtils.dipToPx(AvatarDisplay.preSize - 8, owner));
        params.weight = 1;
        data.setLayoutParams(params);
        data.addView(name);
        data.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        data.addView(pieces);

        addView(ad);
        addView(data);
        addView(this.score);

        User.getForId(player, user -> {
            ad.setUser(user);
            name.setUser(user);
        });

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        boolean normal = getOwner().getFourMode() == FourMode.NORMAL_MODE;
        name.setFill(style.getTextNormal());
        score.setFill(style.getTextNormal());
        int back = normal ? style.getBackgroundTertiary() : style.getBackgroundPrimary();
        if(scoreVal >= 100) {
            back = ColorUtils.blendARGB(back, style.getTextPositive(), .4f);
            if(normal) {
                setPadding(15);
            }
        }
        setBackground(back);
        ad.setOnlineBackground(back);
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
