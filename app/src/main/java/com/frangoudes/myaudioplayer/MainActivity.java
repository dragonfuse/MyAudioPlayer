package com.frangoudes.myaudioplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 77;
    private static final int FILE_SELECT_CODE = 777;
    String TAG = "MainActivity :";

    Uri uri = null;
    List<Uri> uriList = new ArrayList<>();

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

    //MediaPlayer mediaPlayer = new MediaPlayer();
    MediaPlayer mediaPlayer = null;

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            playNext();
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

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
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
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    uri = data.getData();
                    uriList.add(uri);
                    Log.d(TAG, "File Uri: " + uri.toString());

                    // TODO check file type is Audio
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void play(View view) {
        /*
         * User has pressed the Play button. If the mediaPlayer is Paused
         * then simply start it again
         */
        if (mediaPlayerState == MediaPlayerStates.MEDIA_PLAYER_PAUSED) {
            mediaPlayer.start();
            mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_STARTED;
        } else {
            /*
             *Check if at least one file has been selected in which case play it
             * otherwise do nothing
             */
            if (!uriList.isEmpty()) {
                if (mediaPlayerState == MediaPlayerStates.MEDIA_PLAYER_IDLE) {
                    /*
                     * Playing first track after startup or after having been stopped
                     */
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setOnCompletionListener(onCompletionListener);
                    }
                    try {
                        mediaPlayer.setDataSource(getApplicationContext(), uriList.remove(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                    mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_STARTED;
                } else {
                    /*
                     * Play next track on the list
                     */
                    playNext();
                }
            }
        }
    }

    public void pause(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_PAUSED;
        }
    }

    public void stop(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            uriList.clear();
            releaseMediaPlayer();
        }
    }

    private void playNext() {
        /*
         * Release mediaPlayer and then check if there is at least one more file queued
         */
        releaseMediaPlayer();
        if (!uriList.isEmpty()) {
            /*
             * No need to check for mediaPlayer state or null as we have just released
             */
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            try {
                mediaPlayer.setDataSource(getApplicationContext(), uriList.remove(0));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_STARTED;
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            mediaPlayerState = MediaPlayerStates.MEDIA_PLAYER_IDLE;
        }
    }
}