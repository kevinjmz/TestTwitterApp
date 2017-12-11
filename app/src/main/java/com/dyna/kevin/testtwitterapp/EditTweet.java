package com.dyna.kevin.testtwitterapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditTweet extends AppCompatActivity {
    EditText editText;
    Button tweetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tweet);

        editText = (EditText) findViewById(R.id.editText);
        tweetBtn = (Button) findViewById(R.id.tweetbtn);

        //retreieve info from intent and set edittext to whatever speech recognition sent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editText.setText(extras.getString("FROMSPEECH"));
        }

        tweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.setData(Uri.parse(editText.getText().toString()));
                setResult(RESULT_OK,result);
                finish();
            }
        });
    }
}
