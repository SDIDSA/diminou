package org.luke.diminou.app.pages.game.offline.score;

import android.view.Gravity;

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
import org.luke.diminou.app.pages.game.offline.OfflineGame;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayer;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.data.property.Property;

public class OfflinePlayerScore extends HBox implements Styleable {
    private final Label name;
    private final Label score;

    private final int scoreVal;
    public OfflinePlayerScore(App owner, OfflinePlayer player, int score) {
        super(owner);
        this.scoreVal = score;
        setGravity(Gravity.CENTER);
        setCornerRadius(7);

        AvatarDisplay ad = new AvatarDisplay(owner);
        ad.setValue(player.getAvatar());
        ViewUtils.setMarginRight(ad, owner, 15);

        name = new Label(owner, "");
        name.setText(player.getName());
        name.setFont(new Font(18));

        this.score = new Label(owner, "");
        this.score.setText(String.valueOf(score));
        this.score.setFont(new Font(24));

        HBox pieces = new HBox(owner);
        ViewUtils.setMarginRight(pieces, owner, 7);
        OfflineGame game = (OfflineGame) Page.getInstance(owner, OfflineGame.class);
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

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        name.setFill(style.getTextNormal());
        score.setFill(style.getTextNormal());
        if(scoreVal >= 100 && getOwner().getFourMode() == FourMode.NORMAL_MODE) {
            setPadding(15);
            setBackground(App.adjustAlpha(style.getTextPositive(), .4f));
        }
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
