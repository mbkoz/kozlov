package com.example.top100list;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.String;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


class FilmDescription{
    final String id;
    final String genre;
    final String year;
    final String posterUrl;
    FilmDescription(String id, String genre, String year, String posterUrl) {
        this.id = id;
        this.genre = genre;
        this.year = year;
        this.posterUrl = posterUrl;
    }
}

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    OkHttpClient client = new OkHttpClient();
    ArrayList<FilmDescription> alCache = new ArrayList<FilmDescription>();
    public String url= "https://kinopoiskapiunofficial.tech/api/v2.2/films/top?type=TOP_100_POPULAR_FILMS&page=1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.testField);

        OkHttpHandler okHttpHandler= new OkHttpHandler();
        okHttpHandler.execute(url);


    }

    private void addFilmHolders(){

    }

    public class OkHttpHandler extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String...params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            builder.addHeader("X-API-KEY", "5d9acf24-f31a-4486-bad4-85dbf224b544");
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        //нужно через такие запросы загружать по 20 фильмов
        //и по факту загрузки помещать данные в контейнер для вывода (без рисунков)
        // рисунки будут загружаться самими холдерами описания
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String test = null;
            try {
                JSONObject jsonObject = new JSONObject(s);
                test = jsonObject.getJSONArray("films").getJSONObject(0).getString("nameRu").toString();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            textView.setText(test);
        }
    }
}