package org.luke.diminou.app.pages.home.online.friends;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.input.MinimalInputField;
import org.luke.diminou.abs.components.controls.scratches.Loading;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.home.online.global.HomeFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Friends extends HomeFragment implements Styleable {
    private final int MIN_SEARCH = 2;
    private final MinimalInputField search;
    private final ColoredIcon requests;

    private final VBox display;
    private final ColoredLabel hint;

    private final ColoredLabel friends;

    private final ColoredLabel pending;

    private final Loading loading;
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

        hint = new ColoredLabel(owner, "", Style::getTextMuted);
        hint.setLineSpacing(6);
        hint.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        ViewUtils.setMarginTop(hint, owner, 30);

        friends = new ColoredLabel(owner, "Friends", Style::getTextNormal);
        friends.setFont(new Font(18));
        friends.setLayoutParams(new LayoutParams(-1, -2));

        pending = new ColoredLabel(owner, "Pending requests", Style::getTextNormal);
        pending.setFont(new Font(18));
        pending.setLayoutParams(new LayoutParams(-1, -2));

        display = new VBox(owner);
        display.setSpacing(10);
        display.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        LayoutParams dlp = new LayoutParams(-1, -2);
        dlp.weight = 1;
        display.setLayoutParams(dlp);

        loading = new Loading(owner, 10);
        ViewUtils.setMarginTop(loading, owner, 30);

        display.addView(hint);

        ScrollView dScroll = new ScrollView(owner);
        dScroll.setScrollBarSize(0);
        setClipChildren(true);
        dScroll.addView(display);

        addView(top);
        addView(dScroll);

        search.valueProperty().addListener((obs, ov, nv) -> {
            if(ov.length() >= MIN_SEARCH && nv.length() < MIN_SEARCH) {
                sicon.setImageResource(R.drawable.search);
                displayFriends();
            }else if(nv.length() >= MIN_SEARCH) {
                sicon.setImageResource(R.drawable.close);
                searchFor(nv);
            }
        });

        sicon.setOnClick(() -> search.setValue(""));

        applyStyle(owner.getStyle());
    }

    private long runningSearch = -1;
    private void searchFor(String searchFor) {
        displayLoading();
        final long command = System.currentTimeMillis();
        runningSearch = command;
        Session.getForUsername(searchFor, res -> {
            if(runningSearch != command) return;

            JSONArray matches = res.getJSONArray("matches");
            if(matches.length() == 0) {
                displayHint("No players were found, try a different combination of letters :)");
            } else {
                Platform.runBack(() -> {
                    View[] views = IntStream.range(0,matches.length()).mapToObj(i-> {
                        try {
                            int id = matches.getInt(i);
                            return UserDisplay.get(getOwner(), id);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }).toArray(View[]::new);
                    Platform.runLater(() -> display(views));
                });
            }
        });
    }

    public void displayFriends() {
        if(search.getValue().length() >= MIN_SEARCH) return;
        displayLoading();
        Session.getFriends(res -> {
            ArrayList<View> toDisplay = new ArrayList<>();
            JSONArray friends = res.getJSONArray("friends");
            if(friends.length() == 0) {
                displayHint("You don't have friends :(");
                toDisplay.add(hint);
            } else {
                List<View> friendViews = IntStream.range(0,friends.length()).mapToObj(i-> {
                    try {
                        int id = friends.getInt(i);
                        return UserDisplay.get(getOwner(), id);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
                toDisplay.addAll(0, friendViews);
                toDisplay.add(0, this.friends);
            }
            Session.getRequests(reqs -> {
                JSONArray requests = reqs.getJSONArray("requests");
                if(requests.length() > 0) {
                    List<View> reqViews = IntStream.range(0,requests.length()).mapToObj(i-> {
                        try {
                            int id = requests.getInt(i);
                            return UserDisplay.get(getOwner(), id);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
                    toDisplay.addAll(0, reqViews);
                    toDisplay.add(0, pending);
                }
                display(toDisplay.toArray(new View[0]));
            });
        });
    }

    private void displayLoading() {
        display(loading);
        loading.startLoading();
    }

    private void displayHint(String hintText) {
        hint.setKey(hintText);
        display(hint);
    }

    private void display(View... views) {
        loading.stopLoading();
        display.removeAllViews();
        for(View view : views) {
            display.addView(view);
        }
    }

    @Override
    public void setup(boolean direction) {
        super.setup(direction);

        search.setValue("");
        displayFriends();
    }

    @Override
    public void applyStyle(Style style) {
        if(search == null) return;
        super.applyStyle(style);

        search.setBackground(style.getBackgroundPrimary());
        search.setBorderColor(Color.TRANSPARENT);

        requests.setBackgroundColor(style.getBackgroundPrimary());

        loading.setFill(style.getTextMuted());
    }
}
