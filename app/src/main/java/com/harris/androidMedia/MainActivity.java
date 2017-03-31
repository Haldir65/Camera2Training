package com.harris.androidMedia;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.harris.androidMedia.camera2.Camera2MainActivity;
import com.harris.androidMedia.databinding.ActivityMainBinding;
import com.harris.androidMedia.exoPlayer.ExoPlayerMainActivity;
import com.harris.androidMedia.mediaPlayBack.LockScreenNotificationControl;
import com.harris.androidMedia.mediaPlayBack.MediaPlayBackActivity;
import com.harris.androidMedia.util.ActionCallBack;
import com.harris.androidMedia.util.ToastUtil;

/**
 * Created by Harris on 2017/2/18.
 */

public class MainActivity extends AppCompatActivity implements ActionCallBack {
    public static final String TRANSIT_FAB = "transit_fab";
    ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);
        binding.setCallback(this);
   /*     if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<UtilVideo.VideoInfo> list = new ArrayList<>();
        UtilVideo.getAllVideoOnDevice(this, list);
        LogUtil.d("" + list.size());
        *//*List<UtilMusic.MusicInfo> list = new ArrayList<>();
        list = UtilMusic.getAllAudioOnDevice(this, list);*//*
        binding.image.setImageBitmap(BitmapFactory.decodeFile(list.get(0).info));*/

    }

    @Override
    public void onClickView(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.card1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                   intent = new Intent(this, Camera2MainActivity.class);
                } else {
                    ToastUtil.showTextShort(this,"Current Sdk minus Lollipop!");
                }
                break;
            case R.id.card2:
                intent = new Intent(this, ExoPlayerMainActivity.class);
                break;
            case R.id.card3:
                intent = new Intent(this, MediaPlayBackActivity.class);
                break;
            case R.id.fab:
                intent = new Intent(this, LockScreenNotificationControl.class);

                break;
            default:
                break;
        }
        if (intent != null) {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, binding.fab, TRANSIT_FAB);
            ActivityCompat.startActivity(this, intent, optionsCompat.toBundle());
        }
    }
}
