package com.uwm.wmii.student.michal.itmproj;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.uwm.wmii.student.michal.itmproj.model.DaneLogowania;
import com.uwm.wmii.student.michal.itmproj.model.enumy.MetodaLogowania;
import com.uwm.wmii.student.michal.itmproj.singletons.AppLoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;
    private ProgressDialog mDialog;
    private AppLoginManager appLoginManager;
    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // AppLoginManager pośredniczy w dostępie do danych użytkownika w shared preferences
        this.appLoginManager = AppLoginManager.getInstance(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        //Jeśli użytkownik już zalogowany:
        if(appLoginManager.czyUzytkownikZalogowany()) {
            // Po prostu przechodzimy do ekranu głównego:
            przejdzDoMainActivity();
            return;
        }

        ustawLogowanieGoogle();
        ustawLogowaniePrzezFacebooka();
    }

    @Override
    protected void onStart() {
        super.onStart();
   //     GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }

    private void przejdzDoMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        //startActivity(new Intent(LoginActivity.this, UserDataActivity.class));
    }

    private void ustawLogowanieGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        appLoginManager.setGoogleSignInClient(mGoogleSignInClient);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }
            }

            private void signIn() {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }

        });
    }
    public GoogleSignInClient getGoogleSignInClient() {

        return mGoogleSignInClient;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
        // The Task returned from this call is always completed, no need to attach
        // a listener.
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        handleSignInResult(task);
    }

}

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount userData = completedTask.getResult(ApiException.class);
            DaneLogowania daneLogowania = new DaneLogowania();
            daneLogowania.setImie(userData.getDisplayName());
            daneLogowania.setEmail(userData.getEmail());
            daneLogowania.setNazwisko(userData.getFamilyName());
            daneLogowania.setMetodaLogowania(MetodaLogowania.Google);
            new PozyskiwaczGoogleAccessTokena(appLoginManager, getApplicationContext()).execute(userData.getAccount());
            daneLogowania.setUserID(userData.getId());
            if (userData.getPhotoUrl() != null) {
                daneLogowania.setZdjecieProfiloweUrl(userData.getPhotoUrl().toString());
            }
            appLoginManager.zapiszDaneLogowaniaDoSharedPreferences(daneLogowania);
            this.przejdzDoMainActivity();
            // Signed in successfully, show authenticated UI.

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void ustawLogowaniePrzezFacebooka() {
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(LoginActivity.this);
                mDialog.setMessage("Wczytywanie...");
                mDialog.show();

                final DaneLogowania daneLogowania = new DaneLogowania();
                daneLogowania.setToken(loginResult.getAccessToken().getToken());
                daneLogowania.setMetodaLogowania(MetodaLogowania.Facebook);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject facebookUserDTO, GraphResponse response) {
                        mDialog.dismiss();
                        Log.d("response", response.toString());
                        //Zachowujemy dane z fejsa do sharedPreferences:
                        try {
                            daneLogowania.setImie(facebookUserDTO.getString("first_name"));
                            daneLogowania.setNazwisko(facebookUserDTO.getString("last_name"));
                            daneLogowania.setEmail(facebookUserDTO.getString("email"));
                            daneLogowania.setUserID(facebookUserDTO.getString("id"));
                            daneLogowania.setZdjecieProfiloweUrl("https://graph.facebook.com/" + facebookUserDTO.getString("id") + "/picture?width=250&height=250");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        appLoginManager.zapiszDaneLogowaniaDoSharedPreferences(daneLogowania);
                        //Przechodzimy do aktywności pozwalającej na wpisywanie danych użytkownika:
                        przejdzDoMainActivity();
                    }
                });
                //Request Graph API
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync(); // <-- ta metoda tak naprawdę wykonuje metodę asynchroniczną, tzw. callback opisany wyżej funkcją lambda (userDTO, response) -> {...}
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private static class PozyskiwaczGoogleAccessTokena extends AsyncTask<Account, Void, String> {
        private String TAG = "PozyskiwaczGoogleAccessTokena";
        private AppLoginManager appLoginManager;
        private Context context;
        //private int REQ_SIGN_IN_REQUIRED = 55664;

        PozyskiwaczGoogleAccessTokena(AppLoginManager appLoginManager, Context applicationContext) {
            this.appLoginManager = appLoginManager;
            this.context = applicationContext;
        }

        @Override
        protected String doInBackground(Account... params) {
            Account account = params[0];
            String scopes = "oauth2:profile email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(context, account, scopes);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                //startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String googleAccessToken) {
            super.onPostExecute(googleAccessToken);
            appLoginManager.zapiszAccessTokenDoSharedPreferences(googleAccessToken);
        }
    }
}


