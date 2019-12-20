package com.baker.synthesizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toAudioTrackPlayerActivity(View view) {
        startActivity(new Intent(this, AudioTrackPlayerActivity.class));
    }

    public void toMediaTrackPlayerActivity(View view) {
        startActivity(new Intent(this, MediaPlayerActivity.class));
    }
}
