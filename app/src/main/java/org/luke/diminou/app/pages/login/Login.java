package org.luke.diminou.app.pages.login;

import android.view.Gravity;

import androidx.core.graphics.Insets;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.luke.diminou.R;
import org.luke.diminou.abs.App;
import org.luke.diminou.abs.animation.combine.ParallelAnimation;
import org.luke.diminou.abs.animation.easing.Interpolator;
import org.luke.diminou.abs.animation.view.AlphaAnimation;
import org.luke.diminou.abs.animation.view.position.TranslateYAnimation;
import org.luke.diminou.abs.api.Auth;
import org.luke.diminou.abs.components.Page;
import org.luke.diminou.abs.components.controls.image.ColoredIcon;
import org.luke.diminou.abs.components.controls.scratches.Orientation;
import org.luke.diminou.abs.components.controls.text.ColoredLabel;
import org.luke.diminou.abs.components.controls.text.font.Font;
import org.luke.diminou.abs.components.layout.linear.VBox;
import org.luke.diminou.abs.style.Style;
import org.luke.diminou.abs.style.Styleable;
import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.ViewUtils;
import org.luke.diminou.app.pages.home.offline.OfflineHome;
import org.luke.diminou.app.pages.home.online.Home;
import org.luke.diminou.data.SessionManager;
import org.luke.diminou.data.beans.Bean;
import org.luke.diminou.data.beans.User;
import org.luke.diminou.data.property.Property;

import java.net.URISyntaxException;

public class Login extends Page {
    private final VBox root;
    public Login(App owner) {
        super(owner);

        root = new VBox(owner);
        root.setSpacing(15);
        root.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        ColoredIcon diminou = new ColoredIcon(owner, Style::getTextNormal, R.drawable.diminou);
        diminou.setWidth(250);

        ColoredLabel login = new ColoredLabel(owner, "Login with", Style::getTextNormal);
        login.setFont(new Font(20));

        LoginButton google = new LoginButton(owner, "Google", R.drawable.google);
        LoginButton facebook = new LoginButton(owner, "Facebook", R.drawable.facebook);

        ColoredLabel or = new ColoredLabel(owner, "- Or -", Style::getTextNormal);
        or.setFont(new Font(20));

        LoginButton offline = new LoginButton(owner, "Continue Offline", R.drawable.offline);

        offline.setOnClick(() -> owner.loadPage(OfflineHome.class));

        root.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        root.addView(diminou);
        root.addView(ViewUtils.spacer(owner, Orientation.VERTICAL));
        root.addView(login);
        root.addView(google);
        root.addView(facebook);
        root.addView(or);
        root.addView(offline);

        addView(root);

        google.setOnClick(() -> {
            google.startLoading();
            owner.googleSignIn(acc ->
                    Auth.googleLogIn(acc.getEmail(), res -> {
                        if(res.has("token")) {
                            String token = res.getString("token");
                            int userId = res.getInt("user");
                            Bean.clearCache();
                            SessionManager.storeSession(token,
                                    owner, userId, t ->
                                            User.getForId(userId, user -> {
                                                handleUser(user, token);
                                                google.stopLoading();
                                            }));
                        }else if(res.has("empty")) {
                            Auth.googleSignUp(acc.getEmail(), acc.getGivenName(), upres -> {
                                String token = upres.getString("token");
                                int userId = upres.getInt("user");
                                Bean.clearCache();
                                SessionManager.storeSession(token,
                                        owner, userId, t ->
                                                User.getForId(userId, user -> {
                                                    handleUser(user, token);
                                                    google.stopLoading();
                                                })
                                        );
                                User.getForId(userId, user -> {
                                    handleUser(user, token);
                                    google.stopLoading();
                                });
                            });
                        } else {
                            google.stopLoading();
                            owner.toast("google sign in failed...");
                        }
            }), () -> {
                google.stopLoading();
                owner.toast("google sign in failed...");
            });
        });

        applyStyle(owner.getStyle());
    }

    private void handleUser(User user, String token) {
        owner.putUser(user);
        owner.loadPage(Home.class);
    }

    @Override
    public void setup() {
        super.setup();

        GoogleSignInClient client = GoogleSignIn.getClient(owner, GoogleSignInOptions.DEFAULT_SIGN_IN);
        client.signOut();

        root.setAlpha(0f);
        root.setTranslationY(-ViewUtils.by(owner));

        new ParallelAnimation(400)
                .addAnimation(new AlphaAnimation(root, 1))
                .addAnimation(new TranslateYAnimation(root, 0))
                .setInterpolator(Interpolator.EASE_OUT)
                .start();
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public void applyInsets(Insets insets) {
        int add = ViewUtils.dipToPx(15, owner);
        root.setPadding(
                insets.left + add,
                insets.top + add,
                insets.right + add,
                insets.bottom + add);
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
