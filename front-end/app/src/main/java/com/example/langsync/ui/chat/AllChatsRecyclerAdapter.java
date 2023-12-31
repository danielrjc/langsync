package com.example.langsync.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.langsync.ChatActivity;
import com.example.langsync.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AllChatsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<JSONObject> chats;
    private String userID;

    public ArrayList<JSONObject> messages = new ArrayList<>();
    private String TAG = "AllChatsRecyclerAdapter";

    public AllChatsRecyclerAdapter(Context context, HashMap<String, JSONObject> chatrooms, String userId) {
        this.context = context;
        this.chats = new ArrayList<>(chatrooms.values());
        this.userID = userId;
    }

    // ChatGPT Usage: No
    public class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView chat_name;
        private TextView chat_last_message;
        private ImageView chat_profile_pic;
        public ChatViewHolder(View itemView) {
            super(itemView);
            chat_name = itemView.findViewById(R.id.chat_name);
            chat_last_message = itemView.findViewById(R.id.chat_recent_msg);
            chat_profile_pic = itemView.findViewById(R.id.chat_profile_pic);
        }
    }

    // ChatGPT Usage: No
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    // ChatGPT Usage: No
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ChatViewHolder vh = (ChatViewHolder) viewHolder;
        JSONObject chat = chats.get(i);

        try {
            vh.chat_name.setText(chat.getString("otherUserName"));
//            vh.chat_last_message.setText(chat.getJSONObject("messages").);
            try {
                String profilePicUrl = chat.getString("otherUserPicture");
                Picasso.get().load(profilePicUrl).into(vh.chat_profile_pic);
            }catch(Exception e){
                e.printStackTrace();
                Log.d(TAG, "Error setting profile user for chat item");
            }

            vh.itemView.setOnClickListener(v -> {
                try {
                    JSONObject chatObj = chats.get(i);
                    AllChatsFragment.currentChatroom = chatObj.getString("_id");
                    Intent intent = new Intent(context, ChatActivity.class);

                    if(Objects.equals(userID, chatObj.getString("user2Id")))
                        intent.putExtra("otherUserId", chatObj.getString("user1Id"));
                    else if(Objects.equals(userID, chatObj.getString("user1Id")))
                        intent.putExtra("otherUserId", chatObj.getString("user2Id"));

                    intent.putExtra("otherUserName", chatObj.getString("otherUserName"));
                    intent.putExtra("chatroomId", chatObj.getString("_id"));

                    context.startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error in getting chatroom id");
                }
            });
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Error in getting chatroom name");
        }
    }

    // ChatGPT Usage: No
    @Override
    public int getItemCount() {
        if(chats.size() == 0)
            return 1;
        else
            return chats.size();
    }
}
