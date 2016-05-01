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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

/**
 * Created by AMIT on 29-Apr-16.
 */
public class VideoEditor extends Activity implements View.OnClickListener {

    static final String TAG = "VideoEditor";
    Button trimButton, audioSpeedButton, videoSpeedButton;
    Button bothSpeedButton, muteButton, extractaudioButton;
    Button flipButtonm, extractImageButton, replaceAudi0Button;
    Button browseVideoButton, browseAudioButton;
    VideoView videoView;
    static String videopath, audiopath;
    String outputPath;
    RelativeLayout relativeLayout;
    EditText abrowseText;
    ProgressDialog progressBar;
    boolean audioReplace = false;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Utility.START_PROGRESS_MSG) {
                progressBar.show();
            } else if (msg.what == Utility.STOP_PROGRESS_MSG) {
                progressBar.dismiss();
                FFmpeg.getInstance(VideoEditor.this).killRunningProcesses();
            } else if (msg.what == Utility.FFMPEG_FAILURE_MSG) {
                progressBar.dismiss();
                audioReplace = false;
                msgDialog("There is some problem either in input file or format");
            } else if (msg.what == Utility.FFMPEG_SUCESS_MSG) {
                progressBar.dismiss();
                audioReplace = false;
                msgDialog("Output file at path : " + outputPath);
                videoView.suspend();
                playVideo(outputPath);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_layout);
