package com.example.langsync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.langsync.util.AuthenticationUtilities;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private final OkHttpClient client = new OkHttpClient();
    private static String TAG = "SignUpActivity";
    private final AuthenticationUtilities utilities = new AuthenticationUtilities(SignupActivity.this);

    // ChatGPT usage: No
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        MaterialButton signUpButton = findViewById(R.id.sign_up_button);

        signUpButton.setOnClickListener(v -> signUp());

        TextView loginLink = findViewById(R.id.login_link);
        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });
    }

    // ChatGPT usage: No
    ActivityResultLauncher<Intent> signUpLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignUpResult(task);
            }
        }
    });

    // ChatGPT usage: No
    private void signUp() {
        Intent signUpIntent = mGoogleSignInClient.getSignInIntent();
        signUpLauncher.launch(signUpIntent);
    }

    // ChatGPT usage: No
    private void handleSignUpResult(@NonNull Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();

            RequestBody requestBody = RequestBody.create( "{" +
                    "\"idToken\": \"" + idToken + "\"" +
                    "}", MediaType.parse("application/json"));

            String url = getString(R.string.base_url) + "authentication/signup/";
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    utilities.showToast(getString(R.string.sign_up_error));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {

                    if (!response.isSuccessful()) { //User already registered
                        try {
                            JSONObject responseBody = new JSONObject(response.body().string());
                            Log.d(TAG, responseBody.toString());
                            String errMessage = responseBody.getString("error");
                            utilities.navigateTo(LoginActivity.class, errMessage);
                        } catch (JSONException e) {
                            utilities.showToast(getString(R.string.sign_up_error));
                        }
                    } else { //Successful Sign Up
                        try {
                            JSONObject responseBody = new JSONObject(response.body().string());
                            String userId = responseBody.getString("userId");

                            // Get a reference to SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("loggedUserId", userId);
                            editor.apply();

                            utilities.navigateTo(FormActivity.class, getString(R.string.sign_up_granted));
                        } catch (JSONException e) {
                            Log.d(TAG, "Error getting user id");
                            utilities.showToast(getString(R.string.sign_up_error));
                        }
                    }
                }
            });
        } catch (ApiException e) {
            utilities.showToast(getString(R.string.sign_up_error));
        }
    }
}