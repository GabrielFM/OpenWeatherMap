package com.example.gabriel.openweathermap;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.inlocomedia.android.InLocoMedia;
import com.inlocomedia.android.InLocoMediaOptions;
import com.inlocomedia.android.ads.AdError;
import com.inlocomedia.android.ads.AdRequest;
import com.inlocomedia.android.ads.interstitial.InterstitialAd;
import com.inlocomedia.android.ads.interstitial.InterstitialAdListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gabriel on 11/01/2017.
 */

public class CitiesList extends AppCompatActivity {

    private String url;
    private Double lat, lon;
    private String TAG = CitiesList.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cities_list_layout);

        //Get the information from the previously activity
        Bundle extras = getIntent().getExtras();


        // In Loco Media SDK Init
        InLocoMediaOptions options = InLocoMediaOptions.getInstance(this);

        // The AppId you acquired in earlier steps
        options.setAdsKey("02254a84983db7a713ca9ebc5c14144beb088018477f5f3601c28f965e7cce5f");

        // Verbose mode flag, if this is set as true InLocoMedia SDK will let you know about errors on the Logcat
        options.setLogEnabled(true);

        // Development Devices set here are only going to receive test ads
        options.setDevelopmentDevices("E8E585E17CC5C32FB6A849C4EFFC1");

        InLocoMedia.init(this, options);

        //Get the coordinates
        lat = extras.getDouble("Lat");
        lon = extras.getDouble("Lon");

        //Request URL
        url = "http://api.openweathermap.org/data/2.5/find?lat="+lat.toString()+"&lon="+lon.toString()+"&cnt=15&APPID="+"d6536ca79d35ecce687d322e4b27cd3b"+"&units=metric";

        //JSON request
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            //Get the list of cities
                            final JSONArray list = response.getJSONArray("list");

                            //Get the listview reference from the layout
                            ListView listview = (ListView) findViewById(R.id.CitiesList);
                            List<String> cities = new ArrayList<String>();

                            //Add the name of the cities to the adapter
                            for(int i = 0; i < list.length(); i++){
                                JSONObject c = list.getJSONObject(i);
                                cities.add(c.getString("name"));
                            }

                            //Set the list adapter
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(CitiesList.this,android.R.layout.simple_list_item_1, cities);
                            listview.setAdapter(adapter);

                            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    //Create a dialog when a city is chosen
                                    final Dialog dialog = new Dialog(CitiesList.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


                                    try{

                                        //Set the textviews of the dialog
                                        JSONObject c = list.getJSONObject(i);
                                        JSONObject main = c.getJSONObject("main");
                                        JSONArray weather = c.getJSONArray("weather");
                                        dialog.setContentView(R.layout.dialog_layout);

                                        TextView title = (TextView) dialog.findViewById(R.id.dialogText);
                                        TextView maxTemp = (TextView) dialog.findViewById(R.id.maxTempText);
                                        TextView minTemp = (TextView) dialog.findViewById(R.id.minTempText);
                                        TextView weatherText = (TextView) dialog.findViewById(R.id.weatherSituationText);

                                        title.setText(c.getString("name"));
                                        maxTemp.setText(main.getString("temp_max")+"°C");
                                        minTemp.setText(main.getString("temp_min")+"°C");
                                        weatherText.setText(weather.getJSONObject(0).getString("description"));

                                        dialog.show();

                                    } catch (final JSONException e){
                                        Log.e(TAG, "Json Parsing Error: " + e.getMessage());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(),
                                                        "Json parsing error: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    //Don't allow the dialog be canceled when the user click outside
                                    dialog.setCanceledOnTouchOutside(false);


                                    //The dialog is canceled when the button is clicked
                                    Button okButton = (Button) dialog.findViewById(R.id.okButton);
                                    okButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();

                                            //The ad is shown after the dialog close
                                            InterstitialAd interstitialAd = new InterstitialAd(CitiesList.this);
                                            interstitialAd.setInterstitialAdListener(new InterstitialAdListener() {

                                                @Override
                                                public void onAdReady(final InterstitialAd ad) {
                                                    ad.show();
                                                }

                                                @Override
                                                public void onAdError(InterstitialAd ad, AdError error) {
                                                    Log.w("InLocoMedia", "Your interstitial has failed with error: " + error);
                                                }
                                            });

                                            AdRequest adRequest = new AdRequest();
                                            interstitialAd.loadAd(adRequest);


                                        }
                                    });
                                }
                            });

                        } catch (final JSONException e){

                            Log.e(TAG, "Json Parsing Error: " + e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Json parsing error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        //Uses the singleton to make the request
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);


    }


}
