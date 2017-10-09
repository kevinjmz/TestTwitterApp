package com.dyna.kevin.testtwitterapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    String key="DxjP5s3ByLXDpelKLGFFEYZcv";
    String secret_key="LKh10esjmejghPDSdhYDaoCXSJQn91z7kF7GxIXSa4O3JwxOQG";

    TwitterLoginButton login;
    Button logout;
    TextView name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig twitterAuthConfig = new TwitterAuthConfig(key,secret_key);
        Fabric.with(this,new Twitter(twitterAuthConfig));
        setContentView(R.layout.activity_main);
        logout = (Button) findViewById(R.id.logout_button);
        login = (TwitterLoginButton) findViewById(R.id.login_button);
        name = (TextView) findViewById(R.id.welcome_name);

        login.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                name.setText("Hello "+result.data.getUserName());
                login.setVisibility(View.INVISIBLE);//hide login button
                logout.setVisibility(View.VISIBLE);

                //to start the native kit composer and compose a tweet
                final TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                final Intent intent = new ComposerActivity.Builder(MainActivity.this).session(session).createIntent();
                startActivity(intent);

               /* Uri imageUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".file_provider", new File("/path/to/image"));

                TweetComposer.Builder builder = new TweetComposer.Builder(MainActivity.this);
                        *//*.text("just setting up my Twitter Kit.")
                        .image(imageUri);*//*
                builder.show();*/
            }
            @Override
            public void failure(TwitterException exception) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                        .setMessage(exception.getLocalizedMessage()).setNeutralButton("Accept",null).show();
            }
        });

        final TwitterSession twitterSession = Twitter.getSessionManager().getActiveSession();

        if(twitterSession!=null){
            login.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterSession twitterSession1 = TwitterCore.getInstance().getSessionManager().getActiveSession();

                if(twitterSession!=null){
                    CookieManager cookieManager = CookieManager.getInstance();

                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                        cookieManager.removeSessionCookies(null);
                    }
                    else{
                        cookieManager.removeSessionCookie();
                    }
                    Twitter.getSessionManager().clearActiveSession();
                    Twitter.logOut();

                    name.setText("End of session");
                    logout.setVisibility(View.INVISIBLE);
                    login.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        login.onActivityResult(requestCode,resultCode,data);
    }
}
