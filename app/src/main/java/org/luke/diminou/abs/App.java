package org.luke.diminou.abs;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

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
import org.luke.diminou.abs.animation.base.ColorAnimation;
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
import org.luke.diminou.abs.locale.Locale;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.SplashScreen;
import org.luke.diminou.app.pages.game.PlaySound;
import org.luke.diminou.data.property.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class App extends AppCompatActivity {
    private final HashMap<String, Object> data = new HashMap<>();
    private final ArrayList<Overlay> loadedOverlay = new ArrayList<>();
    public Style dark, light;
    public Style dark_auto, light_auto;
    public Locale fr_fr, en_us, ar_ar;
    Animation running = null;
    private FrameLayout root;
    private Page loaded;
    private Property<Style> style;
    private Property<Locale> locale;
    private Insets systemInsets;
    private final HashMap<Integer, MediaPlayer> sounds = new HashMap<>();
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

        Store.init(this);

        ViewUtils.scale = Float.parseFloat(Store.getScale());

        dark = new Style(this, "dark", true);
        light = new Style(this, "light", false);

        dark_auto = dark.clone();
        light_auto = light.clone();

        style = new Property<>();
        applyTheme();

        Platform.runLater(() -> style.addListener((obs, ov, nv) -> applyStyle()));

        root = new FrameLayout(this);
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
            fr_fr = new Locale(this, "fr_FR", Font.DEFAULT_FAMILY_LATIN);
            en_us = new Locale(this, "en_US", Font.DEFAULT_FAMILY_LATIN);
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

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                    Font.DEFAULT_FAMILY = nv.getFontFamily();
                    Font.DEFAULT = new Font(nv.getFontFamily());
                }
            });

            loadSound(R.raw.pass);
            loadSound(R.raw.khabet);
            loadSound(R.raw.saket);
            for(PlaySound s : PlaySound.values()) {
                loadSound(s.getRes());
            }
        }, "app_init_thread").start();
    }

    private void loadSound(@RawRes int res) {
        sounds.put(res, MediaPlayer.create(this, res));
    }

    public synchronized void playSound(@RawRes int res) {
        MediaPlayer mp = sounds.get(res);
        assert mp != null;
        mp.seekTo(0);
        mp.start();
    }

    public void reloadPage() {
        if (loaded != null) {
            root.removeView(loaded);
            Platform.runAfter(() -> root.addView(loaded), 50);
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
            if (old != null)
                running = new ParallelAnimation(500).addAnimation(new AlphaAnimation(old, 0)).addAnimation(new TranslateYAnimation(old, ViewUtils.dipToPx(-30, this))).setInterpolator(Interpolator.EASE_OUT).setOnFinished(() -> {
                    root.removeView(old);
                    old.destroy();
                    if (post != null) post.run();
                }).start();

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

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

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

    public void toast(String content) {
        Label lab = new Label(this, content);
        lab.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        lab.setLineSpacing(10);
        lab.setFont(new Font(18));
        lab.setFill(style.get().getTextNormal());
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

    public void setStyle(Style style) {
        this.style.set(style);
    }

    public void setBackgroundColor(int color) {
        int trans = adjustAlpha(color, 0.005f);
        Window win = getWindow();
        int from = root.getBackground() instanceof ColorDrawable ? ((ColorDrawable) root.getBackground()).getColor() : Color.TRANSPARENT;
        ColorAnimation anim = new ColorAnimation(300, from, color) {
            @Override
            public void updateValue(int color) {
                root.setBackgroundColor(color);
                win.setStatusBarColor(trans);
                win.setNavigationBarColor(trans);
            }
        };
        anim.start();
    }

    public void applyTheme() {
        String theme = Store.getTheme();
        style.set(
                theme.equals(Style.THEME_DARK) ? dark :
                        theme.equals(Style.THEME_LIGHT) ? light :
                                isDarkMode(getResources().getConfiguration()) ? dark_auto : light_auto);
        setTheme(style.get().isDark() ? R.style.Theme_Diminou_Dark : R.style.Theme_Diminou_Light);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyTheme();
    }

    public void applyStyle() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        WindowInsetsControllerCompat controllerCompat = new WindowInsetsControllerCompat(window, root);
        controllerCompat.setAppearanceLightStatusBars(style.get().isLight());
        controllerCompat.setAppearanceLightNavigationBars(style.get().isLight());
    }

    public boolean isDarkMode(Configuration newConfig) {
        return (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public Property<Locale> getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale.set(locale);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTypedData(String key) {
        return (T) data.get(key);
    }

    public void putData(String key, Object value) {
        data.put(key, value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public void putString(String key, String value) {
        putData(key, value);
    }

    public String getString(String key) {
        return getTypedData(key);
    }
}
