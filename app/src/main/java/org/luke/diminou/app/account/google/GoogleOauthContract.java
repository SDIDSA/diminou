package org.luke.diminou.app.account.google;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.luke.diminou.R;
import org.luke.diminou.abs.utils.ErrorHandler;

public class GoogleOauthContract extends ActivityResultContract<String, GoogleSignInAccount> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, String s) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("698119596713-2okvicq74r2bflr0d41m2rlfqaku7a02.apps.googleusercontent.com")
                .requestEmail()
                .requestId()
                .build();
        GoogleSignInClient gsc = GoogleSignIn.getClient(context, gso);
        return gsc.getSignInIntent();
    }

    @Override
    public GoogleSignInAccount parseResult(int i, @Nullable Intent data) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            return task.getResult(ApiException.class);
        } catch (Exception e) {
            ErrorHandler.handle(e, "loging in with google");
            return null;
        }
    }
}
