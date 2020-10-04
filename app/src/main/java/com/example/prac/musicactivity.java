package com.example.prac;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.prac.MainActivity.musicfilesArrayList;
import static com.example.prac.MainActivity.repeat_flag;
import static com.example.prac.MainActivity.shuffle_flag;
import static com.example.prac.MainActivity.shuffle_repeat;

public class musicactivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    static byte[] img = null;
    static ArrayList<musicfiles> listofsong = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    TextView song_name, artist_name, duration_played, duration_total, current_playing;
    ImageView back_btn, menu_btn, song_img, suffle_btn, repeate_btn, btn_prev, btn_next;
    FloatingActionButton btn_play_pause;
    SeekBar seekBar;

    private int positon = -1;
    private Handler handler = new Handler();
    private Thread play_pause_thread, prev_thread, next_thread, suffle_thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicactivity);
        initview();
        getintentdata();
        song_details();
        mediaPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);     // for millisecond   /// load into song song run according to the seekbar, set timing
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positon >= 1) {
                    Intent intent = new Intent(getApplicationContext(), musicactivity.class);
                    intent.putExtra("pos", positon - 1);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
        musicactivity.this.runOnUiThread(new Runnable() {    // for song multi thread
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int current_pos = mediaPlayer.getCurrentPosition() / 1000; // to get in second
                    seekBar.setProgress(current_pos);      // set progress in seek bar
                    duration_played.setText(formaattedTime(current_pos));
                }
                handler.postDelayed(this, 1000);
            }
        });

    }

    private void song_details() {
        if (mediaPlayer != null) {
            duration_total.setText(formaattedTime(mediaPlayer.getDuration() / 1000));
            current_playing.setText(listofsong.get(positon).getTitle());
            song_name.setText(listofsong.get(positon).getTitle());
            artist_name.setText(listofsong.get(positon).getArtist());
        }

    }
    private String formaattedTime(int current_pos) {             // formatted time convert into second and minute;
        String totalout = "";
        String totalnew = "";
        String second = String.valueOf(current_pos % 60); // get in second
        String minute = String.valueOf(current_pos / 60); // get in menute
        totalout = minute + ":" + second;
        totalnew = minute + ":" + "0" + second;
        if (second.length() == 1) {
            return totalnew;
        }
        return totalout;
    }

    private void getintentdata() {
        Intent intent = getIntent();
        positon = intent.getIntExtra("pos", -1);
        listofsong = musicfilesArrayList;
        if (listofsong != null) {
            btn_play_pause.setImageResource(R.drawable.ic_baseline_pause_circle);
            uri = Uri.parse(listofsong.get(positon).getPath());
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            img = musicadapter.getAlbumimg(String.valueOf(uri));
            load_img();
            mediaPlayer.start();
        } else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            load_img();

            mediaPlayer.start();
        }
        if (shuffle_flag) {
            suffle_btn.setImageResource(R.drawable.ic_baseline_shuffle_on);
        } else {
            suffle_btn.setImageResource(R.drawable.ic_baseline_shuffle_off);
        }
        seekBar.setMax(mediaPlayer.getDuration() / 1000);         // take it as second
    }

    private void initview() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.duration_played);
        duration_total = findViewById(R.id.duration_total);
        current_playing = findViewById(R.id.txt_current_playing);
        back_btn = findViewById(R.id.back_btn);
        menu_btn = findViewById(R.id.menu_btn);
        song_img = findViewById(R.id.cover_art);
        suffle_btn = findViewById(R.id.id_shuffle_btn);
        repeate_btn = findViewById(R.id.id_repeat_btn);
        btn_next = findViewById(R.id.id_next);
        btn_prev = findViewById(R.id.id_prev);
        btn_play_pause = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seek_bar);
    }

    private void load_img() {
        img = musicadapter.getAlbumimg(String.valueOf(uri));
        if (img != null) {

            Glide.with(getApplicationContext()).asBitmap().load(img).into(song_img);
        } else {
            Glide.with(getApplicationContext()).asBitmap().load(R.drawable.ic_launcher_background).into(song_img);
        }
    }

    @Override
    protected void onResume() {
        nextThread();
        prevThread();
        play_pausethread();
        shuffle_thread();
        super.onResume();
    }

    private void shuffle_thread() {
        suffle_thread = new Thread() {
            @Override
            public void run() {
                super.run();
                suffle_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!shuffle_flag) {
                            shuffle_flag = true;
                            suffle_btn.setImageResource(R.drawable.ic_baseline_shuffle_on);
                        } else {
                            shuffle_flag = false;
                            suffle_btn.setImageResource(R.drawable.ic_baseline_shuffle_off);
                        }
                    }

                });
            }
        };
        suffle_thread.start();
    }

    private void play_pausethread() {
        play_pause_thread = new Thread() {
            @Override
            public void run() {
                super.run();

                btn_play_pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // play or pause button click listener
                        setPlay_pause_thread();


                    }
                });
            }
        };
        play_pause_thread.start();
    }

    private void nextThread() {
        next_thread = new Thread() {
            @Override
            public void run() {
                super.run();
                btn_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setBtn_next_thread();

                    }
                });

            }
        };
        next_thread.start();
    }

    private void prevThread() {
        prev_thread = new Thread() {
            @Override
            public void run() {
                super.run();
                btn_prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        set_btn_prev_thread();
                    }
                });

            }
        };
        prev_thread.start();
    }

    private void set_btn_prev_thread() {
        if (positon >= 1) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if (!shuffle_flag && !repeat_flag) {
                    positon = positon - 1 % listofsong.size();
                } else if (shuffle_flag && !repeat_flag) {
                    positon = new Random().nextInt(listofsong.size());
                }
                uri = Uri.parse(listofsong.get(positon).getPath());              // song location
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                load_img();
                song_details();
                seekBar.setMax(mediaPlayer.getDuration() / 1000);
                musicactivity.this.runOnUiThread(new Runnable() {    // for song multi thread
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            int current_pos = mediaPlayer.getCurrentPosition() / 1000; // to get in second
                            seekBar.setProgress(current_pos);      // set progress in seek bar
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                mediaPlayer.setOnCompletionListener(this);
                btn_play_pause.setImageResource(R.drawable.ic_baseline_pause_circle);
                mediaPlayer.start();
            } else {
                mediaPlayer.release();
                if (shuffle_repeat.equals("nothing")) {
                    positon = positon - 1 % listofsong.size();
                } else if (shuffle_repeat.equals("shuffle")) {
                    positon = new Random().nextInt(listofsong.size());
                }
                uri = Uri.parse(listofsong.get(positon).getPath());              // song location
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                load_img();
                song_details();
                seekBar.setMax(mediaPlayer.getDuration() / 1000);
                musicactivity.this.runOnUiThread(new Runnable() {    // for song multi thread
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            int current_pos = mediaPlayer.getCurrentPosition() / 1000; // to get in second
                            seekBar.setProgress(current_pos);      // set progress in seek bar
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                mediaPlayer.setOnCompletionListener(this);
                btn_play_pause.setImageResource(R.drawable.ic_baseline_play_circle_);
            }

        }
    }

    private void setPlay_pause_thread() {
        if (!mediaPlayer.isPlaying()) {

            mediaPlayer.start();
            btn_play_pause.setImageResource(R.drawable.ic_baseline_pause_circle);
            //seekBar.setMax(mediaPlayer.getDuration()/1000);
        }    //  after click button if flag is true then music would be play other wise if false then pause
        else {
            btn_play_pause.setImageResource(R.drawable.ic_baseline_play_circle_);
            mediaPlayer.pause();
            musicactivity.this.runOnUiThread(new Runnable() {    // for song multi thread
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int current_pos = mediaPlayer.getCurrentPosition() / 1000; // to get in second
                        seekBar.setProgress(current_pos);      // set progress in seek bar
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }

    private void setBtn_next_thread() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (!shuffle_flag && !repeat_flag) {
                positon = positon + 1 % listofsong.size();
            } else if (shuffle_flag && !repeat_flag) {
                positon = new Random().nextInt(listofsong.size());
            }
            uri = Uri.parse(listofsong.get(positon).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            load_img();
            song_details();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            musicactivity.this.runOnUiThread(new Runnable() {    // for song multi thread
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int current_pos = mediaPlayer.getCurrentPosition() / 1000; // to get in second
                        seekBar.setProgress(current_pos);      // set progress in seek bar
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btn_play_pause.setImageResource(R.drawable.ic_baseline_pause_circle);
            mediaPlayer.start();
        } else {
            mediaPlayer.release();
            if (shuffle_repeat.equals("nothing")) {
                positon = positon + 1 % listofsong.size();
            } else if (shuffle_repeat.equals("shuffle")) {
                positon = new Random().nextInt(listofsong.size());
            } else {
                return;
            }
            uri = Uri.parse(listofsong.get(positon).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            load_img();
            song_details();
            seekBar.setMax(mediaPlayer.getDuration() / 1000);
            musicactivity.this.runOnUiThread(new Runnable() {    // for song multi thread
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        int current_pos = mediaPlayer.getCurrentPosition() / 1000; // to get in second
                        seekBar.setProgress(current_pos);      // set progress in seek bar
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btn_play_pause.setImageResource(R.drawable.ic_baseline_play_circle_);

        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setBtn_next_thread();
        if (mediaPlayer != null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            btn_play_pause.setImageResource(R.drawable.ic_baseline_pause_circle);
        }
        mediaPlayer.setOnCompletionListener(this);
    }
}