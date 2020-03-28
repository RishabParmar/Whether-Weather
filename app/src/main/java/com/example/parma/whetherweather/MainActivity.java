package com.example.parma.whetherweather;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    // Rough Algorithm flow:
    // Enter the city whose weather is to be searched
    // Hit the button and pass the city name to fetchWeatherInfo()
    // First hit the HTTP API call to download the JSON data for a particular city
    // Remove the only relevant data from the downloaded data and present it to the resultbox

    TextView appTitle;
    Button weatherFetcher;
    EditText cityText;
    TextView weatherInfoResult;

    public class DownloadWeatherJSONData extends AsyncTask<String, Void, String> {
        String result = "";

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while (data != -1) {
                    result += (char) data;
                    data = inputStreamReader.read();
                }
                return result;
            } catch (java.io.IOException e) {
                e.printStackTrace();
                return "Failed";
            }
        }

        // The following method is used to do something when the doInBackground method has finished working.
        // Similar to a callback.
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                String finalResult = "";
                JSONObject jsonObject = new JSONObject(s);
                JSONObject generalInfo = jsonObject.getJSONObject("main");
                // Here, the generalInfo.names provide all the properties in the "main" jsonObject
                // jsonObject.getJSONObject("main") gives the value of main JsonObject. Similarly, to get the
                // value of a "temperature" from main, we do main.getString('temp').
                for(int i= 0 ; i< generalInfo.names().length(); i++) {
                    if(generalInfo.names().getString(i).equals("feels_like")) { continue; }
                    finalResult += generalInfo.names().getString(i) + " : " + generalInfo.getString(generalInfo.names().getString(i))+ "\n\n";
                }
                // We used JSONArray here and not JSONObject because weather property provides an array of objects
                // If used with JSONObject, it gives an error. So to avoid that, we use JSONArray
                String weatherInfo = jsonObject.getString("weather");
                JSONArray jArray = new JSONArray(weatherInfo);
                JSONObject weather = jArray.getJSONObject(0);
                for(int i= 0 ; i< weather.names().length(); i++) {
                    // Don't need the id or the icon
                    if(weather.names().getString(i).equals("id") || weather.names().getString(i).equals("icon")) { continue; }
                    finalResult += weather.names().getString(i) + " : " + weather.getString(weather.names().getString(i))+ "\n\n";
                }
                weatherInfoResult.setText(finalResult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void fetchWeatherInfo(View view) {
        String cityName = cityText.getText().toString();
        if(cityName.length() <=2) {
            String errMessage = "Please enter a valid city name";
            weatherInfoResult.setText(errMessage);
        }else{
            try {
                String url = "https://api.openweathermap.org/data/2.5/weather?q="+cityName+"&appid="+MainActivity.this.getString(R.string.weather_app_id);
                new DownloadWeatherJSONData().execute(url).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void initializeUI() {
        appTitle = findViewById(R.id.appTitle);
        weatherInfoResult = findViewById(R.id.weatherInfoResult);
        weatherFetcher = findViewById(R.id.weatherFetcher);
        cityText = findViewById(R.id.cityText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
    }
}
