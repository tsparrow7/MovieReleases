package com.example.tjgaming.moviereleases;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by TJ Gaming on 6/15/2016.
 */
public class GetMovieTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {

        HttpURLConnection urlConnection = null;
        String result = null;
        BufferedReader bufferedReader = null;

        try {
            URL url = new URL(urls[0]);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            InputStreamReader reader = new InputStreamReader(inputStream);

            bufferedReader = new BufferedReader(reader);

            StringBuffer stringBuffer = new StringBuffer();

            String line;
            while ((line = bufferedReader.readLine())!= null) {

                stringBuffer.append(line + "/n");
            }

            while (stringBuffer.length() == 0) {

                Log.i("StringBuffer", "Empty");
                return null;
            }

            result = stringBuffer.toString();

        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public void onPostExecute(String s) {

    }
}
