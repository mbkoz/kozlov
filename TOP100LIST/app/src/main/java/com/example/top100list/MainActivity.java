package com.example.top100list;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.String;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


class FilmDescription{
    private final String id;

    private final String name;
    private final String year;
    private final String previewUrl;
    private final String genre;
    FilmDescription(String id, String name, String year, String previewUrl, String genre) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.previewUrl = previewUrl;
        this.genre = genre;
    }

    public String getFilmName(){return name;}
    public String getFilmGenre(){return genre;}
}

class FilmCard extends FrameLayout {

    private Context mContext;
    private LayoutInflater layoutInflater;
    private FilmDescription filmDescription;

    public FilmCard (Context context, FilmDescription filmDescription) {
        super(context);
        this.mContext = context;
        inflate();
        bindViews();

        this.filmDescription = filmDescription;

        TextView tv = this.findViewById(R.id.textView2);
        tv.setText(filmDescription.getFilmName());
        tv = this.findViewById(R.id.textView3);
        tv.setText(filmDescription.getFilmGenre());
    }

    private void inflate() {
        layoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.film_card, this, true);
    }

    private void bindViews() {
        // bind all views here
    }
}


public class MainActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();
    ArrayList<FilmDescription> alCache = new ArrayList<FilmDescription>();
    LinearLayout ml;
    public String url= "https://kinopoiskapiunofficial.tech/api/v2.2/films/top?type=TOP_100_POPULAR_FILMS&page=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int i = 1; i <= 5; ++i) {
            OkHttpHandler okHttpHandler = new OkHttpHandler();
            okHttpHandler.execute(url + i);
        }

        ml = findViewById(R.id.mainLayout);
    }

    private void addFilmCards(@NonNull Iterator<FilmDescription> iter){
        //TODO: добавить reset для сброса сцена при отображении избранных фильмов
        while(iter.hasNext()){
            FilmCard filmCard = new FilmCard((Context) this, iter.next());
            ml.addView(filmCard);
        }
    }

    public class OkHttpHandler extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(@NonNull String...params) {

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
            //TODO: добавить закрытие соединения
            return null;
        }

        //нужно через такие запросы загружать по 20 фильмов
        //и по факту загрузки помещать данные в контейнер для вывода (без рисунков)
        // рисунки будут загружаться самими холдерами описания
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            int shift = alCache.size();

            try {
                JSONObject jsonObject = new JSONObject(s);

                final int maxCount = jsonObject.getJSONArray("films").length();
                for(int i = 0; i < maxCount; ++i){
                    JSONObject fdObject = jsonObject.getJSONArray("films").getJSONObject(i);

                    StringBuilder genres = new StringBuilder(fdObject.getJSONArray("genres").getJSONObject(0).getString("genre"));
                    int maxCountGenre = fdObject.getJSONArray("genres").length();
                    for(int j = 1; j < maxCountGenre; ++j){
                        genres.append(", ").append(fdObject.getJSONArray("genres").getJSONObject(j).getString("genre"));
                    }

                    alCache.add(
                        new FilmDescription(
                            fdObject.getString("filmId"),
                            fdObject.getString("nameRu"),
                            fdObject.getString("year"),
                            fdObject.getString("posterUrlPreview"),
                            genres.toString()
                        )
                    );
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            //TODO: добавить смещение на ранее выведенные фильмы
            addFilmCards(alCache.listIterator(shift));
        }
    }
}