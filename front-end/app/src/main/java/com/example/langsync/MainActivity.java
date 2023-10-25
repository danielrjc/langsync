package com.example.langsync;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.langsync.R;
import com.example.langsync.VideoCallActivity;

public class MainActivity extends AppCompatActivity {
    Button videoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoButton = findViewById(R.id.video_button);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), VideoCallActivity.class);
                intent.putExtra("REMOTE_USER", "test");
                intent.putExtra("LOCAL_USER", "channel");
                startActivity(intent);
            }
        });
    }
}