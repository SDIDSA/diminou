package org.luke.diminou.app.pages.game.offline.score;

import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;

import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.components.layout.overlay.Overlay;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.game.offline.OfflineGame;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayer;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;

public class OfflineScoreBoard extends Overlay implements Styleable {
    private final App owner;
    private final VBox root;
    private final Label waiting;
    private final Button skip;
    public OfflineScoreBoard(App owner) {
        super(owner);
        this.owner = owner;
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        setBackgroundColor(App.adjustAlpha(Color.BLACK, .4f));
        setAutoHide(false);
        ViewUtils.setPaddingUnified(this, 15, owner);

        root = new VBox(owner);
        root.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        root.setPadding(15);
        root.setSpacing(15);
        root.setCornerRadius(10);
        root.setGravity(Gravity.CENTER);

        addView(root);

        ViewUtils.alignInFrame(root, Gravity.CENTER);

        int by = ViewUtils.dipToPx(50, owner);
        root.setAlpha(0);
        root.setScaleY(.5f);
        root.setScaleX(.5f);
        root.setTranslationY(by);
        addToShow(new ParallelAnimation()
                .addAnimation(new AlphaAnimation(root, 1))
                .addAnimation(new ScaleXYAnimation(root, 1))
                .addAnimation(new TranslateYAnimation(root, 0)));

        addToHide(new ParallelAnimation()
                .addAnimation(new AlphaAnimation(root, 0))
                .addAnimation(new ScaleXYAnimation(root, .5f))
                .addAnimation(new TranslateYAnimation(root, by)));

        waiting = new Label(owner, "waiting_for_host");
        waiting.setFont(new Font(16));

        skip = new Button(owner, "continue");

        addOnShowing(() -> setAlpha(1));
        addOnShowing(this::loadScores);

        setOnTouchListener((v, e) -> {
            if(e.getAction() == MotionEvent.ACTION_DOWN) {
                new AlphaAnimation(300, this, 0)
                        .setInterpolator(Interpolator.EASE_OUT)
                        .start();
            }
            if(e.getAction() == MotionEvent.ACTION_UP) {
                new AlphaAnimation(300, this, 1)
                        .setInterpolator(Interpolator.EASE_OUT)
                        .start();
                performClick();
            }
            return false;
        });

        applyStyle(owner.getStyle());
    }

    private void loadScores() {
        root.removeAllViews();
        boolean gameEnd = false;
        ArrayList<OfflinePlayer> players = new ArrayList<>(owner.getPlayers());

        OfflineGame game = Page.getInstance(owner, OfflineGame.class);
        assert game != null;

        if(owner.getFourMode() == FourMode.NORMAL_MODE) {
            root.setPadding(15);
            players.sort((p1, p2) -> Integer.compare(game.getScoreOf(p2), game.getScoreOf(p1)));

            for(OfflinePlayer player : players) {
                int score = game.getScoreOf(player);
                root.addView(new OfflinePlayerScore(owner, player, score));
                if(score >= 100) {
                    gameEnd = true;
                }
            }
        }else {
            root.setPadding(0);

            OfflinePlayer[][] teams = new OfflinePlayer[2][2];
            teams[0] = new OfflinePlayer[] { players.get(0), players.get(2)};
            teams[1] = new OfflinePlayer[] { players.get(1), players.get(3)};

            int start = game.getScoreOf(players.get(0)) > game.getScoreOf(players.get(1)) ? 0 : 1;
            int end = start == 0 ? 1 : 0;

            for(int i : new int[] {start, end}) {
                VBox team = new VBox(owner);
                team.setPadding(15);
                team.setSpacing(10);
                team.setCornerRadius(7);
                team.setBackground(owner.getStyle().get().getBackgroundPrimary());

                for(OfflinePlayer player : teams[i]) {
                    int score = game.getScoreOf(player);
                    team.addView(new OfflinePlayerScore(owner, player, score));
                    if(score >= 100) {
                        gameEnd = true;
                        team.setBackground(ColorUtils.blendARGB(owner.getStyle().get().getBackgroundPrimary(), owner.getStyle().get().getTextPositive(), .4f));
                    }
                }

                root.addView(team);
            }
        }


        if(gameEnd) {
            players.forEach(player -> game.setScoreOf(player, 0));
            owner.putData("winner", null);
        }

        if(game.isHost()) {
            skip.setOnClick(() -> {
                hide();
                owner.getLoaded().setup();
                owner.getSockets().forEach(s -> s.emit("skip", ""));
            });
            root.addView(skip);
        }else {
            root.addView(waiting);
        }

        applyStyle(owner.getStyle());
    }

    @Override
    public void applySystemInsets(Insets insets) {
        //IGNORE
    }

    @Override
    public void applyStyle(Style style) {
        FourMode mode = owner.getFourMode();
        root.setBackground(mode == FourMode.NORMAL_MODE ? style.getBackgroundTertiary() : Color.TRANSPARENT);
        waiting.setFill(style.getTextMuted());
        skip.setFill(mode == FourMode.NORMAL_MODE ? App.adjustAlpha(style.getSecondaryButtonBack(), .3f) : style.getBackgroundPrimary());
        skip.setTextFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
