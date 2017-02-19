package com.harris.androidMedia.exoPlayer;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.harris.androidMedia.R;
import com.harris.androidMedia.databinding.ActivityExoplayerBinding;

/**
 * Created by Harris on 2017/2/18.
 */

public class ExoPlayerMainActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityExoplayerBinding binding;
    private String uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_exoplayer);
        setSupportActionBar(binding.toolbar);
        uri = "http://odzl05jxx.bkt.clouddn.com/%E6%9D%8E%E5%BF%97&quot;%E7%9C%8B%E8%A7%81&quot;2015%E5%B7%A1%E6%BC%94%E9%A2%84%E5%91%8A%E7%89%87.mp4";
        binding.card1.setOnClickListener(this);
        binding.card2.setOnClickListener(this);
        binding.card3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.card1:
                intent = new Intent(this, SimpleExoPlayerViewActivity.class);
                String[] urlArray = new String[]{uri};
                intent.putExtra(SimpleExoPlayerViewActivity.URI_LIST_EXTRA, urlArray);
                intent.setAction(SimpleExoPlayerViewActivity.ACTION_VIEW_LIST);
                break;
            case R.id.card2:
                intent = new Intent(this, SurfaceViewPlayerActivity.class);
               /* String[] urlArray = new String[]{uri};
                intent.putExtra(SimpleExoPlayerViewActivity.URI_LIST_EXTRA, urlArray);
                intent.setAction(SimpleExoPlayerViewActivity.ACTION_VIEW_LIST);*/
                break;
            case R.id.card3:
                intent = new Intent(this, TextureViewActivity.class);
                break;
            default:
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
