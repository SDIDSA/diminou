package org.luke.diminou.app.cards.online;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.api.Session;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.scratches.Loading;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.StackPane;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.AvatarDisplay;
import org.luke.diminou.app.pages.home.online.friends.Username;
import org.luke.diminou.app.pages.host.ConfirmKick;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerCard extends VBox implements Styleable {
    private static final ConcurrentHashMap<Integer, PlayerCard> track = new ConcurrentHashMap<>();
    private final Loading loading;
    private final GradientDrawable avatarBack;
    private final StackPane preAvatar;
    private final AvatarDisplay avatarDisplay;
    private final Username name;
    private final ColorIcon remove;
    private boolean isBeingDragged = false;

    private PlayerCard boundTo;

    private View holder;

    private final Property<User> user = new Property<>(null);

    private final int index;

    private final boolean host;

    private final int removeBy;
    public PlayerCard(App owner, boolean host, int index) {
        super(owner);

        this.host = host;
        this.index = index;

        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        setSpacing(10);
        setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        avatarBack = new GradientDrawable();
        avatarBack.setCornerRadius(ViewUtils.dipToPx(7, owner));

        int size = ViewUtils.dipToPx(AvatarDisplay.preSize, owner);
        preAvatar = new StackPane(owner);
        preAvatar.setLayoutDirection(LAYOUT_DIRECTION_LTR);
        preAvatar.setLayoutParams(new LayoutParams(size, size));
        preAvatar.setBackground(avatarBack);
        preAvatar.setClipToPadding(false);

        loading = new Loading(owner, 5);

        preAvatar.addView(loading);

        name = new Username(owner);
        name.setFont(new Font(13));
        name.setMaxWidth(size);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);
        setAlpha(.5f);

        avatarDisplay = new AvatarDisplay(owner);
        avatarDisplay.setOnOnlineChanged(online -> applyStyle(owner.getStyle()));

        ConfirmKick confirmKick = new ConfirmKick(owner);
        confirmKick.setOnYes(() -> {
            //KICK

            Session.leave(user.get().getId(), getOwner().getRoom().getId(), res -> {
                if(res.has("err")) owner.toast(res.getString("err"));
            });

            //UPDATE CARDS
        });

        removeBy = ViewUtils.dipToPx(6, owner);
        remove = new ColorIcon(owner, R.drawable.close);
        remove.setSize(28);
        remove.setCornerRadius(7);
        remove.setTranslationY(-removeBy);
        remove.setTranslationX(removeBy);
        remove.setOnClick(() -> {
            if(user.get() != null && !user.get().isSelf()) {
                confirmKick.show();
            }
        });
        ViewUtils.setPaddingUnified(remove, 4, owner);
        ViewUtils.alignInFrame(remove, Gravity.TOP | Gravity.END);

        addView(preAvatar);
        addView(name);

        user.addListener((obs, ov, nv) -> {
            applyStyle(owner.getStyle());
            if (nv == null) {
                preAvatar.removeAllViews();
                preAvatar.addView(loading, 0);
                name.setUser(null);
                name.setKey("empty");
                setAlpha(.5f);
            }else {
                preAvatar.removeAllViews();
                preAvatar.addView(avatarDisplay, 0);
                if(host || (boundTo != null && boundTo.host)) {
                    if (nv.isSelf()) remove.setImageResource(R.drawable.owner);
                    else remove.setImageResource(R.drawable.close);
                    preAvatar.addView(remove);
                }
                avatarDisplay.setUser(nv);
                name.setUser(nv);
                setAlpha(1f);
            }
        });

        initDrag();

        applyStyle(owner.getStyle());
    }

    private void initDrag() {
        if (host || (boundTo != null && boundTo.host)) {
            int id = (int) (Math.random() * 100000);
            while (track.containsKey(id)) {
                id = (int) (Math.random() * 100000);
            }
            track.put(id, this);
            int finalId = id;

            setOnLongClickListener(v -> {
                if (user.isNull()) return true;
                ClipData.Item item = new ClipData.Item(String.valueOf(finalId));

                ClipData dragData = new ClipData(
                        (CharSequence) v.getTag(),
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                        item);

                CardDrag myShadow = new CardDrag(getOwner(), avatarDisplay.getImg());

                getOwner().putData("holder", holder);
                v.startDragAndDrop(dragData,
                        myShadow,
                        null,
                        0
                );
                isBeingDragged = true;

                return true;
            });

            setOnDragListener((v, e) -> {
                if(holder != getOwner().getData("holder")) return false;
                switch (e.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        if (e.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) && !user.isNull()) {
                            new ParallelAnimation(300)
                                    .addAnimations(new AlphaAnimation(remove, 0))
                                    .addAnimation(new TranslateXAnimation(remove, removeBy * 2))
                                    .addAnimation(new TranslateYAnimation(remove, -removeBy * 2))
                                    .setInterpolator(Interpolator.EASE_OUT)
                                    .start();
                            if (isBeingDragged) {
                                new ScaleXYAnimation(300, preAvatar, 1.15f)
                                        .setInterpolator(Interpolator.EASE_OUT)
                                        .start();
                            } else {
                                new ScaleXYAnimation(300, preAvatar, .7f)
                                        .setInterpolator(Interpolator.EASE_OUT)
                                        .start();
                            }
                            return true;
                        }
                        return false;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        if (!isBeingDragged)
                            new ScaleXYAnimation(300, preAvatar, .85f)
                                    .setInterpolator(Interpolator.EASE_OUT)
                                    .start();
                        return true;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        if (!isBeingDragged)
                            new ScaleXYAnimation(300, preAvatar, .7f)
                                    .setInterpolator(Interpolator.EASE_OUT)
                                    .start();
                        return true;

                    case DragEvent.ACTION_DROP:
                        try {
                            int otherId = Integer.parseInt(e.getClipData().getItemAt(0).getText().toString());
                            PlayerCard other = track.get(otherId);
                            assert other != null;

                            if(index != other.index) {
                                if(host) {
                                    swap(other);
                                }else {
                                    boundTo.swap(other.boundTo);
                                }

                                Session.swap(getOwner().getRoom().getId(), index, other.index, res -> {
                                    if(res.has("err")) getOwner().toast(res.getString("err"));
                                });
                            }
                        } catch (Exception x) {
                            ErrorHandler.handle(x, "swapping cards");
                        }
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        new ParallelAnimation(300)
                                .addAnimation(new ScaleXYAnimation(preAvatar, 1f))
                                .addAnimation(new AlphaAnimation(remove, 1f))
                                .addAnimation(new TranslateXAnimation(remove, removeBy))
                                .addAnimation(new TranslateYAnimation(remove, -removeBy))
                                .setInterpolator(Interpolator.EASE_OUT)
                                .start();
                        track.values().forEach(x -> x.isBeingDragged = false);
                        return true;

                    // An unknown action type is received.
                    default:
                        ErrorHandler.handle(new IllegalStateException("unknown action type "
                                .concat(String.valueOf(e.getAction()))), "dragging player card");
                        break;
                }
                return false;
            });
        }
    }

    public void setHolder(View holder) {
        this.holder = holder;
    }

    public void swap(PlayerCard other) {
        if (other == this) return;
        getOwner().playMenuSound(R.raw.swap);

        User otherUser = other.user.get();

        int thisX = getXInParent();
        int otherX = other.getXInParent();

        int duration = 500;

        int by = ViewUtils.dipToPx(AvatarDisplay.preSize / 1.75f, getOwner());

        ParallelAnimation firstHalf = new ParallelAnimation(duration / 2)
                .addAnimation(new TranslateYAnimation(other, -by))
                .addAnimation(new TranslateYAnimation(this, by))
                .setInterpolator(Interpolator.EASE_OUT);

        ParallelAnimation secondHalf = new ParallelAnimation(duration / 2)
                .addAnimation(new TranslateYAnimation(this, by, 0))
                .addAnimation(new TranslateYAnimation(other, -by, 0))
                .setInterpolator(Interpolator.EASE_OUT);

        firstHalf.setOnFinished(secondHalf::start);

        ParallelAnimation anim = new ParallelAnimation(duration)
                .addAnimation(new TranslateXAnimation(this, otherX - thisX))
                .addAnimation(new TranslateXAnimation(other, thisX - otherX))
                .setInterpolator(Interpolator.EASE_OUT);
        anim.start();

        if(boundTo != null) {
            PlayerCard boundToThis = boundTo;

            int boundToThisX = boundToThis.getXInParent();
            int boundToThisY = boundToThis.getYInParent();

            PlayerCard boundToOther = other.boundTo;

            int boundToOtherX = boundToOther.getXInParent();
            int boundToOtherY = boundToOther.getYInParent();

            boundToOther.setElevation(1);
            boundToThis.setElevation(-1);

            firstHalf.addAnimation(new ScaleXYAnimation(boundToOther, 1.3f))
                    .addAnimation(new ScaleXYAnimation(boundToThis, .7f))
                    .addAnimation(new AlphaAnimation(boundToThis, .5f));

            secondHalf.addAnimation(new ScaleXYAnimation(boundToOther, 1))
                    .addAnimation(new ScaleXYAnimation(boundToThis, 1))
                    .addAnimation(new AlphaAnimation(boundToThis, 1));

            anim.addAnimation(new TranslateXAnimation(boundToThis, boundToOtherX - boundToThisX))
                    .addAnimation(new TranslateYAnimation(boundToThis, boundToOtherY - boundToThisY))
                    .addAnimation(new TranslateXAnimation(boundToOther, boundToThisX - boundToOtherX))
                    .addAnimation(new TranslateYAnimation(boundToOther, boundToThisY - boundToOtherY));
        }

        anim.setOnFinished(() -> {
            if(boundTo != null) {
                boundTo.setTranslationX(0);
                boundTo.setTranslationY(0);
                other.boundTo.setTranslationX(0);
                other.boundTo.setTranslationY(0);
            }
            setTranslationX(0);
            other.setTranslationX(0);
            other.loadPlayer(user.get());
            loadPlayer(otherUser);
        });

        firstHalf.start();
        anim.start();
    }

    private int getXInParent() {
        Rect offsetViewBounds = new Rect();
        getDrawingRect(offsetViewBounds);
        getOwner().getLoaded().offsetDescendantRectToMyCoords(this, offsetViewBounds);
        return offsetViewBounds.left;
    }

    private int getYInParent() {
        Rect offsetViewBounds = new Rect();
        getDrawingRect(offsetViewBounds);
        getOwner().getLoaded().offsetDescendantRectToMyCoords(this, offsetViewBounds);
        return offsetViewBounds.top;
    }

    public void bind(PlayerCard other) {
        unbind();
        user.bind(other.user);
        boundTo = other;
        other.boundTo = this;
        initDrag();
    }

    public void unbind() {
        user.unbind();

        if (boundTo != null) {
            boundTo.boundTo = null;
            boundTo = null;
        }
    }

    public void loadPlayer(User user) {
        unloadPlayer(false);
        this.user.set(user);
    }

    public User getUser() {
        return user.get();
    }

    public void unloadPlayer(boolean fix) {
        if (user.isNull()) return;
        user.set(null);
        if(fix) {
            fixParent(this);
        }
    }

    private void fixParent(View view) {
        if(view instanceof DisplayCards disp) {
            disp.fix();
        }else if(view.getParent() instanceof View parent){
            fixParent(parent);
        }
    }

    public boolean isLoaded() {
        return !user.isNull();
    }

    @Override
    public void applyStyle(Style style) {
        avatarBack.setColor(style.getBackgroundTertiary());
        avatarBack.setStroke(ViewUtils.dipToPx(1, getOwner()), style.getTextMuted());

        avatarDisplay.setOnlineBackground(style.getBackgroundTertiary());

        loading.setFill(style.getTextMuted());

        boolean isOnline = (user.get() != null && user.get().isOnline());
        int borderColor = isOnline ? style.getTextPositive() : style.getTextMuted();
        remove.setBackgroundColor(style.getBackgroundTertiary());
        remove.setFill(borderColor);
        remove.setBorder(borderColor);
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
