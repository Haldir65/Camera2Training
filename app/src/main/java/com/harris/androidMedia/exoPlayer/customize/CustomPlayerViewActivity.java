package com.harris.androidMedia.exoPlayer.customize;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.harris.androidMedia.App;
import com.harris.androidMedia.R;
import com.harris.androidMedia.databinding.ActivityCustomizePlayerViewBinding;
import com.harris.androidMedia.util.ToastUtil;
import com.harris.androidMedia.util.Utils;

import java.io.File;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.harris.androidMedia.exoPlayer.SurfaceViewPlayerActivity.REQUEST_READ_EXTERNAL_STORAGE;

/**
 * Created by Harris on 2017/3/4. support swipe left or right to change the track
 */

public class CustomPlayerViewActivity extends AppCompatActivity {

    public static final String TAG = CustomPlayerViewActivity.class.getSimpleName();

    ActivityCustomizePlayerViewBinding binding;

    private Handler mainHandler;
    private Timeline.Window window;
    private CustomExoPlayerView simpleExoPlayerView;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

    private boolean shouldAutoPlay;
    private int playerWindow;
    private long playerPosition;
    private BandwidthMeter bandwidthMeter;
    private DefaultExtractorsFactory extractorsFactory;

    List<String> fileList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_customize_player_view);
        shouldAutoPlay = true;
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);
        mainHandler = new Handler();
        window = new Timeline.Window();
        simpleExoPlayerView = binding.playerView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }


    }



    @TargetApi(23)
    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                show_EXTERNAL_STORAGE_PermissionRequestRationale();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }
        } else {
            //已经拥有permission
          fileList = Utils.getFileAbsolutePathList();

        }
    }

    private void show_EXTERNAL_STORAGE_PermissionRequestRationale() {
        ToastUtil.showTextLong(this,"Please grant permission");
    }

    private void initializePlayer() {
        simpleExoPlayerView.requestFocus();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        boolean preferExtensionDecoders = false;
        @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                ((App) getApplication()).useExtensionRenderers()
                        ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                        : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                        : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                null, extensionRendererMode);
        simpleExoPlayerView.setPlayer(player);
        player.setPlayWhenReady(shouldAutoPlay);
        File file = new File(fileList.get(0));
        Uri uri;
        if (file.exists()) {
            uri = Uri.fromFile(file);
//            uri = Uri.parse(Constants.Mp4uri);
            MediaSource mediaSources = new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                    mainHandler, null);
            LoopingMediaSource loopingMediaSource = new LoopingMediaSource(mediaSources);
            player.prepare(loopingMediaSource);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            playerWindow = player.getCurrentWindowIndex();
            playerPosition = C.TIME_UNSET;
            Timeline timeline = player.getCurrentTimeline();
            if (timeline != null && timeline.getWindow(playerWindow, window).isSeekable) {
                playerPosition = player.getCurrentPosition();
            }
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            // Empty Permission and result array means a cancellation
            if (grantResults.length == 0) {
                return;
            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something with this permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fileList = Utils.getFileAbsolutePathList();
                if (fileList != null && fileList.size() > 0) {
                    if ((Util.SDK_INT <= 23 || player == null)) {
                        initializePlayer();
                    }
                }
            }
        }
    }
}
