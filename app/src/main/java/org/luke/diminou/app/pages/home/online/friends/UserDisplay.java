package org.luke.diminou.app.pages.home.online.friends;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.StackPane;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.AvatarDisplay;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

import java.util.HashMap;

public class UserDisplay extends StackPane implements Styleable {
    private static final HashMap<Integer, UserDisplay> cache = new HashMap<>();

    public static void clearCache() {
        cache.clear();
    }

    public static UserDisplay get(App owner, int userId) {
        UserDisplay found = cache.get(userId);
        if(found == null) {
            Log.i("creating", String.valueOf(userId));
            found = new UserDisplay(owner, userId);
            cache.put(userId, found);
        }else {
            Log.i("found in cache", String.valueOf(userId));
        }
        if(found.getParent() != null) {
            ((ViewGroup) found.getParent()).removeView(found);
        }
        return found;
    }

    private final HBox root;
    private final AvatarDisplay img;
    private UserDisplay(App owner, int userId) {
        super(owner);
        setCornerRadius(12);

        root = new HBox(owner);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setCornerRadius(10);
        root.setPadding(10);
        ViewUtils.setMarginBottom(root, owner, 4);

        img = new AvatarDisplay(owner, 48);

        ViewUtils.setMarginRight(img, owner, 15);

        ColoredLabel name = new ColoredLabel(owner, "", Style::getTextNormal);
        name.setFont(new Font(18));

        root.addView(img);
        root.addView(name);
        root.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));

        addView(root);

        FriendAction accept = new FriendAction(owner, R.drawable.check);
        FriendAction decline = new FriendAction(owner, R.drawable.decline);
        FriendAction send = new FriendAction(owner, R.drawable.add);
        FriendAction pending = new FriendAction(owner, R.drawable.pending);
        FriendAction cancel = new FriendAction(owner, R.drawable.cancel);

        User.getForId(userId, user -> {
            user.usernameProperty().addListener((obs, ov, nv) ->
                    name.setText(nv));
            img.setUser(user);
            user.friendProperty().addListener((obs, ov, nv) -> {
                root.removeView(decline);
                root.removeView(accept);
                root.removeView(send);
                root.removeView(pending);
                root.removeView(cancel);
                switch (nv) {
                    case "none" -> root.addView(send);
                    case "pending_received" -> {
                        root.addView(decline);
                        root.addView(accept);
                    }
                    case "pending_sent" -> {
                        root.addView(cancel);
                        root.addView(pending);
                    }
                }
            });

            send.setOnClick(() ->
                    Session.sendRequest(userId, res -> {
                        if(res.has("err")) {
                            User.refresh(userId);
                        }
                    })
            );
            cancel.setOnClick(() ->
                    Session.cancelRequest(userId, res -> {
                        if(res.has("err")) {
                            User.refresh(userId);
                            user.setFriend(User.getForIdSync(userId).getFriend());
                        }
                    })
            );
            decline.setOnClick(cancel::fire);
            accept.setOnClick(() ->
                    Session.acceptRequest(userId, res -> {
                        if(res.has("err")) {
                            User.refresh(userId);
                            user.setFriend(User.getForIdSync(userId).getFriend());
                        }
                    })
            );
        });

        applyStyle(owner.getStyle());
    }

    @Override
    public void applyStyle(Style style) {
        root.setBackground(style.getBackgroundTertiary());
        root.setBorderColor(style.getTextMuted());
        img.setOnlineBackground(style.getBackgroundTertiary());
        setBackground(style.getTextMuted());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
