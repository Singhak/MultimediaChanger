package com.developer.alienapps.multimediachanger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class StartupScreen extends AppCompatActivity implements View.OnClickListener {

    final static int ON_VIDEO_REQUEST = 1;
    final static int ON_AUDIO_REQUEST = 2;
    private static final String TAG = StartupScreen.class.getSimpleName();
    EditText vbrowseText;
    EditText abrowseText;
    Button vbrowseButton, trimVideo;
    Button abrowseButton, trimAudio;
    Button executeButton;
    String videopath, audioPath;
    String vfinalPath, afinalPath;
    String outputPath;
    private ProgressDialog progressBar;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_screen);
        vbrowseButton = (Button) findViewById(R.id.vbrowse);
        trimVideo = (Button) findViewById(R.id.trim_video);
        vbrowseText = (EditText) findViewById(R.id.vpath);
        abrowseButton = (Button) findViewById(R.id.abrowse);
        trimAudio = (Button) findViewById(R.id.trim_audio);
        abrowseText = (EditText) findViewById(R.id.apath);
        executeButton = (Button) findViewById(R.id.run);

        Utility.setupFfmpeg(this);
        abrowseButton.setOnClickListener(this);
        trimAudio.setOnClickListener(this);
        vbrowseButton.setOnClickListener(this);
        trimVideo.setOnClickListener(this);
        executeButton.setOnClickListener(this);
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ON_VIDEO_REQUEST) {
                Uri selectedImageUri = data.getData();
                vfinalPath = videopath = selectedImageUri.getPath();
                vbrowseText.setText(videopath);
//                String time = Utility.getDuration(videopath, this);
//                Toast.makeText(this, time, Toast.LENGTH_LONG).show();
            } else if (requestCode == ON_AUDIO_REQUEST) {
                Uri selectedImageUri = data.getData();
                afinalPath = audioPath = selectedImageUri.getPath();
                abrowseText.setText(audioPath);
            }

        }
    }

    private void execFFmpegBinary(final String comd) {
        String[] command = comd.split(",");
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
                    Toast.makeText(StartupScreen.this, "There is some problem : " + s, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(StartupScreen.this, "Succesfully : " + outputPath, Toast.LENGTH_LONG).show();
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

    public void trimDialog(final int requestId) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText trimStart = new EditText(this);
        trimStart.setHint("Leave it blank = trim from start");
        trimStart.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
        layout.addView(trimStart);

        final EditText trimEnd = new EditText(this);
        trimEnd.setHint("Leave it blank = trim to end");
        trimEnd.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
        layout.addView(trimEnd);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enter trim value in seconds");
        alertDialog.setView(layout);

        alertDialog.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (trimStart.getText().toString().isEmpty() && trimEnd.getText().toString().isEmpty()) {
                    dialog.cancel();
                }
                String startTime = "0";
                String endTime = "0";
                if (requestId == ON_VIDEO_REQUEST) {
                    int duration = Utility.getDurationinSec(vfinalPath, StartupScreen.this);
                    endTime = String.valueOf(duration);
                    if (!trimStart.getText().toString().isEmpty()) {
                        startTime = trimStart.getText().toString();
                    }
                    if (!trimEnd.getText().toString().isEmpty()) {
                        endTime = trimEnd.getText().toString();
                    }
                    String temp = Utility.getOutputPath() + Utility.generateFilename("trimOut") + ".mp3";
                    String cmd = String.format(Utility.CLIP_VIDEO_OR_AUDIO, vfinalPath, startTime, endTime, outputPath);
                    vfinalPath = temp;
                    execFFmpegBinary(cmd);
                } else if (requestId == ON_AUDIO_REQUEST) {
                    int duration = Utility.getDurationinSec(afinalPath, StartupScreen.this);
                    endTime = String.valueOf(duration);
                    if (!trimStart.getText().toString().isEmpty()) {
                        startTime = trimStart.getText().toString();
                    }
                    if (!trimEnd.getText().toString().isEmpty()) {
                        endTime = trimEnd.getText().toString();
                    }
                    String temp = Utility.getOutputPath() + Utility.generateFilename("trimOut") + ".mp3";
                    String cmd = String.format(Utility.CLIP_VIDEO_OR_AUDIO, afinalPath, startTime, endTime, outputPath);
                    afinalPath = temp;
                    execFFmpegBinary(cmd);
                }
            }
        });
        alertDialog.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
//        alertDialog.setCancelable(false);
//        alertDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "StartupScreen Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.developer.alienapps.multimediachanger/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "StartupScreen Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.developer.alienapps.multimediachanger/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.trim_audio: {
                if (audioPath != null && !audioPath.isEmpty()) {
                    trimDialog(ON_AUDIO_REQUEST);
                } else {
                    showMsg("Browse a audio first");
                }
            }
            break;
            case R.id.trim_video: {
                if (videopath != null && !videopath.isEmpty()) {
                    trimDialog(ON_VIDEO_REQUEST);
                } else {
                    showMsg("Browse a video first");
                }
            }
            break;
            case R.id.run: {
                outputPath = Utility.getOutputPath() + Utility.generateFilename("finalVideo") + ".mp4";
                String cmd = String.format(Utility.REMOVE_ADD_AUDIO_TO_VIDEO, vfinalPath, afinalPath, outputPath);
                execFFmpegBinary(cmd);
            }
            break;
            case R.id.abrowse: {
                Intent intent = new Intent();
                intent.setType("audio/mp3/m4a/ogg");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Audio"), ON_AUDIO_REQUEST);
            }
            break;
            case R.id.vbrowse: {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), ON_VIDEO_REQUEST);
            }
            break;
        }
    }

    public void showMsg(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(msg);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }
}
