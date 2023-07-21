package org.luke.diminou.app.pages.home.online;

import android.widget.LinearLayout;

import androidx.core.graphics.Insets;

import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.layout.fragment.FragmentPane;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.app.pages.home.online.friends.Friends;
import org.luke.diminou.app.pages.home.online.global.Bottom;
import org.luke.diminou.app.pages.home.online.global.HomeFragment;
import org.luke.diminou.app.pages.home.online.global.Top;
import org.luke.diminou.app.pages.home.online.play.Play;
import org.luke.diminou.app.pages.host.online.Host;
import org.luke.diminou.app.pages.join.online.Invited;
import org.luke.diminou.app.pages.join.online.Join;
import org.luke.diminou.data.beans.Room;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.Iterator;

public class Home extends Page {
    private final Top top;
    private final Bottom bottom;
    private final FragmentPane content;

    public Home(App owner) {
        super(owner);

        top = new Top(owner);
        top.setZ(5);

        content = new FragmentPane(owner, HomeFragment.class);
        content.setClipToPadding(false);
        content.setZ(1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.weight = 1;
        content.setLayoutParams(params);

        bottom = new Bottom(owner, content);
        bottom.setZ(5);

        VBox root = new VBox(owner);
        root.setClipChildren(false);

        root.addView(top);
        root.addView(content);
        root.addView(bottom);

        addView(root);
        applyStyle(owner.getStyle());
    }

    @Override
    public void setup() {
        super.setup();
        owner.putOnline(true);

        User user = owner.getUser();

        top.setup(user);

        top.setAlpha(0);
        top.setTranslationY(-ViewUtils.by(owner));

        bottom.setAlpha(0);
        bottom.setTranslationY(ViewUtils.by(owner));

        content.setAlpha(0);
        content.setScaleX(.7f);
        content.setScaleY(.7f);

        if(content.isEmpty()) {
            content.nextInto(Play.class);
        }


        new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(top, 1))
                .addAnimation(new TranslateYAnimation(top, 0))
                .addAnimation(new AlphaAnimation(bottom, 1))
                .addAnimation(new TranslateYAnimation(bottom, 0))
                .addAnimation(new AlphaAnimation(content, 1))
                .addAnimation(new ScaleXYAnimation(content, 1))
                .setInterpolator(Interpolator.OVERSHOOT)
                .start();

        registerSocket();
    }

    private void registerSocket() {
        User user = owner.getUser();
        registeredListeners.forEach(owner.getMainSocket()::off);
        registeredListeners.clear();
        addSocketEventHandler("user_sync", obj -> {
            for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                String key = it.next();
                user.set(key, obj.get(key));
            }
        });
        addSocketEventHandler("user_change", obj ->
                User.getForId(obj.getInt("user_id"), u -> {
                    for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                        String key = it.next();
                        u.set(key, obj.get(key));
                    }
        }));
        addSocketEventHandler("request_sent", obj -> {
            int sender = obj.getInt("sender");
            int receiver = obj.getInt("receiver");

            if(sender == owner.getUser().getId()) {
                User.getForId(receiver, u -> u.setFriend("pending_sent"));
            }else {
                User.getForId(sender, u -> u.setFriend("pending_received"));
            }

            Friends instance = (Friends) HomeFragment.getInstance(owner, Friends.class);
            if(instance != null)
                instance.displayFriends();
        });

        addSocketEventHandler("request_canceled", obj -> {
            int sender = obj.getInt("sender");
            int receiver = obj.getInt("receiver");

            if(sender == owner.getUser().getId()) {
                User.getForId(receiver, u -> u.setFriend("none"));
            }else {
                User.getForId(sender, u -> u.setFriend("none"));
            }

            Friends instance = (Friends) HomeFragment.getInstance(owner, Friends.class);
            if(instance != null)
                instance.displayFriends();
        });

        addSocketEventHandler("request_accepted", obj -> {
            int sender = obj.getInt("sender");
            int receiver = obj.getInt("receiver");

            int other_id = sender == owner.getUser().getId() ? receiver : sender;

            User.getForId(other_id, u -> u.setFriend("friend"));

            Friends instance = (Friends) HomeFragment.getInstance(owner, Friends.class);
            if(instance != null)
                instance.displayFriends();
        });

        addSocketEventHandler("end", data -> {
            Room room = new Room(data.getJSONObject("game"));
            if(owner.getLoaded() instanceof Join join &&
                    room.getId().equals(join.getRoomId())) {
                owner.loadPage(Home.class);
                owner.toast("room_ended");
            }
        });

        addSocketEventHandler("invite", data -> {
            int from = data.getInt("from");
            Room room = new Room(data.getJSONObject("game"));

            User.getForId(from, u ->
                    new Invited(owner, u.getUsername(), room).show());
        });

        addSocketEventHandler("join", data -> {
            int userId = data.getInt("user_id");
            Room room = new Room(data.getJSONObject("game"));

            if(userId == owner.getUser().getId()) {
                owner.putRoom(room);
                owner.loadPage(Join.class);
            } else {
                Page loaded = owner.getLoaded();
                if(loaded instanceof Host host) {
                    host.joined(userId);
                } else if(loaded instanceof Join join) {
                    join.joined(userId);
                }
            }
        });

        addSocketEventHandler("leave", data -> {
            int userId = data.getInt("user_id");

            if(userId != owner.getUser().getId()) {
                Page loaded = owner.getLoaded();
                if(loaded instanceof Host host) {
                    host.left(userId);
                } else if(loaded instanceof Join join) {
                    join.left(userId);
                }
            }
        });

        addSocketEventHandler("kicked", data -> {
            if(owner.getLoaded() instanceof Join) {
                owner.loadPage(Home.class);
                owner.toast("host kicked you");
            }
        });

        addSocketEventHandler("swap", data -> {
            int i1 = data.getInt("i1");
            int i2 = data.getInt("i2");

            Page loaded = owner.getLoaded();
            if(loaded instanceof Join join) {
                join.swap(i1, i2);
            }
        });
    }

    private final ArrayList<String> registeredListeners = new ArrayList<>();
    private void addSocketEventHandler(String event, ObjectConsumer<JSONObject> handler) {
        registeredListeners.add(event);
        owner.getMainSocket().off(event);
        owner.getMainSocket().on(event,
                data -> Platform.runLater(() -> {
                    try {
                        handler.accept(new JSONObject(data[0].toString()));
                    } catch (Exception e) {
                        ErrorHandler.handle(e, "handling socket event " + event);
                    }
                }));
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public void applyInsets(Insets insets) {
        ViewUtils.setMarginTop(top, insets.top);
        int ten = ViewUtils.dipToPx(10, owner);
        ViewUtils.setMargin(bottom, ten, 0, ten, ten + insets.bottom);
    }

    @Override
    public void applyStyle(Style style) {
        //TODO apply style
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
