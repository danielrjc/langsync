package com.example.langsync;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Code adapted from https://docs.agora.io/en/video-calling/get-started/get-started-sdk?platform=android
public class VideoCallActivity extends AppCompatActivity {
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };

    // Fill the App ID of your project generated on Agora Console.
    private final String appId = "8c6d6c9355eb4aeab83c5798f97d89cc";
    // Fill the channel name.
    private String channelName;
    // Fill the temp token generated on Agora Console.
    private String token;
    // An integer that identifies the local user.
    private int uid = 0;
    private boolean isJoined = false;
    private RtcEngine agoraEngine;
    //SurfaceView to render local video in a Container.
    private SurfaceView localSurfaceView;
    //SurfaceView to render Remote video in a Container.
    private SurfaceView remoteSurfaceView;
    private final OkHttpClient client = new OkHttpClient();

    // ChatGPT usage: No
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        Intent intent = getIntent();

        channelName = intent.getStringExtra(getString(R.string.channel_key));

        setupVideoSDKEngine();

        try {
            startVideoCall();
        } catch (IOException e) {
            showMessage("ERROR: Unable to start video call");
            finish();
        }
    }

    // ChatGPT usage: No
    private boolean checkSelfPermission()
    {
        if (ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) !=  PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) !=  PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        return true;
    }

    // ChatGPT usage: No
    void showMessage(String message) {
        Log.d("Agora Video Call", message);
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    // ChatGPT usage: No
    private void startVideoCall() throws IOException{
        String url = getString(R.string.base_url) + "agoraToken/" + channelName + "/publisher/uid/" + uid;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    assert response.body() != null;
                    String jsonData = response.body().string();

                    try {
                        // Convert the string to a JSONObject
                        JSONObject jsonObject = new JSONObject(jsonData);

                        // Now you can get values from the JSONObject as needed, for example:
                        token = jsonObject.getString("agoraToken");
                        joinChannel();

                    } catch (JSONException e) {
                        throw  new IOException(e.getMessage());
                    }
                }
            }
        });
    }

    // ChatGPT usage: No
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote host joining the channel to get the uid of the host.
        public void onUserJoined(int uid, int elapsed) {
            showMessage("Remote user joined " + uid);

            // Set the remote video view
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            showMessage("Joined Channel " + channel);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            showMessage("Remote user offline " + uid + " " + reason);
            runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
        }
    };

    // ChatGPT usage: No
    private void setupVideoSDKEngine() {

        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine.enableVideo();
        } catch (Exception e) {
            showMessage(e.toString());
            finish();
        }
    }

    // ChatGPT usage: No
    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        remoteSurfaceView = new SurfaceView(getBaseContext());
        remoteSurfaceView.setZOrderMediaOverlay(true);
        container.addView(remoteSurfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
        // Display RemoteSurfaceView.
        remoteSurfaceView.setVisibility(View.VISIBLE);
    }

    // ChatGPT usage: No
    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.local_video_view_container);
        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = new SurfaceView(getBaseContext());
        container.addView(localSurfaceView);
        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    // ChatGPT usage: No
    public void joinChannel() {
        runOnUiThread(() -> {
            if (checkSelfPermission()) {
                ChannelMediaOptions options = new ChannelMediaOptions();

                // For a Video call, set the channel profile as COMMUNICATION.
                options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
                // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                // Display LocalSurfaceView.
                setupLocalVideo();
                localSurfaceView.setVisibility(View.VISIBLE);
                // Start local preview.
                agoraEngine.startPreview();
                // Join the channel with a temp token.
                // You need to specify the user ID yourself, and ensure that it is unique in the channel.
                agoraEngine.joinChannel(token, channelName, uid, options);
            } else {
                Toast.makeText(getApplicationContext(), "Permissions was not granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // ChatGPT usage: No
    public void leaveChannel(View view) {
        if (isJoined) {
            agoraEngine.leaveChannel();
            showMessage("You left the channel");
            // Stop remote video rendering.
            if (remoteSurfaceView != null) remoteSurfaceView.setVisibility(View.GONE);
            // Stop local video rendering.
            if (localSurfaceView != null) localSurfaceView.setVisibility(View.GONE);
        }

        finish();
    }

    // ChatGPT usage: No
    protected void onDestroy() {
        super.onDestroy();
        agoraEngine.stopPreview();
        agoraEngine.leaveChannel();

        // Destroy the engine in a sub-thread to avoid congestion
        new Thread(() -> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }

    // ChatGPT usage: No
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQ_ID) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length < 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Permission denied! Show an alert to the user.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("This feature requires camera and microphone permissions to function. Please grant these permissions in settings.")
                        .setTitle("Permissions required")
                        .setPositiveButton(android.R.string.ok, null)
                        .setOnDismissListener(dialog -> finish()); // Close activity on dialog dismiss
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}