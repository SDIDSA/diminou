package org.luke.diminou.app.pages.home.online.global;

import android.view.View;

import androidx.annotation.DrawableRes;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.components.controls.scratches.ColoredSeparator;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.layout.fragment.FragmentPane;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.home.online.ConfirmLogout;
import org.luke.diminou.app.pages.home.online.friends.Friends;
import org.luke.diminou.app.pages.home.online.play.Play;
import org.luke.diminou.app.pages.home.online.store.Store;

public class Bottom extends HomePanel {
    private final FragmentPane parent;
    public Bottom(App owner, FragmentPane parent) {
        super(owner);
        ViewUtils.setPadding(this, 0, 10, 0 ,10, owner);
        this.parent = parent;
        addItem(R.drawable.play, Play.class);
        addItem(R.drawable.friends, Friends.class);
        addItem(R.drawable.store, Store.class);
        addItem(R.drawable.logout, () -> {
            new ConfirmLogout(owner).show();
        });

        select(R.drawable.play);
    }

    private void navigateInto(Class<? extends HomeFragment> type, NavItem os, NavItem ns) {
        if(os == null) {
            parent.nextInto(type);
            return;
        }
        int oi = indexOfChild(os);
        int ni = indexOfChild(ns);
        if(ni > oi) {
            parent.nextInto(type);
        }else {
            parent.previousInto(type);
        }
    }

    public void addItem(@DrawableRes int res, Class<? extends HomeFragment> type) {
        addItem(res, () -> {
            if(parent.isRunning()) return;
            navigateInto(type, selected, find(res));
            select(res);
        });
    }

    public void addItem(@DrawableRes int res, Runnable action) {
        if(getChildCount() > 0) {
            addView(new ColoredSeparator(getOwner(), Orientation.VERTICAL, 0,
                    Style::getTextMuted));
        }
        NavItem item = new NavItem(getOwner(), res);
        item.setOnClick(() -> {
            if(action != null) {
                action.run();
            }
        });
        addView(item);
    }

    private NavItem selected = null;
    private void select(NavItem item) {
        if(selected != null) selected.deselect();

        selected = item;
        selected.select();
    }

    private void select(@DrawableRes int res) {
        select(find(res));
    }

    private NavItem find(@DrawableRes int res) {
        for(int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(child instanceof NavItem item && item.getRes() == res) {
                return item;
            }
        }
        throw new IllegalStateException("can't find item");
    }
}
