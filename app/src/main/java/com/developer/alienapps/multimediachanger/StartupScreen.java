package com.developer.alienapps.multimediachanger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
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
    final static int START_PROGRESS_MSG = 1;
    final static int STOP_PROGRESS_MSG = 2;
    final static int FFMPEG_SUCESS_MSG = 3;
    final static int FFMPEG_FAILURE_MSG = 4;
    private static final String TAG = StartupScreen.class.getSimpleName();
    EditText vbrowseText;
    EditText abrowseText;
    Button vbrowseButton, trimVideo;
    Button abrowseButton, trimAudioButton, slowAudioButton, fastAudioButton;
    Button executeButton;
    String videopath, audioPath;
    String vfinalPath, afinalPath;
    String outputPath;
    boolean isIntermideate;
    private ProgressDialog progressBar;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_PROGRESS_MSG) {
                progressBar.show();
            } else if (msg.what == STOP_PROGRESS_MSG) {
                progressBar.dismiss();
                FFmpeg.getInstance(StartupScreen.this).killRunningProcesses();
            } else if (msg.what == FFMPEG_FAILURE_MSG) {
                progressBar.dismiss();
                msgDialog("There is some problem either in input file or format");
            } else if (msg.what == FFMPEG_SUCESS_MSG) {
                progressBar.dismiss();
                msgDialog("Output file at path : " + outputPath);
            }

        }
    };
    private String ffLogPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_screen);
        Utility.setupFfmpeg(this);
        initUI();

        progressBar = new ProgressDialog(StartupScreen.this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setTitle("Work in Progress");
//        progressBar.setMessage("Press the cancel button to end the operation");
//        progressBar.setMax(100);
//        progressBar.setProgress(0);

        progressBar.setCancelable(false);
        progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.sendEmptyMessage(STOP_PROGRESS_MSG);
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initUI() {
        vbrowseButton = (Button) findViewById(R.id.vbrowse);
        trimVideo = (Button) findViewById(R.id.trim_video);
        vbrowseText = (EditText) findViewById(R.id.vpath);
        abrowseButton = (Button) findViewById(R.id.abrowse);
        trimAudioButton = (Button) findViewById(R.id.trim_audio);
        slowAudioButton = (Button) findViewById(R.id.slow_audio);
        fastAudioButton = (Button) findViewById(R.id.fast_audio);
        abrowseText = (EditText) findViewById(R.id.apath);
        executeButton = (Button) findViewById(R.id.run);

        abrowseButton.setOnClickListener(this);
        trimAudioButton.setOnClickListener(this);
        vbrowseButton.setOnClickListener(this);
        slowAudioButton.setOnClickListener(this);
        fastAudioButton.setOnClickListener(this);
        trimVideo.setOnClickListener(this);
        executeButton.setOnClickListener(this);
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
//                    Toast.makeText(StartupScreen.this, "There is some problem inmerging", Toast.LENGTH_LONG).show();
                    handler.sendEmptyMessage(FFMPEG_FAILURE_MSG);
                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(StartupScreen.this, "Succesfully", Toast.LENGTH_LONG).show();
                    if (!isIntermideate)
                        handler.sendEmptyMessage(FFMPEG_SUCESS_MSG);
                    else
                        progressBar.dismiss();
                }

                @Override
                public void onProgress(String s) {
                    progressBar.setMessage("Processing\n" + s);
                }

                @Override
                public void onStart() {
                    handler.sendEmptyMessage(START_PROGRESS_MSG);
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
//        trimStart.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
        layout.addView(trimStart);

        final EditText trimEnd = new EditText(this);
        trimEnd.setHint("Leave it blank = trim to end");
//        trimEnd.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
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
                    String temp = Utility.getOutputPath() + Utility.generateFilename("trimOut") + ".mp4";
                    String cmd = String.format(Utility.CLIP_VIDEO_OR_AUDIO, startTime, vfinalPath, endTime, temp);
                    vfinalPath = temp;
                    isIntermideate = true;
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
                    String cmd = String.format(Utility.CLIP_VIDEO_OR_AUDIO, startTime, afinalPath, endTime, temp);
                    afinalPath = temp;
                    isIntermideate = true;
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
                outputPath = Utility.getOutputPath() + Utility.generateFilename(Utility.getValidFileNameFromPath(vfinalPath)) + ".mp4";
                String cmd = String.format(Utility.REMOVE_ADD_AUDIO_TO_VIDEO, vfinalPath, afinalPath, outputPath);
                isIntermideate = false;
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
            case R.id.fast_audio: {
                isIntermideate = true;
                changeAudioSpeed("2.0");
            }
            break;
            case R.id.slow_audio: {
                isIntermideate = true;
                changeAudioSpeed("0.5");
            }
            break;
        }
    }

    private void changeAudioSpeed(String speed) {
        String temp = Utility.getOutputPath() + Utility.generateFilename("trimOut") + ".mp3";
        String cmd = String.format(Utility.CHANGE_AUDIO_SPEED, afinalPath, speed, temp);
        afinalPath = temp;
        execFFmpegBinary(cmd);
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

    private void msgDialog(String msg) {
        new AlertDialog.Builder(StartupScreen.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Message")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();

    }
}
