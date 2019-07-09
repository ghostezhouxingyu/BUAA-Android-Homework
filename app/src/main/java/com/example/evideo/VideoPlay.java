package com.example.evideo;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.VideoView;

public class VideoPlay extends AppCompatActivity {

    private VideoView vv;
    private SeekBar seekBar;
    private long videoLength;
    private ImageView pause;
    public boolean isPlaying = false;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            long time = data.getLong("progress");
            Log.d("fordebug", "handleMessage: " + time);
            seekBar.setProgress((int) (time * 1.0 / videoLength * 100));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        String url = getIntent().getStringExtra("url");
        Log.d("fordebug", "" + url);
        vv = findViewById(R.id.videoview);
        pause = findViewById(R.id.pause);
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoLength = mediaPlayer.getDuration();
                seekBar.setMax(100);
                isPlaying = true;
            }
        });

        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Message msg = Message.obtain();
                Bundle data = new Bundle();
                data.putLong("progress", videoLength);
                msg.setData(data);
                mHandler.sendMessageDelayed(msg, 1500);
            }
        });

        vv.setVideoPath(url);
        vv.start();
        vv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(isPlaying){
                    vv.pause();
                    isPlaying = false;
                    pause.setVisibility(View.VISIBLE);
                }else {
                    vv.start();
                    isPlaying = true;
                    pause.setVisibility(View.GONE);
                }
                return false;
            }
        });
        setSeekBar();
    }
    void setSeekBar(){
        seekBar = findViewById(R.id.seekbar);
        seekBar.setProgress(0);
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                    vv.seekTo((int)(progress * 1.0 / 100 * videoLength));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = Message.obtain();
                    Bundle data = new Bundle();
                    long time = vv.getCurrentPosition();
                    data.putLong("progress", time);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }
}
