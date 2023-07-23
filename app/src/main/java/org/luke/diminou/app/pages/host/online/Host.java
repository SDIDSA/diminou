package org.luke.diminou.app.pages.host.online;

import android.widget.LinearLayout;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.button.PrimaryButton;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.cards.online.DisplayCards;
import org.luke.diminou.app.cards.online.MirorredCards;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.game.online.Game;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.app.pages.home.online.global.RoomId;
import org.luke.diminou.data.beans.Room;
import org.luke.diminou.data.beans.User;

public class Host extends Titled {
    private final RoomId idDisp;

    private final DisplayCards cards;

    private final Animation showStart, hideStart;

    private String roomId;

    private final Invite invite;

    public Host(App owner) {
        super(owner, "create_party");

        idDisp = new RoomId(owner);

        cards = new DisplayCards(owner, true);
        cards.setLayoutParams(new LayoutParams(0, 0));
        MirorredCards mirorredCards = new MirorredCards(owner);

        mirorredCards.bind(cards);

        invite = new Invite(owner);

        mirorredCards.forEach(card ->
                card.setOnClickListener(v -> {
                    if(!card.isLoaded()) {
                        invite.show();
                    }
                })
        );

        Button start = new PrimaryButton(owner, "start_game");
        start.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        start.setFont(new Font(18));
        start.setOnClick(() ->
                Session.begin(roomId, res -> {
                    if(res.has("err")) owner.toast(res.getString("err"));
                })
        );

        content.addView(idDisp);
        content.addView(cards);
        content.addView(mirorredCards);
        content.addView(start);

        start.setAlpha(0);
        start.setTranslationY(ViewUtils.by(owner));
        start.setScaleX(.7f);
        start.setScaleY(.7f);

        hideStart = new ParallelAnimation(300)
                .addAnimation(new TranslateYAnimation(start, 40))
                .addAnimation(new AlphaAnimation(start, 0))
                .addAnimation(new ScaleXYAnimation(start, .7f))
                .setInterpolator(Interpolator.EASE_OUT);

        showStart = new ParallelAnimation(300)
                .addAnimation(new TranslateYAnimation(start, 0))
                .addAnimation(new AlphaAnimation(start, 1))
                .addAnimation(new ScaleXYAnimation(start, 1))
                .setInterpolator(Interpolator.OVERSHOOT);
    }

    public void joined(int id) {
        User.getForId(id, user -> {
            owner.playMenuSound(R.raw.joined);
            cards.getLast().loadPlayer(user);
            checkCount();
        });
    }

    public void left(int id) {
        owner.playMenuSound(R.raw.left);
        cards.unloadPlayer(id);
        checkCount();
    }

    private void checkCount() {
        if(cards.size() > 1) {
            showStart.start();
        }else {
            hideStart.start();
        }
    }

    @Override
    public void setup() {
        super.setup();
        cards.unloadAll();

        Room room = owner.getRoom();
        roomId = room.getId();
        idDisp.setId(room.getId());

        Platform.runBack(() -> {
            for (int player : room.getPlayers()) {
                if(player == -1) break;
                User u = User.getForIdSync(player);
                Platform.runLater(() -> cards.getLast().loadPlayer(u));
            }
        });
    }

    @Override
    public void destroy(Page newPage) {
        super.destroy(newPage);

        if(!(newPage instanceof Game)) {
            Session.endGame(roomId, res -> {
                //IGNORE
            });
        }
    }

    @Override
    public boolean onBack() {
        owner.loadPage(Home.class);
        return true;
    }
}
