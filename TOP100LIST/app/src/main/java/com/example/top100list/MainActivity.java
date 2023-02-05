package com.example.top100list;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;




class FilmCard extends FrameLayout {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private FilmDescription filmDescription;
    private ImageView image;

    public FilmCard (Context context, FilmDescription filmDescription) {
        super(context);
        this.mContext = context;
        inflate();
        bindViews();

        this.filmDescription = filmDescription;

        TextView tv = this.findViewById(R.id.textView2);
        tv.setText(filmDescription.getFilmName());
        tv = this.findViewById(R.id.textView3);
        tv.setText(new StringBuilder().append(filmDescription.getFilmGenre()).append(" (").append(filmDescription.getYear()).append(")").toString());

        image = this.findViewById(R.id.imageView2);

        DownloadImage downloadImage = new DownloadImage();
        downloadImage.execute(filmDescription.getPreviewUrl());

        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FilmDescriptionActivity.class);
                intent.putExtra("id", filmDescription.getId());
                intent.putExtra("posterUrl", filmDescription.getPosterUrl());
                intent.putExtra("name", filmDescription.getFilmName());
                intent.putExtra("genre", filmDescription.getFilmGenre());
                mContext.startActivity(intent);
            }
        });
    }



    private void inflate() {
        layoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.film_card, this, true);
    }

    private void bindViews() {
        // bind all views here
    }


    private class DownloadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Bitmap doInBackground(String... URL) {
            String imageURL = URL[0];
            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            image.setImageBitmap(result);
        }
    }

}


public class MainActivity extends AppCompatActivity {
    //TODO: сделать производный класс от загрузчика, который бы выводил окно прогресса и вызывать его в первый раз
    private final OkHttpHandler[] okHttpHandlerArr = new OkHttpHandler[5];
    private final ArrayList<FilmDescription> alCache = new ArrayList<FilmDescription>();
    private LinearLayout ml;
    //TODO при создании производного класса, перенести это поле туда
    private ProgressDialog mProgressDialog;

    private boolean needReload = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String url= "https://kinopoiskapiunofficial.tech/api/v2.2/films/top?type=TOP_100_POPULAR_FILMS&page=";
        for(int i = 1; i <= 5; ++i) {
            okHttpHandlerArr[i - 1] = new OkHttpHandler();
            okHttpHandlerArr[i - 1].execute(url + i);
        }

        ml = findViewById(R.id.mainLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(needReload) {
            needReload = false;
            reloadData();
        }
    }

    public void reloadData(){
        String url= "https://kinopoiskapiunofficial.tech/api/v2.2/films/top?type=TOP_100_POPULAR_FILMS&page=";
        for(int i = 1; i <= 5; ++i) {
            okHttpHandlerArr[i - 1] = new OkHttpHandler();
            okHttpHandlerArr[i - 1].execute(url + i);
        }
    }

    private void addFilmCards(@NonNull Iterator<FilmDescription> iter){
        while(iter.hasNext()){
            FilmCard filmCard = new FilmCard((Context) this, iter.next());
            ml.addView(filmCard);
        }
    }

    private void resetList(){
        ml.removeAllViews();
    }

    private void connectionLost(){
        resetList();
        for(int i = 0; i < 5; ++i)
            okHttpHandlerArr[i].cancel(false);
        //TODO переключиться на другой экран
        needReload = true;
        Intent intent = new Intent(this, NoConnectioActivity.class);
        startActivity(intent);
    }

    public class OkHttpHandler extends AsyncTask<String, String, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setTitle("Download TOP 100 FILMS LIST");
                mProgressDialog.setMessage("Loading...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.show();
            }
        }
        @Override
        protected String doInBackground(@NonNull String...params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            builder.addHeader("X-API-KEY", "5d9acf24-f31a-4486-bad4-85dbf224b544");
            Request request = builder.build();

            try {
                try (Response response = client.newCall(request).execute()) {
                    return response.body().string();
                }

            }catch (IOException ioException){
                //TODO: вызвать метод, который сотрет все виджеты и выведет ошибку подключения
                // с кнопкой перезапуска соединения (и остановит остальные асинхронные запросы)
                if(mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                connectionLost();
            }

            return null;
        }

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
                            genres.toString(),
                            fdObject.getString("posterUrl")
                        )
                    );
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            addFilmCards(alCache.listIterator(shift));
            if(mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }
}