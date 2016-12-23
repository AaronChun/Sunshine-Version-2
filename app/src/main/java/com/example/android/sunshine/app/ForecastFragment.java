package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */

public class ForecastFragment extends Fragment {

    ArrayAdapter<String> mforcastAdapter;

    private String weatherJsonStr;

    public ForecastFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecasefragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        String[] forcastArr = new String[]{
                "Today-Sunny-88/63",
                "Tomorrow-Foggy-70/46",
                "Weds-Cloudy-72/63",
                "Thurs-Rainy-64/51",
                "Fri-Foggy-70/46",
                "Sat-Sunny-76/68"
        };

        List<String> weekList = new ArrayList<String>(Arrays.asList(forcastArr));

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        mforcastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekList);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        listView.setAdapter(mforcastAdapter);

//        new FetchWeatherTask().execute(url);



        return rootView;
    }





    /**
     *
     */
    public class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection httpURLConnection = null;

            BufferedReader reader = null;

            String forecastJsonStr = null;


            try {

                String baseURL = "http://api.openweathermap.org/data/2.5/forecast/daily?id=1790630&mode=json&units=metric&cnt=7";
                String api = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;

                URL url = new URL(baseURL.concat(api));
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();


                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }

                if (stringBuffer.length() == 0) {
                    return null;
                }

                forecastJsonStr = stringBuffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException ", e);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }


            return null;
        }


    }
}
