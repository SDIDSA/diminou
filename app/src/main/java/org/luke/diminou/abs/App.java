package org.luke.diminou.abs;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import org.luke.diminou.abs.components.layout.StackPane;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.luke.diminou.R;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.components.layout.overlay.Overlay;
import org.luke.diminou.abs.net.SocketConnection;
import org.luke.diminou.abs.locale.Locale;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.SplashScreen;
import org.luke.diminou.app.pages.game.player.Player;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.app.pages.settings.Timer;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class App extends AppCompatActivity {
    private final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
    private final ArrayList<Overlay> loadedOverlay = new ArrayList<>();
    public Style dark, light;
    public Locale ar_ar;
    Animation running = null;
    private StackPane root;
    private Page loaded;
    private Property<Style> style;
    private Property<Locale> locale;
    private Insets systemInsets;
    private final ConcurrentHashMap<Integer, MediaPlayer> sounds = new ConcurrentHashMap<>();
    private View old = null;

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("main", Looper.getMainLooper().getThread().getName());
        Platform.runLater(() ->
                Log.i("real main", Thread.currentThread().getName()));

        Store.init(this);

        ViewUtils.scale = Float.parseFloat(Store.getScale());

        Log.i("4px", String.valueOf(ViewUtils.pxToDip(4, this)));

        dark = new Style(this, "dark", true);
        light = new Style(this, "light", false);

        style = new Property<>();
        applyTheme();

        style.addListener((obs, ov, nv) ->
                Platform.runLater(() -> applyStyle(nv)));

        root = new StackPane(this);
        root.setClipChildren(false);
        setContentView(root);

        loadPage(SplashScreen.class);

        WindowCompat.setDecorFitsSystemWindows(this.getWindow(), false);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, ins) -> {
            this.systemInsets = ins.getInsets(WindowInsetsCompat.Type.systemBars());
            if (loaded != null) {
                loaded.applyInsets(systemInsets);
            }
            if (!loadedOverlay.isEmpty()) {
                for (Overlay overlay : loadedOverlay) {
                    overlay.applySystemInsets(systemInsets);
                }
            }
            return WindowInsetsCompat.CONSUMED;
        });

        new Thread(() -> {
            new Locale(this, "fr_FR", Font.DEFAULT_FAMILY_LATIN);
            new Locale(this, "en_US", Font.DEFAULT_FAMILY_LATIN);
            ar_ar = new Locale(this, "ar_AR", Font.DEFAULT_FAMILY_ARABIC);

            Font.init(this);

            locale = new Property<>(Locale.forName(Store.getLanguage()));
            locale.addListener((obs, ov, nv) -> {
                Platform.runLater(() -> {
                    if (nv == ar_ar) {
                        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                    } else {
                        root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    }
                });

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    Font.DEFAULT_FAMILY = nv.getFontFamily();
                    Font.DEFAULT = new Font(nv.getFontFamily());
                }
            });
        }, "app_init_thread").start();
    }

    public Bitmap screenCap() {
        return getBitmapFromView(root);
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private boolean paused = false;
    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
        muteAmbient();
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        if(Store.getAmbient().equals("off")) muteAmbient();
        else unmuteAmbient();
    }

    public void startAmbient() {
        MediaPlayer mp = getSound(R.raw.ambient);
        mp.setLooping(true);
        mp.start();
        if(Store.getAmbient().equals("off")) muteAmbient();
        else unmuteAmbient();
    }

    public void applyAmbient(boolean b) {
        Store.setAmbient(b ? "on" : "off", null);
        if(b) unmuteAmbient();
        else muteAmbient();
    }

    public void muteAmbient() {
        Objects.requireNonNull(getSound(R.raw.ambient)).setVolume(0, 0);
    }

    public void unmuteAmbient() {
        Objects.requireNonNull(getSound(R.raw.ambient)).setVolume(.6f, .6f);
    }

    private synchronized MediaPlayer getSound(@RawRes int res) {
        MediaPlayer mp = sounds.get(res);
        if(mp == null) {
            mp = MediaPlayer.create(this, res);
            sounds.put(res, mp);
        }
        mp.setVolume(1, 1);
        return mp;
    }

    public synchronized void playMenuSound(@RawRes int res) {
        if(Store.getMenuSounds().equals("on")) playSound(res);
    }

    public synchronized void playGameSound(@RawRes int res) {
        if(Store.getGameSounds().equals("on")) playSound(res);
    }

    private synchronized void playSound(@RawRes int res) {
        if(paused) return;
        MediaPlayer mp = getSound(res);
        try {
            mp.seekTo(0);
            mp.start();
        }catch(Exception x) {
            sounds.remove(res);
            playSound(res);
        }
    }

    public void reloadPage() {
        if (loaded != null) {
            Platform.runLater(() -> root.removeView(loaded));
            Platform.runAfter(() -> root.addView(loaded, 0), 100);
        }
    }

    public void loadPage(Class<? extends Page> pageType) {
        loadPage(pageType, null);
    }

    public void loadPage(Class<? extends Page> pageType, Runnable post) {
        hideKeyboard();
        Thread init = new Thread(() -> {
            if (running != null && running.isRunning()) return;

            if (loaded != null && pageType.isInstance(loaded) && Page.hasInstance(pageType)) return;

            AtomicReference<Page> page = new AtomicReference<>();
            Page old = loaded;
            if (old != null) {
                if(!(old instanceof SplashScreen))
                    playMenuSound(R.raw.page);
                running = new ParallelAnimation(500).addAnimation(new AlphaAnimation(old, 0)).addAnimation(new TranslateYAnimation(old, ViewUtils.dipToPx(-30, this))).setInterpolator(Interpolator.EASE_OUT).setOnFinished(() -> {
                    root.removeView(old);
                    old.destroy();
                    if (post != null) post.run();
                });
                running.start();
            }

            new Thread(() -> {
                if (old != null)
                    Platform.sleep(250);
                while (page.get() == null) {
                    Platform.sleep(10);
                }
                Platform.runLater(() -> {
                    page.get().setAlpha(1);
                    page.get().setup();
                    root.addView(page.get(), 0);
                    page.get().applyStyle(style.get());
                });
            }, "page_fade_in_thread").start();

            page.set(Page.getInstance(this, pageType));

            if (page.get() == null) return;

            Platform.runLater(() -> {
                loaded = page.get();
                loaded.applyInsets(this.getSystemInsets());
                loaded.setAlpha(0);
                loaded.setScaleX(1);
                loaded.setScaleY(1);
                loaded.setTranslationY(0);
            });
        }, "load_page_thread_" + pageType.getSimpleName());

        init.start();
    }

    public void removeLoaded() {
        if (loaded == null) return;

        root.removeView(loaded);
        loaded.destroy();
        loaded = null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Page> T getLoaded() {
        return (T) loaded;
    }

    public void addOverlay(Overlay overlay) {
        removeOverlay(overlay);
        root.addView(overlay);
        overlay.requestLayout();

        overlay.applySystemInsets(systemInsets);

        loadedOverlay.add(0, overlay);
    }

    public void toast(View... views) {
        VBox toast = new VBox(this);
        toast.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        StackPane.LayoutParams params = new StackPane.LayoutParams(
                StackPane.LayoutParams.MATCH_PARENT,
                StackPane.LayoutParams.WRAP_CONTENT);

        int margins = ViewUtils.dipToPx(15, this);
        params.setMargins(margins, margins + systemInsets.top, margins, margins);
        params.gravity = Gravity.TOP;

        toast.setLayoutParams(params);

        toast.setCornerRadius(7);
        toast.setBackground(style.get().getBackgroundPrimary());
        toast.setPadding(15);
        toast.setSpacing(15);

        toast.setElevation(ViewUtils.dipToPx(15, this));

        toast.setAlpha(0);
        toast.setTranslationY(-ViewUtils.dipToPx(30, this));
        toast.setScaleX(.7f);
        toast.setScaleY(.7f);

        for (View view : views) {
            toast.addView(view);
        }

        root.addView(toast);

        ParallelAnimation anim = new ParallelAnimation(300)
                .addAnimation(new AlphaAnimation(toast, 1))
                .addAnimation(new TranslateYAnimation(toast, 0))
                .addAnimation(new ScaleXYAnimation(toast, 1))
                .setInterpolator(Interpolator.OVERSHOOT);

        if (old != null) {
            View finalOld = old;
            anim
                    .addAnimation(new AlphaAnimation(old, 0))
                    .addAnimation(new TranslateYAnimation(old, ViewUtils.dipToPx(30, this)))
                    .setOnFinished(() -> root.removeView(finalOld));
        }

        anim.start();

        Platform.runAfter(() -> {
            if (old != toast) return;
            old = null;
            new ParallelAnimation(400)
                    .addAnimation(new AlphaAnimation(toast, 0))
                    .addAnimation(new TranslateYAnimation(toast, ViewUtils.dipToPx(30, this)))
                    .setInterpolator(Interpolator.EASE_IN)
                    .setOnFinished(() -> root.removeView(toast))
                    .start();
        }, 2000);

        old = toast;
    }

    public void toast(String content, String...params) {
        Label lab = new Label(this, content);
        lab.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        lab.setLineSpacing(10);
        lab.setFont(new Font(18));
        lab.setFill(style.get().getTextNormal());
        for(int i = 0; i < params.length; i++) {
            lab.addParam(i, params[i]);
        }
        toast(lab);
    }

    public Insets getSystemInsets() {
        return systemInsets;
    }

    public void removeOverlay(Overlay overlay) {
        root.removeView(overlay);
        loadedOverlay.remove(overlay);
    }

    public void hideKeyboard() {
        View currentFocus;
        if ((currentFocus = getWindow().getCurrentFocus()) != null) currentFocus.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (!loadedOverlay.isEmpty()) {
            loadedOverlay.get(0).back();
        } else if (loaded == null || !loaded.onBack()) {
            moveTaskToBack(false);
            super.onBackPressed();
        }
    }

    public int getScreenHeight() {
        return root.getHeight();
    }

    public int getScreenWidth() {
        return root.getWidth();
    }

    public Property<Style> getStyle() {
        return style;
    }

    public void setBackgroundColor(int color) {
        int trans = adjustAlpha(color, 0.005f);
        Window win = getWindow();
        root.setBackgroundColor(color);
        win.setStatusBarColor(trans);
        win.setNavigationBarColor(trans);
    }

    public void applyTheme() {
        String theme = Store.getTheme();
        Style s = theme.equals(Style.THEME_DARK) ? dark :
                theme.equals(Style.THEME_LIGHT) ? light :
                        isDarkMode(getResources().getConfiguration()) ? dark : light;

        if(s == style.get()) return;

        if(style.get() != null) {
            Bitmap shot = screenCap();
            ImageView view = new ImageView(this);
            view.setImageBitmap(shot);
            view.setLayoutParams(new StackPane.LayoutParams(-1, -1));
            view.setElevation(500);
            root.addView(view);

            Rect clip = new Rect(0, 0, getScreenWidth(), getScreenHeight());
            view.setClipBounds(clip);

            Animation a = new ValueAnimation(400, s.isDark() ? 0 : getScreenWidth(), s.isDark() ? getScreenWidth() : 0) {
                @Override
                public void updateValue(float v) {
                    if(s.isDark()) clip.left = (int) v;
                    else clip.right = (int) v;
                    view.setClipBounds(clip);
                }
            }
                    .setOnFinished(() -> root.removeView(view))
                    .setInterpolator(Interpolator.EASE_OUT);
            a.start();
        }

        Log.i("style", s.toString());
        style.set(s);
        setTheme(s.isDark() ? R.style.Theme_Diminou_Dark : R.style.Theme_Diminou_Light);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyTheme();
    }

    public void applyStyle(Style style) {
        if(style == null) return;
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        WindowInsetsControllerCompat controllerCompat = new WindowInsetsControllerCompat(window, root);
        controllerCompat.setAppearanceLightStatusBars(style.isLight());
        controllerCompat.setAppearanceLightNavigationBars(style.isLight());
    }

    public boolean isDarkMode(Configuration newConfig) {
        return (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public Property<Locale> getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        Platform.runBack(() -> this.locale.set(locale));
    }

    @SuppressWarnings("unchecked")
    private <T> T getTypedData(String key) {
        return (T) data.get(key);
    }

    public void putData(String key, Object value) {
        if(value == null) data.remove(key);
        else data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public void putString(String key, String value) {
        putData(key, value);
    }

    private String getString(String key) {
        return getTypedData(key);
    }

    public FourMode getFourMode() {
        return FourMode.byText(getString("mode"));
    }

    public Timer getTimer() {
        return Timer.byText(getString("timer"));
    }

    public List<Player> getPlayers() {
        return getTypedData("players");
    }

    public ConcurrentHashMap<Player, Integer> getScore() {
        ConcurrentHashMap<Player, Integer> score = getTypedData("score");
        if(score == null) {
            score = new ConcurrentHashMap<>();
            putData("score", score);
        }
        return score;
    }

    public List<SocketConnection> getSockets() {
        return getTypedData("sockets");
    }

    public SocketConnection getSocket() {
        return getTypedData("socket");
    }

    public Player getWinner() {
        return getTypedData("winner");
    }

    public boolean isHost() {
        return getTypedData("host");
    }
}
