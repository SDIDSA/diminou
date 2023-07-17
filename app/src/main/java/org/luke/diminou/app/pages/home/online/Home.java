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
import org.luke.diminou.app.pages.home.online.global.Bottom;
import org.luke.diminou.app.pages.home.online.global.HomeFragment;
import org.luke.diminou.app.pages.home.online.global.Top;
import org.luke.diminou.app.pages.home.online.play.Play;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.Iterator;

public class Home extends Page {
    private final VBox root;
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

        content.nextInto(Play.class);

        bottom = new Bottom(owner, content);
        bottom.setZ(5);

        root = new VBox(owner);
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
    }

    private final ArrayList<String> registeredListeners = new ArrayList<>();
    private void addSocketEventHandler(String event, ObjectConsumer<JSONObject> handler) {
        registeredListeners.add(event);
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
