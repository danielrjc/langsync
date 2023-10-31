package com.example.langsync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.langsync.ui.chat.AllChatsRecyclerAdapter;
import com.example.langsync.ui.chat.ChatMsgRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private List<JSONObject> messages = new ArrayList<>();
    private ImageView goBack, videoCall, sendMsg;
    private String otherUserName, otherUserId, userId, chatroomId;
    private CardView noMessageView;
    private EditText msgInput;
    private TextView chatHeaderName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        userId = sharedPreferences.getString("loggedUserId", null);

        noMessageView = findViewById(R.id.no_messages);
        msgInput = findViewById(R.id.msg_input);
        chatHeaderName = findViewById(R.id.chat_header_name);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            otherUserName = extras.getString("otherUserName");
            otherUserId = extras.getString("otherUserId");
            chatroomId = extras.getString("chatroomId");
            chatHeaderName.setText(otherUserName);
            try {
                JSONArray jsonArray = new JSONArray(extras.getString("messages"));
                for(int i = 0; i < jsonArray.length(); i++){
                    messages.add(jsonArray.getJSONObject(i));
                }
                Log.d(TAG, messages.toString());

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        if(messages.size() > 0) {
            noMessageView.setVisibility(CardView.GONE);
            RecyclerView recyclerView = findViewById(R.id.msg_recycler_view);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);

            RecyclerView.Adapter msgRecyclerAdapter = new ChatMsgRecyclerAdapter(getApplicationContext(), messages, userId);

            recyclerView.setAdapter(msgRecyclerAdapter);
            recyclerView.smoothScrollToPosition(msgRecyclerAdapter.getItemCount() - 1);
        } else {
            Log.d(TAG, "No messages");
            noMessageView.setVisibility(CardView.VISIBLE);
        }

        goBack = findViewById(R.id.back_btn);
        videoCall = findViewById(R.id.video_call);
        sendMsg = findViewById(R.id.send_msg);

        goBack.setOnClickListener(v -> {
            finish();
        });
        videoCall.setOnClickListener(view -> {
            Intent intent = new Intent(ChatActivity.this, VideoCallActivity.class);
            intent.putExtra(getString(R.string.channel_key), chatroomId);
            startActivity(intent);
        });
        sendMsg.setOnClickListener(v -> {
            if(!msgInput.getText().toString().isEmpty())
                sendMessage();
            else
                Toast.makeText(this, "Add text to send a message!", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendMessage(){
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sourceUserId", userId);
            jsonObject.put("content", msgInput.getText().toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8081/chatrooms/" + chatroomId + "/messages")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
                runOnUiThread(() -> {
                    msgInput.setText("");
                    Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d(TAG, "Message sent");
                    runOnUiThread(() -> {
                        msgInput.setText("");
                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}