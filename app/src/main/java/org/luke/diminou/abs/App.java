package org.luke.diminou.abs;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.luke.diminou.R;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.api.API;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.StackPane;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.components.layout.overlay.Overlay;
import org.luke.diminou.abs.components.layout.overlay.media.MediaPickerOverlay;
import org.luke.diminou.abs.locale.Locale;
import org.luke.diminou.abs.net.SocketConnection;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.NotificationAction;
import org.luke.diminou.abs.utils.Permissions;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;
import org.luke.diminou.app.account.google.GoogleAccountHandler;
import org.luke.diminou.app.account.google.GoogleOauthContract;
import org.luke.diminou.app.pages.SplashScreen;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayer;
import org.luke.diminou.app.pages.settings.FourMode;
import org.luke.diminou.app.pages.settings.Timer;
import org.luke.diminou.data.beans.Room;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.media.Media;
import org.luke.diminou.data.property.Property;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import io.socket.client.IO;
import io.socket.client.Socket;

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

    private GoogleAccountHandler googleHandler;
    private ActivityResultLauncher<String> googleSignIn;

    private final HashMap<Integer, Runnable> onPermission = new HashMap<>();
    private MediaPickerOverlay mediaPicker;

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

        googleSignIn = registerForActivityResult(new GoogleOauthContract(),
                acc -> googleHandler.onAcc(acc));

        Store.init(this);

        ViewUtils.scale = Float.parseFloat(Store.getScale());

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
            createNotificationChannel();
        }, "app_init_thread").start();
    }

    public void googleSignIn(ObjectConsumer<GoogleSignInAccount> onAcc, Runnable onFailure) {
        googleHandler = new GoogleAccountHandler(onAcc, onFailure);
        googleSignIn.launch("google sign in");
    }

    public void requirePermissions(Runnable onGranted, String... permissions) {
        if (isGranted(permissions)) {
            onGranted.run();
        } else {
            requestPermission(onGranted, permissions);
        }
    }

    private void requestPermission(Runnable onGranted, String... permissions) {
        int code = Permissions.permissionRequestCode();
        onPermission.put(code, onGranted);
        requestPermissions(permissions, code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Runnable handler = onPermission.get(requestCode);
        if (handler != null && isGranted(permissions)) {
            handler.run();
            onPermission.remove(requestCode);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isGranted(String[] permissions) {
        for (String permission : permissions) {
            if (!isGranted(permission)) {
                return false;
            }
        }
        return true;
    }

    private boolean isGranted(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void pickImage(ObjectConsumer<Media> onRes) {
        if (mediaPicker == null) {
            mediaPicker = new MediaPickerOverlay(this);
        }
        mediaPicker.setOnMedia(onRes);
        mediaPicker.show();
    }

    public Bitmap screenCap() {
        return getBitmapFromView(root);
    }

    public Bitmap getBitmapFromView(View view) {
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
        if (Store.getAmbient().equals("off")) muteAmbient();
        else unmuteAmbient();

        int id = getIntent().getIntExtra("id", -1);
        Runnable action = id == -1 ? null : onNotification.get(id);
        if(action != null) {
            action.run();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void startAmbient() {
        MediaPlayer mp = getSound(R.raw.ambient);
        mp.setLooping(true);
        mp.start();
        if (Store.getAmbient().equals("off")) muteAmbient();
        else unmuteAmbient();
    }

    public void applyAmbient(boolean b) {
        Store.setAmbient(b ? "on" : "off", null);
        if (b) unmuteAmbient();
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
        if (mp == null) {
            mp = MediaPlayer.create(this, res);
            sounds.put(res, mp);
        }
        mp.setVolume(1, 1);
        return mp;
    }

    public synchronized void playMenuSound(@RawRes int res) {
        if (Store.getMenuSounds().equals("on")) playSound(res);
    }

    public synchronized void playGameSound(@RawRes int res) {
        if (Store.getGameSounds().equals("on")) playSound(res);
    }

    private synchronized void playSound(@RawRes int res) {
        if (paused) return;
        MediaPlayer mp = getSound(res);
        try {
            mp.seekTo(0);
            mp.start();
        } catch (Exception x) {
            sounds.remove(res);
            playSound(res);
        }
    }

    public void reloadPage() {
        if (loaded != null) {
            Platform.runLater(() -> root.removeView(loaded));
            Platform.runAfter(() -> root.addView(loaded, 0), 30);
        }
    }

    public void loadPage(Class<? extends Page> pageType) {
        loadPage(pageType, null);
    }

    public void loadPage(Class<? extends Page> pageType, Runnable post) {
        hideKeyboard();
        Thread init = new Thread(() -> {
            if (running != null && running.isRunning()) return;

            if (loaded != null && pageType.isInstance(loaded) && Page.hasInstance(pageType)) {
                Platform.runLater(loaded::setup);
                return;
            }

            AtomicReference<Page> page = new AtomicReference<>();
            Page old = loaded;
            if (old != null) {
                if (!(old instanceof SplashScreen))
                    playMenuSound(R.raw.page);
                running = new ParallelAnimation(500).addAnimation(new AlphaAnimation(old, 0)).addAnimation(new TranslateYAnimation(old, ViewUtils.dipToPx(-30, this))).setInterpolator(Interpolator.EASE_OUT).setOnFinished(() -> {
                    root.removeView(old);
                    old.destroy(loaded);
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
        loaded.destroy(null);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =
                    new NotificationChannel("main", "diminou", importance);
            channel.enableVibration(true);
            channel.setDescription("main channel to notify users");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private final HashMap<Integer, Runnable> onNotification = new HashMap<>();

    public void notify(String head, String body, Runnable onOpen, NotificationAction...actions) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        int notificationId = new Random().nextInt(Integer.MAX_VALUE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "main")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(head)
                .setContentText(body)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE) //Important for heads-up notification
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(createIntent(onOpen))
                .setAutoCancel(true);

        for(NotificationAction action : actions) {
            builder.addAction(new NotificationCompat.Action(
                    action.getIcon(),
                    action.getText(),
                    createIntent(() -> {
                        notificationManager.cancel(notificationId);
                        action.getAction().run();
                    })
            ));
        }

        Notification notification = builder.build();

        notificationManager.notify(notificationId, notification);
    }

    private PendingIntent createIntent(Runnable action) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.setPackage(null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        int id = new Random().nextInt(Integer.MAX_VALUE);
        intent.putExtra("id", id);

        onNotification.put(id, action);

        return PendingIntent.getActivity(this, id,
                intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void toast(long duration, View... views) {
        VBox toast = new VBox(this);
        toast.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        StackPane.LayoutParams params = new StackPane.LayoutParams(
                StackPane.LayoutParams.MATCH_PARENT,
                StackPane.LayoutParams.WRAP_CONTENT);

        int margins = ViewUtils.dipToPx(15, this);
        params.setMargins(margins, margins, margins, margins + systemInsets.bottom);
        params.gravity = Gravity.BOTTOM;

        toast.setLayoutParams(params);

        toast.setCornerRadius(7);
        toast.setBackground(style.get().getBackgroundPrimary());
        toast.setPadding(15);
        toast.setSpacing(15);

        toast.setElevation(ViewUtils.dipToPx(15, this));

        toast.setAlpha(0);
        toast.setTranslationY(ViewUtils.dipToPx(30, this));
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
                    .addAnimation(new TranslateYAnimation(old, -ViewUtils.dipToPx(30, this)))
                    .setOnFinished(() -> root.removeView(finalOld));
        }

        anim.start();

        Platform.runAfter(() -> {
            if (old != toast) return;
            old = null;
            new ParallelAnimation(400)
                    .addAnimation(new AlphaAnimation(toast, 0))
                    .addAnimation(new TranslateYAnimation(toast, -ViewUtils.dipToPx(30, this)))
                    .setInterpolator(Interpolator.EASE_IN)
                    .setOnFinished(() -> root.removeView(toast))
                    .start();
        }, duration);

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
        toast(2000, lab);
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
        Platform.runLater(() -> {
            int trans = adjustAlpha(color, 0.005f);
            Window win = getWindow();
            root.setBackgroundColor(color);
            win.setStatusBarColor(trans);
            win.setNavigationBarColor(trans);
        });
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

        setBackgroundColor(style.getBackgroundTertiary());
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

    protected void initializeSocket(Runnable onConnect, Runnable onError) {
        try {
            Socket mSocket = IO.socket(API.BASE);
            Runnable off = () -> {
                mSocket.off(Socket.EVENT_CONNECT);
                mSocket.off(Socket.EVENT_CONNECT_ERROR);
            };
            mSocket.on(Socket.EVENT_CONNECT, d -> {
                putMainSocket(mSocket);
                onConnect.run();
                off.run();
            });
            mSocket.on(Socket.EVENT_CONNECT_ERROR, d -> {
                onError.run();
                off.run();
            });
            if(!mSocket.connected()){
                mSocket.connect();
            }
        } catch (URISyntaxException e) {
            ErrorHandler.handle(e, "initializing socket");
        }
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

    public List<OfflinePlayer> getPlayers() {
        return getTypedData("players");
    }

    public ConcurrentHashMap<OfflinePlayer, Integer> getOfflineScore() {
        ConcurrentHashMap<OfflinePlayer, Integer> score = getTypedData("offline_score");
        if(score == null) {
            score = new ConcurrentHashMap<>();
            putData("offline_score", score);
        }
        return score;
    }

    public ConcurrentHashMap<Integer, Integer> getScore() {
        ConcurrentHashMap<Integer, Integer> score = getTypedData("score");
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

    public OfflinePlayer getWinner() {
        return getTypedData("winner");
    }

    public boolean isHost() {
        return getTypedData("host");
    }

    public void putMainSocket(Socket socket) {
        putData("main_socket", socket);
    }

    public Socket getMainSocket() {
        return getTypedData("main_socket");
    }

    public void putUser(User user) {
        putData("logged_in", user);
    }

    public User getUser() {
        return getTypedData("logged_in");
    }

    public void putOnline(boolean online) {
        putData("online", online);
    }

    public boolean isOnline() {
        return getTypedData("online");
    }

    public void putRoom(Room room) {
        putData("room", room);
    }

    public Room getRoom() {
        return getTypedData("room");
    }
}
