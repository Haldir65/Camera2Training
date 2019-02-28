package com.harris.androidMedia.mediaCodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by xiaoqi on 2018/1/5.
 */

public class VideoDecodeThread extends Thread {

    private final static String TAG = "VideoDecodeThread";

    /** 用来读取音視频文件 提取器 */
    private MediaCodec mediaCodec;
    /** 用来解码 解碼器 */
    private Surface surface;

    private String path;

    public volatile boolean exitFlag = false;

    public VideoDecodeThread(Surface surface, String path) {
        this.surface = surface;
        this.path = path;
    }

    private MediaCodec createCodec(MediaFormat format, Surface surface) throws IOException{
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodec codec = MediaCodec.createByCodecName(codecList.findDecoderForFormat(format));
        codec.configure(format, surface, null, 0);
        return codec;
    }



    @Override
    public void run() {
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(path); // 设置数据源
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        String mimeType = null;
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) { // 信道总数
            MediaFormat format = mediaExtractor.getTrackFormat(i); // 音频文件信息
            mimeType = format.getString(MediaFormat.KEY_MIME);
            if (mimeType.startsWith("video/")) { // 视频信道
                mediaExtractor.selectTrack(i); // 切换到视频信道
                try {
                     mediaCodec = MediaCodec.createDecoderByType(mimeType); // 创建解码器,提供数据输出
//                    mediaCodec =  createCodec(format,surface);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //用于临时处理 surfaceView还没有create，却调用configure导致崩溃的问题
                while (!VideoPlayView.isCreate){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // https://stackoverflow.com/questions/17233835/illegalstateexception-when-mediacodec-configure-android/17243175#17243175
                mediaCodec.configure(format, surface, null, 0);
                //
                //There are some mandatory values that must be set in the format. If you look at the docs for MediaFormat, it says "all keys not marked optional are mandatory". If you fail to set a mandatory key, MediaCodec throws an error because it has been left in an illegal state.
                //
                //Add:
                //
                //mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, <bit rate>);
                //mMediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, <sample rate>);
                //mMediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
                //KEY_MIME should have been set for you by createEncoderByType().
                break;
            }

        }
        if (mediaCodec == null) {
            Log.e(TAG, "Can't find video info!");
            return;
        }

        mediaCodec.start(); // 启动MediaCodec ，等待传入数据
        // 输入
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers(); // 用来存放目标文件的数据
        // 输出
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers(); // 解码后的数据
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo(); // 用于描述解码得到的byte[]数据的相关信息
        boolean bIsEos = false;
        long startMs = System.currentTimeMillis();

        // ==========开始解码=============
        while (VideoPlayView.isCreate) {
            if (exitFlag){
                mediaCodec.stop();
                mediaCodec.release();
                mediaExtractor.release();
                return;
            }

            if (!bIsEos) {
                int inIndex = mediaCodec.dequeueInputBuffer(0);
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    int nSampleSize = mediaExtractor.readSampleData(buffer, 0); // 读取一帧数据至buffer中
                    if (nSampleSize < 0) {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        mediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        bIsEos = true;
                    } else {
                        // 填数据
                        mediaCodec.queueInputBuffer(inIndex, 0, nSampleSize, mediaExtractor.getSampleTime(), 0); // 通知MediaDecode解码刚刚传入的数据
                        mediaExtractor.advance(); // 继续下一取样
                    }
                }
            }

            int outIndex = mediaCodec.dequeueOutputBuffer(info, 0);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = mediaCodec.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(TAG, "New format " + mediaCodec.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d(TAG, "dequeueOutputBuffer timed out!");
                    break;
                default:
                    ByteBuffer buffer = outputBuffers[outIndex];
                    Log.v(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);

                    //防止视频播放过快
                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    mediaCodec.releaseOutputBuffer(outIndex, true);
                    break;
            }

            // All decoded frames have been rendered, we can stop playing
            // now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                break;
            }
        }

        mediaCodec.stop();
        mediaCodec.release();
        mediaExtractor.release();
    }
}