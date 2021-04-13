package com.lobanov.autosprink;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    TextView hum, dateTime;
    SwipeRefreshLayout swipe;

    String ip = "23.111.204.160";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hum = findViewById(R.id.hum);
        dateTime = findViewById(R.id.datetime);
        swipe = findViewById(R.id.swipe);
        swipe.setColorSchemeColors(getColor(R.color.swipe1), getColor(R.color.swipe2), getColor(R.color.swipe3));
        new JsonTask().execute("http://" + ip + "/get");
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new JsonTask().execute("http://" + ip + "/get");
            }
        });
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected();
    }
    public void ErrorDialogMe(String err) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Произошла ошибка")
                .setMessage("Видимо, что то пошло не так с нашей стороны, простите. \n\nКод ошибки: " + err + "\nСообщите: antnlobanov@gmail.com")
                .setCancelable(false)
                .setPositiveButton("Понятно",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                System.exit(0);
                            }
                        });
        builder.create();
        builder.show();
        hum.setVisibility(View.INVISIBLE);
        dateTime.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private class JsonTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPostExecute(String s) {
            if (isNetworkConnected()) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ErrorDialogMe("0x00000AIBG");
                    }
                });
            }
            super.onPostExecute(s);
            String humidity = null;
            String date = null;
            String time = null;
            try {
                JSONObject contentObjPost = new JSONObject(s);
                humidity = contentObjPost.getString("mouisture");
                date = contentObjPost.getString("date");
                time = contentObjPost.getString("time");
            } catch (JSONException | NullPointerException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ErrorDialogMe("0x0000AJNPE");
                    }
                });
            }
            String res = humidity + "%";
            String resTime = "Текущие данные на " + date + " " + time;
            hum.setText(res);
            dateTime.setText(resTime);
            swipe.setRefreshing(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            if (isNetworkConnected()) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ErrorDialogMe("0x00000AIBG");
                    }
                });
            }
            String res = null;
            HttpsURLConnection connection;
            BufferedReader reader;
            try {
                URL url = new URL(params[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    JSONObject main = new JSONObject(line);
                    res = main.toString();
                }
                return res;
            } catch (JSONException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ErrorDialogMe("0x00000AJBG");
                    }
                });
            } catch (IOException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ErrorDialogMe("0x00000AIBG");
                    }
                });
            }
            return res;
        }
    }
}