package org.luke.diminou.app.cards;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateXAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColorIcon;
import org.luke.diminou.abs.components.controls.scratches.Loading;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.local.SocketConnection;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.AvatarDisplay;
import org.luke.diminou.app.pages.host.ConfirmKick;
import org.luke.diminou.app.pages.host.Host;
import org.luke.diminou.data.observable.Observable;
import org.luke.diminou.data.property.Property;

import java.util.HashMap;

public class PlayerCard extends VBox implements Styleable {
    private static final HashMap<Integer, PlayerCard> track = new HashMap<>();
    private final Loading loading;
    private final GradientDrawable avatarBack;
    private final FrameLayout preAvatar;
    private final AvatarDisplay avatarDisplay;
    private final Label name;
    private final Property<String> username = new Property<>(null);
    private final Property<String> avatar = new Property<>(null);
    private final Property<SocketConnection> connection = new Property<>(null);
    private final Property<Type> type = new Property<>(Type.EMPTY);
    private final ColorIcon remove;
    private boolean loaded = false;
    private boolean isBeingDragged = false;

    private PlayerCard boundTo;

    private View holder;

    private final int index;

    private final boolean host;
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
        preAvatar = new FrameLayout(owner);
        preAvatar.setLayoutDirection(LAYOUT_DIRECTION_LTR);
        preAvatar.setLayoutParams(new LayoutParams(size, size));
        preAvatar.setBackground(avatarBack);
        preAvatar.setClipToPadding(false);

        loading = new Loading(owner, 5);

        preAvatar.addView(loading);

        name = new Label(owner, "");
        name.setFont(new Font(13));
        name.setMaxWidth(size);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);
        setAlpha(.5f);

        avatarDisplay = new AvatarDisplay(owner);

        ConfirmKick confirmKick = new ConfirmKick(owner);
        confirmKick.setOnYes(() -> {
            if (!connection.isNull())
                connection.get().emit("leave", "kick");
            unloadPlayer();

            Host hp = (Host) Page.getInstance(owner, Host.class);
            assert hp != null;
            hp.updateCards();
        });

        int by = ViewUtils.dipToPx(6, owner);
        remove = new ColorIcon(owner, R.drawable.close);
        remove.setSize(32);
        remove.setCornerRadius(7);
        remove.setTranslationY(-by);
        remove.setTranslationX(by);
        remove.setOnClick(confirmKick::show);
        ViewUtils.setPaddingUnified(remove, 5, owner);
        ViewUtils.alignInFrame(remove, Gravity.TOP | Gravity.END);

        addView(preAvatar);
        addView(name);

        avatar.addListener((obs, ov, nv) -> {
            if (nv == null) {
                preAvatar.removeAllViews();
                preAvatar.addView(loading, 0);
            } else {
                preAvatar.removeAllViews();
                preAvatar.addView(avatarDisplay, 0);
                if (host && type.get() != Type.SELF) preAvatar.addView(remove);

                avatarDisplay.setValue(nv);
            }
        });

        username.addListener((obs, ov, nv) -> {
            if (nv == null) {
                name.setKey("empty");
                setAlpha(.5f);
            } else {
                name.setText(nv);
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
                if (avatar.isNull()) return true;
                ClipData.Item item = new ClipData.Item(String.valueOf(finalId));

                ClipData dragData = new ClipData(
                        (CharSequence) v.getTag(),
                        new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                        item);

                View.DragShadowBuilder myShadow = new CardDrag(getOwner(), this);

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
                        if (e.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) && !avatar.isNull()) {
                            new AlphaAnimation(300, remove, 0).setInterpolator(Interpolator.EASE_OUT).start();
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
                            if(host) {
                                swap(other);

                                Host hp = (Host) Page.getInstance(getOwner(), Host.class);
                                assert hp != null;
                                hp.swap(index, other.index);
                            }else {
                                boundTo.swap(other.boundTo);

                                Host hp = (Host) Page.getInstance(getOwner(), Host.class);
                                assert hp != null;
                                hp.swap(boundTo.index, other.boundTo.index);
                            }


                        } catch (Exception x) {
                            ErrorHandler.handle(x, "swapping cards");
                        }
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        new ParallelAnimation(300)
                                .addAnimation(new ScaleXYAnimation(preAvatar, 1f))
                                .addAnimation(new AlphaAnimation(remove, 1f))
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
        String otherUsername = other.getUsername();
        String otherAvatar = other.getAvatar();
        SocketConnection otherConnection = other.getConnection();
        Type otherType = other.getType();

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

            other.loadPlayer(getUsername(), getAvatar(), getConnection(), getType());

            loadPlayer(otherUsername, otherAvatar, otherConnection, otherType);
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

    public Observable<String> usernameProperty() {
        return username;
    }

    public Observable<String> avatarProperty() {
        return avatar;
    }

    public Observable<Type> typeProperty() {
        return type;
    }

    public void bind(PlayerCard other) {
        unbind();
        username.bind(other.usernameProperty());
        avatar.bind(other.avatarProperty());
        type.bind(other.typeProperty());

        boundTo = other;
        other.boundTo = this;

        initDrag();
    }

    public void unbind() {
        username.unbind();
        avatar.unbind();
        type.unbind();

        if (boundTo != null) {
            boundTo.boundTo = null;
            boundTo = null;
        }
    }

    public Type getType() {
        return type.get();
    }

    public String getUsername() {
        return username.get();
    }

    public String getAvatar() {
        return avatar.get();
    }

    public SocketConnection getConnection() {
        return connection.get();
    }

    public void loadPlayer(String username, String avatar, SocketConnection connection, Type type) {
        unloadPlayer();

        this.type.set(type);
        this.avatar.set(avatar);
        this.username.set(username);
        this.connection.set(connection);

        loaded = true;
    }

    public void loadPlayer(String name, String avatar, Type type) {
        loadPlayer(name, avatar, null, type);
    }

    public void unloadPlayer() {
        if (!loaded) return;

        this.type.set(null);
        this.avatar.set(null);
        this.username.set(null);
        this.connection.set(null);

        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void applyStyle(Style style) {
        avatarBack.setColor(style.getBackgroundTertiary());
        avatarBack.setStroke(ViewUtils.dipToPx(1, getOwner()), style.getTextMuted());

        loading.setFill(style.getTextMuted());

        remove.setBackgroundColor(style.getBackgroundTertiary());
        remove.setFill(style.getTextMuted());
        remove.setBorder(style.getTextMuted());

        name.setFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }

    public enum Type {
        SELF, BOT, PLAYER, EMPTY, DISPLAY
    }
}
