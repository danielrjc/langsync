package com.example.langsync;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.langsync.util.AuthenticationUtilities;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity {

    private MaterialButton loginButton;
    private TextView signUpLink;
    private GoogleSignInClient mGoogleSignInClient;
    private final OkHttpClient client = new OkHttpClient();
    private static String TAG = "LoginActivity";
    private final AuthenticationUtilities  utilities = new AuthenticationUtilities(LoginActivity.this);

    private TextView signUpText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .requestServerAuthCode(getString(R.string.server_client_id))
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        loginButton = findViewById(R.id.login_button);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        signUpLink = findViewById(R.id.sign_up_link);
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    ActivityResultLauncher<Intent> loginLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Log.d(TAG, result.toString());
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleLoginResult(task);
            }
        }
    });

    private void login() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        loginLauncher.launch(signInIntent);
    }

    private void handleLoginResult(@NonNull Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "{" +
                    "\"idToken\": \"" + idToken + "\"" +
                    "}");

            String url = "http://10.0.2.2:8081/authentication/login/";
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    utilities.showToast(getString(R.string.login_error));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    Log.d(TAG, response.toString());
                    if (!response.isSuccessful()) {
                        try {
                            JSONObject responseBody = new JSONObject(response.body().string());
                            Log.d(TAG, responseBody.toString());
                            String errMessage = responseBody.getString("error");
                            utilities.navigateTo(SignupActivity.class, errMessage);
                        } catch (JSONException e) {
                            utilities.showToast(getString(R.string.login_error));
                        }
                    } else {
                        try {
                            JSONObject responseBody = new JSONObject(response.body().string());
                            String userId = responseBody.getString("userId");

                            // Get a reference to SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("loggedUserId", userId);
                            editor.apply();

                            utilities.navigateTo(MainActivity.class, getString(R.string.login_granted));
                        } catch (JSONException e) {
                            Log.d(TAG, "Error getting user id");
                            utilities.showToast(getString(R.string.login_error));
                        }
                    }
                }
            });
        } catch (ApiException e) {
            utilities.showToast(getString(R.string.login_error));
        }
    }
}