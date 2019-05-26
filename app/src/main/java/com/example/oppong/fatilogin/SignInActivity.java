package com.example.oppong.fatilogin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton facebookLoginButton;
    private SignInButton googleSignInButton;
    private GoogleSignInClient googleSignInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        facebookLoginButton = findViewById(R.id.facebookLogin);
        facebookLoginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        googleSignInButton = findViewById(R.id.googleSignInButton);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 101);
            }
        });


        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                useFacebookLoginInformation(accessToken);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 101) {
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    onLoggedIn(account);
                } catch (ApiException ex) {
                    ex.printStackTrace();
                }
            } else {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void useFacebookLoginInformation(AccessToken accessToken) {
        // creating the Graph request to fetch the user details
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    Log.e("Facebook Name:: ", object.getString("name"));
                    String image = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    String name = object.getString("name");
                    String email = object.getString("email");
                    openProfileActivity(name, email, image);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // set the parameters of the Graph request using a Bundle.
        Bundle params = new Bundle();
        params.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(params);
        request.executeAsync();
    }

    private void onLoggedIn(GoogleSignInAccount account) {
        Log.e("Display Name:::", account.getDisplayName());
        String image = String.valueOf(account.getPhotoUrl());
        String email = account.getEmail();
        String name = account.getDisplayName();
        openProfileActivity(name, email, image);
    }

    private void openProfileActivity(String profileName, String profileEmail, String profileImage) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("profileName", profileName);
        intent.putExtra("profileEmail", profileEmail);
        intent.putExtra("profileImage", profileImage);

        finish();
        finishAffinity();
        startActivity(intent);
    }
}