//        Utility.setupFfmpeg(this);
        progressBar = new ProgressDialog(VideoEditor.this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setTitle("Work in Progress");
        progressBar.setCancelable(false);
        progressBar.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.sendEmptyMessage(Utility.STOP_PROGRESS_MSG);
            }
        });
        initUI();
    }

    private void initUI() {
        trimButton = (Button) findViewById(R.id.trim);
        trimButton.setOnClickListener(this);

        audioSpeedButton = (Button) findViewById(R.id.aspeed);
        audioSpeedButton.setOnClickListener(this);

        videoSpeedButton = (Button) findViewById(R.id.vspeed);
        videoSpeedButton.setOnClickListener(this);

        bothSpeedButton = (Button) findViewById(R.id.backA);
        bothSpeedButton.setOnClickListener(this);

        muteButton = (Button) findViewById(R.id.mute);
        muteButton.setOnClickListener(this);

        extractImageButton = (Button) findViewById(R.id.get_img);
        extractImageButton.setOnClickListener(this);

        extractaudioButton = (Button) findViewById(R.id.get_audio);
        extractaudioButton.setOnClickListener(this);

        flipButtonm = (Button) findViewById(R.id.flip);
        flipButtonm.setOnClickListener(this);

        replaceAudi0Button = (Button) findViewById(R.id.change_audio);
        replaceAudi0Button.setOnClickListener(this);

        browseVideoButton = (Button) findViewById(R.id.vbrowse_video);
        browseVideoButton.setOnClickListener(this);

        browseAudioButton = (Button) findViewById(R.id.audio_browse);
        browseAudioButton.setOnClickListener(this);

        videoView = (VideoView) findViewById(R.id.videoplayer);
        videoView.setMediaController(new MediaController(this));

        videoView.requestFocus();


        relativeLayout = (RelativeLayout) findViewById(R.id.audio_browse_layout);
        abrowseText = (EditText) findViewById(R.id.audio_path);

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.trim: {
                if (videopath != null && !videopath.isEmpty()) {
                    trimDialog(Utility.ON_VIDEO_REQUEST);
                } else {
                    showMsg("Browse a video first");
                }
            }
            break;
            case R.id.vspeed: {
                if (videopath != null && !videopath.isEmpty()) {
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("finalVideo") + ".mp4";
                    String cmd = String.format(Utility.CHANGE_VIDEO_SPEED, videopath, outputPath);
                    execFFmpegBinary(cmd);
                } else {
                    showMsg("Browse a video first");
                }
            }
            break;
            case R.id.aspeed: {
                if (videopath != null && !videopath.isEmpty()) {
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("finalVideo") + ".mp4";
                    String cmd = String.format(Utility.CHANGE_VIDEO_AUDIO_SPEED, videopath, outputPath);
                    execFFmpegBinary(cmd);
                } else {
                    showMsg("Browse a video first");
                }
            }
            break;
            case R.id.backA: {
                Intent intent = new Intent(VideoEditor.this, StartupScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            break;
            case R.id.mute: {
                if (videopath != null && !videopath.isEmpty()) {
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("finalVideo") + ".mp4";
                    String cmd = String.format(Utility.REMOVE_SOUND_VIDEO, videopath, outputPath);
                    execFFmpegBinary(cmd);
                } else {
                    showMsg("Browse a video first");
                }
            }
            break;
            case R.id.get_audio: {
                if (videopath != null && !videopath.isEmpty()) {
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("finalVideo") + ".mp3";
                    String cmd = String.format(Utility.EXTRACT_AUDIO_VIDEO, videopath, outputPath);
                    execFFmpegBinary(cmd);
                } else {
                    showMsg("Browse a video first");
                }
            }
            break;
            case R.id.get_img: {
                Log.d(TAG, "onClick: getImage :"+id);
                Log.d(TAG, "onClick: getImage :"+videopath);
                if (videopath != null && !videopath.isEmpty()) {
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("clipimage");
                    String cmd = String.format(Utility.IMAGE_FROM_VIDEO, videopath, outputPath);
                    Log.d(TAG, "onClick: getImage :"+cmd);
                    execFFmpegBinary(cmd);
                } else {
                    showMsg("Browse a video first");
                }
            }
            break;
            case R.id.flip: {
                if (videopath != null && !videopath.isEmpty()) {
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("finalVideo") + ".mp4";
                    String cmd = String.format(Utility.FLIP_VIDEO, videopath, outputPath);
                    execFFmpegBinary(cmd);
                } else {
                    showMsg("Browse a video first");
                }
            }
            break;
            case R.id.vbrowse_video: {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                this.startActivityForResult(Intent.createChooser(intent, "Select Video"), Utility.ON_VIDEO_REQUEST);
            }
            break;
            case R.id.change_audio: {
                audioReplace = true;
               relativeLayout.setVisibility(View.VISIBLE);
            }
            break;

            case R.id.abrowse :
            {
                Intent intent = new Intent();
                intent.setType("audio/mp3/m4a/ogg");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                this.startActivityForResult(Intent.createChooser(intent, "Select Audio"), Utility.ON_AUDIO_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == Utility.ON_VIDEO_REQUEST) {
                Uri selectedImageUri = data.getData();
                videopath = selectedImageUri.getPath();
                playVideo(videopath);
//                String time = Utility.getDuration(videopath, this);
//                Toast.makeText(this, time, Toast.LENGTH_LONG).show();
                if (audioReplace) {
                    replaceSound();
                }
            } else if (requestCode == Utility.ON_AUDIO_REQUEST) {
                Uri selectedImageUri = data.getData();
                audiopath = selectedImageUri.getPath();
                abrowseText.setText(audiopath);
                if (audioReplace){
                    replaceSound();
                }
            }

        }
    }

    public void replaceSound() {
        if (videopath != null && !videopath.isEmpty() && audiopath != null && !audiopath.isEmpty()) {
            outputPath = Utility.getOutputPath() + Utility.generateFilename("finalVideo") + ".mp4";
            String cmd = String.format(Utility.REMOVE_ADD_AUDIO_TO_VIDEO, videopath, audiopath, outputPath);
            execFFmpegBinary(cmd);
            relativeLayout.setVisibility(View.INVISIBLE);
            audioReplace = false;
        } else {
            showMsg("Browse a video and audio to replace");
        }
    }

    private void playVideo(String outputPath) {
        videoView.setVideoPath(outputPath);
        videoView.start();
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
                if (requestId == Utility.ON_VIDEO_REQUEST) {
                    int duration = Utility.getDurationinSec(videopath, VideoEditor.this);
                    endTime = String.valueOf(duration);
                    if (!trimStart.getText().toString().isEmpty()) {
                        startTime = trimStart.getText().toString();
                    }
                    if (!trimEnd.getText().toString().isEmpty()) {
                        endTime = trimEnd.getText().toString();
                    }
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("trimOut") + ".mp4";
                    String cmd = String.format(Utility.CLIP_VIDEO_OR_AUDIO, videopath, startTime, endTime, outputPath);
//                    vfinalPath = temp;
                    execFFmpegBinary(cmd);
                } else if (requestId == Utility.ON_AUDIO_REQUEST) {
                    int duration = Utility.getDurationinSec(audiopath, VideoEditor.this);
                    endTime = String.valueOf(duration);
                    if (!trimStart.getText().toString().isEmpty()) {
                        startTime = trimStart.getText().toString();
                    }
                    if (!trimEnd.getText().toString().isEmpty()) {
                        endTime = trimEnd.getText().toString();
                    }
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("trimOut") + ".mp3";
                    String cmd = String.format(Utility.CLIP_VIDEO_OR_AUDIO, audiopath, startTime, endTime, outputPath);
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
                    handler.sendEmptyMessage(Utility.FFMPEG_FAILURE_MSG);
                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(VideoEditor.this, "Succesfully", Toast.LENGTH_LONG).show();
                    handler.sendEmptyMessage(Utility.FFMPEG_SUCESS_MSG);
                }

                @Override
                public void onProgress(String s) {
                    progressBar.setMessage("Processing\n" + s);
                }

                @Override
                public void onStart() {
                    handler.sendEmptyMessage(Utility.START_PROGRESS_MSG);
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

    void showPathDialoge() {
        final EditText editText = new EditText(this);
        editText.setHint("Enter file name");
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enter file name");
        alertDialog.setView(editText);

        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editText.getText().toString().isEmpty()) {
                    outputPath = Utility.getOutputPath() + Utility.generateFilename("finalVideo") + ".mp4";
                } else {
                    outputPath = editText.getText().toString().trim().replace(" ", "_");
                }
            }
        });
    }

    private void msgDialog(String msg) {
        new AlertDialog.Builder(VideoEditor.this)
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

