package com.developer.alienapps.multimediachanger;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.util.Date;

/**
 * Created by AMIT on 21-Apr-16.
 */
public class Utility {

    final static int ON_VIDEO_REQUEST = 1;
    final static int ON_AUDIO_REQUEST = 2;
    final static int START_PROGRESS_MSG = 1;
    final static int STOP_PROGRESS_MSG = 2;
    final static int FFMPEG_SUCESS_MSG = 3;
    final static int FFMPEG_FAILURE_MSG = 4;

    public static final String TAG = "Utility";
    public static String REMOVE_SOUND_VIDEO = "-y,-i,%s,-vcodec,copy,-an,%s";
    public static String ADD_SOUND_VIDEO = "-y,-i,%s,-i,%s,-c:v,copy,-c:a,copy,%s";
    public  static String EXTRACT_AUDIO_VIDEO = "-y,-i,%s,-vn,%s";
    public  static String IMAGE_FROM_VIDEO = "-y,-i,%s,-ss,5,-vframes,1,%s_%d.jpg";
    public static String CLIP_VIDEO_OR_AUDIO = "-y,-ss,%s,-i,%s,-t,%s,-c,copy,%s";
    public static String CHANGE_AUDIO_SPEED = "-y,-i,%s,-filter:a,atempo=%s,%s";
    public static String CHANGE_VIDEO_AUDIO_SPEED = "-y,-i,%s,-filter:a,atempo=2.0,-vn,%s";
    public static String CHANGE_VIDEO_SPEED = "-y,-i,%s,-filter:v,setpts=N/(25*TB),%s";
    public static String REMOVE_ADD_AUDIO_TO_VIDEO = "-y,-i,%s,-i,%s,-c:v,copy,-map,0:v:0,-map,1:a:0,-c:a,copy,%s";
    public static String FLIP_VIDEO = "-y,-i,%s,-vf,vflip,%s";
    public static void setupFfmpeg(Context context) {
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            Toast.makeText(context,"Your device does not support", Toast.LENGTH_LONG).show();
        }
    }

    public static String getDuration(String path, Context context) {
        if (!path.isEmpty()) {
            MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
            if (mp != null) {
                int duration = mp.getDuration();
                mp.release();
                return getTimeForTrackFormat(duration);
            }
        }
        return getTimeForTrackFormat(0);
    }

    public static int getDurationinSec(String path, Context context) {
        if (!path.isEmpty()) {
            MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
            if (mp != null) {
                int duration = mp.getDuration();
                mp.release();
                return duration / 1000;
            }
        }
        return 0;
    }

    public static String getOutputPath() {
       String path =  Environment.getExternalStorageDirectory().getAbsolutePath() + "/alien/";
        File dir = new File(path);
        try{
            if(dir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return path;
    }

    public static String generateFilename(String prifix) {
        return prifix +"_"+ String.valueOf(((new Date().getTime())%(1000000000))%1000);
    }

    public static String getTimeForTrackFormat(int duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        String result = "";
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;
        if (hours < 10) {
            result = "0" + hours + ":";
        } else {
            result = hours + ":";
        }
        if (minutes < 10) {
            result = "0" + minutes + ":";
        } else {
            result = minutes + ":";
        }
        if (seconds < 10) {
            result += "0" + seconds;
        } else {
            result += seconds;
        }
        return result;
    }

    public static String getValidFileNameFromPath(String path) {
        int startIndex = path.lastIndexOf("/") + 1;
        int endIndex = path.lastIndexOf(".");

        String name = path.substring(startIndex, endIndex);
        String ext = path.substring(endIndex + 1);
        Log.d(TAG, "name: " + name + " ext: " + ext);
        String validName = (name.replaceAll("\\Q.\\E", "_")).replaceAll(" ", "_");
        Log.d(TAG, "Valid_name: " + validName + " ext: " + ext);
        return validName;
    }

    public static String getValidFileNameExth(String path) {
        int startIndex = path.lastIndexOf("/") + 1;
        int endIndex = path.lastIndexOf(".");

        String name = path.substring(startIndex, endIndex);
        String ext = path.substring(endIndex + 1);
        Log.d(TAG, "name: " + name + " ext: " + ext);
        String validName = (name.replaceAll("\\Q.\\E", "_")).replaceAll(" ", "_");
        Log.d(TAG, "Valid_name: " + validName + " ext: " + ext);
        return ext;
    }
}
