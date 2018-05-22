package com.frangoudes.myaudioplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 77;
    private static final int FILE_SELECT_CODE = 777;
    String TAG = "MainActivity :";

    Uri uri = null;
    List<Uri> uriList = new ArrayList<>();
    List<String> trackList = new ArrayList<String>();

    enum MediaPlayerStates {
        MEDIA_PLAYER_STATE_ERROR,
        MEDIA_PLAYER_IDLE,
        MEDIA_PLAYER_INITIALIZED,
        MEDIA_PLAYER_PREPARING,
        MEDIA_PLAYER_PREPARED,
        MEDIA_PLAYER_STARTED,
        MEDIA_PLAYER_PAUSED,
        MEDIA_PLAYER_STOPPED,
        MEDIA_PLAYER_PLAYBACK_COMPLETE
    }

    MediaPlayerStates mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_IDLE;
    MediaPlayer mediaPlayer = null;

    private MediaPlayer.OnCompletionListener onCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (uriList.isEmpty()) {
                        /*
                         * No more tracks to be played. Release mediaPlacompileyer
                         */
                        releaseMediaPlayer();
                    } else {
                        /*
                         * There is a track waiting to be played
                         */
                        mediaPlayer.reset();
                        startMediaPlayer();
                        displayPlayList();
                        if (uriList.isEmpty()) {
                            findViewById(R.id.next_button).setVisibility(View.INVISIBLE);
                        }
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this,
                        "Permission required to access Storage.",
                        Toast.LENGTH_LONG).show();
            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void selectFile(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Add the Uri of the selected file to the list
                    uriList.add(data.getData());
                    if (mediaPlayerState == MediaPlayerStates.MEDIA_PLAYER_IDLE) {
                        Button b = findViewById(R.id.play_pause_button);
                        b.setText(R.string.play);
                        b.setVisibility(View.VISIBLE);
                        findViewById(R.id.stop_button).setVisibility(View.VISIBLE);

                    } else {
                        if (mediaPlayerState == MediaPlayerStates.MEDIA_PLAYER_STARTED ||
                                mediaPlayerState == MediaPlayerStates.MEDIA_PLAYER_PAUSED) {
                            findViewById(R.id.next_button).setVisibility(View.VISIBLE);
                        }
                    }
                }
                displayPlayList();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void play_pause(View view) {

        /*
         * User has pressed the Play button
         */
        if (mediaPlayerState == MediaPlayerStates.MEDIA_PLAYER_PAUSED) {
            /*
             * mediaPlayer is Paused so simply restart it
             */
            try {
                mediaPlayer.start();
                mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_STARTED;
                Button b = findViewById(R.id.play_pause_button);
                b.setText(R.string.pause);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "File access error.",
                        Toast.LENGTH_LONG).show();

                e.printStackTrace();
            }
        } else {
            if (mediaPlayerState == MediaPlayerStates.MEDIA_PLAYER_IDLE) {
                /*
                 * User has pressed Play before a track has ever been played
                 * Check that at least on track has been selected and play, otherwise do nothing
                 */
                if (!uriList.isEmpty()) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnCompletionListener(onCompletionListener);
                    startMediaPlayer();
                    if (!uriList.isEmpty()) {
                        findViewById(R.id.next_button).setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (mediaPlayerState == MediaPlayerStates.MEDIA_PLAYER_STARTED) {
                    mediaPlayer.pause();
                    mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_PAUSED;
                    Button b = findViewById(R.id.play_pause_button);
                    b.setText(R.string.play);
                }
            }
        }
    }

    public void next(View view) {

        mediaPlayer.reset();
        startMediaPlayer();
        if (uriList.isEmpty()) {
            findViewById(R.id.next_button).setVisibility(View.INVISIBLE);
        }
    }

    public void stop(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        releaseMediaPlayer();
        uriList.clear();
    }

    private void startMediaPlayer() {
        try {
            uri = uriList.remove(0);
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_STARTED;
            Button b = findViewById(R.id.play_pause_button);
            b.setText(R.string.pause);
            findViewById(R.id.stop_button).setVisibility(View.VISIBLE);
            TextView textViewPlaying = (TextView) findViewById(R.id.playing);
           // textViewPlaying.setText(R.string.playing);
            textViewPlaying.setText(FilenameUtils.getBaseName(uri.toString()));
            displayPlayList();
        } catch (IOException | IllegalStateException e) {
            Toast.makeText(this, "File access error",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_IDLE;
        findViewById(R.id.play_pause_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.next_button).setVisibility(View.INVISIBLE);
        findViewById(R.id.stop_button).setVisibility(View.INVISIBLE);
        TextView textViewPlaying = (TextView) findViewById(R.id.playing);
        textViewPlaying.setText("");
        TextView textViewPlayList = (TextView) findViewById(R.id.play_list);
        textViewPlayList.setText("");
    }

    private void displayPlayList() {
        TextView textViewPlayList = (TextView) findViewById(R.id.play_list);
        textViewPlayList.setMovementMethod(new ScrollingMovementMethod());
        textViewPlayList.setText(null);
        if (uriList.isEmpty()) {
            textViewPlayList.setText("");
        } else {
            for (int i = uriList.size() -1; i >=0; i--) {
                uri = uriList.get(i);
                trackList.add(i, FilenameUtils.getBaseName(uriList.get(i).toString()));
                textViewPlayList.append("\n" + trackList.get(i));
            }
        }
    }
}
