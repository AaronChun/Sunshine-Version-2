package com.example.android.sunshine.app;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        ArrayAdapter<String> mforcastAdapter;

        public PlaceholderFragment() {
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

            HttpURLConnection httpURLConnection = null;

            BufferedReader reader = null;

            String forecastJsonStr = null;



            try {
                String baseURL = "http://api.openweathermap.org/data/2.5/forecast/daily?id=1790630&mode=json&units=metric&cnt=7";
                String api = "&APPID="+BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL(baseURL.concat(api));
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();



                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if(inputStream == null){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }

                if(stringBuffer.length()==0){
                    return null;
                }

                forecastJsonStr = stringBuffer.toString();

            } catch (MalformedURLException e) {
//                e.printStackTrace();
                Log.e("PlaceholderFragment", "MalformedURLException ", e);
            } catch (ProtocolException e) {
//                e.printStackTrace();
                Log.e("PlaceholderFragment", "ProtocolException ", e);
            } catch (IOException e) {
//                e.printStackTrace();
                Log.e("PlaceholderFragment", "IOException ", e);
            }finally {
                if(httpURLConnection!= null){
                    httpURLConnection.disconnect();
                }

                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
//                        e.printStackTrace();
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }


            return rootView;
        }
    }
}
