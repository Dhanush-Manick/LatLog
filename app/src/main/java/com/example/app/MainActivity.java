package com.example.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.LruCache;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button btn;
    EditText lat, log;
    TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).hide();

        btn = findViewById(R.id.button);
        lat = findViewById(R.id.lat);
        log = findViewById(R.id.log);

        txt = findViewById(R.id.txt);
        txt.setMovementMethod(new ScrollingMovementMethod());


        Volley.newRequestQueue(this);

        btn.setOnClickListener(v -> {
            String url = "https://api.weatherbit.io/v2.0/current?lat="
                    +lat.getText().toString()+"&lon="+log.getText().toString()
                    +"&key=d66f587e78fb43fcb6d0c8f73b6f8a89";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, response -> {


                        try {
                            txt.setText(details(response));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                        // TODO: Handle error

                    });

// Access the RequestQueue through your singleton class.
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        });
    }


    String details(JSONObject obj) throws JSONException {
        JSONArray array = obj.getJSONArray("data");
        JSONObject object = array.getJSONObject(0);

        String rh, timeZone, countryCode, clouds, windSpd, cityName, sunrise, sunset, temp;

        rh = object.getString("rh");
        timeZone = object.getString("timezone");
        countryCode = object.getString("country_code");
        clouds = object.getString("clouds");
        windSpd = object.getString("wind_spd");
        cityName = object.getString("city_name");
        sunrise = object.getString("sunrise");
        sunset = object.getString("sunset");
        temp = object.getString("temp");

        return "Country Code : " + countryCode +
                "\n\nCity Name : " + cityName +
                "\n\nTime Zone : " + timeZone +
                "\n\nTemperature : " + temp +
                "\n\nClouds Coverage : " + clouds +
                "\n\nWind Speed : " + windSpd +
                "\n\nRelative Humidity : " + rh +
                "\n\nSunrise Time : " + sunrise +
                "\n\nSunset Time : " + sunset;
    }
}


class MySingleton {
    @SuppressLint("StaticFieldLeak")
    private static MySingleton instance;

    private RequestQueue requestQueue;
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;

    private MySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();

        new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new MySingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}