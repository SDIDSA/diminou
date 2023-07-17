package org.luke.diminou.app.pages.home.online.friends;

import android.graphics.Color;
import android.view.Gravity;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.input.MinimalInputField;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.home.online.global.HomeFragment;

public class Friends extends HomeFragment implements Styleable {
    private final MinimalInputField search;
    private final ColoredIcon requests;

    private final VBox display;
    private final ColoredLabel hint;
    public Friends(App owner) {
        super(owner);

        HBox top = new HBox(owner);
        top.setGravity(Gravity.CENTER_VERTICAL);

        setClipChildren(false);

        search = new MinimalInputField(owner, "Search by username...");
        search.setRadius(15);
        LayoutParams lp = new LayoutParams(-2, ViewUtils.dipToPx(50, owner));
        lp.weight = 1;
        search.setLayoutParams(lp);

        ColoredIcon sicon = new ColoredIcon(owner, Style::getTextNormal, R.drawable.search);
        ViewUtils.setPaddingUnified(sicon, 13, owner);
        search.addPostInput(sicon);

        requests = new ColoredIcon(owner, Style::getTextNormal, R.drawable.friend_request);
        requests.setSize(50);
        requests.setCornerRadius(15);
        ViewUtils.setPaddingUnified(requests, 15, owner);
        ViewUtils.setMarginRight(search, owner, 10);

        requests.setOnClick(() -> owner.loadPage(FriendRequests.class));

        top.addView(search);
        top.addView(requests);

        hint = new ColoredLabel(owner, "You don't have friends :(", Style::getTextMuted);

        display = new VBox(owner);
        display.setGravity(Gravity.CENTER);
        LayoutParams dlp = new LayoutParams(-1, -2);
        dlp.weight = 1;
        display.setLayoutParams(dlp);

        display.addView(hint);

        addView(top);
        addView(display);

        applyStyle(owner.getStyle());
    }

    private void displayHint(String hintText) {
        hint.setKey(hintText);
        display.removeAllViews();
        display.addView(hint);
    }

    @Override
    public void setup(boolean direction) {
        super.setup(direction);
    }

    @Override
    public void applyStyle(Style style) {
        if(search == null) return;
        super.applyStyle(style);

        search.setBackground(style.getBackgroundPrimary());
        search.setBorderColor(Color.TRANSPARENT);

        requests.setBackgroundColor(style.getBackgroundPrimary());
    }
}
