package com.example.langsync;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView loginLink;
    private MaterialButton signUpButton;
    private GoogleSignInClient mGoogleSignInClient;
    private final OkHttpClient client = new OkHttpClient();
    private static String TAG = "SignUpActivity";
    private final AuthenticationUtilities utilities = new AuthenticationUtilities(SignupActivity.this);

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
        signUpButton = findViewById(R.id.sign_up_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        loginLink = findViewById(R.id.login_link);
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

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

    private void signUp() {
        Intent signUpIntent = mGoogleSignInClient.getSignInIntent();
        signUpLauncher.launch(signUpIntent);
    }

    private void handleSignUpResult(@NonNull Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), "{" +
                    "\"idToken\": \"" + idToken + "\"" +
                    "}");

            String url = "http://10.0.2.2:8081/authentication/signup/";
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
                    Log.d(TAG, response.body().toString());
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
                        utilities.navigateTo(MainActivity.class, getString(R.string.sign_up_granted));
                    }
                }
            });
        } catch (ApiException e) {
            utilities.showToast(getString(R.string.sign_up_error));
        }
    }
}