package com.developer.alienapps.multimediachanger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

public class StartupScreen extends AppCompatActivity {

    final static int ON_VIDEO_REQUEST = 1;
    final static int ON_AUDIO_REQUEST = 2;
    private static final String TAG = StartupScreen.class.getSimpleName();
    EditText vbrowseText;
    EditText abrowseText;
    Button vbrowseButton;
    Button abrowseButton;
    Button executeButton;
    String videopath, audioPath;
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_screen);
        vbrowseButton = (Button) findViewById(R.id.vbrowse);
        vbrowseText = (EditText) findViewById(R.id.vpath);
        abrowseButton = (Button) findViewById(R.id.abrowse);
        abrowseText = (EditText) findViewById(R.id.apath);
        executeButton = (Button) findViewById(R.id.run);
        Utility.setupFfmpeg(this);
        abrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/mp3/m4a/ogg");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Audio"), ON_AUDIO_REQUEST);
            }
        });

        vbrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video*//*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), ON_VIDEO_REQUEST);
            }
        });

        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String output_path = Utility.getOutputPath() + Utility.generateFilename() + ".mp4";
                String cmd = String.format(Utility.REMOVE_ADD_AUDIO_TO_VIDEO, videopath, audioPath, output_path);
                execFFmpegBinary(cmd);
            }
        });
//        progressBar = new ProgressDialog(this);
//        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressBar.setTitle("FFmpeg4Android Transcoding...");
//        progressBar.setMessage("Press the cancel button to end the operation");
//        progressBar.setCancelable(false);
       /* progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FFmpeg.getInstance(StartupScreen.this).killRunningProcesses();
            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ON_VIDEO_REQUEST) {
                Uri selectedImageUri = data.getData();
                videopath = selectedImageUri.getPath();
                vbrowseText.setText(videopath);
                String time = Utility.getDuration(videopath, this);
                Toast.makeText(this, time, Toast.LENGTH_LONG).show();
            } else if (requestCode == ON_AUDIO_REQUEST) {
                Uri selectedImageUri = data.getData();
                audioPath = selectedImageUri.getPath();
                abrowseText.setText(audioPath);
            }

        }
    }

    private void execFFmpegBinary(final String comd) {
        String[] command = comd.split(" ");
        Log.i(TAG, "execFFmpegBinary: " + comd);
        FFmpeg fFmpeg = FFmpeg.getInstance(this);
        PowerManager powerManager = (PowerManager) this.getSystemService(Activity.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
        wakeLock.acquire();
        try {
            fFmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "onFailure: " + s);
                    Toast.makeText(StartupScreen.this, "There is some problem : "+s, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(StartupScreen.this, "Succes full conversion", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onProgress(String s) {
                    progressBar.setMessage("Processing\n" + s);
                }

                @Override
                public void onStart() {
                    progressBar = new ProgressDialog(StartupScreen.this);
                    progressBar.setMessage("Processing...");
                    progressBar.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + comd);

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        } finally {
            wakeLock.release();
            progressBar.dismiss();
        }
    }
}
