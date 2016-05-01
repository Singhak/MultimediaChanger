package com.developer.alienapps.multimediachanger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by AMIT on 01-May-16.
 */
public class StartScreen extends Activity {

    Button partAButton, partBButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        partAButton = (Button) findViewById(R.id.part_a);
        partBButton = (Button) findViewById(R.id.part_b);

        partAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreen.this, StartupScreen.class);
                StartScreen.this.startActivity(intent);
            }
        });

        partBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreen.this, VideoEditor.class);
                StartScreen.this.startActivity(intent);
            }
        });
        showMsg();

    }

    public void showMsg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message");
        builder.setMessage("Time duration of processing depends on length of video, quality of video and type of operation. It also depends on cpu power of mobile. So keep patience");
       builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {

           }
       });
        builder.show();
    }
}
