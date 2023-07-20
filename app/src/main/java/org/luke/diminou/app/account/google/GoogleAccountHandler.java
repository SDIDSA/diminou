package org.luke.diminou.app.account.google;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.luke.diminou.abs.utils.ErrorHandler;
import org.luke.diminou.abs.utils.functional.ObjectConsumer;

public class GoogleAccountHandler {
    private final ObjectConsumer<GoogleSignInAccount> onAcc;
    private final Runnable onFailure;

    public GoogleAccountHandler(ObjectConsumer<GoogleSignInAccount> onAcc, Runnable onFailure) {
        this.onAcc = onAcc;
        this.onFailure = onFailure;
    }

    public void onAcc(GoogleSignInAccount acc) {
        if(onAcc != null && acc != null) {
            try {
                onAcc.accept(acc);
            } catch (Exception e) {
                ErrorHandler.handle(e, "handling google account");
            }
        }else if(onFailure != null) {
            onFailure.run();
        }
    }
}
