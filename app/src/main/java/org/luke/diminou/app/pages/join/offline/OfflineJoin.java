package org.luke.diminou.app.pages.join.offline;

import android.view.Gravity;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.base.Animation;
import org.luke.diminou.abs.animation.base.ValueAnimation;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.animation.view.scale.ScaleXYAnimation;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.button.Button;
import org.luke.diminou.abs.components.controls.image.Image;
import org.luke.diminou.abs.components.controls.scratches.Loading;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.Label;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.HBox;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.net.Local;
import org.luke.diminou.abs.net.SocketConnection;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.Platform;
import org.luke.diminou.abs.utils.Store;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.avatar.Avatar;
import org.luke.diminou.app.cards.offline.OfflineDisplayCards;
import org.luke.diminou.app.cards.offline.OfflinePlayerCard;
import org.luke.diminou.app.pages.Titled;
import org.luke.diminou.app.pages.game.offline.OfflineGame;
import org.luke.diminou.app.pages.game.offline.player.OfflinePlayer;
import org.luke.diminou.app.pages.home.offline.OfflineHome;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class OfflineJoin extends Titled {

    private final Button leave;
    private final Label searching;
    private final VBox found;
    private final HBox bottom;
    private final Label joiningAs;
    private final Label username;
    private final Image avatar;

    private final Loading loading;

    private final OfflineDisplayCards cards;

    private volatile static boolean destroyed = false;

    private final Animation shrinkFound;
    private final Animation hideSearching;
    private final Animation showLeave;
    private final Animation showCards;

    private final Animation expandFound;
    private final Animation showSearching;
    private final Animation hideLeave;
    private final Animation hideCards;

    public OfflineJoin(App owner) {
        super(owner, "join_party");

        searching = new Label(owner, "scanning_network");
        searching.setFont(new Font(18));

        found = new VBox(owner);
        found.setGravity(Gravity.CENTER);
        found.setCornerRadius(7);
        found.setPadding(10);
        found.setSpacing(10);
        found.setMinimumHeight(ViewUtils.dipToPx(300, owner));

        loading = new Loading(owner, 14);

        joiningAs = new Label(owner, "joining_as");
        joiningAs.setFont(new Font(18));

        username = new Label(owner, "");
        username.setFont(new Font(18));

        avatar = new Image(owner);
        avatar.setSize(32);
        ViewUtils.setMarginRight(avatar, owner, 10);
        avatar.setCornerRadius(7);

        bottom = new HBox(owner);
        bottom.setGravity(Gravity.CENTER);
        bottom.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewUtils.dipToPx(32 + 20, owner)));
        bottom.setPadding(10);
        bottom.setCornerRadius(7);

        bottom.addView(joiningAs);
        bottom.addView(ViewUtils.spacer(owner, Orientation.HORIZONTAL));
        bottom.addView(avatar);
        bottom.addView(username);

        content.addView(searching);
        content.addView(found);
        content.addView(bottom);

        leave = new Button(owner, "leave");
        leave.setFont(new Font(22));
        ViewUtils.setPadding(leave, 15,0,15,0, owner);
        leave.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        ViewUtils.dipToPx(36, owner)));
        getPreTitle().addView(leave);

        cards = new OfflineDisplayCards(owner, false);
        ViewUtils.setPadding(cards,0, 30, 0, 0, owner);
        content.addView(cards);

        shrinkFound = new ValueAnimation(400,
                ViewUtils.dipToPx(300, owner),
                ViewUtils.dipToPx(52, owner)) {
            @Override
            public void updateValue(float v) {
                found.setMinimumHeight((int) v);
            }
        }.setLateFromInt(found::getMinimumHeight).
                setInterpolator(Interpolator.EASE_OUT);

        int by = -ViewUtils.dipToPx(15, owner);
        hideSearching = new ParallelAnimation(400)
                .addAnimation(new ValueAnimation(400, 0, 0) {
                    @Override
                    public void updateValue(float v) {
                        searching.setMaxHeight((int) v);
                    }
                }.setLateFromInt(searching::getHeight))
                .addAnimation(new TranslateYAnimation(found, by))
                .addAnimation(new TranslateYAnimation(bottom, by))
                .addAnimation(new TranslateYAnimation(cards, by))
                .setInterpolator(Interpolator.EASE_OUT);

        showLeave = new ParallelAnimation(400)
                .addAnimation(new TranslateYAnimation(leave, 0))
                .addAnimation(new AlphaAnimation(leave, 1))
                .setInterpolator(Interpolator.EASE_OUT);

        showCards = new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(cards, 1))
                .addAnimation(new ScaleXYAnimation(cards, 1))
                .setInterpolator(Interpolator.EASE_OUT);

        /////////////

        expandFound = new ValueAnimation(400,
                ViewUtils.dipToPx(52, owner),
                ViewUtils.dipToPx(300, owner)) {
            @Override
            public void updateValue(float v) {
                found.setMinimumHeight((int) v);
            }
        }.setLateFromInt(found::getMinimumHeight)
                .setInterpolator(Interpolator.EASE_OUT);

        showSearching = new ParallelAnimation(400)
                .addAnimation(new ValueAnimation(400, 0, ViewUtils.dipToPx(26, owner)) {
                    @Override
                    public void updateValue(float v) {
                        searching.setMaxHeight((int) v);
                    }
                }.setLateFromInt(searching::getHeight))
                .addAnimation(new TranslateYAnimation(found, 0))
                .addAnimation(new TranslateYAnimation(bottom, 0))
                .setInterpolator(Interpolator.EASE_OUT);

        hideLeave = new ParallelAnimation(400)
                .addAnimation(new TranslateYAnimation(leave, -40))
                .addAnimation(new AlphaAnimation(leave, 0))
                .setInterpolator(Interpolator.EASE_OUT);

        hideCards = new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(cards, by))
                .addAnimation(new ScaleXYAnimation(cards, .7f))
                .setInterpolator(Interpolator.EASE_OUT);

        applyStyle(owner.getStyle());
    }

    private final ArrayList<PartyDisplay> parties = new ArrayList<>();

    private volatile static Thread loader;
    @Override
    public void setup() {
        super.setup();
        destroyed = false;
        username.setText(Store.getUsername());
        avatar.setImageResource(Avatar.valueOf(Store.getAvatar()).getRes());
        found.setGravity(Gravity.CENTER);
        found.removeView(loading);
        found.addView(loading);
        loading.startLoading();
        found.setMinimumHeight(ViewUtils.dipToPx(300, owner));

        cards.setAlpha(0);
        cards.setScaleX(.7f);
        cards.setScaleY(.7f);
        leave.setTranslationY(-30);
        leave.setAlpha(0);

        parties.forEach(p -> {
            p.getData().getConnection().stop();
            found.removeView(p);
        });
        parties.clear();

        if(loader != null) loader.interrupt();
        loader = scanningThread();
        loader.start();
    }

    private volatile static PartyDisplay joined = null;
    private void joined(PartyDisplay pd) {
        joined = pd;

        destroyed = true;
        if(loader != null) {
            loader.interrupt();
        }

        Platform.runLater(() -> {
            found.removeAllViews();
            found.addView(pd);
        });

        new ParallelAnimation(400)
                .addAnimation(shrinkFound)
                .addAnimation(hideSearching)
                .addAnimation(getHideBack())
                .addAnimation(showLeave)
                .addAnimation(showCards)
                .setInterpolator(Interpolator.OVERSHOOT)
                .setOnFinished(() -> {
                    content.removeView(searching);
                    found.setTranslationY(0);
                    bottom.setTranslationY(0);
                    cards.setTranslationY(0);
                })
                .start();

        Platform.runLater(() ->  leave.setOnClick(() -> {
            pd.getData().getConnection().emit("leave", "");
            left();
        }));

    }

    private void left() {
        found.removeAllViews();
        Platform.runAfter(() -> parties.forEach(found::addView), 100);

        int by = -ViewUtils.dipToPx(15, owner);

        found.setTranslationY(by);
        bottom.setTranslationY(by);
        cards.setTranslationY(by);
        content.removeView(searching);
        content.addView(searching, 0);

        new ParallelAnimation(400)
                .addAnimation(expandFound)
                .addAnimation(showSearching)
                .addAnimation(getShowBack())
                .addAnimation(hideLeave)
                .addAnimation(hideCards)
                .setInterpolator(Interpolator.EASE_OUT)
                .start();

        joined.decrementCount();

        joined = null;
        destroyed = false;
        loader = scanningThread();
        loader.start();
    }

    private static final Semaphore scanningLock = new Semaphore(1);
    private Thread scanningThread() {
        return new Thread(() -> {
            scanningLock.acquireUninterruptibly();
            while(!destroyed && !Thread.currentThread().isInterrupted() && joined == null) {
                try {
                    if(joined != null || destroyed) break;
                    List<Socket> servers = scan();
                    if(joined != null || destroyed) break;

                    Platform.runLater(loading::stopLoading);
                    Platform.runLater(() -> found.removeView(loading));
                    Platform.runLater(() -> found.setGravity(Gravity.START));
                    if(joined != null || destroyed) break;

                    servers.forEach(socket -> {
                        if(joined != null || destroyed) return;
                        SocketConnection server = new SocketConnection(socket);
                        AtomicReference<PartyDisplay> display = new AtomicReference<>();
                        server.on("by", byData -> {
                            if(joined != null || destroyed) return;
                            JSONObject obj = new JSONObject(byData);
                            Party party = new Party(server, obj.getString("username"), obj.getString("avatar"));

                            PartyDisplay pd = new PartyDisplay(owner, party);

                            if(!parties.contains(pd)) {
                                if(joined != null || destroyed) return;
                                pd.setCount(obj.getInt("count"));
                                display.set(pd);
                                parties.add(display.get());
                                pd.setOnClickListener(e -> {
                                    if(joined != null || destroyed) return;
                                    try {
                                        JSONObject client = new JSONObject();
                                        client.put("username", Store.getUsername());
                                        client.put("avatar", Store.getAvatar());
                                        Platform.runBack(()-> server.emit("join", client));
                                    }catch(JSONException x) {
                                        ErrorHandler.handle(x, "joining party");
                                    }
                                });
                                Platform.runLater(() -> found.addView(display.get()));

                                server.on("full", data -> {
                                    if(display.get() != null)
                                        Platform.runLater(() -> owner.toast("This party is full"));
                                });
                                server.on("count", data -> {
                                    if(display.get() != null)
                                        Platform.runLater(() -> display.get().setCount(Integer.parseInt(data)));
                                });
                                server.on("joined", data -> {
                                    if(display.get() != null)
                                        joined(display.get());
                                });
                                server.on("already_in", data -> {
                                    if(display.get() != null)
                                        joined(display.get());
                                });
                                server.on("leave", data -> {
                                    if(joined != null) {
                                        Platform.runLater(this::left);
                                    }
                                    if(data.contains("kick")) Platform.runLater(() -> owner.toast("kicked"));
                                    if(display.get() != null) {
                                        parties.remove(display.get());
                                        Platform.runLater(() -> found.removeView(display.get()));
                                    }
                                });
                                server.setOnError(() -> {
                                        if(display.get() != null) {
                                            if(joined != null && joined == display.get()) {
                                                Platform.runLater(this::left);
                                            }
                                            parties.remove(display.get());
                                            Platform.runLater(() -> found.removeView(display.get()));
                                        }
                                });
                                server.on("players", data -> Platform.runLater(() -> {
                                    try {
                                        cards.unloadAll();
                                        JSONArray arr = new JSONArray(data);
                                        int count = 0;
                                        for(int i = 0; i < arr.length(); i++) {
                                            JSONObject po = arr.getJSONObject(i);
                                            int order = po.getInt("order");
                                            if(!po.has("empty")) {
                                                cards.getAt(order).loadPlayer(
                                                        po.getString("username"),
                                                        po.getString("avatar"),
                                                        OfflinePlayerCard.Type.DISPLAY);
                                                count++;
                                            }
                                        }
                                        pd.setCount(count);
                                    }catch(Exception x) {
                                        ErrorHandler.handle(x, "displaying players");
                                    }
                                }));
                                server.on("swap", data -> {
                                    JSONObject d = new JSONObject(data);
                                    int a = d.getInt("a");
                                    int b = d.getInt("b");
                                    cards.swap(a, b);
                                });
                                server.on("begin", data -> {
                                    Platform.runLater(this::left);
                                    owner.putData("host", false);
                                    JSONObject dataObj = new JSONObject(data);
                                    ArrayList<OfflinePlayer> players = new ArrayList<>();
                                    JSONArray arr = dataObj.getJSONArray("players");
                                    for(int i = 0; i < arr.length(); i++) {
                                        players.add(OfflinePlayer.deserialize(arr.getJSONObject(i)));
                                    }
                                    owner.putData("players", players);
                                    owner.putData("socket", server);
                                    owner.putString("mode", dataObj.getString("mode"));
                                    owner.putString("timer", dataObj.getString("timer"));
                                    owner.loadPage(OfflineGame.class);
                                });
                            } else {
                                party.getConnection().stop();
                            }
                        });

                        server.start();
                        server.emit("by", "");
                    });
                }catch (Exception x) {
                    ErrorHandler.handle(x, "scanning");
                    if(joined != null || destroyed) break;
                }
                if(joined != null || destroyed) break;
            }
            scanningLock.release();
        }, "scanning_thread");
    }

    public List<Socket> scan() {
        ArrayList<Socket> res = new ArrayList<>();

        ArrayList<Thread> ths = new ArrayList<>();
        for(String hit : Local.scanNetwork()) {
            if(joined != null && hit.equals(joined.getData().getConnection().getIp())) {
                //SKIP
                continue;
            }
            Thread th = new Thread(()->{
                try {
                    if(isPresent(hit)) {
                        if(!Objects.requireNonNull(isPresentGet(hit)).getData().getConnection().isRunning()) {
                            PartyDisplay p = isPresentGet(hit);
                            parties.remove(p);
                            Platform.runLater(() -> found.removeView(p));
                        }
                    }else {
                        if(hit != null) {
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(hit, Local.PORT), 1000);
                            res.add(socket);
                        }
                    }
                } catch (IOException e) {
                    //IGNORE
                }
            });
            ths.add(th);
        }

        ths.forEach(Thread::start);
        ths.forEach(th -> {
            try {
                th.join();
            } catch (InterruptedException e) {
                //IGNORE
            }
        });

        return res;
    }

    private boolean isPresent(String ip) {
        for(PartyDisplay party : parties) {
            if(Objects.equals(
                    party.getData().getConnection().getIp(),
                    ip)) {
                return true;
            }
        }
        return false;
    }

    private PartyDisplay isPresentGet(String ip) {
        for(PartyDisplay party : parties) {
            if(Objects.equals(
                    party.getData().getConnection().getIp(),
                    ip)) {
                return party;
            }
        }
        return null;
    }

    @Override
    public void destroy(Page newPage) {
        super.destroy(newPage);
        loading.stopLoading();
        loader.interrupt();
        destroyed = true;
    }

    @Override
    public void applyStyle(Style style) {
        if(found == null) return;
        super.applyStyle(style);

        searching.setFill(style.getTextNormal());

        found.setBackground(style.getBackgroundPrimary());
        found.setBorderColor(style.getTextMuted());

        loading.setFill(style.getTextNormal());

        bottom.setBackground(style.getBackgroundPrimary());
        bottom.setBorderColor(style.getTextMuted());

        joiningAs.setFill(style.getTextNormal());
        username.setFill(style.getTextNormal());
        avatar.setBackgroundColor(style.getTextMuted());

        leave.setFill(style.getBackgroundTertiary());
        leave.setTextFill(style.getTextDanger());
    }

    @Override
    public boolean onBack() {
        if(joined == null)
            owner.loadPage(OfflineHome.class);
        else {
            joined.getData().getConnection().emit("leave", "");
            left();
        }
        return true;
    }
}
