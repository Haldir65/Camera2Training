package com.harris.androidMedia.mediaPlayBack;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;

import com.harris.androidMedia.R;
import com.harris.androidMedia.databinding.ActivityMediaPlaybackBinding;

import static com.harris.androidMedia.MainActivity.TRANSIT_FAB;

/**
 * Created by Harris on 2017/2/18.
 */

public class MediaPlayBackActivity extends AppCompatActivity {
    ActivityMediaPlaybackBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_media_playback);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ViewCompat.setTransitionName(binding.fab, TRANSIT_FAB);



    }
}
