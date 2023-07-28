package org.luke.diminou.app.pages.host.online;

import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.components.layout.overlay.PartialSlideOverlay;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.app.pages.home.online.friends.UserDisplay;

import java.util.stream.IntStream;

public class Invite extends PartialSlideOverlay {
    public Invite(App owner) {
        super(owner, 0.5f);
        list.setPadding(15);
        list.setSpacing(20);

        ColoredLabel header = new ColoredLabel(owner, "Invite your friends", Style::getTextNormal);
        ColoredLabel noFriends = new ColoredLabel(owner, "You don't have friends :(", Style::getTextNormal);

        header.setFont(new Font(18));

        VBox fdisp = new VBox(owner);
        fdisp.setSpacing(10);

        ScrollView sv = new ScrollView(owner);
        sv.addView(fdisp);
        sv.setScrollBarSize(0);

        list.addView(header);
        list.addView(sv);

        addOnShowing(() -> {
            fdisp.removeAllViews();

            Session.getFriends(friends -> {
                if(friends.size() == 0) {
                    fdisp.addView(noFriends);
                } else {
                    friends.forEach(user_id -> {
                        UserDisplay disp = UserDisplay.get(owner, user_id);
                        disp.setOnClickListener(v ->
                                Session.invite(user_id, owner.getRoom().getId(), ires -> {
                                    hide();
                                    owner.toast(ires.has("err") ?
                                            ires.getString("err")
                                            : "Player invited");
                        }));
                        fdisp.addView(disp);
                    });
                }
            });
        });

    }
}
