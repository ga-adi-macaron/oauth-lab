package drewmahrt.generalassemb.ly.twitteroauthlab;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import drewmahrt.generalassemb.ly.twitteroauthlab.models.Tweet;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    private String mAccessToken;

    private List<Tweet> mTweets;
    private RecyclerView.Adapter mAdapter;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.search_edit_text);

        // Setup recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.tweets_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mTweets = new ArrayList<>();
        mAdapter = new TweetRvAdapter(mTweets);
        recyclerView.setAdapter(mAdapter);

        // Get the access token which will be used every time the getTweets() method is called
        getAccessToken();

        // Set up search button
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTweets();
            }
        });
    }

    private void getAccessToken() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterAppData.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        TwitterApiEndpointInterface twitterApi = retrofit.create(TwitterApiEndpointInterface.class);

        String plainString = TwitterAppData.CONSUMER_KEY + ":" + TwitterAppData.CONSUMER_SECRET;
        byte[] data = new byte[0];
        try {
            data = plainString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String base64 = Base64.encodeToString(data, Base64.NO_WRAP);

        Log.d(TAG, "Ready to make bearer req: " + base64);

        Call<ResponseBody> call = twitterApi.authorizeApplication("Basic " + base64,
                "application/x-www-form-urlencoded;charset=UTF-8", "client_credentials");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Call completed!");
                try {
                    if (response.errorBody() != null) {
                        Log.d(TAG, "onResponse: error: " + response.errorBody().string());
                    }
                    String responseString = response.body().string();
                    Log.d(TAG, "Token: " + responseString);
                    JSONObject object = new JSONObject(responseString);
                    mAccessToken = object.getString("access_token");
                    Log.d(TAG, "Access Token: " + mAccessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Bearer token call failed");
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void getTweets() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterAppData.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        TwitterApiEndpointInterface twitterApi = retrofit.create(TwitterApiEndpointInterface.class);

        String searchName = mEditText.getText().toString();
        Call<ResponseBody> call = twitterApi.userTimeline("Bearer " + mAccessToken, searchName, 20);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Call completed!");
                try {
                    if (response.errorBody() != null) {
                        Log.d(TAG, "onResponse: error: " + response.errorBody().string());
                    }
                    String responseBody = response.body().string();
                    JSONArray tweets = new JSONArray(responseBody);

                    mTweets.clear();

                    for (int i = 0; i < tweets.length(); i++) {
                        JSONObject object = tweets.getJSONObject(i);
                        String text = object.getString("text");
                        String date = object.getString("created_at");
                        Tweet tweet = new Tweet();
                        tweet.setText(text);
                        tweet.setCreatedAt(date);
                        mTweets.add(tweet);
                    }

                    // Notify the adapter that mTweets has been updated
                    mAdapter.notifyDataSetChanged();

                    // Close the keyboard
                    View view = MainActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Get tweet call failed");
                Log.d(TAG, t.getMessage());
            }
        });

    }
}
