package com.dyna.kevin.testtwitterapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;


import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

import static io.fabric.sdk.android.services.network.UrlUtils.urlEncode;


public class MainActivity extends AppCompatActivity {

    String key="O3FqqTDb6kNChcHweK140mtG3";
    String secret_key="BjmgTOQ0lRdAC1i8EwwNLiZCyTYulpbn866hSxrF5qQoTJgVTK";
    String callback_url = "http://cs.utep.edu/cheon";

    TwitterLoginButton login;
    Button logout;
    TextView name;
    TextView result;
    ImageButton record;
    String finalTweet;
    TwitterAuthToken tauthtoken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig twitterAuthConfig = new TwitterAuthConfig(key,secret_key);
        Fabric.with(this,new Twitter(twitterAuthConfig));
        setContentView(R.layout.activity_main);
        logout = (Button) findViewById(R.id.logout_button);
        login = (TwitterLoginButton) findViewById(R.id.login_button);
        name = (TextView) findViewById(R.id.welcome_name);
        result =  (TextView) findViewById(R.id.tv_result);
        record = (ImageButton) findViewById(R.id.imageButton);


        login.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> r) {
                name.setText("Hello "+r.data.getUserName());
                login.setVisibility(View.INVISIBLE);//hide login button
                logout.setVisibility(View.VISIBLE);
                record.setVisibility(View.VISIBLE);
                result.setVisibility(View.VISIBLE);
                tauthtoken = r.data.getAuthToken();

            }
            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(MainActivity.this, "Sorry couldnt identify you", Toast.LENGTH_SHORT).show();
            }
        });

        final TwitterSession twitterSession = Twitter.getSessionManager().getActiveSession();

        if(twitterSession!=null){
            login.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);
            record.setVisibility(View.VISIBLE);
            result.setVisibility(View.VISIBLE);
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // TwitterSession twitterSession1 = TwitterCore.getInstance().getSessionManager().getActiveSession();

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
                    record.setVisibility(View.INVISIBLE);
                    result.setVisibility(View.INVISIBLE);
                    login.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void onButtonClick(View v){
        if(v.getId()==R.id.imageButton){
            promptSpeechInput();
        }
    }

    public void promptSpeechInput(){
        Intent i = new Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!");

        try {
            startActivityForResult(i, 100);
        }
        catch (ActivityNotFoundException e){
            Toast.makeText(MainActivity.this,"Sorry your device doesnt support speech language", Toast.LENGTH_LONG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        login.onActivityResult(requestCode,resultCode,data);

        switch (requestCode){
            case 100: if (resultCode == RESULT_OK && data != null){
                ArrayList <String> r = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Toast.makeText(MainActivity.this,r.toString(),Toast.LENGTH_LONG);
                result.setText(r.get(0));
                Intent i = new Intent(MainActivity.this, EditTweet.class);
                Bundle e = new Bundle();
                e.putSerializable("FROMSPEECH", r.get(0));
                i.putExtras(e);
                startActivityForResult(i, 200);//intent, request code
                break;
            }
            case 200: if (resultCode == RESULT_OK && data != null){
                //send tweet with data retreived from EditTweet class
                finalTweet = data.getData().toString();

                try{
                    String tweet_data = new sendTweet(tauthtoken,finalTweet).execute().get();
                    Log.d("Developer",tweet_data);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                catch (ExecutionException e){
                    e.printStackTrace();
                }

             //   Toast.makeText(this, data.getData().toString() , Toast.LENGTH_SHORT).show();
                break;
            }
            default: break;
        }
    }

    protected String writeJSON(String tweetInfo){
        StringBuilder tweetUrl = new StringBuilder("https://api.twitter.com/1.1/statuses/update.json?status=");
        tweetUrl.append(urlEncode(tweetInfo));
        return tweetUrl.toString();
    }

    private class sendTweet extends AsyncTask <String, Void, String >{
        TwitterAuthToken token;
        String message;
        public sendTweet(TwitterAuthToken token,String message) {
            this.token = token;
            this.message = message;
        }

        @Override
        protected String doInBackground(String... strings) {
            final OAuthService s = new ServiceBuilder()
                    .provider(TwitterApi.SSL.class)
                    .apiKey(key)
                    .apiSecret(secret_key)
                    .callback(callback_url).build();

            Token newAccessToken = new Token(token.token,token.secret);
            Log.d("Developer",token.token + "   secret token: "+ token.secret);
            final OAuthRequest request = new OAuthRequest(Verb.POST, writeJSON(message));
            s.signRequest (newAccessToken,request);
            Response response = request.send();
            String body = response.getBody();
            try{
                JSONArray data_list = new JSONArray(body);
                final ArrayList <String> items = new ArrayList<String>();
                int len = data_list.length();
                for(int i = 0; i<len ;i++){
                    JSONObject item = data_list.getJSONObject(i);
                    try{
                        String tweet = item.get("text").toString();
                        items.add(tweet);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return body;
        }
    }
}
