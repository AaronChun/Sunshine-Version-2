package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
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
  public void onStart() {
    super.onStart();
    updateWeather();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  private void updateWeather() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String location = prefs.getString(getString(R.string.pref_location_key),
        getString(R.string.pref_location_default));
    new FetchWeatherTask().execute(location);
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    inflater.inflate(R.menu.forecasefragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.action_refresh:
        updateWeather();
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

    List<String> weekList = new ArrayList<String>();

    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    mforcastAdapter = new ArrayAdapter<String>(getActivity(),
        R.layout.list_item_forecast,
        R.id.list_item_forecast_textview,
        weekList);
    ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

    listView.setAdapter(mforcastAdapter);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TextView textView = (TextView) view;
        String forecastStr = mforcastAdapter.getItem(position);
        //TODO:以上两种方式有什么区别？
//                Toast.makeText(getActivity(),forecastStr,Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, forecastStr);
        startActivity(intent);
      }
    });

    return rootView;
  }


  /**
   *
   */
  public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    /**
     * The date/time conversion code is going to be moved outside the asynctask later, so for
     * convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
      // Because the API returns a unix timestamp (measured in seconds),
      // it must be converted to milliseconds in order to be converted to valid date.
      SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
      return shortenedDateFormat.format(time);
    }


    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
      // For presentation, assume the user doesn't care about tenths of a degree.
      long roundedHigh = Math.round(high);
      long roundedLow = Math.round(low);

      String highLowStr = roundedHigh + "/" + roundedLow;
      return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and pull out the data we
     * need to construct the Strings needed for the wireframes. <p> Fortunately parsing is easy:
     * constructor takes the JSON string and converts it into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
        throws JSONException {
      // These are the names of the JSON objects that need to be extracted.
      final String OWM_LIST = "list";
      final String OWM_WEATHER = "weather";
      final String OWM_TEMPERATURE = "temp";
      final String OWM_MAX = "max";
      final String OWM_MIN = "min";
      final String OWM_DESCRIPTION = "main";

      JSONObject forecastJson = new JSONObject(forecastJsonStr);
      JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

      Time dayTime = new Time();
      dayTime.setToNow();

      // we start at the day returned by local time. Otherwise this is a mess.
      int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

      // now we work exclusively in UTC
      dayTime = new Time();

      String[] resultStrs = new String[numDays];

      for (int i = 0; i < weatherArray.length(); i++) {
        // For now, using the format "Day, description, hi/low"
        String day;
        String description;
        String highAndLow;

        // Get the JSON object representing the day
        JSONObject dayForecast = weatherArray.getJSONObject(i);

        // The date/time is returned as a long.  We need to convert that
        // into something human-readable, since most people won't read "1400356800" as
        // "this saturday".
        long dateTime;
        // Cheating to convert this to UTC time, which is what we want anyhow
        dateTime = dayTime.setJulianDay(julianStartDay + i);
        day = getReadableDateString(dateTime);

        // description is in a child array called "weather", which is 1 element long.
        JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
        description = weatherObject.getString(OWM_DESCRIPTION);

        // Temperatures are in a child object called "temp".  Try not to name variables
        // "temp" when working with temperature.  It confuses everybody.
        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
        double high = temperatureObject.getDouble(OWM_MAX);
        double low = temperatureObject.getDouble(OWM_MIN);

        highAndLow = formatHighLows(high, low);
        resultStrs[i] = day + " - " + description + " - " + highAndLow;
      }

      for (String s : resultStrs) {
        Log.v(LOG_TAG, "Forecast entry: " + s);
      }
      return resultStrs;

    }


    @Override
    protected String[] doInBackground(String... params) {

      if (params.length == 0) {
        return null;
      }

      HttpURLConnection httpURLConnection = null;

      BufferedReader reader = null;

      String forecastJsonStr = null;

      String format = "json";
      String units = "metric";
      int numDays = 7;

      try {

        final String FORCAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "APPID";

        Uri buildURI = Uri.parse(FORCAST_BASE_URL).buildUpon()
            .appendQueryParameter(QUERY_PARAM, params[0])
            .appendQueryParameter(FORMAT_PARAM, format)
            .appendQueryParameter(UNITS_PARAM, units)
            .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
            .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY).build();

        URL url = new URL(buildURI.toString());

        Log.v(LOG_TAG, "Built URI:" + buildURI.toString());

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

        Log.v(LOG_TAG, "Forecast JSON String" + forecastJsonStr);


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

      try {
        return getWeatherDataFromJson(forecastJsonStr, numDays);
      } catch (JSONException e) {
        Log.e(LOG_TAG, e.getMessage(), e);
        e.printStackTrace();
      }

      return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
      if (strings != null) {
        mforcastAdapter.clear();
        for (String s : strings) {
          mforcastAdapter.add(s);
        }
      }
    }
  }


}
