package com.hennatuominen.recipes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;

import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(v -> createSignInIntent());
    }

    public void createSignInIntent() {
        Log.d(TAG, "Creating signInIntent");
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        try {
            IdpResponse response = result.getIdpResponse();
            if (result.getResultCode() == RESULT_OK) {
                Log.d(TAG, "Successfully signed in");
                changeToHomePageActivity();
            } else {
                if (response == null) Log.d(TAG, "Sign in canceled by user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }

    }

    public void changeToHomePageActivity() {
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

}